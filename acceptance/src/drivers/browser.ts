import { mkdirSync } from "node:fs";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import { chromium, type Browser, type BrowserContext, type Locator, type Page } from "playwright-core";
import type { BuildingName, MarketResource, UnitName } from "../api/types.ts";
import { whole, type Buildings, type Credentials, type GateDriver, type Outcome, type RulerDriver, type Units } from "../dsl/driver.ts";
import type { Visitors } from "../dsl/game.ts";
import { startUi, type Ui } from "../ui.ts";

// The game as a player sees it: Chrome on the built pages. Actions are taken by filling in and clicking what a player
// would, and their outcome is what the game answered to that click; what the ruler sees is read off the pages.

const evidence = join(resolve(dirname(fileURLToPath(import.meta.url)), "..", ".."), "test-results");

/** the names the pages give buildings and units, as players read them */
const buildingPlural: Record<BuildingName, string> = {
  house: "Houses",
  goldMine: "Gold mines",
  ironMine: "Iron mines",
  workshop: "Workshops",
  farm: "Farms",
  market: "Markets",
  barracks: "Barracks",
  guardHouse: "Guard houses",
  spyGuild: "Spy guilds",
  tower: "Towers",
  castle: "Castles",
};

const unitPlural: Record<UnitName, string> = {
  goldMiner: "Gold miners",
  ironMiner: "Iron miners",
  farmer: "Farmers",
  blacksmith: "Blacksmiths",
  builder: "Builders",
  carrier: "Carriers",
  guard: "Guards",
  spy: "Spies",
  infantry: "Infantry",
  bowman: "Bowmen",
  cavalry: "Cavalry",
};

const resourceLabel: Record<MarketResource, string> = { food: "Food", iron: "Iron", tools: "Tools", weapons: "Weapons" };

const entries = <K extends string>(counts: Partial<Record<K, number>>): [K, number][] =>
  (Object.entries(counts) as [K, number][]).filter(([, count]) => count > 0);

const number = (text: string): number => Number(text.replace(/[^\d]/g, ""));

/** the soldiers in a list such as "10 infantry, 5 cavalry" */
const soldiersIn = (text: string): number => [...text.matchAll(/([\d,]+) [a-z]/g)].reduce((sum, match) => sum + number(match[1]), 0);

class BrowserRuler implements RulerDriver {
  private readonly page: Page;
  private readonly ui: string;

  public constructor(page: Page, ui: string) {
    this.page = page;
    this.ui = ui;
  }

  /** opens a page of the kingdom afresh, once the kingdom is on screen */
  private async open(path: string): Promise<void> {
    await this.page.goto(`${this.ui}/kingdom/${path}`);
    await this.page.locator(".rail-name").waitFor();
    await this.page.locator("main h1").waitFor();
  }

  /** clicks and answers with what the game said to it */
  private async answer(endpoint: RegExp, click: () => Promise<void>): Promise<Outcome> {
    const body = await this.reply(endpoint, click);
    return { done: body.success === true, message: body.message ?? "" };
  }

  private async reply(endpoint: RegExp, click: () => Promise<void>): Promise<{ success?: boolean; message?: string; data?: unknown }> {
    const response = this.page.waitForResponse(candidate => endpoint.test(new URL(candidate.url()).pathname) && candidate.request().method() === "POST");
    await click();
    const body = (await (await response).json()) as { success?: boolean; message?: string; data?: unknown };
    // the page reloads the kingdom after every action; reading on before it has would read the old one
    await this.page.waitForLoadState("networkidle");
    return body;
  }

  /** a panel by its title; the heading's name goes on with the help mark beside it */
  private panel(title: string): Locator {
    const escaped = title.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
    return this.page.locator(".panel").filter({ has: this.page.getByRole("heading", { name: new RegExp(`^${escaped}`) }) });
  }

  private async lines(items: Locator): Promise<string[]> {
    return (await items.allInnerTexts()).map(text => text.replace(/\s+/g, " ").trim());
  }

  public async kingdomName(): Promise<string> {
    await this.page.locator(".rail-name").waitFor();
    return (await this.page.locator(".rail-name").innerText()).trim();
  }

  public async signOut(): Promise<void> {
    this.page.once("dialog", dialog => void dialog.accept());
    await this.page.getByRole("button", { name: "Sign out" }).click();
    await this.page.waitForURL(url => !url.pathname.startsWith("/kingdom"));
  }

