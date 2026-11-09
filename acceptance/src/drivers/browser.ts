import { mkdirSync } from "node:fs";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import { chromium, type Browser, type BrowserContext, type Locator, type Page } from "playwright-core";
import type { BuildingName, MarketResource, SpecialBuildingType, UnitName } from "../api/types.ts";
import {
  moreLandThanAnyTreasuryBuys,
  whole,
  type Battle,
  type Buildings,
  type Credentials,
  type GateDriver,
  type Goal,
  type MonumentGoods,
  type MonumentLedger,
  type MonumentView,
  type Outcome,
  type PastEra,
  type RulerDriver,
  type Store,
  type Units,
} from "../dsl/driver.ts";
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

const storeLabel: Record<Store, string> = { gold: "Gold", ...resourceLabel };

const greatWorkLabel: Record<SpecialBuildingType, string> = {
  goldShaft: "Gold shaft",
  ironShaft: "Iron shaft",
  granary: "Granary",
  forge: "Forge",
  warehouse: "Warehouse",
};

const goalLabel: Record<Goal, string> = { land: "Take land", plunder: "Plunder stores", monument: "Assault its monument" };

const marketResources = Object.keys(resourceLabel) as MarketResource[];

const entries = <K extends string>(counts: Partial<Record<K, number>>): [K, number][] =>
  (Object.entries(counts) as [K, number][]).filter(([, count]) => count > 0);

const number = (text: string): number => Number(text.replace(/[^\d]/g, ""));

