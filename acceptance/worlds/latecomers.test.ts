import assert from "node:assert/strict";
import { test } from "node:test";
import { World } from "../src/api/client.ts";
import { startServer, type ConfigOverrides } from "../src/server.ts";
import { economist, hoarder } from "../src/simulation/strategies.ts";
import { foundRulers, runWorld, type Snapshot, type WorldRun } from "../src/simulation/world.ts";

const newcomerName = "Late economist";

/** an economist founded at the start, and another who founds a kingdom ten turns later */
const withNewcomer = async (overrides: ConfigOverrides): Promise<WorldRun> => {
  const server = await startServer(overrides);
  try {
    const world = new World(server.url);
    const rulers = await foundRulers(world, [economist], "Founder");
    return await runWorld(world, rulers, 30, { bots: false, newcomers: [{ name: newcomerName, strategy: economist, joinsOnTurn: 10 }] });
  } finally {
    await server.stop();
  }
};

const turnsOf = (run: WorldRun, kingdom: string): Snapshot[] => run.timeline.filter(entry => entry.kingdom === kingdom);

test("a newcomer founds a kingdom mid-era and grows faster for the small kingdoms' production bonus", async () => {
  const withBonus = await withNewcomer({});
  const without = await withNewcomer({ "common.smallKingdomProductionBonus": 0 });
  assert.deepEqual([...withBonus.violations, ...without.violations], []);

  const played = turnsOf(withBonus, newcomerName);
  assert.deepEqual(
    played.map(entry => entry.turn),
    Array.from({ length: 21 }, (_, index) => 10 + index),
  );
  const landWith = played.at(-1)?.land ?? 0;
  const landWithout = turnsOf(without, newcomerName).at(-1)?.land ?? 0;
  assert.ok(landWith > landWithout, `the newcomer reached ${landWith} acres with the bonus and ${landWithout} without`);
});

test("a hoarder keeps its starting land for half the era, then spends the treasury on land", async () => {
  // an era of forty ticks, so half of it passes in twenty turns
  const server = await startServer({ "era.maxLengthInTicks": 40 });
  try {
    const world = new World(server.url);
    const rulers = await foundRulers(world, [hoarder], "Miser");
    const run = await runWorld(world, rulers, 25, { bots: false });
    assert.deepEqual(run.violations, []);

    const turns = turnsOf(run, rulers[0].player.name);
    const hoarding = turns.filter(entry => entry.turn < 20);
    const startLand = hoarding[0].land;
    assert.ok(
      hoarding.every(entry => entry.land === startLand),
      `the land changed while hoarding: ${hoarding.map(entry => entry.land).join(", ")}`,
    );
    const lastHoard = hoarding.at(-1) as Snapshot;
    assert.ok(lastHoard.gold > hoarding[0].gold, `the treasury did not grow while hoarding: ${hoarding[0].gold} to ${lastHoard.gold}`);

    const spent = turns.find(entry => entry.turn === 20) as Snapshot;
    assert.ok(spent.land > startLand, `no land bought: ${spent.land} acres`);
    assert.ok(spent.gold < lastHoard.gold / 10, `the treasury was not spent: ${spent.gold} gold left of ${lastHoard.gold}`);
  } finally {
    await server.stop();
  }
});