  public async keepKingdom(credentials: Credentials): Promise<Outcome> {
    await this.open("overview");
    await this.page.locator(".trial-notice").getByRole("link", { name: "Keep it" }).click();
    await this.page.getByLabel("Email").fill(credentials.email);
    await this.page.getByLabel(/^Password/).fill(credentials.password);
    return this.answer(/^\/api\/auth\/keep$/, () => this.page.getByRole("button", { name: "Keep it" }).click());
  }

  public async isTrial(): Promise<boolean> {
    await this.open("overview");
    return (await this.page.locator(".trial-notice").count()) > 0;
  }

  public async passTurn(): Promise<Outcome> {
    return this.answer(/^\/api\/kingdom\/pass-turn$/, () => this.page.getByRole("button", { name: /^Pass turn/ }).click());
  }

  public async build(buildings: Buildings): Promise<Outcome> {
    await this.open("build");
    const order = entries<BuildingName>(buildings);
    for (const [building, count] of order) await this.page.getByLabel(`${buildingPlural[building]} to raise`, { exact: true }).fill(String(count));
    const button = order.length === 1 ? `Raise ${buildingPlural[order[0][0]].toLowerCase()}` : "Raise all";
    return whole(buildings, await this.reply(/^\/api\/kingdom\/build$/, () => this.page.getByRole("button", { name: button, exact: true }).click()));
  }

  public async train(units: Units): Promise<Outcome> {
    await this.open("train");
    const order = entries<UnitName>(units);
    for (const [unit, count] of order) await this.page.getByLabel(`${unitPlural[unit]} to train`, { exact: true }).fill(String(count));
    const button = order.length === 1 ? `Train ${unitPlural[order[0][0]].toLowerCase()}` : "Train all";
    return whole(units, await this.reply(/^\/api\/kingdom\/train$/, () => this.page.getByRole("button", { name: button, exact: true }).click()));
  }

  public async buildingCount(building: BuildingName): Promise<number> {
    await this.open("build");
    return number(await this.page.locator(`.plate-count[title="${buildingPlural[building]} you own"]`).innerText());
  }

  public async landPrice(acres: number): Promise<number> {
    await this.open("build");
    await this.page.getByLabel("Acres to buy").fill(String(acres));
    const quote = this.page.getByText(new RegExp(`^${acres} acres? costs? [\\d,]+ gold`));
    await quote.waitFor();
    return number((await quote.innerText()).replace(/^\d+ acres? costs?/, ""));
  }

  public async buyLand(acres: number): Promise<Outcome> {
    await this.open("build");
    await this.page.getByLabel("Acres to buy").fill(String(acres));
    return this.answer(/^\/api\/kingdom\/buy-land$/, () => this.page.getByRole("button", { name: "Buy land" }).click());
  }

  public async soldiersPay(): Promise<number> {
    await this.open("overview");
    const forecast = this.panel("If the turn passed now");
    await forecast.getByText("Gold", { exact: true }).waitFor();
    const pay = /Soldiers' pay\s*-?([\d,]+)/.exec(await forecast.innerText());
    return pay ? number(pay[1]) : 0;
  }

  private async fillArmy(units: Units, label: (unit: UnitName) => string | RegExp): Promise<void> {
    for (const [unit, count] of entries<UnitName>(units)) await this.page.getByLabel(label(unit), { exact: typeof label(unit) === "string" }).fill(String(count));
  }

  public async march(target: string, units: Units): Promise<Outcome> {
    await this.open("war");
    await this.page.getByRole("combobox", { name: /^Target/ }).selectOption(target);
    await this.fillArmy(units, unit => `${unitPlural[unit]} to send`);
    return this.answer(/^\/api\/kingdom\/send-attack$/, () => this.page.getByRole("button", { name: "March" }).click());
  }

  public async joinStrike(leader: string, target: string, units: Units): Promise<Outcome> {
    await this.open("war");
    const strike = this.panel("Allies on the march").locator("li").filter({ hasText: `${leader} marches` }).filter({ hasText: target });
    await strike.getByRole("button", { name: "Join the strike" }).click();
    await this.fillArmy(units, unit => `${unitPlural[unit]} to send`);
    const send = this.panel("Send an army").getByRole("button", { name: "Join the strike" });
    return this.answer(/^\/api\/kingdom\/send-attack$/, () => send.click());
  }

  public async armiesComing(): Promise<{ from: string; soldiers: number }[]> {
    await this.open("war");
    return (await this.lines(this.panel("Armies marching on us").locator("li"))).flatMap(line => {
      const seen = /^(.+?) with about ([\d,]+) soldiers?/.exec(line);
      return seen ? [{ from: seen[1], soldiers: number(seen[2]) }] : [];
    });
  }

