import type { BuildingName, Counts, MarketResource, UnitName } from "../api/types.ts";

// The seam between the domain language and the game: a driver reaches the game one way (the HTTP API, or Chrome
// through the real pages) and answers in the game's own terms. The DSL above never knows which one it talks to.

export type Units = Partial<Counts<UnitName>>;
export type Buildings = Partial<Counts<BuildingName>>;

/** what the game answered when a ruler tried something: whether it went through and what it said */
export type Outcome = { done: boolean; message: string };

/** the share of the forges that makes weapons, as a new ruler leaves it */
export const defaultWeaponsShare = 20;

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
  build(buildings: Buildings): Promise<Outcome>;
  train(units: Units): Promise<Outcome>;
  buildingCount(building: BuildingName): Promise<number>;
  landPrice(acres: number): Promise<number>;
  buyLand(acres: number): Promise<Outcome>;
  /** the gold soldiers will be paid next turn */
  soldiersPay(): Promise<number>;

  march(target: string, units: Units): Promise<Outcome>;
  /** sends an army to wait at the target's border and strike right after an ally's army lands */
  joinStrike(leader: string, target: string, units: Units): Promise<Outcome>;
  /** armies marching on this kingdom, as its scouts report them */
  armiesComing(): Promise<{ from: string; soldiers: number }[]>;
  /** our own armies out on the march, each with its target, whose strike it joins, and whether it waits at the border */
  armiesOut(): Promise<{ to: string; joining: string | null; atTheBorder: boolean }[]>;
  /** battles fought, newest first */
  battles(): Promise<{ attacker: string; defender: string; attackerWon: boolean }[]>;
  sendSpies(target: string, spies: number): Promise<Outcome>;
  /** the newest spy report: whose kingdom, and whether it saw the stores */
  latestIntel(): Promise<{ about: string; stores: boolean } | null>;

  sendGuards(ally: string, units: Units): Promise<Outcome>;
  /** soldiers of ours standing guard with allies */
  guardsAway(): Promise<{ with: string; soldiers: number }[]>;
  /** allies' soldiers standing guard with us */
  guardsHosted(): Promise<{ from: string; soldiers: number }[]>;
  callGuardsHome(): Promise<Outcome>;

  /** buys from the cheapest offer of a good */
  buy(resource: MarketResource, count: number): Promise<Outcome>;
  /** asks allies for gold: one piece of goods on the market at that price */
  askAlliesForGold(gold: number): Promise<Outcome>;
  /** the gold allies ask for, by ally */
  alliesAsks(): Promise<{ from: string; gold: number }[]>;
  /** pays an ally's ask in full */
  payAsk(ally: string): Promise<Outcome>;

  sendCarriers(to: string, resource: MarketResource, count: number): Promise<Outcome>;
  carriersOut(): Promise<{ to: string; resource: MarketResource; count: number }[]>;

  writeLetter(to: string, body: string): Promise<Outcome>;
  unreadLetters(): Promise<number>;
  letters(): Promise<{ from: string; body: string }[]>;
  sentLetters(): Promise<{ to: string; body: string }[]>;

  foundAlliance(name: string): Promise<Outcome>;
  invite(kingdomName: string): Promise<Outcome>;
  /** accepts the invitation to the named alliance */
  join(allianceName: string): Promise<Outcome>;
  alliance(): Promise<{ name: string; members: string[] } | null>;
  /** allied armies on the march, as the war room shows them */
  warRoomMarches(): Promise<{ from: string; to: string }[]>;

  /** the chronicle: what happened to the kingdom, newest first */
  chronicle(): Promise<string[]>;
}
