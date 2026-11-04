import assert from "node:assert/strict";
import { after, test } from "node:test";
import { World } from "../src/api/client.ts";
import { startServer, type GameServer } from "../src/server.ts";
import { economist, idle, turtle, warlord } from "../src/simulation/strategies.ts";
import { finalSnapshot, foundRulers, runWorld, type WorldRun } from "../src/simulation/world.ts";

// One busy world shared by the checks below: four rulers and the merchant bots, 40 turns. The first check that runs
// plays it out, since node does not wait for a before hook when a name filter skipped every check here.
type Played = { server: GameServer; run: WorldRun; names: Record<string, string> };

let played: Promise<Played> | undefined;

const busyWorld = (): Promise<Played> =>
  (played ??= (async () => {
    const server = await startServer();
    const world = new World(server.url);
    const rulers = await foundRulers(world, [economist, warlord(), turtle, idle], "World");
    const names = Object.fromEntries(rulers.map(ruler => [ruler.strategy.name, ruler.player.name]));
    return { server, names, run: await runWorld(world, rulers, 40) };
  })());

after(async () => {
  if (played) await (await played).server.stop();
});

test("no rule is broken in any kingdom on any turn", async () => {
  const { run } = await busyWorld();
  assert.deepEqual(run.violations, []);
});

test("an economist outgrows a ruler who only passes turns", async () => {
  const { run, names } = await busyWorld();
  const grown = finalSnapshot(run, names.economist);
  const lazy = finalSnapshot(run, names.idle);
  assert.ok(grown.land >= 3 * lazy.land, `economist ${grown.land} land against idle ${lazy.land}`);
  assert.ok(grown.people >= 3 * lazy.people, `economist ${grown.people} people against idle ${lazy.people}`);
});

test("a kingdom that staffs its farms never starves", async () => {
  const { run, names } = await busyWorld();
  const hungryTurns = run.timeline.filter(entry => entry.kingdom === names.economist && entry.starved > 0);
  assert.deepEqual(hungryTurns, []);
});

test("the warlord raises an army and the turtle out-defends everyone", async () => {
  const { run, names } = await busyWorld();
  const warlordEnd = finalSnapshot(run, names.warlord);
  const turtleEnd = finalSnapshot(run, names.turtle);
  assert.ok(warlordEnd.army > 0, "the warlord has no army");
  for (const other of [names.economist, names.warlord, names.idle]) {
    assert.ok(turtleEnd.defense > finalSnapshot(run, other).defense, `turtle defense ${turtleEnd.defense} is not above ${other}`);
  }
});

test("the warlord wins battles and takes land", async () => {
  const { run, names } = await busyWorld();
  const victories = run.battles.filter(battle => battle.attacker === names.warlord && battle.attackerWon);
  assert.ok(victories.length > 0, "no battle won");
  assert.ok(
    victories.some(battle => battle.landCaptured > 0),
    "victories took no land",
  );
});

test("merchant bots stay within their limits after every run", async () => {
  const { server } = await busyWorld();
  // players may buy from a bot between its runs, so the stores are checked right after the bots have acted
  const world = new World(server.url);
  const config = await world.config();
  // the market is open to signed-in rulers only, so a ruler looks at it
  const watcher = await world.foundKingdom("Market Watcher");
  await world.advance(1);
  const rankings = await world.rankings();
  const bots = (await world.kingdoms()).filter(kingdom => rankings.kingdoms.some(rank => rank.bot && rank.kingdomName === kingdom.name));
  assert.equal(bots.length, 4);
  for (const bot of bots) {
    assert.ok(bot.resources.land <= config.bots.maxLand, `${bot.name} holds ${bot.resources.land} land`);
    for (const [resource, cap] of Object.entries(config.bots.maxStock)) {
      assert.ok(bot.resources[resource as keyof typeof bot.resources] <= (cap ?? Infinity), `${bot.name} hoards ${bot.resources[resource as keyof typeof bot.resources]} ${resource}, cap ${cap}`);
    }
  }
  for (const resource of ["food", "iron", "tools", "weapons"] as const) {
    for (const offer of (await watcher.offers(resource)).filter(candidate => bots.some(bot => bot.name === candidate.sellerName))) {
      assert.ok(offer.count <= (config.bots.maxOffer[resource] ?? Infinity), `${offer.sellerName} offers ${offer.count} ${resource}`);
    }
  }
});

test("bots are listed but never ranked", async () => {
  const { server } = await busyWorld();
  const rankings = await new World(server.url).rankings();
  const ranked = rankings.kingdoms.filter(rank => rank.rank > 0);
  assert.ok(ranked.every(rank => !rank.bot));
  assert.deepEqual(
    ranked.map(rank => rank.rank),
    ranked.map((_, index) => index + 1),
  );
});
