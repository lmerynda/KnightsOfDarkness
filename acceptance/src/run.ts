// node src/run.ts <suite> [node --test options]: builds what the suite needs once, then runs its test files, each on a
// game server of its own, with a database of its own in memory or in the run's PostgreSQL
import { spawnSync } from "node:child_process";
import { rmSync } from "node:fs";
import { availableParallelism } from "node:os";
import { postgresEnvironment, startPostgres, stopPostgres } from "./postgres.ts";
import { buildServer } from "./server.ts";
import { buildUi } from "./ui.ts";

type Suite = { driver: "api" | "browser"; files: string[]; ui: boolean; database: "memory" | "postgres" };

const suites: Record<string, Suite> = {
  // the domain scenarios through the HTTP API, the worlds scripted rulers play, and races and hostile requests
  api: { driver: "api", files: ["scenarios/*.test.ts", "worlds/*.test.ts", "robustness/*.test.ts"], ui: false, database: "memory" },
  // the same domain scenarios through Chrome, and the checks only a browser can make
  browser: { driver: "browser", files: ["scenarios/*.test.ts", "ui/*.test.ts"], ui: true, database: "memory" },
  // months of eras played out, on demand
  long: { driver: "api", files: ["long/*.test.ts"], ui: false, database: "memory" },
  // the balance targets of backend/ERA.md, each a whole era of rival coalitions, on demand
  balance: { driver: "api", files: ["balance/*.test.ts"], ui: false, database: "memory" },
  // the races and hostile requests again on PostgreSQL, which locks as production does and H2 does not
  "robustness-pg": { driver: "api", files: ["robustness/*.test.ts"], ui: false, database: "postgres" },
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

const postgres = suite.database === "postgres" ? await startPostgres() : null;
const run = spawnSync(
  process.execPath,
  ["--test", `--test-concurrency=${Math.max(1, Math.min(4, Math.floor(availableParallelism() / 2)))}`, ...options, ...suite.files],
  { stdio: "inherit", env: { ...process.env, KOD_DRIVER: suite.driver, KOD_SERVER_JAR: jar, ...(postgres ? postgresEnvironment(postgres) : {}) } },
);
if (postgres) stopPostgres(postgres);
process.exit(run.status ?? 1);
