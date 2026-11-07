import assert from "node:assert/strict";
import { test } from "node:test";
import { World } from "../src/api/client.ts";
import { startServer } from "../src/server.ts";
import { strikeLargest } from "../src/simulation/strategies.ts";

// The largest kingdom in reach holds a garrison the striker cannot beat; a smaller one behind it stands open. A striker
// that looked only at the largest never marched again once a rival grew past its army, and the rival took everyone's land.
test("a striker that cannot beat the largest kingdom in reach marches on the largest it can beat", async () => {
  const server = await startServer({ "kingdomStartConfiguration.resources.gold": 3_000_000, "kingdomStartConfiguration.resources.weapons": 5_000, "kingdomStartConfiguration.resources.unemployed": 300 });
  try {
    const world = new World(server.url);
    const [striker, fortress, open] = [await world.foundKingdom("Striker"), await world.foundKingdom("Fortress"), await world.foundKingdom("Open")];
    assert.ok((await fortress.buyLand(20)).success, "the fortress could not buy land");
    assert.ok((await fortress.train({ bowman: 100 })).success, "the fortress could not train its garrison");
    assert.ok((await open.buyLand(10)).success, "the open kingdom could not buy land");
    assert.ok((await striker.build({ spyGuild: 1 })).success, "the striker could not build a spy guild");
    assert.ok((await striker.train({ infantry: 100, spy: 12 })).success, "the striker could not train its army");
    await world.advance(1, false);

    const rankings = await world.rankings();
    const config = await world.config();
    const kingdom = await striker.kingdom();
    const details = await striker.details();
    assert.deepEqual(rankings.kingdoms.filter(rank => rank.kingdomName !== striker.name).sort((a, b) => b.land - a.land).slice(0, 2).map(rank => rank.kingdomName), [fortress.name, open.name]);
    await strikeLargest({ player: striker, world, config, kingdom, details, rankings, turn: 6, memory: {} });

    assert.deepEqual((await striker.kingdom()).ongoingAttacks.map(attack => attack.to), [open.name]);
  } finally {
    await server.stop();
  }
});
