// A throwaway PostgreSQL for a test run, on the image production and the server tests use, so that what a world does at
// the same moment is locked as production locks it; H2 locks differently, and a race that passes there proves nothing.
// The run starts one container, each world gets a database of its own in it, and the container goes with the run.
import { spawnSync } from "node:child_process";
import { randomUUID } from "node:crypto";
import { readFileSync } from "node:fs";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const composeFile = join(resolve(dirname(fileURLToPath(import.meta.url)), "..", ".."), "backend", "compose.yaml");
const user = "postgres";
const password = "world";

/** the run's PostgreSQL: the container that holds it and the port it answers on */
export type Postgres = { container: string; port: number };

/** the image backend/compose.yaml pins for the local world, which the server tests run on too */
const image = (): string => {
  const pinned = /^\s*image:\s*(postgres:\S+)\s*$/m.exec(readFileSync(composeFile, "utf8"))?.[1];
  if (!pinned) throw new Error(`No postgres image in ${composeFile}`);
  return pinned;
};

const docker = (...args: string[]): string => {
  const run = spawnSync("docker", args, { encoding: "utf8" });
  if (run.status !== 0) throw new Error(`docker ${args.join(" ")} failed: ${run.stderr || run.error?.message}`);
  return run.stdout.trim();
};

/** starts the run's container, its data in memory, and waits until it takes connections over TCP */
export const startPostgres = async (): Promise<Postgres> => {
  const container = `kod-acceptance-${randomUUID().slice(0, 8)}`;
  docker("run", "--detach", "--rm", "--name", container, "--publish", "127.0.0.1::5432", "--tmpfs", "/var/lib/postgresql",
    "--env", `POSTGRES_PASSWORD=${password}`, image());
  try {
    const port = Number(/:(\d+)\s*$/m.exec(docker("port", container, "5432/tcp"))?.[1]);
    // the image first runs a server on a socket alone to set itself up, so only an answer over TCP means ready
    const deadline = Date.now() + 60_000;
    while (spawnSync("docker", ["exec", container, "pg_isready", "--host=127.0.0.1", `--username=${user}`]).status !== 0) {
      if (Date.now() > deadline) throw new Error(`PostgreSQL in ${container} did not become ready`);
      await new Promise(wait => setTimeout(wait, 250));
    }
    return { container, port };
  } catch (error) {
    stopPostgres({ container, port: 0 });
    throw error;
  }
};

export const stopPostgres = ({ container }: Postgres): void => {
  spawnSync("docker", ["rm", "--force", container], { stdio: "ignore" });
};

/** the run's PostgreSQL as an environment for the test files, which startServer reads back */
export const postgresEnvironment = ({ container, port }: Postgres): Record<string, string> => ({ KOD_POSTGRES: `${container}:${port}` });

/** the run's PostgreSQL when the suite runs on one, from the environment postgresEnvironment set */
export const runPostgres = (): Postgres | null => {
  const named = process.env.KOD_POSTGRES;
  if (!named) return null;
  const [container, port] = named.split(":");
  return { container, port: Number(port) };
};

/** a fresh, empty database for one world, and the server options that point the world at it */
export const freshDatabase = ({ container, port }: Postgres): string[] => {
  const database = `world_${randomUUID().replaceAll("-", "")}`;
  docker("exec", container, "psql", `--username=${user}`, "--quiet", "--command", `CREATE DATABASE ${database}`);
  return [
    `--spring.datasource.url=jdbc:postgresql://127.0.0.1:${port}/${database}`,
    `--spring.datasource.username=${user}`,
    `--spring.datasource.password=${password}`,
    // the schema the migrations build, as production's, which Hibernate only checks
    "--spring.flyway.enabled=true",
    "--spring.jpa.hibernate.ddl-auto=validate",
  ];
};
