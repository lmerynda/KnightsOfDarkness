import assert from "node:assert/strict";
import { test } from "node:test";
import { World } from "../src/api/client.ts";
import type { Era, GameConfig, Kingdom, Rankings } from "../src/api/types.ts";
import { buildingNames, resourceNames, unitNames } from "../src/api/types.ts";
import { startServer } from "../src/server.ts";
import { coalitionWarlord, foundCoalition, guardian } from "../src/simulation/coalition.ts";
import { economist, idle, raider, turtle, warlord } from "../src/simulation/strategies.ts";
import { foundRulers, runWorld, unevenPace } from "../src/simulation/world.ts";

// Months of the game as it is configured: eras of four months played hour by hour, by rival coalitions and lone
// rulers who come back at their own pace while the merchants trade. Every rule holds for every kingdom after every
// tick, and every era ends the way the game promises: the alliance with the most land winning, the era written into the
// hall of fame, and every kingdom starting afresh while alliances stay.

const eras = 2;

/** what is wrong with a kingdom that should have just started afresh */
const freshStartProblems = (kingdom: Kingdom, config: GameConfig, era: number): string[] => {
  const start = config.kingdomStartConfiguration;
  const where = `era ${era} begins, ${kingdom.name}`;
  const problems: string[] = [];
  for (const resource of resourceNames) {
    if (kingdom.resources[resource] !== start.resources[resource]) problems.push(`${where}: ${resource} is ${kingdom.resources[resource]}, not ${start.resources[resource]}`);
  }
  for (const building of buildingNames) {
    if (kingdom.buildings[building] !== start.buildings[building]) problems.push(`${where}: ${kingdom.buildings[building]} ${building}, not ${start.buildings[building]}`);
  }
  for (const unit of unitNames) {
    if (kingdom.units.availableUnits[unit] !== start.units[unit]) problems.push(`${where}: ${kingdom.units.availableUnits[unit]} ${unit}, not ${start.units[unit]}`);
    if (kingdom.units.mobileUnits[unit] !== 0) problems.push(`${where}: ${kingdom.units.mobileUnits[unit]} ${unit} still away`);
  }
  const leftovers = {
    "armies on the march": kingdom.ongoingAttacks.length,
    "soldiers standing guard elsewhere": kingdom.reinforcements.length,
    "allies' soldiers standing guard": kingdom.garrisons.length,
    "carriers on the road": kingdom.carriersOnTheMove.length,
    "great works": kingdom.specialBuildings.length,
    "market offers": kingdom.marketOffers.length,
  };
  for (const [what, count] of Object.entries(leftovers)) if (count > 0) problems.push(`${where}: ${count} ${what} left from the last era`);
  if (kingdom.protectionTicks !== config.attack.protectionTicksAtStart) problems.push(`${where}: ${kingdom.protectionTicks} turns of peace, not ${config.attack.protectionTicksAtStart}`);
  return problems;
};

/** what is wrong with the hall of fame's record of the era that just ended */
const recordProblems = (record: Era | undefined, ended: Era, standing: Rankings): string[] => {
  if (!record) return [`era ${ended.number} is missing from the hall of fame`];
  const problems: string[] = [];
  if (record.number !== ended.number) problems.push(`the hall of fame's newest era is ${record.number}, not ${ended.number}`);
  if (!record.endedAt) problems.push(`era ${ended.number} has no end`);
  if (record.endedBy !== "clock") problems.push(`era ${ended.number} ended by ${record.endedBy}`);
  if (record.winningAlliance !== standing.alliances[0]?.allianceName) problems.push(`era ${ended.number} was won by ${record.winningAlliance}, but ${standing.alliances[0]?.allianceName} held the most land`);
  const final = (record.finalRankings?.kingdoms ?? []).map(rank => `${rank.rank} ${rank.kingdomName} ${rank.land}`);
  const last = standing.kingdoms.map(rank => `${rank.rank} ${rank.kingdomName} ${rank.land}`);
  if (JSON.stringify(final) !== JSON.stringify(last)) problems.push(`era ${ended.number} was recorded as ${final.slice(0, 3).join(", ")}, but ended at ${last.slice(0, 3).join(", ")}`);
  return problems;
};

test(`${eras} eras of four months keep every rule and end as promised`, async () => {
  const server = await startServer();
  try {
    const world = new World(server.url);
    const config = await world.config();
    const order = await foundCoalition(world, "Order", council => [coalitionWarlord(council), economist, guardian(council)]);
    const horde = await foundCoalition(world, "Horde", () => [warlord(), economist, turtle, raider]);
    const loners = await foundRulers(world, [economist, idle], "Loner");
    const rulers = [...order.rulers, ...horde.rulers, ...loners];
    const herald = rulers[0].player;

    const problems: string[] = [];
    let era = await world.era();
    let standing = await world.rankings();
    const started = Date.now();

    const run = await runWorld(world, rulers, eras * config.era.maxLengthInTicks, {
      pace: unevenPace,
      afterTick: async tick => {
        const now = await world.era();
        if (now.number !== era.number) {
          if (now.number !== era.number + 1 || now.ticksElapsed !== 0) problems.push(`tick ${tick}: era ${era.number} was followed by era ${now.number} at tick ${now.ticksElapsed}`);
          problems.push(...recordProblems((await herald.finishedEras())[0], era, standing));
          const bots = new Set(standing.kingdoms.filter(rank => rank.bot).map(rank => rank.kingdomName));
          for (const kingdom of await world.kingdoms()) {
            if (!bots.has(kingdom.name)) problems.push(...freshStartProblems(kingdom, config, now.number));
          }
          const alliances = (await world.rankings()).alliances.map(alliance => alliance.allianceName).sort();
          if (JSON.stringify(alliances) !== JSON.stringify(["Horde", "Order"])) problems.push(`era ${now.number} begins with alliances ${alliances.join(", ")}`);
          if (!(await herald.notifications()).some(note => note.message.startsWith(`Era ${era.number} has ended`))) problems.push(`nobody heard that era ${era.number} ended`);
          console.log(`era ${era.number} ended after ${Math.round((Date.now() - started) / 1000)} s, won by ${standing.alliances[0]?.allianceName}`);
        }
        era = now;
      },
      afterTurn: async () => {
        standing = await world.rankings();
      },
    });

    assert.deepEqual(run.violations.slice(0, 20), [], `${run.violations.length} broken rules`);
    assert.deepEqual(problems, []);
    assert.equal(era.number, eras + 1, `the world reached era ${era.number}`);
  } finally {
    await server.stop();
  }
});
