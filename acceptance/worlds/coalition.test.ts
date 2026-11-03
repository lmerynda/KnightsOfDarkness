import assert from "node:assert/strict";
import { test } from "node:test";
import { World } from "../src/client.ts";
import { askingWarlord, coalitionWarlord, foundCoalition, guardian, treasurer } from "../src/coalition.ts";
import { startServer } from "../src/server.ts";
import { economist, raider, turtle, warlord } from "../src/strategies.ts";
import { foundRulers, runWorld, unevenPace } from "../src/world.ts";

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

// A warlord short of gold asks for it on the market and the coalition's treasurer pays into the ask.
test("a treasurer funds an allied warlord through the market", async () => {
  const server = await startServer();
  try {
    const world = new World(server.url);
    const purse = await foundCoalition(world, "Purse", council => [askingWarlord(council), treasurer()]);
    const run = await runWorld(world, purse.rulers, 15, { bots: false });

    assert.deepEqual(run.violations, []);
    const [warlordRuler, treasurerRuler] = purse.rulers;
    const trades = (await warlordRuler.player.notifications()).filter(note => note.kind === "trade");
    assert.ok(
      trades.some(note => note.message.startsWith(`${treasurerRuler.player.name} bought`)),
      "the treasurer never paid into the warlord's ask",
    );
  } finally {
    await server.stop();
  }
});

// Allied warlords who play at different paces still land one strike together: the later armies join the first one's.
test("allied warlords at uneven paces join one strike", async () => {
  const server = await startServer();
  try {
    const world = new World(server.url);
    const order = await foundCoalition(world, "Order", council => [coalitionWarlord(council), coalitionWarlord(council), coalitionWarlord(council)]);
    const wall = await foundRulers(world, [turtle, economist], "Wall");
    const run = await runWorld(world, [...order.rulers, ...wall], 36, { bots: false, pace: unevenPace });

    assert.deepEqual(run.violations, []);
    const joined = (
      await Promise.all(order.rulers.map(async ruler => (await ruler.player.notifications()).map(note => note.message)))
    ).flat();
    assert.ok(
      joined.some(message => / joins our strike on /.test(message)),
      "no warlord ever joined an ally's strike",
    );
    assert.ok(
      joined.some(message => / struck .* right after our army/.test(message)),
      "no joined army ever fought right after its lead",
    );
  } finally {
    await server.stop();
  }
});
