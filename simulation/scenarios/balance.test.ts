import assert from "node:assert/strict";
import { test } from "node:test";
import { World } from "../src/client.ts";
import { startServer, type ConfigOverrides } from "../src/server.ts";
import { economist, warlord } from "../src/strategies.ts";
import { foundRulers, runWorld, type WorldRun } from "../src/world.ts";

// The same duel under two configs: the harness can tell whether a balance change does what it claims.
const duel = async (overrides: ConfigOverrides): Promise<{ run: WorldRun; attacker: string }> => {
  const server = await startServer(overrides);
  try {
    const world = new World(server.url);
    const rulers = await foundRulers(world, [economist, warlord()], "Duel");
    return { run: await runWorld(world, rulers, 24, { bots: false }), attacker: rulers[1].player.name };
  } finally {
    await server.stop();
  }
};

const capturedLand = ({ run, attacker }: { run: WorldRun; attacker: string }): number =>
  run.battles.filter(battle => battle.attacker === attacker && battle.attackerWon).reduce((sum, battle) => sum + battle.landCaptured, 0);

test("doubling the land capture ratio lets the same victories take more land", async () => {
  const base = await duel({});
  const doubled = await duel({ "attack.maxLandCaptureRatio": 0.2 });
  assert.deepEqual(base.run.violations, []);
  assert.deepEqual(doubled.run.violations, []);
  assert.ok(capturedLand(base) > 0, "no land captured in the base world");
  assert.ok(capturedLand(doubled) > capturedLand(base), `doubled ratio took ${capturedLand(doubled)} acres, base took ${capturedLand(base)}`);
});

test("a zero capture ratio still lets armies win without taking land", async () => {
  const none = await duel({ "attack.maxLandCaptureRatio": 0 });
  const victories = none.run.battles.filter(battle => battle.attacker === none.attacker && battle.attackerWon);
  assert.ok(victories.length > 0, "no battle won");
  // the server always takes at least one acre from a won land attack
  assert.ok(victories.every(battle => battle.landCaptured <= 1));
});
