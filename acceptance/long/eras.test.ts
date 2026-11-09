import assert from "node:assert/strict";
import { test } from "node:test";
import { World } from "../src/api/client.ts";
import type { Era, GameConfig, Kingdom, Rankings } from "../src/api/types.ts";
import { buildingNames, resourceNames, unitNames } from "../src/api/types.ts";
import { startServer } from "../src/server.ts";
import { coalitionWarlord, foundCoalition, guardian, mason } from "../src/simulation/coalition.ts";
import { economist, idle, raider, turtle, warlord } from "../src/simulation/strategies.ts";
import { foundRulers, runWorld, unevenPace } from "../src/simulation/world.ts";

// Months of the game as it is configured: eras of up to four months played hour by hour, by rival coalitions and lone
// rulers who come back at their own pace while the merchants trade. Every rule holds for every kingdom after every
// tick, and every era ends the way the game promises, written into the hall of fame, with every kingdom starting afresh
// while alliances stay. In the first era Order founds a monument a month in and its mason pays for it, so Order wins by
// the monument before the clock runs out; in the second nobody builds one, so the clock ends it and the alliance with
// the most land wins. The monument is priced in gold alone, at what one mason earns in weeks rather than a coalition's
// month of every craft, so that this world tests the ending, not the balance.

const eras = 2;
const monument = {
  "monument.stages": 4,
  "monument.stageCost.gold": 50_000_000,
  "monument.stageCost.iron": 0,
  "monument.stageCost.tools": 0,
  "monument.upkeepPerStage.gold": 100_000,
  "monument.upkeepPerStage.food": 0,
};
/** a month into the first era */
const foundedOnTick = 720;
/** how each era must end */
const endings: { endedBy: "monument" | "clock"; winner: (standing: Rankings) => string | undefined }[] = [
  { endedBy: "monument", winner: () => "Order" },
  { endedBy: "clock", winner: standing => standing.alliances[0]?.allianceName },
];

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
const recordProblems = (record: Era | undefined, ended: Era, standing: Rankings, ending: (typeof endings)[number]): string[] => {
  if (!record) return [`era ${ended.number} is missing from the hall of fame`];
  const problems: string[] = [];
  if (record.number !== ended.number) problems.push(`the hall of fame's newest era is ${record.number}, not ${ended.number}`);
  if (!record.endedAt) problems.push(`era ${ended.number} has no end`);
  if (record.endedBy !== ending.endedBy) problems.push(`era ${ended.number} ended by ${record.endedBy}, not by ${ending.endedBy}`);
  // the clock ends an era only on its last tick, while a monument ends it before
  const ranOut = ended.ticksElapsed + 1 >= ended.maxLengthInTicks;
  if (ranOut !== (ending.endedBy === "clock")) problems.push(`era ${ended.number} ended by ${record.endedBy} after ${ended.ticksElapsed + 1} of ${ended.maxLengthInTicks} ticks`);
  if (record.winningAlliance !== ending.winner(standing)) problems.push(`era ${ended.number} was won by ${record.winningAlliance}, not ${ending.winner(standing)}`);
  const final = (record.finalRankings?.kingdoms ?? []).map(rank => `${rank.rank} ${rank.kingdomName} ${rank.land}`);
  const last = standing.kingdoms.map(rank => `${rank.rank} ${rank.kingdomName} ${rank.land}`);
  if (JSON.stringify(final) !== JSON.stringify(last)) problems.push(`era ${ended.number} was recorded as ${final.slice(0, 3).join(", ")}, but ended at ${last.slice(0, 3).join(", ")}`);
  return problems;
};

test(`${eras} eras keep every rule and end as promised, the first by a monument and the second by the clock`, async () => {
  const server = await startServer(monument);
  try {
    const world = new World(server.url);
    const config = await world.config();
    const order = await foundCoalition(world, "Order", council => [coalitionWarlord(council), mason("Order"), guardian(council)]);
    const horde = await foundCoalition(world, "Horde", () => [warlord(), economist, turtle, raider]);
    const loners = await foundRulers(world, [economist, idle], "Loner");
    const rulers = [...order.rulers, ...horde.rulers, ...loners];
    const herald = rulers[0].player;

    const problems: string[] = [];
    let era = await world.era();
    const first = era.number;
    let standing = await world.rankings();
    const started = Date.now();

    const run = await runWorld(world, rulers, eras * config.era.maxLengthInTicks, {
      pace: unevenPace,
      afterTick: async tick => {
        const now = await world.era();
        if (now.number === first && now.ticksElapsed === foundedOnTick) {
          const emperor = order.rulers[0].player;
          const founded = await emperor.foundMonument(emperor.name);
          if (!founded.success) problems.push(`tick ${tick}: Order could not found its monument, ${founded.message}`);
        }
        if (now.number !== era.number) {
          if (now.number !== era.number + 1 || now.ticksElapsed !== 0) problems.push(`tick ${tick}: era ${era.number} was followed by era ${now.number} at tick ${now.ticksElapsed}`);
          const ending = endings[era.number - first];
          if (ending) problems.push(...recordProblems((await herald.finishedEras())[0], era, standing, ending));
          const bots = new Set(standing.kingdoms.filter(rank => rank.bot).map(rank => rank.kingdomName));
          for (const kingdom of await world.kingdoms()) {
            if (!bots.has(kingdom.name)) problems.push(...freshStartProblems(kingdom, config, now.number));
          }
          const alliances = (await world.rankings()).alliances.map(alliance => alliance.allianceName).sort();
          if (JSON.stringify(alliances) !== JSON.stringify(["Horde", "Order"])) problems.push(`era ${now.number} begins with alliances ${alliances.join(", ")}`);
          if (!(await herald.notifications()).some(note => note.message.startsWith(`Era ${era.number} has ended`))) problems.push(`nobody heard that era ${era.number} ended`);
          console.log(`era ${era.number} ended after ${era.ticksElapsed + 1} ticks and ${Math.round((Date.now() - started) / 1000)} s, won by ${(await herald.finishedEras())[0]?.winningAlliance} by ${(await herald.finishedEras())[0]?.endedBy}`);
        }
        era = now;
      },
      afterTurn: async () => {
        standing = await world.rankings();
      },
      until: () => era.number > first + eras - 1,
    });

    assert.deepEqual(run.violations.slice(0, 20), [], `${run.violations.length} broken rules`);
    assert.deepEqual(problems, []);
    assert.equal(era.number, first + eras, `the world reached era ${era.number}`);
  } finally {
    await server.stop();
  }
});