/** whether the page offers a choice but will not let it be picked; Playwright's isDisabled calls every option enabled */
const optionDisabled = (option: Locator): Promise<boolean> => option.evaluate(element => (element as HTMLOptionElement).disabled);

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
    const seal = this.page.getByRole("button", { name: /^(Pass turn|No turns left)/ });
    // the page asks for new turns every half minute; a ruler who sees none looks again before giving up
    if ((await seal.getAttribute("aria-label")) === "No turns left") await this.page.reload();
    if ((await seal.getAttribute("aria-label")) === "No turns left") return { done: false, message: "No turns left" };
    return this.answer(/^\/api\/kingdom\/pass-turn$/, () => seal.click());
  }

  public async turnsStored(): Promise<number> {
    await this.open("overview");
    // the innermost of the boxes that hold the words: its own figure reads "20 / 48"
    const box = this.page.locator(".panel div").filter({ has: this.page.getByText("turns stored", { exact: true }) }).last();
    const stored = box.locator(":scope > strong");
    return number((await stored.innerText()).split("/")[0]);
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

  public async demolish(buildings: Buildings): Promise<Outcome> {
    await this.open("build");
    await this.page.getByRole("group", { name: "Build or tear down" }).getByRole("button", { name: "Tear down" }).click();
    const order = entries<BuildingName>(buildings);
    for (const [building, count] of order) await this.page.getByLabel(`${buildingPlural[building]} to tear down`, { exact: true }).fill(String(count));
    const button = order.length === 1 ? `Tear down ${buildingPlural[order[0][0]].toLowerCase()}` : "Tear all down";
    return whole(buildings, await this.reply(/^\/api\/kingdom\/demolish$/, () => this.page.getByRole("button", { name: button, exact: true }).click()));
  }

  public async dismiss(units: Units): Promise<Outcome> {
    await this.open("train");
    await this.page.getByRole("group", { name: "Train or dismiss" }).getByRole("button", { name: "Dismiss" }).click();
    const order = entries<UnitName>(units);
    for (const [unit, count] of order) await this.page.getByLabel(`${unitPlural[unit]} to dismiss`, { exact: true }).fill(String(count));
    const button = order.length === 1 ? `Dismiss ${unitPlural[order[0][0]].toLowerCase()}` : "Dismiss all";
    return whole(units, await this.reply(/^\/api\/kingdom\/fire$/, () => this.page.getByRole("button", { name: button, exact: true }).click()));
  }

  public async unitsAtHome(unit: UnitName): Promise<number> {
    await this.open("train");
    return number(await this.page.locator(`.plate-count[title="${unitPlural[unit]} at home"]`).innerText());
  }

  private greatWorkRow(type: SpecialBuildingType): Locator {
    return this.panel("Great works").locator("ul.specials > li").filter({ has: this.page.getByRole("heading", { name: greatWorkLabel[type], exact: true }) });
  }

  public async beginGreatWork(type: SpecialBuildingType): Promise<Outcome> {
    await this.open("build");
    const choice = this.panel("Great works").getByRole("combobox", { name: /^Begin a new great work/ });
    const option = choice.locator(`option[value="${type}"]`);
    if (await optionDisabled(option)) return { done: false, message: (await option.innerText()).trim() };
    await choice.selectOption(type);
    return this.answer(/^\/api\/kingdom\/start-special-building$/, () => this.panel("Great works").getByRole("button", { name: "Begin" }).click());
  }

  public async investInGreatWork(type: SpecialBuildingType, buildingPoints: number): Promise<Outcome> {
    await this.open("build");
    const work = this.greatWorkRow(type);
    await work.getByLabel("Points to invest").fill(String(buildingPoints));
    return this.answer(/^\/api\/kingdom\/build-special-building$/, () => work.getByRole("button", { name: "Invest" }).click());
  }

  public async tearDownGreatWork(type: SpecialBuildingType): Promise<Outcome> {
    await this.open("build");
    this.page.once("dialog", dialog => void dialog.accept());
    return this.answer(/^\/api\/kingdom\/demolish-special-building$/, () => this.greatWorkRow(type).getByRole("button", { name: "Tear down" }).click());
  }

  public async greatWorks(): Promise<{ type: SpecialBuildingType; level: number }[]> {
    await this.open("build");
    await this.panel("Great works").waitFor();
    const works: { type: SpecialBuildingType; level: number }[] = [];
    for (const row of await this.panel("Great works").locator("ul.specials > li").all()) {
      const label = (await row.locator("h3").innerText()).trim();
      const type = (Object.keys(greatWorkLabel) as SpecialBuildingType[]).find(candidate => greatWorkLabel[candidate] === label);
      const level = /^Level (\d+)/.exec((await row.locator(".pips").getAttribute("aria-label")) ?? "")?.[1];
      if (type && level) works.push({ type, level: Number(level) });
    }
    return works;
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

  public async buyAllTheLandWeCanAfford(): Promise<Outcome> {
    await this.open("build");
    await this.page.getByLabel("Acres to buy").fill(String(moreLandThanAnyTreasuryBuys));
    // the quote answers with what the gold pays for, and the purchase buys that much
    await this.page.getByText(/^You can afford [\d,]+ acres?/).waitFor();
    return this.answer(/^\/api\/kingdom\/buy-land$/, () => this.page.getByRole("button", { name: "Buy land" }).click());
  }

  public async land(): Promise<number> {
    await this.open("build");
    return number(/([\d,]+) acres, [\d,]+ free/.exec(await this.panel("Land").innerText())?.[1] ?? "");
  }

  public async soldiersPay(): Promise<number> {
    await this.open("overview");
    const forecast = this.panel("If the turn passed now");
    await forecast.getByText("Gold", { exact: true }).waitFor();
    const pay = /Soldiers' pay\s*-?([\d,]+)/.exec(await forecast.innerText());
    return pay ? number(pay[1]) : 0;
  }

  public async stores(store: Store): Promise<number> {
    await this.open("overview");
    const item = this.page.getByRole("button", { name: new RegExp(`^${storeLabel[store]} [\\d,]+$`) }).first();
    return number(((await item.getAttribute("aria-label")) ?? "").replace(storeLabel[store], ""));
  }

  public async nextYield(store: Store): Promise<number> {
    await this.open("overview");
    const forecast = this.panel("If the turn passed now");
    const row = forecast.locator("tr").filter({ has: this.page.locator("td:first-child", { hasText: new RegExp(`^\\s*${storeLabel[store]}\\s*$`) }) });
    return number(await row.locator("td.number").first().innerText());
  }

  private async fillArmy(units: Units, label: (unit: UnitName) => string | RegExp): Promise<void> {
    for (const [unit, count] of entries<UnitName>(units)) await this.page.getByLabel(label(unit), { exact: typeof label(unit) === "string" }).fill(String(count));
  }

  public async march(target: string, units: Units, goal: Goal): Promise<Outcome> {
    await this.open("war");
    // the page lets nobody pick a kingdom that may not be attacked, and says why beside its name
    const option = this.page.getByRole("combobox", { name: /^Target/ }).locator(`option[value="${target}"]`);
    await option.waitFor({ state: "attached" });
    // the rankings, which say who is protected, may come after the list of kingdoms
    await this.page.waitForLoadState("networkidle");
    if (await optionDisabled(option)) return { done: false, message: (await option.innerText()).trim() };
    await this.page.getByRole("combobox", { name: /^Target/ }).selectOption(target);
    await this.page.getByRole("group", { name: "Goal of the attack" }).getByRole("button", { name: goalLabel[goal] }).click();
    await this.fillArmy(units, unit => `${unitPlural[unit]} to send`);
    const march = this.page.getByRole("button", { name: "March" });
    if (await march.isDisabled()) return { done: false, message: (await this.panel("Send an army").innerText()).replace(/\s+/g, " ") };
    return this.answer(/^\/api\/kingdom\/send-attack$/, () => march.click());
  }

  public async recallArmy(target: string): Promise<Outcome> {
    await this.open("war");
    const army = this.panel("Armies on the march").locator("tbody tr").filter({ hasText: target });
    return this.answer(/^\/api\/kingdom\/withdraw-attack$/, () => army.getByRole("button", { name: "Call back" }).click());
  }

  public async joinStrike(leader: string, target: string, units: Units, _goal?: Goal): Promise<Outcome> {
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

  public async battles(): Promise<Battle[]> {
    await this.open("war");
    const me = await this.kingdomName();
    const battles: Battle[] = [];
    // battles in a row stand in one line, with their reports inside
    const closed = this.page.locator("details.battle-run:not([open]) > summary");
    while ((await closed.count()) > 0) await closed.first().click();
    for (const report of await this.page.locator(".battle").all()) {
      const title = (await report.locator("h3").innerText()).trim();
      const facts = (await report.locator(".battle-facts").innerText()).replace(/\s+/g, " ");
      const spoils = {
        landTaken: number(/(?:Took|Lost) ([\d,]+) acres?/.exec(facts)?.[1] ?? "0"),
        goldPlundered: number(/(?:Plundered|Stolen) ([\d,]+) gold/.exec(facts)?.[1] ?? "0"),
        stagesRazed: Number(/(?:Knocked down|Lost) ([\d.]+) stages?/.exec(facts)?.[1] ?? 0),
      };
      const stormed = /^Victory at the monument at (.+)$/.exec(title);
      const thrownBack = /^Defeat at the monument at (.+)$/.exec(title);
      const heldOff = /^(.+) was repelled at our monument$/.exec(title);
      const overran = /^(.+) stormed our monument$/.exec(title);
      if (stormed) battles.push({ attacker: me, defender: stormed[1], attackerWon: true, ...spoils });
      else if (thrownBack) battles.push({ attacker: me, defender: thrownBack[1], attackerWon: false, ...spoils });
      else if (heldOff) battles.push({ attacker: heldOff[1], defender: me, attackerWon: false, ...spoils });
      else if (overran) battles.push({ attacker: overran[1], defender: me, attackerWon: true, ...spoils });
      if (stormed || thrownBack || heldOff || overran) continue;
      const won = /^Victory over (.+)$/.exec(title);
      const lost = /^Defeat at (.+)$/.exec(title);
      const repelled = /^(.+) was repelled$/.exec(title);
      const broke = /^(.+) broke through$/.exec(title);
      if (won) battles.push({ attacker: me, defender: won[1], attackerWon: true, ...spoils });
      else if (lost) battles.push({ attacker: me, defender: lost[1], attackerWon: false, ...spoils });
      else if (repelled) battles.push({ attacker: repelled[1], defender: me, attackerWon: false, ...spoils });
      else if (broke) battles.push({ attacker: broke[1], defender: me, attackerWon: true, ...spoils });
    }
    return battles;
  }

  public async sendSpies(target: string, spies: number): Promise<Outcome> {
    await this.open("spies");
    await this.page.getByRole("combobox", { name: /^Target/ }).selectOption(target);
    await this.page.getByLabel(/Spies, \d+ at home/).fill(String(spies));
    return this.answer(/^\/api\/war\/send-spies$/, () => this.page.getByRole("button", { name: "Send spies", exact: true }).click());
  }

  public async latestIntel(): Promise<{ about: string; stores: boolean; spiesLost: number } | null> {
    await this.open("spies");
    // the kingdom looked at last comes first, and says when the look failed
    const row = this.page.locator(".dossier").first();
    if (!(await row.count())) return null;
    const caught = /([\d,]+) (?:spy|spies) never came back/.exec(await row.innerText());
    return { about: (await row.locator(".dossier-name").innerText()).trim(), stores: !caught, spiesLost: number(caught?.[1] ?? "0") };
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
      const standing = /^(.+) stand guard (?:at the monument at|in) (.+?)(?: Call home)?$/.exec(line);
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

  private async openMarketFor(resource: MarketResource): Promise<void> {
    await this.open("market");
    await this.page.getByRole("tab", { name: new RegExp(resourceLabel[resource]) }).click();
    await this.panel(`${resourceLabel[resource]} for sale`).waitFor();
  }

  public async buyFrom(seller: string, resource: MarketResource, count: number): Promise<Outcome> {
    await this.openMarketFor(resource);
    const offer = this.page.locator("main tbody tr").filter({ hasText: seller }).filter({ has: this.page.getByRole("spinbutton") }).first();
    await offer.getByRole("spinbutton").fill(String(count));
    return this.answer(/^\/api\/market\/[^/]+\/buy$/, () => offer.getByRole("button", { name: /^Buy/ }).click());
  }

  public async offer(resource: MarketResource, count: number, price: number): Promise<Outcome> {
    await this.openMarketFor(resource);
    const sell = this.panel(`Sell ${resourceLabel[resource].toLowerCase()}`);
    await sell.getByLabel(/^Amount, [\d,]+ in store/).fill(String(count));
    await sell.getByLabel(/^Gold each/).fill(String(price));
    return this.answer(/^\/api\/market\/create$/, () => sell.getByRole("button", { name: /^Offer for/ }).click());
  }

  public async ownOffers(): Promise<{ resource: MarketResource; count: number; price: number }[]> {
    const offers: { resource: MarketResource; count: number; price: number }[] = [];
    for (const resource of marketResources) {
      await this.openMarketFor(resource);
      // the page lists the offers once they have come, or says there are none
      await this.page.waitForLoadState("networkidle");
      for (const row of await this.page.locator("main tbody tr").filter({ has: this.page.getByRole("button", { name: "Withdraw" }) }).all()) {
        const cells = await row.locator("td").allInnerTexts();
        offers.push({ resource, count: number(cells[1]), price: number(cells[2]) });
      }
    }
    return offers;
  }

  public async withdrawOffer(resource: MarketResource): Promise<Outcome> {
    await this.openMarketFor(resource);
    const own = this.page.locator("main tbody tr").getByRole("button", { name: "Withdraw" }).first();
    return this.answer(/^\/api\/market\/[^/]+\/withdraw$/, () => own.click());
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

  public async recallCarriers(to: string): Promise<Outcome> {
    await this.open("carriers");
    const load = this.panel("On the road").locator("tbody tr").filter({ hasText: to });
    return this.answer(/^\/api\/kingdom\/withdraw-carriers$/, () => load.getByRole("button", { name: "Call back" }).click());
  }

  public async writeToAlliance(body: string): Promise<Outcome> {
    await this.open("letters");
    await this.page.getByRole("combobox", { name: /^To/ }).selectOption("__alliance__");
    await this.page.getByLabel(/Letter, \d+ of \d+ characters/).fill(body);
    return this.answer(/^\/api\/messages\/send-alliance$/, () => this.page.getByRole("button", { name: "Seal and send" }).click());
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

  public async decline(allianceName: string): Promise<Outcome> {
    await this.open("alliance");
    const invitation = this.panel("Invitations").locator("li").filter({ hasText: allianceName });
    return this.answer(/^\/api\/alliance\/reject$/, () => invitation.getByRole("button", { name: "Decline" }).click());
  }

  public async leaveAlliance(): Promise<Outcome> {
    await this.open("alliance");
    this.page.once("dialog", dialog => void dialog.accept());
    return this.answer(/^\/api\/alliance\/leave$/, () => this.page.getByRole("button", { name: /^Leave/ }).click());
  }

  public async removeMember(kingdomName: string): Promise<Outcome> {
    await this.open("alliance");
    const member = this.page.locator("main table").first().locator("tbody tr").filter({ hasText: kingdomName });
    this.page.once("dialog", dialog => void dialog.accept());
    return this.answer(/^\/api\/alliance\/remove$/, () => member.getByRole("button", { name: "Remove" }).click());
  }

  public async raiseVassal(name: string): Promise<Outcome> {
    await this.open("alliance");
    await this.page.getByLabel("Vassal name").fill(name);
    return this.answer(/^\/api\/alliance\/add-bot$/, () => this.page.getByRole("button", { name: "Raise vassal" }).click());
  }

  public async foundMonument(seat: string): Promise<Outcome> {
    await this.open("alliance");
    await this.panel("Monument").waitFor();
    const found = this.page.getByRole("button", { name: "Found the monument" });
    if ((await found.count()) === 0) {
      const built = (await this.page.getByRole("button", { name: "Pay in" }).count()) > 0;
      return { done: false, message: built ? "already has its monument" : (await this.panel("Monument").innerText()).replace(/\s+/g, " ") };
    }
    await this.page.getByRole("combobox", { name: /^Seat of the monument/ }).selectOption(seat);
    return this.answer(/^\/api\/alliance\/monument\/found$/, () => found.click());
  }

  public async payIntoMonument(goods: Partial<MonumentGoods>): Promise<Outcome> {
    await this.open("alliance");
    await this.panel("Monument").waitFor();
    const pay = this.page.getByRole("button", { name: "Pay in" });
    if ((await pay.count()) === 0) return { done: false, message: (await this.panel("Monument").innerText()).replace(/\s+/g, " ") };
    for (const [good, count] of Object.entries(goods)) await this.page.getByLabel(new RegExp(`^${good} to pay`, "i")).fill(String(count));
    if (await pay.isDisabled()) return { done: false, message: (await this.panel("Monument").locator(".warning").last().innerText()).replace(/\s+/g, " ") };
    return this.answer(/^\/api\/alliance\/monument\/contribute$/, () => pay.click());
  }

  public async sendToMonument(units: Units): Promise<Outcome> {
    await this.open("alliance");
    await this.panel("Monument").waitFor();
    const send = this.page.getByRole("button", { name: "Send to the monument" });
    if ((await send.count()) === 0) return { done: false, message: (await this.panel("Monument").innerText()).replace(/\s+/g, " ") };
    await this.fillArmy(units, unit => new RegExp(`^${unitPlural[unit]} to the monument`));
    return this.answer(/^\/api\/alliance\/monument\/garrison$/, () => send.click());
  }

  public async monumentLedger(): Promise<MonumentLedger | null> {
    await this.open("alliance");
    await this.panel("Monument").waitFor();
    const panel = this.panel("Monument");
    if ((await panel.getByRole("button", { name: "Pay in" }).count()) === 0) return null;
    const stored = async (good: string): Promise<number> => number(await panel.locator("tbody tr").filter({ hasText: good }).locator("td").nth(1).innerText());
    const text = await panel.innerText();
    return {
      stages: Number(/Stage ([\d.]+) of/.exec(text)?.[1] ?? Number(await panel.getByRole("meter").getAttribute("aria-valuenow"))),
      store: { gold: await stored("Gold"), iron: await stored("Iron"), tools: await stored("Tools"), food: await stored("Food") },
      crumbling: text.includes("the monument is crumbling"),
      soldiersOnGuard: (await this.lines(panel.locator(".monument-garrison li"))).reduce((sum, line) => {
        const guarding = /^.+?: (.+), on guard/.exec(line);
        return sum + (guarding ? soldiersIn(guarding[1]) : 0);
      }, 0),
    };
  }

  public async monuments(): Promise<MonumentView[]> {
    await this.open("rankings");
    await this.panel("Monuments").waitFor();
    return (await this.lines(this.panel("Monuments").locator("li"))).map(line => {
      const seen = /^(.+) at (.+?) (Stage ([\d.]+) of \d+|Stands complete), (?:no garrison|about ([\d,]+) soldiers? on guard)(?: March on it)?$/.exec(line);
      if (!seen) throw new Error(`The rankings tell of a monument as "${line}"`);
      return { alliance: seen[1], seat: seen[2], stages: Number(seen[4] ?? 0), complete: seen[3] === "Stands complete", garrison: number(seen[5] ?? "0") };
    });
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

  public async pastEras(): Promise<PastEra[]> {
    await this.open("rankings");
    return (await this.lines(this.panel("Past eras").locator("li"))).map(line => ({
      era: number(/^Era (\d+)/.exec(line)?.[1] ?? ""),
      winner: /^Era \d+: (.+?) won with [\d,]+ land/.exec(line)?.[1] ?? null,
      largest: /, largest kingdom (.+?) with [\d,]+ land/.exec(line)?.[1] ?? null,
      endedBy: line.includes("when the clock ran out") ? "clock" : null,
    }));
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
