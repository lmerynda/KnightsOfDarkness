import type {
  ActionResult,
  AllianceInvitation,
  AttackType,
  BattleReport,
  BuildingName,
  Counts,
  GameConfig,
  Kingdom,
  KingdomDetails,
  LandTransaction,
  MarketOffer,
  MarketPurchase,
  MarketTrade,
  MarketResource,
  Notification,
  Rankings,
  Round,
  SignedIn,
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

export const call = async <T>(url: string, init: { method?: string; body?: unknown; token?: string } = {}): Promise<T> => {
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

/** a request sent as it is, whatever it holds, and the answer as it came: for checks that the server stands up to anything */
export const send = async (url: string, init: { method?: string; body?: string; token?: string } = {}): Promise<{ status: number; text: string }> => {
  const response = await fetch(url, {
    method: init.method ?? "GET",
    headers: { "Content-Type": "application/json", ...(init.token ? { Authorization: `Bearer ${init.token}` } : {}) },
    body: init.body,
  });
  return { status: response.status, text: await response.text() };
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

  /** every sale the market has seen, which no player can list */
  public trades(): Promise<MarketTrade[]> {
    return call(`${this.url}/simulation/trades`);
  }

  public rankings(): Promise<Rankings> {
    return call(`${this.url}/game/rankings`);
  }

  public round(): Promise<Round> {
    return call(`${this.url}/game/round`);
  }

  /** registers a kingdom through the public sign-up and signs in as its ruler */
  public async foundKingdom(kingdomName: string): Promise<Player> {
    const credentials = simulationCredentials(kingdomName);
    await call(`${this.url}/auth/register`, { method: "POST", body: { ...credentials, kingdomName } });
    return new Player(this.url, kingdomName, credentials, await signIn(this.url, credentials));
  }
}

export type Credentials = { email: string; password: string };

/** the address and password a scripted ruler registers its kingdom with */
export const simulationCredentials = (kingdomName: string): Credentials => ({
  email: `${kingdomName.toLowerCase().replace(/[^a-z0-9]+/g, "-")}@simulation.test`,
  password: "simulation-password",
});

const signIn = async (url: string, credentials: Credentials): Promise<string> =>
  (await call<SignedIn>(`${url}/auth/authenticate`, { method: "POST", body: credentials })).token;

/**
 * a signed-in ruler, using exactly the endpoints the browser uses; like the browser it signs in again when its session runs
 * out, so a simulation can play longer than a token lasts (an hour)
 */
export class Player {
  public readonly name: string;
  private readonly url: string;
  private readonly credentials: Credentials;
  private token: string;

  public constructor(url: string, name: string, credentials: Credentials, token: string) {
    this.url = url;
    this.name = name;
    this.credentials = credentials;
    this.token = token;
  }

  private async signedIn<T>(request: (token: string) => Promise<T>): Promise<T> {
    try {
      return await request(this.token);
    } catch (error) {
      if (!(error instanceof ApiError) || error.status !== 401) throw error;
      this.token = await signIn(this.url, this.credentials);
      return request(this.token);
    }
  }

  private get<T>(path: string): Promise<T> {
    return this.signedIn(token => call(`${this.url}${path}`, { token }));
  }

  private post<T>(path: string, body: unknown = {}): Promise<T> {
    return this.signedIn(token => call(`${this.url}${path}`, { method: "POST", body, token }));
  }

  /** any request as this ruler, with the body exactly as given */
  public send(method: string, path: string, body?: string): Promise<{ status: number; text: string }> {
    return send(`${this.url}${path}`, { method, body, token: this.token });
  }

  public kingdom(): Promise<Kingdom> {
    return this.get("/kingdom");
  }

  public details(weaponsPercentage = 0): Promise<KingdomDetails> {
    return this.get(`/kingdom/details?weaponsProductionPercentage=${weaponsPercentage}`);
  }

  /** the hall of fame: every finished round with its final rankings, newest first */
  public finishedRounds(): Promise<Round[]> {
    return this.get("/game/rounds");
  }

  public offers(resource: MarketResource): Promise<MarketOffer[]> {
    return this.get(`/market/${resource}`);
  }

  public passTurn(weaponsPercentage = 0): Promise<ActionResult> {
    return this.post("/kingdom/pass-turn", weaponsPercentage);
  }

  public build(buildings: Partial<Counts<BuildingName>>): Promise<ActionResult<Counts<BuildingName>>> {
    return this.post("/kingdom/build", buildings);
  }

  public train(units: Partial<Counts<UnitName>>): Promise<ActionResult<Counts<UnitName>>> {
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

  public dismiss(units: Partial<Counts<UnitName>>): Promise<ActionResult<Counts<UnitName>>> {
    return this.post("/kingdom/fire", units);
  }

  public landQuote(count: number): Promise<LandTransaction> {
    return this.get(`/kingdom/land-quote?count=${count}`);
  }

  public buyLand(count: number): Promise<ActionResult<LandTransaction>> {
    return this.post("/kingdom/buy-land", count);
  }

  /** joins is an ally's army on the march against the same target: ours waits at the border and fights right after it lands */
  public sendAttack(target: string, attackType: AttackType, units: Partial<Counts<UnitName>>, joins?: string): Promise<ActionResult> {
    return this.post("/kingdom/send-attack", { destinationKingdomName: target, attackType, units, joins });
  }

  public sendSpies(target: string, spies: number): Promise<ActionResult<SpyReport>> {
    return this.post("/war/send-spies", { targetKingdomName: target, spies });
  }

  public notifications(): Promise<Notification[]> {
    return this.get("/kingdom/notifications");
  }

  public battleReports(): Promise<BattleReport[]> {
    return this.get("/war/battle-reports");
  }

  public buyOffer(id: string, count: number): Promise<ActionResult<MarketPurchase>> {
    return this.post(`/market/${id}/buy`, { count });
  }

  public postOffer(resource: MarketResource, count: number, price: number): Promise<ActionResult> {
    return this.post("/market/create", { resource, count, price });
  }

  public withdrawOffer(id: string): Promise<ActionResult> {
    return this.post(`/market/${id}/withdraw`);
  }

  /** sends goods to another kingdom by carriers, as many as the idle carriers hold; they arrive after a few turns */
  public sendCarriers(to: string, resource: MarketResource, count: number): Promise<ActionResult<{ destinationKingdomName: string; resource: MarketResource; count: number }>> {
    return this.post("/kingdom/send-carriers", { destinationKingdomName: to, resource, count });
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

  public invitations(): Promise<AllianceInvitation[]> {
    return this.get("/alliance/invitations");
  }

  public acceptInvitation(invitationId: string): Promise<ActionResult> {
    return this.post("/alliance/accept", { invitationId });
  }
}
