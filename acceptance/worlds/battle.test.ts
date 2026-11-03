import assert from "node:assert/strict";
import { test } from "node:test";
import { World } from "../src/client.ts";
import { startServer } from "../src/server.ts";
import { raider, turtle } from "../src/strategies.ts";
import { foundRulers, runWorld } from "../src/world.ts";

// A raider of infantry and cavalry charges a turtle that defends with bowmen and infantry: arrows first, then melee.
test("a charge into a garrison with bowmen bleeds both sides", async () => {
  const server = await startServer();
  try {
    const world = new World(server.url);
    const rulers = await foundRulers(world, [raider, turtle], "Siege");
    const [attacker, defender] = rulers.map(ruler => ruler.player.name);
    const run = await runWorld(world, rulers, 24, { bots: false });

    assert.deepEqual(run.violations, []);
    const charges = run.battles.filter(battle => battle.attacker === attacker && battle.defender === defender);
    assert.ok(charges.length > 0, "the raider never charged the turtle");
    for (const charge of charges) {
      assert.equal(charge.unitsSent.bowman ?? 0, 0, "the raider sent bowmen");
      const fallenRaiders = (charge.attackerLosses.infantry ?? 0) + (charge.attackerLosses.cavalry ?? 0);
      assert.ok(fallenRaiders > 0, `the turtle's arrows felled nobody: ${JSON.stringify(charge.attackerLosses)}`);
      // the raider brings no archers, so bowmen can only fall to melee blows
      assert.ok((charge.defenderLosses.bowman ?? 0) > 0, `no bowman fell in melee: ${JSON.stringify(charge.defenderLosses)}`);
    }
  } finally {
    await server.stop();
  }
});
