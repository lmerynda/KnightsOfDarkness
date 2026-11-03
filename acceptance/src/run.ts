// node src/run.ts <suite> [node --test options]: builds what the suite needs once, then runs its test files, each on a
// game server of its own
import { spawnSync } from "node:child_process";
import { rmSync } from "node:fs";
import { availableParallelism } from "node:os";
import { buildServer } from "./server.ts";
import { buildUi } from "./ui.ts";

type Suite = { driver: "api" | "browser"; files: string[]; ui: boolean };

const suites: Record<string, Suite> = {
  // the domain scenarios through the HTTP API, and the worlds scripted rulers play
  api: { driver: "api", files: ["scenarios/*.test.ts", "worlds/*.test.ts"], ui: false },
  // the same domain scenarios through Chrome, and the checks only a browser can make
  browser: { driver: "browser", files: ["scenarios/*.test.ts", "ui/*.test.ts"], ui: true },
  // months of rounds played out, on demand
  long: { driver: "api", files: ["long/*.test.ts"], ui: false },
};

const [name, ...options] = process.argv.slice(2);
const suite = suites[name ?? ""];
if (!suite) {
  console.error(`Name a suite: ${Object.keys(suites).join(", ")}`);
  process.exit(2);
}

const jar = buildServer();
if (suite.ui) {
  buildUi();
  // screenshots of failed scenarios from an earlier run would only mislead
  rmSync(new URL("../test-results", import.meta.url), { recursive: true, force: true });
}

const run = spawnSync(
  process.execPath,
  ["--test", `--test-concurrency=${Math.max(1, Math.min(4, Math.floor(availableParallelism() / 2)))}`, ...options, ...suite.files],
  { stdio: "inherit", env: { ...process.env, KOD_DRIVER: suite.driver, KOD_SERVER_JAR: jar } },
);
process.exit(run.status ?? 1);
