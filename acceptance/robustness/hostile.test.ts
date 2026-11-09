import assert from "node:assert/strict";
import { after, test } from "node:test";
import { send, World, type Player } from "../src/api/client.ts";
import { buildingNames, unitNames, type Kingdom } from "../src/api/types.ts";
import { startServer, type GameServer } from "../src/server.ts";
import { invariantViolations } from "../src/simulation/world.ts";

// Players who type anything into anything: every endpoint gets numbers below zero and past what an int holds, text where
// numbers go, names and ids of what belongs to someone else, and bodies that are not JSON at all. The server may refuse
// any of it, but it never fails with a server error, never breaks a rule of the game, and never makes gold out of nothing.

let started: Promise<GameServer> | undefined;

// the first check starts the world: node does not wait for a before hook when a name filter skipped every check here
const sharedWorld = async (): Promise<World> => new World((await (started ??= startServer())).url);

after(async () => {
  if (started) await (await started).stop();
});

/** a small seeded generator, so that a failing run plays again exactly; KOD_FUZZ_SEED tries other runs */
const generator = (seed: number): (() => number) => {
  let state = seed >>> 0;
  return () => {
    state = (state + 0x6d2b79f5) >>> 0;
    let mixed = Math.imul(state ^ (state >>> 15), state | 1);
    mixed ^= mixed + Math.imul(mixed ^ (mixed >>> 7), mixed | 61);
    return ((mixed ^ (mixed >>> 14)) >>> 0) / 4294967296;
  };
};

const seed = Number(process.env.KOD_FUZZ_SEED ?? 20261104);

type Request = { method: "GET" | "POST"; path: string; body?: string };

/** what the world holds that a hostile request could name: kingdoms, alliances, and the ids of everything in it */
type Known = { names: string[]; alliances: string[]; ids: string[] };

