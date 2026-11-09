import { writeFileSync } from "node:fs";
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
  Era,
  Monument,
  MonumentLedger,
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

/** KOD_PROFILE names a file that gets, when the process exits, every endpoint's calls, wall time and bytes received */
const profile = process.env.KOD_PROFILE ? new Map<string, { calls: number; ms: number; bytes: number }>() : undefined;
if (profile) {
  process.once("exit", () => {
    const rows = [...profile.entries()].map(([endpoint, row]) => ({ endpoint, ...row, ms: Math.round(row.ms) })).sort((one, other) => other.ms - one.ms);
    writeFileSync(process.env.KOD_PROFILE!, JSON.stringify(rows, null, 1));
  });
}
const endpointOf = (method: string, url: string): string =>
  `${method} ${new URL(url).pathname
    .split("/")
    .map(segment => (segment.includes("%20") ? ":name" : segment))
    .join("/")}`;

export const call = async <T>(url: string, init: { method?: string; body?: unknown; token?: string } = {}): Promise<T> => {
  const started = profile ? performance.now() : 0;
  const response = await fetch(url, {
    method: init.method ?? "GET",
    headers: { "Content-Type": "application/json", ...(init.token ? { Authorization: `Bearer ${init.token}` } : {}) },
    body: init.body === undefined ? undefined : JSON.stringify(init.body),
  });
  const text = await response.text();
  if (profile) {
    const endpoint = endpointOf(init.method ?? "GET", url);
    const row = profile.get(endpoint) ?? { calls: 0, ms: 0, bytes: 0 };
    row.calls++;
    row.ms += performance.now() - started;
    row.bytes += text.length;
    profile.set(endpoint, row);
  }
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

  public advance(ticks = 1, bots = true): Promise<Era> {
    return call(`${this.url}/simulation/advance?ticks=${ticks}&bots=${bots}`, { method: "POST" });
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

  public era(): Promise<Era> {
    return call(`${this.url}/game/era`);
  }

  public monuments(): Promise<Monument[]> {
    return call(`${this.url}/game/monuments`);
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

/** how often a ruler playing alongside others tries one write before the conflict counts as a failure */
const maxConflictAttempts = 8;

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
  /**
   * a ruler playing at the same moment as others sends a write again when the server refuses it because the kingdom
   * changed under it; the refused write changed nothing, as the 409 promises
   */
  public retryConflicts = false;

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

  private async post<T>(path: string, body: unknown = {}): Promise<T> {
    for (let attempt = 1; ; attempt++) {
      try {
        return await this.signedIn(token => call<T>(`${this.url}${path}`, { method: "POST", body, token }));
      } catch (error) {
        if (!this.retryConflicts || !(error instanceof ApiError) || error.status !== 409 || attempt >= maxConflictAttempts) throw error;
        await new Promise(wait => setTimeout(wait, Math.random() * 5 * attempt));
      }
    }
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

  /** the hall of fame: every finished era with its final rankings, newest first */
  public finishedEras(): Promise<Era[]> {
    return this.get("/game/eras");
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

  public postOffer(resource: MarketResource, count: number, price: number): Promise<ActionResult<MarketOffer>> {
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

  public removeFromAlliance(kingdomName: string): Promise<ActionResult> {
    return this.post("/alliance/remove", { kingdomName });
  }

  public sendToMonument(units: Partial<Counts<UnitName>>): Promise<ActionResult> {
    return this.post("/alliance/monument/garrison", { units });
  }

  public monument(): Promise<MonumentLedger> {
    return this.get("/alliance/monument");
  }

  public foundMonument(seat: string): Promise<ActionResult> {
    return this.post("/alliance/monument/found", { seat });
  }

  public payIntoMonument(goods: Partial<Record<"gold" | "iron" | "tools" | "food", number>>): Promise<ActionResult> {
    return this.post("/alliance/monument/contribute", { gold: 0, iron: 0, tools: 0, food: 0, ...goods });
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
