import { appendFileSync } from "node:fs";
import { World } from "../api/client.ts";
import { militaryUnits, type MarketTrade } from "../api/types.ts";
import { startServer, type ConfigOverrides } from "../server.ts";
import { foundCoalition } from "./coalition.ts";
import { freshLessons, type Band, type MonumentEvent, type MonumentPolicy } from "./monuments.ts";
import { coalitionOf, type Craft } from "./trade.ts";
import { runWorld, type WorldRun } from "./world.ts";

export const ticksPerDay = 24;

/** how the coalitions play the era's monuments; see the usage of `economy.ts` */
export type MonumentPlay = {
  /** the lead at which each named coalition founds its monument */
  found: Map<string, number>;
  /** the first day a coalition may found one */
  foundFromDay: number;
  /** the coalitions whose warriors assault rival monuments; null for every coalition */
  assault: string[] | null;
  /** assaulting coalitions march together, on the same turns, counting their armies together */
  band: boolean;
  /** the stage from which a rival monument is assaulted; half the stages when not given */
  assaultFrom?: number;
  payShare: number;
  guardShare: number;
};

export type EconomyWorld = {
  coalitions: [string, Craft[]][];
  turns: number;
  overrides: ConfigOverrides;
  /** play the era's monuments and stop when the era ends */
  monuments?: MonumentPlay;
  /** a file to append a line per kingdom and day to, and a line on the alliances and monuments */
  progress?: string;
};

/** an alliance's land and its lead over the other alliances' average, and the monuments, at the start of a day */
export type EraDay = {
  turn: number;
  alliances: Record<string, { land: number; lead: number }>;
  monuments: { alliance: string; stages: number; garrison: number; ticksToWin: number | null }[];
};

/** how the era ended, when the world plays monuments */
export type EraOutcome = {
  endedOnTurn: number | null;
  endedBy: string | null;
  winner: string | null;
  events: MonumentEvent[];
  assaults: { turn: number; attacker: string; defender: string; won: boolean; stagesRazed: number; attackPower: number; defensePower: number; sent: number }[];
  days: EraDay[];
};

export type EconomyRun = { run: WorldRun; trades: MarketTrade[]; groups: Record<string, string[]>; bots: string[]; era?: EraOutcome };

