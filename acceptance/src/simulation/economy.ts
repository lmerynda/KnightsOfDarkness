import { writeFileSync } from "node:fs";
import { parseArgs } from "node:util";
import { World } from "../api/client.ts";
import type { MarketTrade } from "../api/types.ts";
import { startServer, type ConfigOverrides } from "../server.ts";
import { foundCoalition } from "./coalition.ts";
import { selfSufficient, specialist, tradeShares, type Craft } from "./trade.ts";
import { finalSnapshot, runWorld, type WorldRun } from "./world.ts";

const usage = `Play coalitions that feed, tool and arm themselves, and report where their market purchases came from.

  node src/simulation/economy.ts [--crafts gold,gold,gold,gold,food,tools,tools,iron] [--coalitions 2] [--turns 120] [--set path=value ...] [--compare] [--out run.json]

Every coalition has the same make-up, one kingdom per craft: gold producers buy what they eat and the tools they train
with, the farmer sells food, blacksmiths sell tools and buy iron, the iron miner sells iron. Each buys from its allies
first and from anyone else on the market for the rest. The merchant bots play too. The report gives each kingdom's land
and gold, and for each coalition the pieces of every good its members bought from allies, from the bots and from other
kingdoms; the point of a coalition is that most of its market stays inside it.`;

const { values } = parseArgs({
  options: {
    crafts: { type: "string", default: selfSufficient.join(",") },
    coalitions: { type: "string", default: "2" },
    turns: { type: "string", default: "120" },
    set: { type: "string", multiple: true, default: [] },
    compare: { type: "boolean", default: false },
    out: { type: "string" },
    help: { type: "boolean", default: false },
  },
});
if (values.help) {
  console.log(usage);
  process.exit(0);
}

const crafts = values.crafts.split(",").map(craft => craft.trim()) as Craft[];
const unknown = crafts.filter(craft => !["gold", "food", "tools", "iron"].includes(craft));
if (unknown.length) {
  console.error(`Unknown crafts: ${unknown.join(", ")}\n\n${usage}`);
  process.exit(2);
}
const turns = Number(values.turns);
const names = ["Guild", "Hanse", "League", "Union"].slice(0, Number(values.coalitions));
const overrides: ConfigOverrides = Object.fromEntries(
  values.set.map(entry => {
    const [path, ...rest] = entry.split("=");
    const raw = rest.join("=");
    try {
      return [path, JSON.parse(raw)];
    } catch {
      return [path, raw];
    }
  }),
);

type EconomyRun = { run: WorldRun; trades: MarketTrade[]; groups: Record<string, string[]>; bots: string[] };

export const simulate = async (configOverrides: ConfigOverrides): Promise<EconomyRun> => {
  const server = await startServer(configOverrides);
  try {
    const world = new World(server.url);
    const coalitions = [];
    for (const name of names) coalitions.push(await foundCoalition(world, name, () => crafts.map(specialist)));
    const rulers = coalitions.flatMap(coalition => coalition.rulers);
    const run = await runWorld(world, rulers, turns);
    const bots = (await world.rankings()).kingdoms.filter(rank => rank.bot).map(rank => rank.kingdomName);
    const groups = Object.fromEntries(coalitions.map(coalition => [coalition.name, coalition.rulers.map(ruler => ruler.player.name)]));
    return { run, trades: await world.trades(), groups, bots };
  } finally {
    await server.stop();
  }
};

const report = (title: string, { run, trades, groups, bots }: EconomyRun): void => {
  console.log(`\n${title}`);
  console.table(
    Object.values(groups)
      .flat()
      .map(kingdom => {
        const last = finalSnapshot(run, kingdom);
        return { kingdom, land: last.land, gold: last.gold.toLocaleString("en-US"), food: last.food.toLocaleString("en-US"), people: last.people, starved: run.timeline.filter(entry => entry.kingdom === kingdom).reduce((sum, entry) => sum + entry.starved, 0) };
      }),
  );
  for (const [name, members] of Object.entries(groups)) {
    const shares = tradeShares(trades, members, bots);
    console.log(`${name} bought ${Math.round(shares.fromAllies * 100)}% of its pieces from its own members`);
    console.table(Object.fromEntries(Object.entries(shares.bought).filter(([, row]) => row.allies + row.bots + row.others > 0)));
  }
  const botSales = trades.filter(trade => bots.includes(trade.seller)).reduce((sum, trade) => sum + trade.count, 0);
  const allSales = trades.reduce((sum, trade) => sum + trade.count, 0);
  console.log(`The bots sold ${botSales.toLocaleString("en-US")} of ${allSales.toLocaleString("en-US")} pieces traded.`);
  console.log(run.violations.length ? `${run.violations.length} rule violations, first: ${run.violations.slice(0, 5).join("; ")}` : "No rule violations.");
};

const runs: Record<string, EconomyRun> = {};
try {
  if (values.compare) {
    runs.base = await simulate({});
    report("Base config", runs.base);
  }
  runs.variant = await simulate(overrides);
  report(Object.keys(overrides).length ? `With ${values.set.join(", ")}` : "Base config", runs.variant);
  if (values.out) writeFileSync(values.out, JSON.stringify(runs, null, 2));
  process.exitCode = Object.values(runs).some(run => run.run.violations.length) ? 1 : 0;
} catch (error) {
  console.error(error instanceof Error ? error.message : error);
  process.exitCode = 2;
}
