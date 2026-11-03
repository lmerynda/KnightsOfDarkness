import { after, before, test } from "node:test";
import { World } from "../api/client.ts";
import { apiVisitors } from "../drivers/api.ts";
import { startChrome, type Chrome } from "../drivers/browser.ts";
import { startServer, type GameServer } from "../server.ts";
import { Game, type Visitors } from "./game.ts";

// Every scenario file plays on a world of its own, reached through the driver the suite names (src/run.ts): the HTTP
// API, or Chrome through the real pages.
const driver = process.env.KOD_DRIVER ?? "api";

let server: GameServer | undefined;
let chrome: Chrome | undefined;

before(async () => {
  server = await startServer();
  if (driver === "browser") chrome = await startChrome(worldUrl());
});

after(async () => {
  await chrome?.stop();
  await server?.stop();
});

const worldUrl = (): string => {
  if (!server) throw new Error("The game server never started");
  return server.url;
};

const visitors = (): Visitors => {
  if (driver === "api") return apiVisitors(worldUrl());
  if (driver === "browser" && chrome) return chrome.visitors();
  throw new Error(`No driver called ${driver}`);
};

/** a scenario in the game's own words, played on this file's world */
export const scenario = (name: string, play: (game: Game) => Promise<void>): void => {
  test(name, { timeout: 180_000 }, async () => {
    const arrivals = visitors();
    try {
      await play(new Game(arrivals, new World(worldUrl())));
    } catch (error) {
      await arrivals.keepEvidence(name);
      throw error;
    } finally {
      await arrivals.close();
    }
  });
};
