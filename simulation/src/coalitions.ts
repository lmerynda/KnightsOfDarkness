import { writeFileSync } from "node:fs";
import { parseArgs } from "node:util";
import { World } from "./client.ts";
import { coalitionWarlord, foundCoalition, type Coalition } from "./coalition.ts";
import { startServer, type ConfigOverrides } from "./server.ts";
import { economist, turtle, warlord, type Strategy } from "./strategies.ts";
import { finalSnapshot, foundRulers, runWorld, type Ruler, type WorldRun } from "./world.ts";

const usage = `Play coalitions of ten against each other and report how each group fared.

  node src/coalitions.ts [--turns 120] [--set path=value ...] [--compare] [--out run.json]

Three groups of ten with the same make-up (5 economists, 3 warlords, 2 turtles): Order, a coalition whose warlords
strike together through a war council; Horde, a coalition whose warlords each pick their own target; and Free, ten
kingdoms in no alliance. The merchant bots play too.`;

const { values } = parseArgs({
  options: {
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

const makeUp = (warlords: () => Strategy): Strategy[] => [
  economist,
  economist,
  economist,
  economist,
  economist,
  warlords(),
  warlords(),
  warlords(),
  turtle,
  turtle,
];

type Group = { name: string; rulers: Ruler[] };
type GroupRun = { run: WorldRun; groups: Record<string, string[]> };

const simulate = async (label: string, configOverrides: ConfigOverrides): Promise<GroupRun> => {
  const server = await startServer(configOverrides);
  try {
    const world = new World(server.url);
    const order: Coalition = await foundCoalition(world, "Order", council => makeUp(() => coalitionWarlord(council)));
    const horde: Coalition = await foundCoalition(world, "Horde", () => makeUp(() => warlord()));
    const free: Group = {
      name: "Free",
      rulers: await foundRulers(
        world,
        makeUp(() => warlord()),
        "Free",
      ),
    };
    const groups = [order, horde, free];
    const started = Date.now();
    const run = await runWorld(
      world,
      groups.flatMap(group => group.rulers),
      turns,
    );
    console.error(`${label}: ${turns} turns in ${((Date.now() - started) / 1000).toFixed(1)} s`);
    return {
      run,
      groups: Object.fromEntries(groups.map(group => [group.name, group.rulers.map(ruler => ruler.player.name)])),
    };
  } finally {
    await server.stop();
  }
};

const summarize = ({ run, groups }: GroupRun): Record<string, Record<string, number>> => {
  const groupOf = (kingdom: string): string => Object.entries(groups).find(([, members]) => members.includes(kingdom))?.[0] ?? "bots";
  return Object.fromEntries(
    Object.entries(groups).map(([name, members]) => {
      const finals = members.map(member => finalSnapshot(run, member));
      const won = run.battles.filter(battle => members.includes(battle.attacker) && battle.attackerWon);
      const lost = run.battles.filter(battle => members.includes(battle.defender) && battle.attackerWon);
      const sum = (pick: (entry: (typeof finals)[number]) => number): number => finals.reduce((total, entry) => total + pick(entry), 0);
      return [
        name,
        {
          land: sum(entry => entry.land),
          "largest member": Math.max(...finals.map(entry => entry.land)),
          army: sum(entry => entry.army),
          defense: Math.round(sum(entry => entry.defense)),
          victories: won.length,
          "land taken": won.reduce((total, battle) => total + battle.landCaptured, 0),
          "of it from players": won
            .filter(battle => groupOf(battle.defender) !== "bots")
            .reduce((total, battle) => total + battle.landCaptured, 0),
          "defeats suffered": lost.length,
          "land lost": lost.reduce((total, battle) => total + battle.landCaptured, 0),
        },
      ];
    }),
  );
};

const printTable = (
  title: string,
  summary: Record<string, Record<string, number>>,
  baseline?: Record<string, Record<string, number>>,
): void => {
  console.log(`\n${title}`);
  console.table(
    Object.entries(summary).map(([group, metrics]) => ({
      group,
      ...Object.fromEntries(
        Object.entries(metrics).map(([metric, value]) => {
          const before = baseline?.[group]?.[metric];
          return [
            metric,
            before === undefined || before === value
              ? value.toLocaleString("en-US")
              : `${value.toLocaleString("en-US")} (${value > before ? "+" : ""}${(value - before).toLocaleString("en-US")})`,
          ];
        }),
      ),
    })),
  );
};

const main = async (): Promise<void> => {
  const runs: Record<string, GroupRun> = {};
  if (values.compare) runs.base = await simulate("base", {});
  runs.variant = await simulate(Object.keys(overrides).length ? "variant" : "base", overrides);
  if (runs.base) {
    printTable("Base config", summarize(runs.base));
    printTable(`With ${values.set.join(", ")} (change against base in brackets)`, summarize(runs.variant), summarize(runs.base));
  } else {
    printTable(Object.keys(overrides).length ? `With ${values.set.join(", ")}` : "Base config", summarize(runs.variant));
  }
  const violations = Object.values(runs).flatMap(entry => entry.run.violations);
  console.log(
    violations.length ? `\n${violations.length} rule violations, first: ${violations.slice(0, 5).join("; ")}` : "\nNo rule violations.",
  );
  if (values.out) {
    writeFileSync(values.out, JSON.stringify(runs, null, 2));
    console.log(`Full runs written to ${values.out}`);
  }
  process.exitCode = violations.length ? 1 : 0;
};

try {
  await main();
} catch (error) {
  console.error(error instanceof Error ? error.message : error);
  process.exitCode = 2;
}
