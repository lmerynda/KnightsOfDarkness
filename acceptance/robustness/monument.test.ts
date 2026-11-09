import assert from "node:assert/strict";
import { test } from "node:test";
import { World } from "../src/api/client.ts";
import { noPeace, startServer } from "../src/server.ts";

// What only requests made by hand can try: the browser offers none of these. Everything is refused, and nothing moves.

test("a monument refuses soldiers that are not there, assaults on nothing and assaults by its own", async () => {
  const server = await startServer({ ...noPeace, "monument.stageCost.gold": 1000, "monument.stageCost.iron": 10, "monument.stageCost.tools": 10 });
  try {
    const world = new World(server.url);
    const emperor = await world.foundKingdom("Guarding Emperor");
    const squire = await world.foundKingdom("Guarding Squire");
    const stranger = await world.foundKingdom("Guarding Stranger");
    for (const ruler of [emperor, squire, stranger]) {
      for (let turn = 0; turn < 5; turn++) await ruler.passTurn(100);
      assert.ok((await ruler.train({ infantry: 10 })).success);
    }
    assert.ok((await emperor.createAlliance("Guarding Order")).success);
    assert.ok((await emperor.invite(squire.name, "Guarding Order")).success);
    assert.ok((await squire.acceptInvitation((await squire.invitations())[0].id)).success);

    assert.match((await emperor.sendToMonument({ infantry: 1 })).message, /no monument/i);
    assert.match((await stranger.sendToMonument({ infantry: 1 })).message, /no monument/i);
    assert.match((await stranger.sendAttack(emperor.name, "monument", { infantry: 1 })).message, /no monument stands at/i);
    assert.ok((await emperor.foundMonument(emperor.name)).success);

    assert.match((await stranger.sendToMonument({ infantry: 1 })).message, /no monument/i);
    assert.match((await emperor.sendToMonument({})).message, /at least one soldier/);
    assert.match((await emperor.sendToMonument({ infantry: 1000 })).message, /not enough/i);
    assert.match((await emperor.sendToMonument({ infantry: -5, bowman: 1 })).message, /negative/);
    assert.deepEqual((await emperor.monument()).garrison, []);
    assert.equal((await emperor.kingdom()).units.availableUnits.infantry, 10);

    // its own members may not strike it, wherever they stand
    assert.match((await squire.sendAttack(emperor.name, "monument", { infantry: 1 })).message, /your own alliance's monument/);
    assert.match((await emperor.sendAttack(emperor.name, "monument", { infantry: 1 })).message, /your own alliance's monument/);
    assert.match((await stranger.sendAttack(emperor.name, "monument", { infantry: 1000 })).message, /not enough/i);
    assert.match((await stranger.sendAttack(emperor.name, "monument", {})).message, /at least one unit/i);
    assert.equal((await stranger.kingdom()).ongoingAttacks.length, 0);

    // a strike on a kingdom is not an assault on its monument
    assert.ok((await stranger.createAlliance("Guarding Rivals")).success);
    const ally = await world.foundKingdom("Guarding Ally");
    for (let turn = 0; turn < 5; turn++) await ally.passTurn(100);
    assert.ok((await ally.train({ infantry: 10 })).success);
    assert.ok((await stranger.invite(ally.name, "Guarding Rivals")).success);
    assert.ok((await ally.acceptInvitation((await ally.invitations())[0].id)).success);
    assert.ok((await stranger.sendAttack(emperor.name, "monument", { infantry: 5 })).success);
    const assault = (await stranger.kingdom()).ongoingAttacks[0];
    assert.match((await ally.sendAttack(emperor.name, "land", { infantry: 5 }, assault.id)).message, /marches on the monument/);
    assert.equal((await ally.kingdom()).ongoingAttacks.length, 0);
  } finally {
    await server.stop();
  }
});

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
