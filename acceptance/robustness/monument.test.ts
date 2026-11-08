import assert from "node:assert/strict";
import { test } from "node:test";
import { World } from "../src/api/client.ts";
import { noPeace, startServer } from "../src/server.ts";

// What only requests made by hand can try: the browser offers none of these. Everything is refused, and nothing moves.

test("a monument refuses a seat outside the alliance, strangers and goods that are not there", async () => {
  const server = await startServer({ ...noPeace, "monument.stageCost.gold": 1000, "monument.stageCost.iron": 10, "monument.stageCost.tools": 10 });
  try {
    const world = new World(server.url);
    const emperor = await world.foundKingdom("Refusing Emperor");
    const stranger = await world.foundKingdom("Refusing Stranger");
    assert.ok((await emperor.createAlliance("Refusing Order")).success);

    assert.match((await stranger.foundMonument(stranger.name)).message, /only an alliance/i);
    assert.match((await stranger.payIntoMonument({ gold: 1 })).message, /only an alliance's members/i);
    assert.match((await emperor.payIntoMonument({ gold: 1 })).message, /no monument/i);
    for (const seat of [stranger.name, "Nobody at all", ""]) assert.match((await emperor.foundMonument(seat)).message, /must be a member's kingdom/);
    assert.deepEqual(await world.monuments(), []);
    assert.ok((await emperor.foundMonument(emperor.name)).success);

    const before = (await emperor.kingdom()).resources;
    assert.match((await emperor.payIntoMonument({})).message, /choose what to pay/i);
    assert.match((await emperor.payIntoMonument({ gold: -1000, iron: 2000 })).message, /gives none back/);
    assert.match((await emperor.payIntoMonument({ gold: 1000, iron: 1_000_000 })).message, /don't hold that much/);
    assert.deepEqual((await emperor.kingdom()).resources, before);
    assert.deepEqual((await emperor.monument()).store, { gold: 0, iron: 0, tools: 0, food: 0 });

    assert.match((await emperor.removeFromAlliance(emperor.name)).message, /only after every other member is gone/);
    assert.equal((await emperor.kingdom()).allianceName, "Refusing Order");
  } finally {
    await server.stop();
  }
});
