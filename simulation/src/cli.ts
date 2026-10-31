import { writeFileSync } from "node:fs";
import { parseArgs } from "node:util";
import { World } from "./client.ts";
import { startServer, type ConfigOverrides } from "./server.ts";
import { strategies } from "./strategies.ts";
import { finalSnapshot, foundRulers, runWorld, type WorldRun } from "./world.ts";

const usage = `Simulate a world on a fresh game server and report how each ruler fared.

  node src/cli.ts [--players economist,warlord,turtle,idle] [--turns 60] [--set path=value ...] [--compare] [--no-bots] [--out report.json]

  --players   strategies to play, one kingdom each: ${Object.keys(strategies).join(", ")}
  --turns     turns to play
  --set       override gameconfig.json, for example --set attack.maxLandCaptureRatio=0.2 (repeatable)
  --compare   run the base config and the overridden one with the same rulers and print both
  --no-bots   leave the merchant bots idle
  --out       write the full runs (timelines, battles, violations) as JSON`;

const parseValue = (raw: string): unknown => {
  try {
    return JSON.parse(raw);
  } catch {
    return raw;
  }
};

const { values } = parseArgs({
  options: {
    players: { type: "string", default: "economist,warlord,turtle,idle" },
    turns: { type: "string", default: "60" },
    set: { type: "string", multiple: true, default: [] },
    compare: { type: "boolean", default: false },
    "no-bots": { type: "boolean", default: false },
    out: { type: "string" },
    help: { type: "boolean", default: false },
  },
});

if (values.help) {
  console.log(usage);
  process.exit(0);
}

const playerNames = values.players.split(",").map(name => name.trim());
const unknown = playerNames.filter(name => !(name in strategies));
if (unknown.length) {
  console.error(`Unknown strategies: ${unknown.join(", ")}\n\n${usage}`);
  process.exit(2);
}
const turns = Number(values.turns);
const overrides: ConfigOverrides = Object.fromEntries(
  values.set.map(entry => {
    const [path, ...rest] = entry.split("=");
    return [path, parseValue(rest.join("="))];
  }),
);

const simulate = async (label: string, configOverrides: ConfigOverrides): Promise<WorldRun> => {
  const server = await startServer(configOverrides);
  try {
    const world = new World(server.url);
    const rulers = await foundRulers(
      world,
      playerNames.map(name => strategies[name]()),
    );
    const started = Date.now();
    const run = await runWorld(world, rulers, turns, { bots: !values["no-bots"] });
    console.error(`${label}: ${turns} turns in ${((Date.now() - started) / 1000).toFixed(1)} s`);
    return run;
  } finally {
    await server.stop();
  }
};

const summarize = (run: WorldRun): Record<string, Record<string, number>> =>
  Object.fromEntries(
    [...new Set(run.timeline.map(entry => entry.kingdom))].map(kingdom => {
      const last = finalSnapshot(run, kingdom);
      const won = run.battles.filter(battle => battle.attacker === kingdom && battle.attackerWon).length;
      const lost = run.battles.filter(battle => battle.defender === kingdom && battle.attackerWon).length;
      return [kingdom, { land: last.land, gold: last.gold, food: last.food, buildings: last.buildings, people: last.people, army: last.army, defense: Math.round(last.defense), "battles won": won, "raids suffered": lost }];
    }),
  );

const printTable = (title: string, summary: Record<string, Record<string, number>>, baseline?: Record<string, Record<string, number>>): void => {
  console.log(`\n${title}`);
  const rows = Object.entries(summary).map(([kingdom, metrics]) => ({
    kingdom,
    ...Object.fromEntries(
      Object.entries(metrics).map(([metric, value]) => {
        const before = baseline?.[kingdom]?.[metric];
        return [metric, before === undefined || before === value ? value.toLocaleString("en-US") : `${value.toLocaleString("en-US")} (${value > before ? "+" : ""}${(value - before).toLocaleString("en-US")})`];
      }),
    ),
  }));
  console.table(rows);
};

const main = async (): Promise<void> => {
  const runs: Record<string, WorldRun> = {};
  if (values.compare) {
    runs.base = await simulate("base", {});
  }
  runs.variant = await simulate(Object.keys(overrides).length ? "variant" : "base", overrides);

  const variantSummary = summarize(runs.variant);
  if (runs.base) {
    printTable("Base config", summarize(runs.base));
    printTable(`With ${values.set.join(", ")} (change against base in brackets)`, variantSummary, summarize(runs.base));
  } else {
    printTable(Object.keys(overrides).length ? `With ${values.set.join(", ")}` : "Base config", variantSummary);
  }

  const violations = Object.values(runs).flatMap(run => run.violations);
  console.log(violations.length ? `\n${violations.length} rule violations, first: ${violations.slice(0, 5).join("; ")}` : "\nNo rule violations.");
  if (values.out) {
    writeFileSync(values.out, JSON.stringify(runs, null, 2));
    console.log(`Full runs written to ${values.out}`);
  }
  process.exitCode = violations.length ? 1 : 0;
};

await main();
