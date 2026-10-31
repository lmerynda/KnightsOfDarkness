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

test("a beaten kingdom is spared for the protection period", async () => {
  const protectedDuel = await duel({});
  const unprotected = await duel({ "attack.protectionTicksAfterDefeat": 0 });
  // the economist is the only other ruler; the idle merchant bots stay fair game
  const defeats = ({ run, attacker }: { run: WorldRun; attacker: string }): number =>
    run.battles.filter(battle => battle.attacker === attacker && battle.attackerWon && !battle.defender.endsWith("Bot")).length;

  assert.ok(defeats(protectedDuel) > 0, "the warlord never won");
  assert.ok(defeats(protectedDuel) < defeats(unprotected), `${defeats(protectedDuel)} defeats with protection, ${defeats(unprotected)} without`);
  // a defeat restarts the full protection; it must never happen while the last one is still running
  const victim = protectedDuel.run.timeline.filter(entry => entry.kingdom !== protectedDuel.attacker);
  for (const [index, entry] of victim.entries()) {
    const before = victim[index - 1]?.protection ?? 0;
    if (entry.protection > before) assert.ok(before <= 1, `turn ${entry.turn}: beaten again with ${before} turns of protection left`);
  }
});

test("a zero capture ratio still lets armies win without taking land", async () => {
  const none = await duel({ "attack.maxLandCaptureRatio": 0 });
  const victories = none.run.battles.filter(battle => battle.attacker === none.attacker && battle.attackerWon);
  assert.ok(victories.length > 0, "no battle won");
  // the server always takes at least one acre from a won land attack
  assert.ok(victories.every(battle => battle.landCaptured <= 1));
});