/** plays coalitions of crafts on a game server of their own; with monuments, until the era ends or the turns run out */
export const playEconomy = async ({ coalitions: makeUps, turns, overrides, monuments: play, progress }: EconomyWorld): Promise<EconomyRun> => {
  const server = await startServer(overrides);
  try {
    const world = new World(server.url);
    const config = await world.config();
    const policies: MonumentPolicy[] = [];
    const band: Band = { armies: new Map(), lessons: freshLessons() };
    const coalitions = [];
    for (const [index, [name, crafts]] of makeUps.entries()) {
      const policy: MonumentPolicy | undefined = play
        ? {
            alliance: name,
            emperor: "",
            foundAtLead: play.found.get(name) ?? null,
            foundFromTurn: play.foundFromDay * ticksPerDay,
            payShare: play.payShare,
            guardShare: play.guardShare,
            assaultFromStage: play.assault === null || play.assault.includes(name) ? (play.assaultFrom ?? config.monument.stages / 2) : null,
            assaultOffset: play.band ? 0 : 2 * index,
            band: play.band ? band : undefined,
            lessons: freshLessons(),
            armies: new Map(),
            events: [],
          }
        : undefined;
      const coalition = await foundCoalition(world, name, () => coalitionOf(crafts, policy));
      if (policy) {
        policy.emperor = coalition.rulers[0].player.name;
        policies.push(policy);
      }
      coalitions.push(coalition);
    }
    const rulers = coalitions.flatMap(coalition => coalition.rulers);
    const coalitionOfKingdom = new Map(coalitions.flatMap(coalition => coalition.rulers.map(ruler => [ruler.player.name, coalition.name] as const)));
    const days: EraDay[] = [];
    const startOfDay = async (turn: number): Promise<void> => {
      if (turn % ticksPerDay !== 0) return;
      const lines = [];
      if (progress) {
        for (const ruler of rulers) {
          const [kingdom, details] = await Promise.all([ruler.player.kingdom(), ruler.player.details(ruler.strategy.weaponsPercentage)]);
          const produced = details.forecast.resourcesProduced;
          lines.push(JSON.stringify({
            turn,
            coalition: coalitionOfKingdom.get(ruler.player.name),
            kingdom: ruler.player.name,
            land: kingdom.resources.land,
            gold: kingdom.resources.gold,
            army: militaryUnits.reduce((sum, unit) => sum + kingdom.units.availableUnits[unit] + kingdom.units.mobileUnits[unit], 0),
            defense: details.defensePower,
            goldUpkeep: details.forecast.goldUpkeep,
            produced: { gold: produced.gold ?? 0, food: produced.food ?? 0, iron: produced.iron ?? 0, tools: produced.tools ?? 0, weapons: produced.weapons ?? 0 },
          }));
        }
      }
      if (play) {
        const rankings = await world.rankings();
        const average = (except: string): number => {
          const others = rankings.alliances.filter(entry => entry.allianceName !== except);
          return others.reduce((sum, entry) => sum + entry.land, 0) / Math.max(1, others.length);
        };
        const day: EraDay = {
          turn,
          alliances: Object.fromEntries(rankings.alliances.map(entry => [entry.allianceName, { land: entry.land, lead: Math.round((entry.land / average(entry.allianceName) - 1) * 1000) / 1000 }])),
          monuments: (await world.monuments()).map(monument => ({ alliance: monument.allianceName, stages: monument.stages, garrison: monument.garrison, ticksToWin: monument.ticksToWin })),
        };
        days.push(day);
        lines.push(JSON.stringify(day));
      }
      if (progress && lines.length) appendFileSync(progress, lines.join("\n") + "\n");
    };
    // assaults are read as they happen, since the era's end clears every battle report
    const assaults = new Map<string, EraOutcome["assaults"][number]>();
    const readAssaults = async (turn: number): Promise<void> => {
      for (const ruler of rulers) {
        for (const report of await ruler.player.battleReports()) {
          if (report.attackType !== "monument" || report.attacker !== ruler.player.name || assaults.has(report.id)) continue;
          assaults.set(report.id, {
            turn,
            attacker: report.attacker,
            defender: report.defender,
            won: report.attackerWon,
            stagesRazed: report.stagesRazed,
            attackPower: report.attackPower,
            defensePower: report.defensePower,
            sent: Object.values(report.unitsSent).reduce((sum, count) => sum + (count ?? 0), 0),
          });
        }
      }
    };
    const firstEra = (await world.era()).number;
    let endedOnTurn: number | null = null;
    const run = await runWorld(world, rulers, turns, {
      shuffle: true,
      atOnce: true,
      afterTurn: async turn => {
        await startOfDay(turn);
        if (play && turn % 6 === 5) await readAssaults(turn);
      },
      afterTick: play
        ? async turn => {
            if (endedOnTurn === null && (await world.era()).number !== firstEra) endedOnTurn = turn;
          }
        : undefined,
      until: () => endedOnTurn !== null,
    });
    let era: EraOutcome | undefined;
    if (play) {
      const record = endedOnTurn === null ? undefined : (await rulers[0].player.finishedEras())[0];
      era = {
        endedOnTurn,
        endedBy: record?.endedBy ?? null,
        winner: record?.winningAlliance ?? null,
        events: policies.flatMap(policy => policy.events).sort((one, other) => one.turn - other.turn),
        assaults: [...assaults.values()],
        days,
      };
    }
    const bots = (await world.rankings()).kingdoms.filter(rank => rank.bot).map(rank => rank.kingdomName);
    const groups = Object.fromEntries(coalitions.map(coalition => [coalition.name, coalition.rulers.map(ruler => ruler.player.name)]));
    return { run, trades: await world.trades(), groups, bots, era };
  } finally {
    await server.stop();
  }
};
