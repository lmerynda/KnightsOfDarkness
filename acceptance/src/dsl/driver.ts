import type { BuildingName, Counts, MarketResource, SpecialBuildingType, UnitName } from "../api/types.ts";

// The seam between the domain language and the game: a driver reaches the game one way (the HTTP API, or Chrome
// through the real pages) and answers in the game's own terms. The DSL above never knows which one it talks to.

export type Units = Partial<Counts<UnitName>>;
export type Buildings = Partial<Counts<BuildingName>>;

/** an era in the hall of fame: the alliance that won it, if any, its largest kingdom and how it ended */
export type PastEra = { era: number; winner: string | null; largest: string | null; endedBy: string | null };

/** goods a monument's store holds or takes */
export type MonumentGoods = { gold: number; iron: number; tools: number; food: number };

/** what everyone sees of a monument */
export type MonumentView = { alliance: string; seat: string; stages: number; complete: boolean; garrison: number };

/** what an alliance's members see of their own */
export type MonumentLedger = { stages: number; store: MonumentGoods; crumbling: boolean; soldiersOnGuard: number };

/** what an army marches for: to take land, to plunder the stores, or to assault the monument in that kingdom */
export type Goal = "land" | "plunder" | "monument";

/** what a kingdom keeps in its stores, and what its people bring in each turn */
export type Store = "gold" | MarketResource;

export type Battle = { attacker: string; defender: string; attackerWon: boolean; landTaken: number; goldPlundered: number; stagesRazed: number };

/** what the game answered when a ruler tried something: whether it went through and what it said */
export type Outcome = { done: boolean; message: string };

/** the share of the forges that makes weapons, as a new ruler leaves it */
export const defaultWeaponsShare = 20;

/** acres a ruler asks for to spend the whole treasury on land: the game sells as many as the gold pays for */
export const moreLandThanAnyTreasuryBuys = 100_000;

/** an order went through only when all of it did: the game raises or trains what it can and says how many */
export const whole = (ordered: Partial<Record<string, number>>, answer: { success?: boolean; message?: string; data?: unknown }): Outcome => {
  const made = (answer.data ?? {}) as Record<string, number>;
  const short = Object.entries(ordered).filter(([kind, count]) => (made[kind] ?? 0) < (count ?? 0));
  const message = answer.message ?? "";
  if (answer.success !== true) return { done: false, message };
  return short.length ? { done: false, message: `only ${JSON.stringify(made)} of ${JSON.stringify(ordered)}: ${message}` } : { done: true, message };
};

export type Credentials = { email: string; password: string };

/** a visitor before signing in */
export interface GateDriver {
  foundKingdom(kingdomName: string, credentials: Credentials): Promise<Outcome>;
  signIn(credentials: Credentials): Promise<Outcome>;
  /** starts a kingdom with only its name; the game makes up the account */
  playWithOnlyAName(kingdomName: string): Promise<Outcome>;
  /** the ruler this visitor became once signed in or playing */
  ruler(): RulerDriver;
  close(): Promise<void>;
}

/** a signed-in ruler: everything they can do, and what they can see of their kingdom and the world */
export interface RulerDriver {
  kingdomName(): Promise<string>;
  signOut(): Promise<void>;
  /** gives a kingdom started with only its name an address and password of its own */
  keepKingdom(credentials: Credentials): Promise<Outcome>;
  isTrial(): Promise<boolean>;

  passTurn(): Promise<Outcome>;
  /** turns the clock has brought that the ruler has not played yet */
  turnsStored(): Promise<number>;
  build(buildings: Buildings): Promise<Outcome>;
  demolish(buildings: Buildings): Promise<Outcome>;
  train(units: Units): Promise<Outcome>;
  dismiss(units: Units): Promise<Outcome>;
  buildingCount(building: BuildingName): Promise<number>;
  unitsAtHome(unit: UnitName): Promise<number>;
  beginGreatWork(type: SpecialBuildingType): Promise<Outcome>;
  investInGreatWork(type: SpecialBuildingType, buildingPoints: number): Promise<Outcome>;
  tearDownGreatWork(type: SpecialBuildingType): Promise<Outcome>;
  greatWorks(): Promise<{ type: SpecialBuildingType; level: number }[]>;
  landPrice(acres: number): Promise<number>;
  buyLand(acres: number): Promise<Outcome>;
  /** as many acres as the treasury pays for */
  buyAllTheLandWeCanAfford(): Promise<Outcome>;
  land(): Promise<number>;
  /** the gold soldiers will be paid next turn */
  soldiersPay(): Promise<number>;
  /** how much of a good the kingdom holds */
  stores(store: Store): Promise<number>;
  /** how much of a good the kingdom's people will bring in next turn */
  nextYield(store: Store): Promise<number>;

