import { spawn, spawnSync, type ChildProcess } from "node:child_process";
import { existsSync, mkdtempSync, readFileSync, writeFileSync } from "node:fs";
import { createServer } from "node:net";
import { tmpdir } from "node:os";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const repository = resolve(dirname(fileURLToPath(import.meta.url)), "..", "..");
const backend = join(repository, "backend");
export const defaultJar = join(backend, "build", "libs", "kod-game-server.jar");
export const baseConfigPath = join(backend, "src", "main", "resources", "gameconfig.json");

/** dotted paths into gameconfig.json, for example "attack.maxLandCaptureRatio": 0.2 */
export type ConfigOverrides = Record<string, unknown>;

export type GameServer = { url: string; stop: () => Promise<void> };

export const readBaseConfig = (): Record<string, unknown> => JSON.parse(readFileSync(baseConfigPath, "utf8")) as Record<string, unknown>;

export const applyOverrides = (config: Record<string, unknown>, overrides: ConfigOverrides): Record<string, unknown> => {
  const result = structuredClone(config);
  for (const [path, value] of Object.entries(overrides)) {
    const keys = path.split(".");
    let node = result;
    for (const key of keys.slice(0, -1)) {
      if (typeof node[key] !== "object" || node[key] === null) {
        throw new Error(`Unknown config path ${path}`);
      }
      node = node[key] as Record<string, unknown>;
    }
    const last = keys[keys.length - 1];
    if (!(last in node)) {
      throw new Error(`Unknown config path ${path}`);
    }
    node[last] = value;
  }
  return result;
};

const freePort = (): Promise<number> =>
  new Promise((resolvePort, reject) => {
    const probe = createServer();
    probe.once("error", reject);
    probe.listen(0, () => {
      const address = probe.address();
      probe.close(() => (typeof address === "object" && address ? resolvePort(address.port) : reject(new Error("No port"))));
    });
  });

let jarBuilt = false;

/**
 * brings the server jar up to date with the backend sources once per process (Gradle skips the work when nothing
 * changed), KOD_SERVER_JAR points at a prebuilt one instead
 */
const serverJar = (): string => {
  const prebuilt = process.env.KOD_SERVER_JAR;
  if (prebuilt) {
    if (!existsSync(prebuilt)) {
      throw new Error(`KOD_SERVER_JAR points at a missing file: ${prebuilt}`);
    }
    return prebuilt;
  }
  if (!jarBuilt) {
    const gradle = process.platform === "win32" ? ".\\gradlew.bat" : "./gradlew";
    const build = spawnSync(gradle, ["bootJar", "--console=plain", "-q"], { cwd: backend, stdio: "inherit", shell: process.platform === "win32" });
    if (build.status !== 0 || !existsSync(defaultJar)) {
      throw new Error(`Could not build the server jar at ${defaultJar}`);
    }
    jarBuilt = true;
  }
  return defaultJar;
};

const waitUntilReady = async (url: string, process: ChildProcess, output: () => string): Promise<void> => {
  const deadline = Date.now() + 120_000;
  while (Date.now() < deadline) {
    if (process.exitCode !== null) {
      throw new Error(`The game server exited with ${process.exitCode}:\n${output()}`);
    }
    try {
      const response = await fetch(`${url}/health`);
      if (response.ok) return;
    } catch {
      // not listening yet
    }
    await new Promise(wait => setTimeout(wait, 250));
  }
  throw new Error(`The game server did not become ready:\n${output()}`);
};

/**
 * starts a fresh world: the shipped server jar in the simulation profile, an in-memory database, and the base
 * gameconfig.json with the given overrides
 */
export const startServer = async (overrides: ConfigOverrides = {}): Promise<GameServer> => {
  const jar = serverJar();
  const port = await freePort();
  const configDirectory = mkdtempSync(join(tmpdir(), "kod-sim-"));
  const configPath = join(configDirectory, "gameconfig.json");
  writeFileSync(configPath, JSON.stringify(applyOverrides(readBaseConfig(), overrides), null, 2));

  const child = spawn("java", ["-jar", jar, "--spring.profiles.active=simulation", `--server.port=${port}`, `--game.config-path=${configPath}`], {
    stdio: ["ignore", "pipe", "pipe"],
  });
  let log = "";
  const keep = (chunk: Buffer): void => {
    log = (log + chunk.toString()).slice(-20_000);
  };
  child.stdout?.on("data", keep);
  child.stderr?.on("data", keep);

  const url = `http://localhost:${port}`;
  await waitUntilReady(url, child, () => log);

  return {
    url,
    stop: () =>
      new Promise(done => {
        if (child.exitCode !== null) {
          done();
          return;
        }
        child.once("exit", () => done());
        child.kill();
      }),
  };
};
