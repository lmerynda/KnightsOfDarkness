import { writeFileSync } from "node:fs";
import { parseArgs } from "node:util";
import type { ConfigOverrides } from "../server.ts";
import { playEconomy, ticksPerDay, type EconomyRun, type MonumentPlay } from "./economyWorld.ts";
import { parseCraft, selfSufficient, tradeShares, type Craft } from "./trade.ts";
import { finalSnapshot } from "./world.ts";

const usage = `Play coalitions that trade goods, and report how each fares and where its market purchases came from.

  node src/simulation/economy.ts [--coalition Name=craft,craft,... ...] [--turns 120] [--set path=value ...] [--compare] [--out run.json] [--progress days.jsonl]
      [--monuments [--found Name=lead ...] [--found-from 30] [--assault Name ...] [--band] [--assault-from stages] [--pay-share 0.5] [--guard-share 0.9]]

Each --coalition founds one coalition with one kingdom per craft; without any, two self-sufficient coalitions play
(Guild and Hanse, each ${selfSufficient.join(",")}). Crafts: gold producers buy what they eat and the tools they train
with; a farmer (food), a smith (tools and weapons, as either sells, buying iron) and an iron miner (iron) sell their
goods and grow gold mines instead while their goods pile up unsold; a warrior arms an army with bought weapons and
strikes the largest kingdom in reach it clearly beats every six turns; corner:iron (or corner: with food, tools or weapons) is a
gold producer that buys every piece rivals and merchants offer and asks twice the merchants' price on the market; the
coalition's own craftsmen of that good ask that price too, and the coalition passes the good to its members by
carriers instead, billed at the merchants' price. Every member but the warrior keeps a garrison of bowmen and infantry
in barracks on 1% of its land, and 1% more for each strike it lost in the last 24 turns, up to 4%. A coalition may leave a craft out and rely on the market. Each buys
from its allies first and from anyone else for the rest, and grows its own food when it cannot buy enough. Rulers play
their turns at the same moment, as players do, so no coalition always meets the market and the armies first. The merchant bots play
too. The report gives each kingdom's land, gold and army, each coalition's land and its craftsmen's land against its
gold producers', and the pieces of every good each coalition bought from allies, from the bots and from other kingdoms.
--progress appends one line per kingdom every 24 turns (a day of hourly turns) as the run goes: its land, gold, army,
defense, the gold its soldiers cost and what it will produce next turn, so a run of months can be read while it plays and
survives a crash.

--monuments plays the era's monuments and ends the run when the era ends. Each --found lets that coalition's emperor
found a monument in its own kingdom once the alliance's land leads the other alliances' average by the share given
(0.2 for 20%), from day --found-from on (30 by default, since a lead in members shows before the economy can carry a
monument); every member then pays --pay-share of what it can spare towards the next stage and two days of upkeep
each turn, puts 2% of its land into barracks for the guard, and keeps --guard-share of its soldiers standing guard
there. Each --assault coalition (every coalition when none is named) marches on any other alliance's monument from
--assault-from stages on (half the stages by default) as one army, its members joining whoever leads with all but a
tenth of their soldiers, whenever its armies match what its battle reports taught it the garrison takes; coalitions
march two turns apart, or with --band all on the same turn, counting their armies together.
The report tells how the era ended, who won, and what happened to every monument; --progress adds a line a day with
each alliance's lead and each monument's stages and garrison.`;

const { values } = parseArgs({
  options: {
    coalition: { type: "string", multiple: true, default: [] },
    turns: { type: "string", default: "120" },
    set: { type: "string", multiple: true, default: [] },
    compare: { type: "boolean", default: false },
    out: { type: "string" },
    progress: { type: "string" },
    monuments: { type: "boolean", default: false },
    found: { type: "string", multiple: true, default: [] },
    "found-from": { type: "string", default: "30" },
    assault: { type: "string", multiple: true, default: [] },
    "assault-from": { type: "string" },
    band: { type: "boolean", default: false },
    "pay-share": { type: "string", default: "0.5" },
    "guard-share": { type: "string", default: "0.9" },
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

const play: MonumentPlay | undefined = values.monuments
  ? {
      found: new Map(
        values.found.map(entry => {
          const [name, lead] = entry.split("=");
          return [name.trim(), Number(lead)] as const;
        }),
      ),
      foundFromDay: Number(values["found-from"]),
      assault: values.assault.length ? values.assault : null,
      band: values.band,
      assaultFrom: values["assault-from"] === undefined ? undefined : Number(values["assault-from"]),
      payShare: Number(values["pay-share"]),
      guardShare: Number(values["guard-share"]),
    }
  : undefined;

const simulate = (configOverrides: ConfigOverrides): Promise<EconomyRun> => playEconomy({ coalitions: makeUps, turns, overrides: configOverrides, monuments: play, progress: values.progress });

const report = (title: string, { run, trades, groups, bots, era }: EconomyRun): void => {
  console.log(`\n${title}`);
  if (era) {
    console.log(era.endedOnTurn === null ? "The era did not end within the run." : `The era ended on turn ${era.endedOnTurn} (day ${Math.floor(era.endedOnTurn / ticksPerDay)}) by ${era.endedBy}, won by ${era.winner}.`);
    for (const event of era.events.filter(candidate => !candidate.what.includes(" marched on "))) console.log(`  day ${Math.floor(event.turn / ticksPerDay)}: ${event.alliance} ${event.what}`);
    const byAttacker = new Map<string, { won: number; lost: number; razed: number }>();
    for (const assault of era.assaults) {
      const entry = byAttacker.get(assault.attacker) ?? { won: 0, lost: 0, razed: 0 };
      if (assault.won) entry.won++;
      else entry.lost++;
      entry.razed += assault.stagesRazed;
      byAttacker.set(assault.attacker, entry);
    }
    for (const assault of era.assaults) console.log(`  day ${Math.floor(assault.turn / ticksPerDay)}: ${assault.attacker} ${assault.won ? "stormed" : "was repelled at"} ${assault.defender} with ${assault.sent} soldiers, attack ${assault.attackPower} against defense ${assault.defensePower}${assault.won ? `, ${assault.stagesRazed.toFixed(2)} stages razed` : ""}`);
    for (const [attacker, entry] of byAttacker) console.log(`  ${attacker}: ${entry.won} assaults won, ${entry.lost} lost, ${entry.razed.toFixed(2)} stages razed`);
  }
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
