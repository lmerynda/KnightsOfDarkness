import assert from "node:assert/strict";
import { test } from "node:test";
import { World } from "../src/api/client.ts";
import { startServer } from "../src/server.ts";
import { foundCoalition } from "../src/simulation/coalition.ts";
import { selfSufficient, specialist, tradeShares } from "../src/simulation/trade.ts";
import { runWorld } from "../src/simulation/world.ts";

test("a coalition of specialists buys most of what it needs from its own members", async () => {
  const server = await startServer();
  try {
    const world = new World(server.url);
    const guild = await foundCoalition(world, "Guild", () => selfSufficient.map(specialist));
    const run = await runWorld(world, guild.rulers, 30);
    assert.deepEqual(run.violations, []);

    const bots = (await world.rankings()).kingdoms.filter(rank => rank.bot).map(rank => rank.kingdomName);
    const shares = tradeShares(await world.trades(), guild.rulers.map(ruler => ruler.player.name), bots);
    assert.ok(shares.bought.food.allies > 0, "no member bought food from the guild's farmer");
    assert.ok(shares.bought.iron.allies > 0, "no blacksmith bought iron from the guild's miner");
    assert.ok(shares.fromAllies > 0.5, `only ${Math.round(shares.fromAllies * 100)}% of the pieces bought came from members: ${JSON.stringify(shares.bought)}`);
    // nobody starves while the farmer sells
    assert.deepEqual(run.timeline.filter(entry => entry.starved > 0), []);
  } finally {
    await server.stop();
  }
});
