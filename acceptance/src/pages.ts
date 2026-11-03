import { mkdirSync } from "node:fs";
import { dirname, join, resolve } from "node:path";
import { after, before, test } from "node:test";
import { fileURLToPath } from "node:url";
import { chromium, type Browser, type BrowserContext, type Locator, type Page } from "playwright-core";
import { World } from "./api/client.ts";
import { apiVisitors } from "./drivers/api.ts";
import { Game } from "./dsl/game.ts";
import type { Ruler } from "./dsl/ruler.ts";
import { startServer, type GameServer } from "./server.ts";
import { startUi, type Ui } from "./ui.ts";

// Checks only a browser can make: how the pages look and help. The world is set up through the API in the game's own
// words, then looked at in Chrome.

const evidence = join(resolve(dirname(fileURLToPath(import.meta.url)), ".."), "test-results");

let server: GameServer | undefined;
let ui: Ui | undefined;
let browser: Browser | undefined;

before(async () => {
  server = await startServer();
  ui = await startUi(server.url);
  browser = await chromium.launch({ channel: "chrome" });
});

after(async () => {
  await browser?.close();
  await ui?.stop();
  await server?.stop();
});

const timeout = 10_000;

/** waits until the element shows the text */
export const shows = async (locator: Locator, text: string | RegExp): Promise<void> => {
  const deadline = Date.now() + timeout;
  for (;;) {
    const seen = (await locator.count()) ? await locator.first().innerText() : "";
    if (typeof text === "string" ? seen.includes(text) : text.test(seen)) return;
    if (Date.now() > deadline) throw new Error(`${locator} shows "${seen.slice(0, 300)}", not ${text}`);
    await new Promise(wait => setTimeout(wait, 200));
  }
};

/** waits until nothing matches */
export const gone = async (locator: Locator): Promise<void> => {
  const deadline = Date.now() + timeout;
  while ((await locator.count()) > 0) {
    if (Date.now() > deadline) throw new Error(`${locator} is still there`);
    await new Promise(wait => setTimeout(wait, 200));
  }
};

export const holds = async (locator: Locator, value: string | RegExp): Promise<void> => {
  const deadline = Date.now() + timeout;
  for (;;) {
    const seen = await locator.inputValue();
    if (typeof value === "string" ? seen === value : value.test(seen)) return;
    if (Date.now() > deadline) throw new Error(`${locator} holds "${seen}", not ${value}`);
    await new Promise(wait => setTimeout(wait, 200));
  }
};

export type Looking = {
  game: Game;
  ui: string;
  /** a fresh browser window, signed in as the ruler when one is given */
  open: (ruler?: Ruler, viewport?: { width: number; height: number }) => Promise<Page>;
};

/** a browser check on this file's world */
export const pageCheck = (name: string, look: (looking: Looking) => Promise<void>): void => {
  test(name, { timeout: 120_000 }, async () => {
    if (!server || !ui || !browser) throw new Error("The world or the browser never started");
    const contexts: BrowserContext[] = [];
    const home = ui.url;
    const chrome = browser;
    const open = async (ruler?: Ruler, viewport = { width: 1440, height: 900 }): Promise<Page> => {
      const context = await chrome.newContext({ viewport });
      context.setDefaultTimeout(timeout);
      contexts.push(context);
      const page = await context.newPage();
      if (ruler) {
        await page.goto(`${home}/sign-in`);
        await page.getByLabel("Email").fill(ruler.credentials.email);
        await page.getByLabel("Password").fill(ruler.credentials.password);
        await page.getByRole("button", { name: "Enter" }).click();
        await page.waitForURL(/\/kingdom\/overview/);
      }
      return page;
    };
    const visitors = apiVisitors(server.url);
    try {
      await look({ game: new Game(visitors, new World(server.url)), ui: home, open });
    } catch (error) {
      mkdirSync(evidence, { recursive: true });
      const file = name.replace(/[^a-z0-9]+/gi, "-").toLowerCase();
      for (const [index, page] of contexts.flatMap(context => context.pages()).entries()) {
        await page.screenshot({ path: join(evidence, `${file}-${index + 1}.png`), fullPage: true });
      }
      throw error;
    } finally {
      for (const context of contexts) await context.close();
    }
  });
};

/** follows a link in the kingdom's navigation */
export const goTo = async (page: Page, link: string): Promise<void> => {
  await page.getByRole("navigation", { name: "Kingdom" }).getByRole("link", { name: link, exact: false }).first().click();
};

export const toast = (page: Page, text: string | RegExp): Locator => page.locator(".toast").filter({ hasText: text });