  public async armiesOut(): Promise<{ to: string; joining: string | null; atTheBorder: boolean }[]> {
    await this.open("war");
    const rows = this.panel("Armies on the march").locator("tbody tr");
    const armies: { to: string; joining: string | null; atTheBorder: boolean }[] = [];
    for (const row of await rows.all()) {
      const target = (await row.locator("td").first().innerText()).split("\n")[0].trim();
      const text = (await row.innerText()).replace(/\s+/g, " ");
      armies.push({ to: target, joining: /joining (.+?)'s strike/.exec(text)?.[1] ?? null, atTheBorder: text.includes("At the border") });
    }
    return armies;
  }

  public async battles(): Promise<{ attacker: string; defender: string; attackerWon: boolean }[]> {
    await this.open("war");
    const me = await this.kingdomName();
    return (await this.lines(this.page.locator(".battle h3"))).flatMap((title): { attacker: string; defender: string; attackerWon: boolean }[] => {
      const won = /^Victory over (.+)$/.exec(title);
      if (won) return [{ attacker: me, defender: won[1], attackerWon: true }];
      const lost = /^Defeat at (.+)$/.exec(title);
      if (lost) return [{ attacker: me, defender: lost[1], attackerWon: false }];
      const repelled = /^(.+) was repelled$/.exec(title);
      if (repelled) return [{ attacker: repelled[1], defender: me, attackerWon: false }];
      const broke = /^(.+) broke through$/.exec(title);
      return broke ? [{ attacker: broke[1], defender: me, attackerWon: true }] : [];
    });
  }

  public async sendSpies(target: string, spies: number): Promise<Outcome> {
    await this.open("spies");
    await this.page.getByRole("combobox", { name: /^Target/ }).selectOption(target);
    await this.page.getByLabel(/Spies, \d+ at home/).fill(String(spies));
    return this.answer(/^\/api\/war\/send-spies$/, () => this.page.getByRole("button", { name: "Send spies", exact: true }).click());
  }

  public async latestIntel(): Promise<{ about: string; stores: boolean } | null> {
    await this.open("spies");
    const report = this.page.locator(".intel").first();
    if (!(await report.count())) return null;
    const title = (await report.locator("h3").innerText()).trim();
    const about = /^(?:Inside|Caught in) (.+)$/.exec(title)?.[1] ?? title;
    return { about, stores: (await report.getByRole("heading", { name: "Stores" }).count()) > 0 };
  }

  public async sendGuards(ally: string, units: Units): Promise<Outcome> {
    await this.open("war");
    await this.page.getByRole("combobox", { name: "Ally" }).selectOption(ally);
    await this.fillArmy(units, unit => new RegExp(`^${unitPlural[unit]} to send, \\d+ at home`));
    return this.answer(/^\/api\/kingdom\/send-reinforcements$/, () => this.page.getByRole("button", { name: "Send to stand guard" }).click());
  }

  public async guardsAway(): Promise<{ with: string; soldiers: number }[]> {
    await this.open("war");
    return (await this.lines(this.panel("Stand guard with allies").locator("li"))).flatMap(line => {
      const standing = /^(.+) stand guard in (.+?)(?: Call home)?$/.exec(line);
      return standing ? [{ with: standing[2], soldiers: soldiersIn(standing[1]) }] : [];
    });
  }

  public async guardsHosted(): Promise<{ from: string; soldiers: number }[]> {
    await this.open("war");
    return (await this.lines(this.panel("Stand guard with allies").locator("li"))).flatMap(line => {
      const hosted = /^(.+) from (.+) stand guard with us$/.exec(line);
      return hosted ? [{ from: hosted[2], soldiers: soldiersIn(hosted[1]) }] : [];
    });
  }

  public async callGuardsHome(): Promise<Outcome> {
    await this.open("war");
    return this.answer(/^\/api\/kingdom\/recall-reinforcements$/, () => this.page.getByRole("button", { name: "Call home" }).first().click());
  }

  public async buy(resource: MarketResource, count: number): Promise<Outcome> {
    await this.open("market");
    await this.page.getByRole("tab", { name: new RegExp(resourceLabel[resource]) }).click();
    const field = new RegExp(`^Amount of ${resource} to buy`);
    const offer = this.page.locator("main tbody tr").filter({ has: this.page.getByRole("spinbutton", { name: field }) }).first();
    await offer.waitFor();
    await offer.getByRole("spinbutton").fill(String(count));
    return this.answer(/^\/api\/market\/[^/]+\/buy$/, () => offer.getByRole("button", { name: /^Buy/ }).click());
  }

  public async askAlliesForGold(gold: number): Promise<Outcome> {
    await this.open("alliance");
    await this.page.getByLabel("Gold to ask for").fill(String(gold));
    return this.answer(/^\/api\/market\/create$/, () => this.page.getByRole("button", { name: /^Ask allies for/ }).click());
  }

  private allyOffers(): Locator {
    return this.panel("Gold between allies").locator("li").filter({ hasText: / asks for [\d,]+ gold/ });
  }

  public async alliesAsks(): Promise<{ from: string; gold: number }[]> {
    await this.open("alliance");
    return (await this.lines(this.allyOffers())).flatMap(line => {
      const ask = /^(.+?) asks for ([\d,]+) gold/.exec(line);
      return ask ? [{ from: ask[1], gold: number(ask[2]) }] : [];
    });
  }

  public async payAsk(ally: string): Promise<Outcome> {
    await this.open("alliance");
    const ask = this.allyOffers().filter({ hasText: `${ally} asks for` });
    return this.answer(/^\/api\/market\/[^/]+\/buy$/, () => ask.getByRole("button", { name: /^Send/ }).click());
  }

  public async sendCarriers(to: string, resource: MarketResource, count: number): Promise<Outcome> {
    await this.open("carriers");
    await this.page.getByRole("combobox", { name: /^To/ }).selectOption(to);
    await this.page.getByRole("combobox", { name: /^Goods/ }).selectOption(resource);
    await this.page.getByLabel("Amount", { exact: true }).fill(String(count));
    return this.answer(/^\/api\/kingdom\/send-carriers$/, () => this.page.getByRole("button", { name: /^Send \d+ carriers?$/ }).click());
  }

  public async carriersOut(): Promise<{ to: string; resource: MarketResource; count: number }[]> {
    await this.open("carriers");
    const loads: { to: string; resource: MarketResource; count: number }[] = [];
    for (const row of await this.panel("On the road").locator("tbody tr").all()) {
      const cells = await row.locator("td").allInnerTexts();
      const [amount, label] = cells[1].trim().split(" ");
      const resource = (Object.keys(resourceLabel) as MarketResource[]).find(name => resourceLabel[name].toLowerCase() === label);
      if (resource) loads.push({ to: cells[0].trim(), resource, count: number(amount) });
    }
    return loads;
  }

  public async writeLetter(to: string, body: string): Promise<Outcome> {
    await this.open("letters");
    await this.page.getByRole("combobox", { name: /^To/ }).selectOption(to);
    await this.page.getByLabel(/Letter, \d+ of \d+ characters/).fill(body);
    return this.answer(/^\/api\/messages\/send$/, () => this.page.getByRole("button", { name: "Seal and send" }).click());
  }

  public async unreadLetters(): Promise<number> {
    await this.open("overview");
    const badge = this.page.getByRole("navigation", { name: "Kingdom" }).getByRole("link", { name: /Letters/ }).locator(".badge");
    return (await badge.count()) ? number(await badge.innerText()) : 0;
  }

  private async letterList(box: "Inbox" | "Sent"): Promise<{ who: string; body: string }[]> {
    await this.open("letters");
    if (box === "Sent") await this.page.getByRole("tab", { name: "Sent" }).click();
    await this.page.locator(".letters, p.empty").first().waitFor();
    const letters: { who: string; body: string }[] = [];
    for (const letter of await this.page.locator(".letters li.letter").all()) {
      letters.push({ who: (await letter.locator(".letter-who strong").innerText()).trim(), body: (await letter.locator(".letter-preview").innerText()).trim() });
    }
    return letters;
  }

  public async letters(): Promise<{ from: string; body: string }[]> {
    return (await this.letterList("Inbox")).map(letter => ({ from: letter.who, body: letter.body }));
  }

  public async sentLetters(): Promise<{ to: string; body: string }[]> {
    return (await this.letterList("Sent")).map(letter => ({ to: letter.who.replace(/^To /, ""), body: letter.body }));
  }

  public async foundAlliance(name: string): Promise<Outcome> {
    await this.open("alliance");
    await this.page.getByLabel("Alliance name").fill(name);
    return this.answer(/^\/api\/alliance\/create$/, () => this.page.getByRole("button", { name: "Found it" }).click());
  }

  public async invite(kingdomName: string): Promise<Outcome> {
    await this.open("alliance");
    await this.page.getByRole("combobox", { name: /^Kingdom/ }).selectOption(kingdomName);
    return this.answer(/^\/api\/alliance\/invite$/, () => this.page.getByRole("button", { name: "Send invitation" }).click());
  }

  public async join(allianceName: string): Promise<Outcome> {
    await this.open("alliance");
    const invitation = this.panel("Invitations").locator("li").filter({ hasText: allianceName });
    return this.answer(/^\/api\/alliance\/accept$/, () => invitation.getByRole("button", { name: "Join" }).click());
  }

  public async alliance(): Promise<{ name: string; members: string[] } | null> {
    await this.open("alliance");
    const name = (await this.page.locator(".rail-alliance").innerText()).trim();
    if (name === "No alliance") return null;
    const members = this.page.locator("main table").first().locator("tbody tr td:first-child .figure");
    await members.first().waitFor();
    return { name, members: (await this.lines(members)).map(member => member.replace(/ emperor$/, "")) };
  }

  public async warRoomMarches(): Promise<{ from: string; to: string }[]> {
    await this.open("alliance");
    return (await this.lines(this.panel("War room").locator("li"))).flatMap(line => {
      const march = /^(.+?) → (.+?), [\d,]+ soldiers?/.exec(line);
      return march && !line.includes("about") ? [{ from: march[1], to: march[2] }] : [];
    });
  }

  public async chronicle(): Promise<string[]> {
    await this.open("chronicle");
    return this.lines(this.page.locator("ol.chronicle > li"));
  }
}

class BrowserGate implements GateDriver {
  private readonly page: Page;
  private readonly ui: string;

  public constructor(page: Page, ui: string) {
    this.page = page;
    this.ui = ui;
  }

  /** submits a form and waits to land in the kingdom or to be told what is wrong */
  private async enter(submit: () => Promise<void>): Promise<Outcome> {
    await submit();
    const inside = this.page.waitForURL(/\/kingdom\//).then(() => ({ done: true, message: "" }));
    const refused = this.page
      .locator("p.warning")
      .first()
      .waitFor()
      .then(async () => ({ done: false, message: await this.page.locator("p.warning").first().innerText() }));
    return Promise.race([inside, refused]);
  }

  public async foundKingdom(kingdomName: string, credentials: Credentials): Promise<Outcome> {
    await this.page.goto(`${this.ui}/register`);
    await this.page.getByLabel("Kingdom name").fill(kingdomName);
    await this.page.getByLabel("Email").fill(credentials.email);
    await this.page.getByLabel("Password, at least 8 characters").fill(credentials.password);
    await this.page.getByLabel("Password again").fill(credentials.password);
    return this.enter(() => this.page.getByRole("button", { name: "Found the kingdom" }).click());
  }

  public async signIn(credentials: Credentials): Promise<Outcome> {
    await this.page.goto(`${this.ui}/sign-in`);
    await this.page.getByLabel("Email").fill(credentials.email);
    await this.page.getByLabel("Password").fill(credentials.password);
    return this.enter(() => this.page.getByRole("button", { name: "Enter" }).click());
  }

  public async playWithOnlyAName(kingdomName: string): Promise<Outcome> {
    await this.page.goto(`${this.ui}/`);
    await this.page.getByLabel("Play now with only a kingdom name").fill(kingdomName);
    return this.enter(() => this.page.getByRole("button", { name: "Play now" }).click());
  }

  public ruler(): RulerDriver {
    return new BrowserRuler(this.page, this.ui);
  }

  public async close(): Promise<void> {}
}

export type Chrome = { visitors: () => Visitors; stop: () => Promise<void> };

/** the installed Chrome on the built pages, which talk to the given game server */
export const startChrome = async (gameServer: string): Promise<Chrome> => {
  const ui: Ui = await startUi(gameServer);
  const browser: Browser = await chromium.launch({ channel: "chrome" });
  return {
    visitors: () => {
      const contexts: BrowserContext[] = [];
      return {
        arrive: async () => {
          const context = await browser.newContext({ viewport: { width: 1440, height: 900 } });
          context.setDefaultTimeout(10_000);
          contexts.push(context);
          return new BrowserGate(await context.newPage(), ui.url);
        },
        keepEvidence: async scenario => {
          mkdirSync(evidence, { recursive: true });
          const name = scenario.replace(/[^a-z0-9]+/gi, "-").toLowerCase();
          const pages = contexts.flatMap(context => context.pages());
          for (const [index, page] of pages.entries()) await page.screenshot({ path: join(evidence, `${name}-${index + 1}.png`), fullPage: true });
        },
        close: async () => {
          for (const context of contexts) await context.close();
        },
      };
    },
    stop: async () => {
      await browser.close();
      await ui.stop();
    },
  };
};
