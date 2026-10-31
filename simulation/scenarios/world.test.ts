import assert from "node:assert/strict";
import { after, before, test } from "node:test";
import { World } from "../src/client.ts";
import { startServer, type GameServer } from "../src/server.ts";
import { economist, idle, turtle, warlord } from "../src/strategies.ts";
import { finalSnapshot, foundRulers, runWorld, type WorldRun } from "../src/world.ts";

// One busy world shared by the checks below: four rulers and the merchant bots, 40 turns.
let server: GameServer;
let run: WorldRun;
let names: Record<string, string>;

before(async () => {
  server = await startServer();
  const world = new World(server.url);
  const rulers = await foundRulers(world, [economist, warlord(), turtle, idle], "World");
  names = Object.fromEntries(rulers.map(ruler => [ruler.strategy.name, ruler.player.name]));
  run = await runWorld(world, rulers, 40);
});

after(() => server.stop());

test("no rule is broken in any kingdom on any turn", () => {
  assert.deepEqual(run.violations, []);
});

test("an economist outgrows a ruler who only passes turns", () => {
  const grown = finalSnapshot(run, names.economist);
  const lazy = finalSnapshot(run, names.idle);
  assert.ok(grown.land >= 3 * lazy.land, `economist ${grown.land} land against idle ${lazy.land}`);
  assert.ok(grown.people >= 3 * lazy.people, `economist ${grown.people} people against idle ${lazy.people}`);
});

test("a kingdom that staffs its farms never starves", () => {
  const hungryTurns = run.timeline.filter(entry => entry.kingdom === names.economist && entry.starved > 0);
  assert.deepEqual(hungryTurns, []);
});

test("the warlord raises an army and the turtle out-defends everyone", () => {
  const warlordEnd = finalSnapshot(run, names.warlord);
  const turtleEnd = finalSnapshot(run, names.turtle);
  assert.ok(warlordEnd.army > 0, "the warlord has no army");
  for (const other of [names.economist, names.warlord, names.idle]) {
    assert.ok(turtleEnd.defense > finalSnapshot(run, other).defense, `turtle defense ${turtleEnd.defense} is not above ${other}`);
  }
});

test("the warlord wins battles and takes land", () => {
  const victories = run.battles.filter(battle => battle.attacker === names.warlord && battle.attackerWon);
  assert.ok(victories.length > 0, "no battle won");
  assert.ok(
    victories.some(battle => battle.landCaptured > 0),
    "victories took no land",
  );
});

test("merchant bots stay within their limits after every run", async () => {
  // players may buy from a bot between its runs, so the stores are checked right after the bots have acted
  const world = new World(server.url);
  const config = await world.config();
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
    for (const offer of (await world.offers(resource)).filter(candidate => bots.some(bot => bot.name === candidate.sellerName))) {
      assert.ok(offer.count <= (config.bots.maxOffer[resource] ?? Infinity), `${offer.sellerName} offers ${offer.count} ${resource}`);
    }
  }
});

test("bots are listed but never ranked", async () => {
  const rankings = await new World(server.url).rankings();
  const ranked = rankings.kingdoms.filter(rank => rank.rank > 0);
  assert.ok(ranked.every(rank => !rank.bot));
  assert.deepEqual(
    ranked.map(rank => rank.rank),
    ranked.map((_, index) => index + 1),
  );
});
