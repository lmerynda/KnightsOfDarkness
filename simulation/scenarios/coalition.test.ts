import assert from "node:assert/strict";
import { test } from "node:test";
import { World } from "../src/client.ts";
import { coalitionWarlord, foundCoalition, guardian } from "../src/coalition.ts";
import { startServer } from "../src/server.ts";
import { economist, raider, warlord } from "../src/strategies.ts";
import { runWorld } from "../src/world.ts";

// Two small coalitions at war: Order strikes together and guards its own, Horde plays every kingdom for itself.
test("coalitions fight, guard each other and keep every rule", async () => {
  const server = await startServer();
  try {
    const world = new World(server.url);
    const order = await foundCoalition(world, "Order", council => [coalitionWarlord(council), economist, guardian(council)]);
    const horde = await foundCoalition(world, "Horde", () => [warlord(), economist, raider]);
    const run = await runWorld(world, [...order.rulers, ...horde.rulers], 30, { bots: false });

    assert.deepEqual(run.violations, []);
    const rankings = await world.rankings();
    assert.deepEqual(rankings.alliances.map(alliance => alliance.allianceName).sort(), ["Horde", "Order"]);
    const kingdoms = await world.kingdoms();
    const guardianKingdom = kingdoms.find(kingdom => kingdom.name === order.rulers[2].player.name);
    assert.ok(guardianKingdom, "the guardian is gone");
    const standing = guardianKingdom.reinforcements;
    assert.ok(standing.length > 0, "the guardian keeps no soldiers with an ally");
    for (const garrison of standing) {
      const host = kingdoms.find(kingdom => kingdom.name === garrison.to);
      assert.equal(host?.allianceName, "Order", `${garrison.to} is no ally`);
      assert.ok(host?.garrisons.some(hosted => hosted.id === garrison.id), `${garrison.to} does not list the garrison`);
    }
  } finally {
    await server.stop();
  }
});
