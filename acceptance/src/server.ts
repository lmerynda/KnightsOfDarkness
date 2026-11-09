import { spawn, spawnSync, type ChildProcess } from "node:child_process";
import { appendFileSync, copyFileSync, existsSync, mkdtempSync, readFileSync, rmSync, writeFileSync } from "node:fs";
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

/** for a world about war, which cannot wait out the peace every kingdom starts with */
export const noPeace: ConfigOverrides = { "attack.protectionTicksAtStart": 0 };

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

/** brings the server jar up to date with the backend sources; Gradle skips the work when nothing changed */
export const buildServer = (): string => {
  const gradle = process.platform === "win32" ? ".\\gradlew.bat" : "./gradlew";
  const build = spawnSync(`${gradle} bootJar --console=plain -q`, { cwd: backend, stdio: "inherit", shell: true });
  if (build.status !== 0 || !existsSync(defaultJar)) {
    throw new Error(`Could not build the server jar at ${defaultJar}`);
  }
  return defaultJar;
};

let jarBuilt = false;

/** the jar KOD_SERVER_JAR names, which the test runner builds once for every test file, or one built here */
const serverJar = (): string => {
  const prebuilt = process.env.KOD_SERVER_JAR;
  if (prebuilt) {
    if (!existsSync(prebuilt)) {
      throw new Error(`KOD_SERVER_JAR points at a missing file: ${prebuilt}`);
    }
    return prebuilt;
  }
  if (!jarBuilt) {
    buildServer();
    jarBuilt = true;
  }
  return defaultJar;
};

/** the server picks a free port itself and says which in its log, so worlds started side by side never collide */
const listeningOn = (output: string): string | null => {
  const port = /Tomcat started on port (\d+)/.exec(output)?.[1];
  return port ? `http://localhost:${port}` : null;
};

const waitUntilReady = async (process: ChildProcess, output: () => string): Promise<string> => {
  const deadline = Date.now() + 120_000;
  while (Date.now() < deadline) {
    if (process.exitCode !== null) {
      throw new Error(`The game server exited with ${process.exitCode}:\n${output()}`);
    }
    const url = listeningOn(output());
    try {
      if (url && (await fetch(`${url}/health`)).ok) return url;
    } catch {
      // not answering yet
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
  const configDirectory = mkdtempSync(join(tmpdir(), "kod-sim-"));
  // each world runs its own copy of the jar, so a rebuild for another run cannot pull it from under this one
  const jar = join(configDirectory, "server.jar");
  copyFileSync(serverJar(), jar);
  const configPath = join(configDirectory, "gameconfig.json");
  writeFileSync(configPath, JSON.stringify(applyOverrides(readBaseConfig(), overrides), null, 2));

  // the Java that JAVA_HOME names, as Gradle uses it, or else the one on the path
  const java = process.env.JAVA_HOME ? join(process.env.JAVA_HOME, "bin", "java") : "java";
  // KOD_JAVA_OPTS passes options to the JVM, for example a flight recording to profile a world
  const javaOptions = process.env.KOD_JAVA_OPTS?.split(" ").filter(Boolean) ?? [];
  const child = spawn(java, [...javaOptions, "-jar", jar, "--spring.profiles.active=simulation", "--server.port=0", "--logging.level.org.springframework.boot.tomcat=INFO", `--game.config-path=${configPath}`], {
    stdio: ["ignore", "pipe", "pipe"],
  });
  let log = "";
  // KOD_SERVER_LOG names a file that gets the whole server log, for reading what went wrong inside a long world
  const logFile = process.env.KOD_SERVER_LOG;
  const keep = (chunk: Buffer): void => {
    log = (log + chunk.toString()).slice(-20_000);
    if (logFile) appendFileSync(logFile, chunk);
  };
  child.stdout?.on("data", keep);
  child.stderr?.on("data", keep);
  // the world goes when its test file does: node runs no after hook for a file whose tests a name filter skipped, and
  // a server still running would keep the file from ever finishing
  child.unref();
  for (const stream of [child.stdout, child.stderr]) (stream as unknown as { unref?: () => void } | null)?.unref?.();
  process.once("exit", () => child.kill());

  const url = await waitUntilReady(child, () => log);

  const cleanUp = (): void => rmSync(configDirectory, { recursive: true, force: true, maxRetries: 5, retryDelay: 200 });
  return {
    url,
    stop: () =>
      new Promise(done => {
        if (child.exitCode !== null) {
          cleanUp();
          done();
          return;
        }
        child.once("exit", () => {
          cleanUp();
          done();
        });
        // a stop the test waits for keeps the file running until the server is gone and its copy removed
        child.ref();
        child.kill();
      }),
  };
};
