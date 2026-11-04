import { after, test } from "node:test";
import { World } from "../api/client.ts";
import { apiVisitors } from "../drivers/api.ts";
import { startChrome, type Chrome } from "../drivers/browser.ts";
import { startServer, type GameServer } from "../server.ts";
import { Game, type Visitors } from "./game.ts";

// Every scenario file plays on a world of its own, reached through the driver the suite names (src/run.ts): the HTTP
// API, or Chrome through the real pages.
const driver = process.env.KOD_DRIVER ?? "api";

type Stage = { server: GameServer; chrome: Chrome | null };

let stage: Promise<Stage> | undefined;

/**
 * the file's world, started by its first scenario: node runs no after hook for a file whose scenarios a name filter
 * skipped, and a world started for nothing would keep that file running forever
 */
const world = (): Promise<Stage> =>
  (stage ??= (async () => {
    const server = await startServer();
    return { server, chrome: driver === "browser" ? await startChrome(server.url) : null };
  })());

after(async () => {
  if (!stage) return;
  const { server, chrome } = await stage;
  await chrome?.stop();
  await server.stop();
});

const visitors = ({ server, chrome }: Stage): Visitors => {
  if (driver === "api") return apiVisitors(server.url);
  if (driver === "browser" && chrome) return chrome.visitors();
  throw new Error(`No driver called ${driver}`);
};

/** a scenario in the game's own words, played on this file's world */
export const scenario = (name: string, play: (game: Game) => Promise<void>): void => {
  test(name, { timeout: 180_000 }, async () => {
    const started = await world();
    const arrivals = visitors(started);
    try {
      await play(new Game(arrivals, new World(started.server.url)));
    } catch (error) {
      await arrivals.keepEvidence(name);
      throw error;
    } finally {
      await arrivals.close();
    }
  });
};