const uuidPattern = /[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/g;

const look = async (world: World, players: Player[]): Promise<Known> => {
  const kingdoms = await world.kingdoms();
  const seen = await Promise.all(
    players.flatMap(player => [
      player.send("GET", "/market/food"),
      player.send("GET", "/market/iron"),
      player.send("GET", "/market/allies"),
      player.send("GET", "/alliance/invitations"),
      player.send("GET", "/kingdom/notifications"),
      player.send("GET", "/messages/inbox"),
    ]),
  );
  const text = JSON.stringify(kingdoms) + seen.map(answer => answer.text).join("");
  return {
    names: kingdoms.map(kingdom => kingdom.name),
    alliances: [...new Set(kingdoms.flatMap(kingdom => (kingdom.allianceName ? [kingdom.allianceName] : [])))],
    ids: [...new Set(text.match(uuidPattern) ?? [])],
  };
};

const hostileRequests = (random: () => number) => {
  const pick = <T,>(values: readonly T[]): T => values[Math.floor(random() * values.length)];
  const numbers = [0, 1, -1, 7, 100, 999, 46_341, 65_536, 1_000_000_000, 2_147_483_647, -2_147_483_648, 2_147_483_648, 1e15, 1.5, -0.5];
  const oddities = [null, "", "12", "abc", true, [], {}];
  const words = ["", " ", "x".repeat(5000), "<img src=x onerror=alert(1)>", "'; DROP TABLE kingdom_entity; --", "Ünïcödé 👑", "../../etc/passwd", null, 42];
  const brokenBodies = ["{", "null", "[]", '""', "", '{"count":'];

  // mostly what a player could mean, so that requests get far enough into the rules to find their edges
  const number = (): unknown => {
    const roll = random();
    if (roll < 0.45) return 1 + Math.floor(random() * 20);
    return roll < 0.88 ? pick(numbers) : pick(oddities);
  };
  const oneOf = (values: readonly string[]): unknown => (random() < 0.7 ? pick(values) : pick(["nonsense", "", null, 3, "FOOD", "gold", "land", "turns"]));
  const counts = (keys: readonly string[]): Record<string, unknown> =>
    Object.fromEntries(Array.from({ length: 1 + Math.floor(random() * 3) }, () => [random() < 0.9 ? pick(keys) : pick(["nonsense", "__proto__", ""]), number()]));
  const resources = ["food", "iron", "tools", "weapons"] as const;

  return (known: Known): Request => {
    const name = (): unknown => (random() < 0.6 ? pick(known.names) : pick(words));
    const alliance = (): unknown => (random() < 0.6 && known.alliances.length > 0 ? pick(known.alliances) : pick(words));
    const id = (): string => (random() < 0.75 && known.ids.length > 0 ? pick(known.ids) : pick(["not-an-id", crypto.randomUUID(), "", "00000000-0000-0000-0000-000000000000"]));
    const json = (value: unknown): string => (random() < 0.05 ? pick(brokenBodies) : JSON.stringify(value));
    const raw = (value: unknown): string => encodeURIComponent(String(value));

    const requests: (() => Request)[] = [
      () => ({ method: "POST", path: "/kingdom/build", body: json(counts(buildingNames)) }),
      () => ({ method: "POST", path: "/kingdom/demolish", body: json(counts(buildingNames)) }),
      () => ({ method: "POST", path: "/kingdom/train", body: json(counts(unitNames)) }),
      () => ({ method: "POST", path: "/kingdom/fire", body: json(counts(unitNames)) }),
      () => ({ method: "POST", path: "/kingdom/buy-land", body: json(number()) }),
      () => ({ method: "GET", path: `/kingdom/land-quote?count=${raw(number())}` }),
      () => ({ method: "GET", path: `/kingdom/details?weaponsProductionPercentage=${raw(number())}` }),
      () => ({ method: "POST", path: "/kingdom/start-special-building", body: json({ name: oneOf(["goldShaft", "ironShaft", "granary", "forge", "warehouse"]) }) }),
      () => ({ method: "POST", path: "/kingdom/build-special-building", body: json({ id: id(), buildingPoints: number() }) }),
      () => ({ method: "POST", path: "/kingdom/demolish-special-building", body: json({ id: id() }) }),
      () => ({ method: "POST", path: "/kingdom/send-attack", body: json({ destinationKingdomName: name(), attackType: oneOf(["land", "economy", "plunder"]), units: counts(unitNames), joins: random() < 0.3 ? id() : undefined }) }),
      () => ({ method: "POST", path: "/kingdom/withdraw-attack", body: json(id()) }),
      () => ({ method: "POST", path: "/kingdom/send-carriers", body: json({ destinationKingdomName: name(), resource: oneOf(resources), count: number() }) }),
      () => ({ method: "POST", path: "/kingdom/withdraw-carriers", body: json(id()) }),
      () => ({ method: "POST", path: "/kingdom/send-reinforcements", body: json({ hostKingdomName: name(), units: counts(unitNames) }) }),
      () => ({ method: "POST", path: "/kingdom/recall-reinforcements", body: json(id()) }),
      () => ({ method: "POST", path: `/kingdom/notifications/${raw(id())}/mark-as-read` }),
      () => ({ method: "POST", path: "/market/create", body: json({ resource: oneOf(resources), count: number(), price: number() }) }),
      () => ({ method: "POST", path: `/market/${raw(id())}/buy`, body: json({ count: number() }) }),
      () => ({ method: "POST", path: `/market/${raw(id())}/withdraw` }),
      () => ({ method: "GET", path: `/market/${raw(oneOf(resources))}` }),
      () => ({ method: "POST", path: "/war/send-spies", body: json({ targetKingdomName: name(), spies: number() }) }),
      () => ({ method: "POST", path: "/alliance/create", body: json({ name: random() < 0.5 ? `Fuzz ${Math.floor(random() * 1000)}` : name() }) }),
      () => ({ method: "POST", path: "/alliance/invite", body: json({ kingdomName: name(), allianceName: alliance() }) }),
      () => ({ method: "POST", path: "/alliance/accept", body: json({ invitationId: id() }) }),
      () => ({ method: "POST", path: "/alliance/reject", body: json({ invitationId: id() }) }),
      () => ({ method: "POST", path: "/alliance/remove", body: json({ kingdomName: name() }) }),
      () => ({ method: "POST", path: "/alliance/leave" }),
      () => ({ method: "POST", path: "/alliance/add-bot", body: json({ botName: random() < 0.5 ? `Fuzz Bot ${Math.floor(random() * 1000)}` : name() }) }),
      () => ({ method: "GET", path: "/alliance/war-room" }),
      () => ({ method: "POST", path: "/alliance/monument/found", body: json({ seat: name() }) }),
      () => ({ method: "POST", path: "/alliance/monument/contribute", body: json(counts(["gold", "iron", "tools", "food"])) }),
      () => ({ method: "GET", path: "/alliance/monument" }),
      () => ({ method: "POST", path: "/alliance/monument/garrison", body: json({ units: counts(["infantry", "bowman", "cavalry"]) }) }),
      () => ({ method: "POST", path: "/kingdom/send-attack", body: json({ destinationKingdomName: name(), attackType: "monument", units: counts(["infantry", "bowman", "cavalry"]) }) }),
      () => ({ method: "POST", path: "/messages/send", body: json({ recipient: name(), body: pick(words) }) }),
      () => ({ method: "POST", path: "/messages/send-alliance", body: json({ recipient: name(), body: pick(words) }) }),
      () => ({ method: "POST", path: `/messages/${raw(id())}/mark-as-read` }),
      () => ({ method: "POST", path: "/auth/register", body: json({ email: pick([...words, "fuzz@simulation.test"]), kingdomName: name(), password: pick(words) }) }),
      () => ({ method: "POST", path: "/auth/trial", body: json({ kingdomName: random() < 0.5 ? `Fuzz Trial ${Math.floor(random() * 1000)}` : name() }) }),
      () => ({ method: "POST", path: "/auth/authenticate", body: json({ email: pick(words), password: pick(words) }) }),
      () => ({ method: "POST", path: "/auth/keep", body: json({ email: pick([...words, "kept@simulation.test"]), password: pick(words) }) }),
      () => ({ method: "GET", path: pick(["/kingdom", "/kingdom/notifications/count", "/war/battle-reports", "/war/spy-reports", "/messages/sent", "/messages/unread-count", "/alliance", "/alliance/all", "/auth/account", "/game/eras", "/game/era", "/game/monuments", "/game/rankings"]) }),
    ];
    return pick(requests)();
  };
};

const totalGold = (kingdoms: Kingdom[]): number => kingdoms.reduce((total, kingdom) => total + kingdom.resources.gold, 0);

type Actor = { who: string; send: (request: Request) => Promise<{ status: number; text: string }> };

/** three rulers with something to aim at: stores on the market, an alliance, a great work begun, and a turn played */
const hostileRulers = async (world: World, prefix: string): Promise<Player[]> => {
  const players = await Promise.all(["Prober", "Victim", "Bystander"].map(name => world.foundKingdom(`${prefix} ${name}`)));
  for (const player of players) {
    await player.passTurn();
    assert.ok((await player.postOffer("food", 100, 20)).success);
    await player.startGreatWork("granary");
  }
  assert.ok((await players[0].createAlliance(`${prefix} Pact`)).success);
  return players;
};

/**
 * sends batches of hostile requests from the rulers, someone signed out and someone with a forged pass; after each batch
 * the test looks at the world and names what went wrong. Answers every server error and everything named.
 */
const playHostile = async (
  world: World,
  players: Player[],
  random: () => number,
  afterBatch: (batch: number, check: (who: string, request: Request, answer: { status: number; text: string }) => void) => Promise<string[]>,
): Promise<{ serverErrors: string[]; problems: string[] }> => {
  const next = hostileRequests(random);
  const rulers: Actor[] = players.map(player => ({ who: player.name, send: request => player.send(request.method, request.path, request.body) }));
  const outsiders: Actor[] = [
    { who: "a stranger", send: request => send(`${world.url}${request.path}`, { method: request.method, body: request.body }) },
    { who: "a forger", send: request => send(`${world.url}${request.path}`, { method: request.method, body: request.body, token: "forged.token.value" }) },
  ];
  const serverErrors: string[] = [];
  const problems: string[] = [];
  const check = (who: string, request: Request, answer: { status: number; text: string }): void => {
    if (answer.status >= 500) serverErrors.push(`${who}: ${request.method} ${request.path} ${(request.body ?? "").slice(0, 300)} answered ${answer.status} ${answer.text.slice(0, 300)}`);
  };

  for (let batch = 1; batch <= 30; batch++) {
    const known = await look(world, players);
    for (let request = 0; request < 30; request++) {
      // the signed-out and the forger are turned away at the door, so they come rarely
      const actor = random() < 0.9 ? rulers[Math.floor(random() * rulers.length)] : outsiders[Math.floor(random() * outsiders.length)];
      const sent = next(known);
      check(actor.who, sent, await actor.send(sent));
    }
    problems.push(...(await afterBatch(batch, check)));
  }
  return { serverErrors, problems };
};

test("hundreds of hostile requests from players and strangers never break the server or the rules", { timeout: 600_000 }, async () => {
  const world = await sharedWorld();
  const config = await world.config();
  const players = await hostileRulers(world, "Hostile");
  const startingKingdoms = (await world.kingdoms()).length;
  const startingGold = totalGold(await world.kingdoms());

  const { serverErrors, problems } = await playHostile(world, players, generator(seed), async batch => {
    const kingdoms = await world.kingdoms();
    // no time passes here, so gold only changes hands, is spent, or comes with a kingdom founded meanwhile
    const allowed = startingGold + (kingdoms.length - startingKingdoms) * config.kingdomStartConfiguration.resources.gold;
    const madeOfNothing = totalGold(kingdoms) > allowed ? [`batch ${batch}: the world holds ${totalGold(kingdoms)} gold, at most ${allowed} is possible`] : [];
    return [...invariantViolations(kingdoms, config, batch), ...madeOfNothing];
  });

  assert.deepEqual(serverErrors, [], `seed ${seed}`);
  assert.deepEqual(problems, [], `seed ${seed}`);
});

test("hostile requests between turns leave every turn played by the rules", { timeout: 600_000 }, async () => {
  const world = await sharedWorld();
  const config = await world.config();
  const players = await hostileRulers(world, "Turning");
  const random = generator(seed + 1);
  const shares = [0, 50, 100, -1, -100, 101, 1000, 2_147_483_647, -2_147_483_648, "abc", null];

  const { serverErrors, problems } = await playHostile(world, players, random, async (batch, check) => {
    // every ruler passes a turn with whatever share of weapons, then the clock moves and the bots act
    for (const player of players) {
      const request: Request = { method: "POST", path: "/kingdom/pass-turn", body: JSON.stringify(shares[Math.floor(random() * shares.length)]) };
      check(player.name, request, await player.send(request.method, request.path, request.body));
    }
    await world.advance(1);
    return invariantViolations(await world.kingdoms(), config, batch);
  });

  assert.deepEqual(serverErrors, [], `seed ${seed + 1}`);
  assert.deepEqual(problems, [], `seed ${seed + 1}`);
});
