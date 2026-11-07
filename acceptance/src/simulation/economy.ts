import { writeFileSync } from "node:fs";
import { parseArgs } from "node:util";
import { World } from "../api/client.ts";
import type { MarketTrade } from "../api/types.ts";
import { startServer, type ConfigOverrides } from "../server.ts";
import { foundCoalition } from "./coalition.ts";
import { coalitionOf, parseCraft, selfSufficient, tradeShares, type Craft } from "./trade.ts";
import { finalSnapshot, runWorld, type WorldRun } from "./world.ts";

const usage = `Play coalitions that trade goods, and report how each fares and where its market purchases came from.

  node src/simulation/economy.ts [--coalition Name=craft,craft,... ...] [--turns 120] [--set path=value ...] [--compare] [--out run.json]

Each --coalition founds one coalition with one kingdom per craft; without any, two self-sufficient coalitions play
(Guild and Hanse, each ${selfSufficient.join(",")}). Crafts: gold producers buy what they eat and the tools they train
with; a farmer (food), a smith (tools and weapons, as either sells, buying iron) and an iron miner (iron) sell their
goods and grow gold mines instead while their goods pile up unsold; a warrior arms an army with bought weapons and
strikes the largest kingdom in reach it clearly beats every six turns; corner:iron (or corner: with food, tools or weapons) is a
gold producer that buys every piece rivals and merchants offer and asks twice the merchants' price on the market; the
coalition's own craftsmen of that good ask that price too, and the coalition passes the good to its members by
carriers instead, billed at the merchants' price. A coalition may leave a craft out and rely on the market. Each buys
from its allies first and from anyone else for the rest, and grows its own food when it cannot buy enough. Rulers play
in a new random order every turn, so no coalition always meets the market and the armies first. The merchant bots play
too. The report gives each kingdom's land, gold and army, each coalition's land and its craftsmen's land against its
gold producers', and the pieces of every good each coalition bought from allies, from the bots and from other kingdoms.`;

const { values } = parseArgs({
  options: {
    coalition: { type: "string", multiple: true, default: [] },
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

const makeUps: [string, Craft[]][] = [];
try {
  const given = values.coalition.length ? values.coalition : [`Guild=${selfSufficient.join(",")}`, `Hanse=${selfSufficient.join(",")}`];
  for (const entry of given) {
    const [name, crafts] = entry.split("=");
    if (!name || !crafts) throw new Error(`A coalition is written Name=craft,craft,..., not "${entry}"`);
    makeUps.push([name.trim(), crafts.split(",").map(parseCraft)]);
  }
} catch (error) {
  console.error(`${error instanceof Error ? error.message : error}\n\n${usage}`);
  process.exit(2);
}
const turns = Number(values.turns);
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
    for (const [name, crafts] of makeUps) coalitions.push(await foundCoalition(world, name, () => coalitionOf(crafts)));
    const rulers = coalitions.flatMap(coalition => coalition.rulers);
    const run = await runWorld(world, rulers, turns, { shuffle: true });
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
        return { kingdom, land: last.land, gold: last.gold.toLocaleString("en-US"), food: last.food.toLocaleString("en-US"), people: last.people, army: last.army, starved: run.timeline.filter(entry => entry.kingdom === kingdom).reduce((sum, entry) => sum + entry.starved, 0) };
      }),
  );
  for (const [name, members] of Object.entries(groups)) {
    const lands = members.map(kingdom => ({ craft: kingdom.replace(`${name} `, "").replace(/ \d+$/, ""), land: finalSnapshot(run, kingdom).land }));
    const average = (craft: string): number => {
      const of = lands.filter(entry => entry.craft === craft);
      return of.reduce((sum, entry) => sum + entry.land, 0) / of.length;
    };
    const gold = average("gold");
    const crafts = [...new Set(lands.map(entry => entry.craft))].map(craft => `${craft} ${Math.round(average(craft))} (${Math.round((100 * average(craft)) / gold)}%)`);
    console.log(`${name} holds ${lands.reduce((sum, entry) => sum + entry.land, 0).toLocaleString("en-US")} acres; average land by craft against its gold producers: ${crafts.join(", ")}`);
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
