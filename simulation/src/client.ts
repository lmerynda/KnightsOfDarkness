import type {
  ActionResult,
  AttackType,
  BattleReport,
  BuildingName,
  Counts,
  GameConfig,
  Kingdom,
  KingdomDetails,
  MarketOffer,
  MarketResource,
  Rankings,
  Round,
  SpecialBuildingType,
  SpyReport,
  UnitName,
} from "./types.ts";

export class ApiError extends Error {
  public readonly status: number;

  public constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

const call = async <T>(url: string, init: { method?: string; body?: unknown; token?: string } = {}): Promise<T> => {
  const response = await fetch(url, {
    method: init.method ?? "GET",
    headers: { "Content-Type": "application/json", ...(init.token ? { Authorization: `Bearer ${init.token}` } : {}) },
    body: init.body === undefined ? undefined : JSON.stringify(init.body),
  });
  const text = await response.text();
  if (!response.ok) {
    throw new ApiError(response.status, `${init.method ?? "GET"} ${new URL(url).pathname} answered ${response.status}: ${text}`);
  }
  return (text ? JSON.parse(text) : undefined) as T;
};

/** what only the harness may do: move time and look at the whole world */
export class World {
  public readonly url: string;

  public constructor(url: string) {
    this.url = url;
  }

  public advance(ticks = 1, bots = true): Promise<Round> {
    return call(`${this.url}/simulation/advance?ticks=${ticks}&bots=${bots}`, { method: "POST" });
  }

  public endRound(): Promise<Round> {
    return call(`${this.url}/simulation/end-round`, { method: "POST" });
  }

  public kingdoms(): Promise<Kingdom[]> {
    return call(`${this.url}/simulation/kingdoms`);
  }

  public config(): Promise<GameConfig> {
    return call(`${this.url}/simulation/config`);
  }

  public rankings(): Promise<Rankings> {
    return call(`${this.url}/game/rankings`);
  }

  public round(): Promise<Round> {
    return call(`${this.url}/game/round`);
  }

  public finishedRounds(): Promise<Round[]> {
    return call(`${this.url}/game/rounds`);
  }

  public offers(resource: MarketResource): Promise<MarketOffer[]> {
    return call(`${this.url}/market/${resource}`);
  }

  /** registers a kingdom through the public sign-up and signs in as its ruler */
  public async foundKingdom(kingdomName: string): Promise<Player> {
    const email = `${kingdomName.toLowerCase().replace(/[^a-z0-9]+/g, "-")}@simulation.test`;
    const password = "simulation-password";
    await call(`${this.url}/auth/register`, { method: "POST", body: { email, kingdomName, password } });
    const { token } = await call<{ token: string }>(`${this.url}/auth/authenticate`, { method: "POST", body: { email, password } });
    return new Player(this.url, kingdomName, token);
  }
}

/** a signed-in ruler, using exactly the endpoints the browser uses */
export class Player {
  public readonly name: string;
  private readonly url: string;
  private readonly token: string;

  public constructor(url: string, name: string, token: string) {
    this.url = url;
    this.name = name;
    this.token = token;
  }

  private get<T>(path: string): Promise<T> {
    return call(`${this.url}${path}`, { token: this.token });
  }

  private post<T>(path: string, body: unknown = {}): Promise<T> {
    return call(`${this.url}${path}`, { method: "POST", body, token: this.token });
  }

  public kingdom(): Promise<Kingdom> {
    return this.get("/kingdom");
  }

  public details(weaponsPercentage = 0): Promise<KingdomDetails> {
    return this.get(`/kingdom/details?weaponsProductionPercentage=${weaponsPercentage}`);
  }

  public passTurn(weaponsPercentage = 0): Promise<ActionResult> {
    return this.post("/kingdom/pass-turn", weaponsPercentage);
  }

  public build(buildings: Partial<Counts<BuildingName>>): Promise<{ buildings: Counts<BuildingName> }> {
    return this.post("/kingdom/build", buildings);
  }

  public train(units: Partial<Counts<UnitName>>): Promise<{ units: Counts<UnitName> }> {
    return this.post("/kingdom/train", units);
  }

  /** sends soldiers to stand guard in an ally's kingdom */
  public sendReinforcements(hostKingdomName: string, units: Partial<Counts<UnitName>>): Promise<ActionResult> {
    return this.post("/kingdom/send-reinforcements", { hostKingdomName, units });
  }

  public recallReinforcements(id: string): Promise<boolean> {
    return this.post("/kingdom/recall-reinforcements", id);
  }

  public startGreatWork(type: SpecialBuildingType): Promise<unknown> {
    return this.post("/kingdom/start-special-building", { name: type });
  }

  /** puts building points into a great work, answers how many it took */
  public investInGreatWork(id: string, buildingPoints: number): Promise<number> {
    return this.post("/kingdom/build-special-building", { id, buildingPoints });
  }

  public dismiss(units: Partial<Counts<UnitName>>): Promise<{ units: Counts<UnitName> }> {
    return this.post("/kingdom/fire", units);
  }

  public landQuote(count: number): Promise<{ count: number; cost: number }> {
    return this.get(`/kingdom/land-quote?count=${count}`);
  }

  public buyLand(count: number): Promise<{ count: number; cost: number }> {
    return this.post("/kingdom/buy-land", count);
  }

  /** joins is an ally's army on the march against the same target: ours waits at the border and fights right after it lands */
  public sendAttack(target: string, attackType: AttackType, units: Partial<Counts<UnitName>>, joins?: string): Promise<ActionResult> {
    return this.post("/kingdom/send-attack", { destinationKingdomName: target, attackType, units, joins });
  }

  public sendSpies(target: string, spies: number): Promise<ActionResult & { report: SpyReport | null }> {
    return this.post("/war/send-spies", { targetKingdomName: target, spies });
  }

  public notifications(): Promise<{ kind: string; message: string }[]> {
    return this.get("/kingdom/notifications");
  }

  public battleReports(): Promise<BattleReport[]> {
    return this.get("/war/battle-reports");
  }

  public buyOffer(id: string, count: number): Promise<{ count: number; totalCost: number }> {
    return this.post(`/market/${id}/buy`, { count });
  }

  public postOffer(resource: MarketResource, count: number, price: number): Promise<ActionResult> {
    return this.post("/market/create", { resource, count, price });
  }

  /** every offer the ruler's allies keep on the market, however dear */
  public alliesOffers(): Promise<MarketOffer[]> {
    return this.get("/market/allies");
  }

  public createAlliance(name: string): Promise<ActionResult> {
    return this.post("/alliance/create", { name });
  }

  public invite(kingdomName: string, allianceName: string): Promise<ActionResult> {
    return this.post("/alliance/invite", { kingdomName, allianceName });
  }

  public invitations(): Promise<{ id: string; allianceName: string }[]> {
    return this.get("/alliance/invitations");
  }

  public acceptInvitation(invitationId: string): Promise<ActionResult> {
    return this.post("/alliance/accept", { invitationId });
  }
}