  march(target: string, units: Units, goal: Goal): Promise<Outcome>;
  /** calls back the army on the march to the target */
  recallArmy(target: string): Promise<Outcome>;
  /** sends an army to wait at the target's border and strike right after an ally's army lands */
  joinStrike(leader: string, target: string, units: Units, goal?: Goal): Promise<Outcome>;
  /** armies marching on this kingdom, as its scouts report them */
  armiesComing(): Promise<{ from: string; soldiers: number }[]>;
  /** our own armies out on the march, each with its target, whose strike it joins, and whether it waits at the border */
  armiesOut(): Promise<{ to: string; joining: string | null; atTheBorder: boolean }[]>;
  /** battles fought, newest first */
  battles(): Promise<Battle[]>;
  sendSpies(target: string, spies: number): Promise<Outcome>;
  /** the newest spy report: whose kingdom, whether it saw the stores, and how many spies never came back */
  latestIntel(): Promise<{ about: string; stores: boolean; spiesLost: number } | null>;

  sendGuards(ally: string, units: Units): Promise<Outcome>;
  /** soldiers of ours standing guard with allies */
  guardsAway(): Promise<{ with: string; soldiers: number }[]>;
  /** allies' soldiers standing guard with us */
  guardsHosted(): Promise<{ from: string; soldiers: number }[]>;
  callGuardsHome(): Promise<Outcome>;

  /** buys from the cheapest offer of a good */
  buy(resource: MarketResource, count: number): Promise<Outcome>;
  /** buys from the named seller's offer of a good */
  buyFrom(seller: string, resource: MarketResource, count: number): Promise<Outcome>;
  offer(resource: MarketResource, count: number, price: number): Promise<Outcome>;
  ownOffers(): Promise<{ resource: MarketResource; count: number; price: number }[]>;
  withdrawOffer(resource: MarketResource): Promise<Outcome>;
  /** asks allies for gold: one piece of goods on the market at that price */
  askAlliesForGold(gold: number): Promise<Outcome>;
  /** the gold allies ask for, by ally */
  alliesAsks(): Promise<{ from: string; gold: number }[]>;
  /** pays an ally's ask in full */
  payAsk(ally: string): Promise<Outcome>;

  sendCarriers(to: string, resource: MarketResource, count: number): Promise<Outcome>;
  carriersOut(): Promise<{ to: string; resource: MarketResource; count: number }[]>;
  /** calls back the carriers on the road to the kingdom */
  recallCarriers(to: string): Promise<Outcome>;

  writeLetter(to: string, body: string): Promise<Outcome>;
  writeToAlliance(body: string): Promise<Outcome>;
  unreadLetters(): Promise<number>;
  letters(): Promise<{ from: string; body: string }[]>;
  sentLetters(): Promise<{ to: string; body: string }[]>;

  foundAlliance(name: string): Promise<Outcome>;
  invite(kingdomName: string): Promise<Outcome>;
  /** accepts the invitation to the named alliance */
  join(allianceName: string): Promise<Outcome>;
  /** turns down the invitation to the named alliance */
  decline(allianceName: string): Promise<Outcome>;
  leaveAlliance(): Promise<Outcome>;
  removeMember(kingdomName: string): Promise<Outcome>;
  /** raises a kingdom the game runs, into the alliance */
  raiseVassal(name: string): Promise<Outcome>;
  alliance(): Promise<{ name: string; members: string[] } | null>;
  foundMonument(seat: string): Promise<Outcome>;
  payIntoMonument(goods: Partial<MonumentGoods>): Promise<Outcome>;
  /** sends soldiers to stand guard at the alliance's monument; they come home with callGuardsHome */
  sendToMonument(units: Units): Promise<Outcome>;
  /** the alliance's own monument, or null when it has none */
  monumentLedger(): Promise<MonumentLedger | null>;
  /** every monument in the world */
  monuments(): Promise<MonumentView[]>;
  /** allied armies on the march, as the war room shows them */
  warRoomMarches(): Promise<{ from: string; to: string }[]>;

  /** the chronicle: what happened to the kingdom, newest first */
  chronicle(): Promise<string[]>;
  /** the hall of fame: every finished era, newest first */
  pastEras(): Promise<PastEra[]>;
}
