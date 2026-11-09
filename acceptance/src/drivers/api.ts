import { ApiError, call } from "../api/client.ts";
import type {
  ActionResult,
  AllianceInvitation,
  BattleReport,
  BuildingName,
  Kingdom,
  KingdomDetails,
  LandTransaction,
  MarketOffer,
  MarketResource,
  Notification,
  Era,
  SignedIn,
  SpecialBuildingType,
  SpyReport,
  UnitName,
} from "../api/types.ts";
import type { components } from "../api/schema.ts";
import {
  defaultWeaponsShare,
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

type Schemas = components["schemas"];

const marketResources: MarketResource[] = ["food", "iron", "tools", "weapons"];

const outcome = (result: { success: boolean; message: string }): Outcome => ({ done: result.success, message: result.message });

const soldiers = (units: Units): number => Object.values(units).reduce((sum, count) => sum + (count ?? 0), 0);

/** a ruler through the HTTP API, with exactly the calls the browser makes */
const attackTypes = { land: "land", plunder: "economy", monument: "monument" } as const;

class ApiRuler implements RulerDriver {
  private readonly url: string;
  private token: string | null;

  public constructor(url: string, token: string) {
    this.url = url;
    this.token = token;
  }

  private get<T>(path: string): Promise<T> {
    return call(`${this.url}${path}`, { token: this.token ?? undefined });
  }

  private post<T>(path: string, body: unknown = {}): Promise<T> {
    return call(`${this.url}${path}`, { method: "POST", body, token: this.token ?? undefined });
  }

  private act(path: string, body: unknown = {}): Promise<Outcome> {
    return this.post<ActionResult<unknown>>(path, body).then(outcome);
  }

  private kingdom(): Promise<Kingdom> {
    return this.get("/kingdom");
  }

  public async kingdomName(): Promise<string> {
    return (await this.kingdom()).name;
  }

  public async signOut(): Promise<void> {
    this.token = null;
  }

  public async keepKingdom(credentials: Credentials): Promise<Outcome> {
    const kept = await this.post<ActionResult<string>>("/auth/keep", credentials);
    if (kept.success && kept.data) this.token = kept.data;
    return outcome(kept);
  }

  public async isTrial(): Promise<boolean> {
    return (await this.get<Schemas["AccountDto"]>("/auth/account")).trial;
  }

  public passTurn(): Promise<Outcome> {
    return this.act("/kingdom/pass-turn", defaultWeaponsShare);
  }

  public async turnsStored(): Promise<number> {
    return (await this.kingdom()).resources.turns;
  }

  public async build(buildings: Buildings): Promise<Outcome> {
    return whole(buildings, await this.post("/kingdom/build", buildings));
  }

  public async train(units: Units): Promise<Outcome> {
    return whole(units, await this.post("/kingdom/train", units));
  }

  public async demolish(buildings: Buildings): Promise<Outcome> {
    return whole(buildings, await this.post("/kingdom/demolish", buildings));
  }

  public async dismiss(units: Units): Promise<Outcome> {
    return whole(units, await this.post("/kingdom/fire", units));
  }

  public async unitsAtHome(unit: UnitName): Promise<number> {
    return (await this.kingdom()).units.availableUnits[unit];
  }

  public beginGreatWork(type: SpecialBuildingType): Promise<Outcome> {
    return this.act("/kingdom/start-special-building", { name: type });
  }

  private async greatWork(type: SpecialBuildingType): Promise<string | undefined> {
    return (await this.kingdom()).specialBuildings.find(work => work.buildingType === type)?.id;
  }

  public async investInGreatWork(type: SpecialBuildingType, buildingPoints: number): Promise<Outcome> {
    const id = await this.greatWork(type);
    return id ? this.act("/kingdom/build-special-building", { id, buildingPoints }) : { done: false, message: `No ${type} begun` };
  }

  public async tearDownGreatWork(type: SpecialBuildingType): Promise<Outcome> {
    const id = await this.greatWork(type);
    return id ? this.act("/kingdom/demolish-special-building", { id }) : { done: false, message: `No ${type} begun` };
  }

  public async greatWorks(): Promise<{ type: SpecialBuildingType; level: number }[]> {
    return (await this.kingdom()).specialBuildings.map(work => ({ type: work.buildingType, level: work.level }));
  }

  public async buildingCount(building: BuildingName): Promise<number> {
    return (await this.kingdom()).buildings[building];
  }

  public async landPrice(acres: number): Promise<number> {
    return (await this.get<LandTransaction>(`/kingdom/land-quote?count=${acres}`)).cost;
  }

  public buyLand(acres: number): Promise<Outcome> {
    return this.act("/kingdom/buy-land", acres);
  }

  public buyAllTheLandWeCanAfford(): Promise<Outcome> {
    return this.act("/kingdom/buy-land", moreLandThanAnyTreasuryBuys);
  }

  public async land(): Promise<number> {
    return (await this.kingdom()).resources.land;
  }

  public async soldiersPay(): Promise<number> {
    return (await this.get<KingdomDetails>(`/kingdom/details?weaponsProductionPercentage=${defaultWeaponsShare}`)).forecast.goldUpkeep;
  }

  public async stores(store: Store): Promise<number> {
    return (await this.kingdom()).resources[store];
  }

  public async nextYield(store: Store): Promise<number> {
    const details = await this.get<KingdomDetails>(`/kingdom/details?weaponsProductionPercentage=${defaultWeaponsShare}`);
    return details.forecast.resourcesProduced[store] ?? 0;
  }

  public march(target: string, units: Units, goal: Goal): Promise<Outcome> {
    return this.act("/kingdom/send-attack", { destinationKingdomName: target, attackType: attackTypes[goal], units });
  }

  public async recallArmy(target: string): Promise<Outcome> {
    const army = (await this.kingdom()).ongoingAttacks.find(attack => attack.to === target);
    return army ? this.act("/kingdom/withdraw-attack", army.id) : { done: false, message: `No army marches on ${target}` };
  }

  public async joinStrike(leader: string, target: string, units: Units, goal: Goal = "land"): Promise<Outcome> {
    const room = await this.get<Schemas["AllianceWarRoomDto"]>("/alliance/war-room");
    const strike = room.marching.find(march => march.from === leader && march.to === target);
    if (!strike) return { done: false, message: `${leader} has no army on the march to ${target}` };
    return this.act("/kingdom/send-attack", { destinationKingdomName: target, attackType: attackTypes[goal], units, joins: strike.id });
  }

  public async armiesComing(): Promise<{ from: string; soldiers: number }[]> {
    return (await this.kingdom()).incoming.map(army => ({ from: army.from, soldiers: army.soldiers }));
  }

  public async armiesOut(): Promise<{ to: string; joining: string | null; atTheBorder: boolean }[]> {
    return (await this.kingdom()).ongoingAttacks.map(attack => ({
      to: attack.to,
      joining: attack.joining ?? null,
      atTheBorder: attack.joining !== null && attack.turnsLeft === 0,
    }));
  }

  public async battles(): Promise<Battle[]> {
    return (await this.get<BattleReport[]>("/war/battle-reports")).map(report => ({
      attacker: report.attacker,
      defender: report.defender,
      attackerWon: report.attackerWon,
      landTaken: report.attackerWon && report.attackType === "land" ? report.landCaptured : 0,
      goldPlundered: report.attackerWon ? (report.plunder.gold ?? 0) : 0,
      stagesRazed: report.stagesRazed,
    }));
  }

  public sendSpies(target: string, spies: number): Promise<Outcome> {
    return this.act("/war/send-spies", { targetKingdomName: target, spies });
  }

  public async latestIntel(): Promise<{ about: string; stores: boolean; spiesLost: number } | null> {
    const [report] = await this.get<SpyReport[]>("/war/spy-reports");
    return report ? { about: report.targetKingdomName, stores: report.success, spiesLost: report.spiesLost } : null;
  }

  public sendGuards(ally: string, units: Units): Promise<Outcome> {
    return this.act("/kingdom/send-reinforcements", { hostKingdomName: ally, units });
  }

  public async guardsAway(): Promise<{ with: string; soldiers: number }[]> {
    return (await this.kingdom()).reinforcements.filter(guard => guard.turnsLeft === 0).map(guard => ({ with: guard.to, soldiers: soldiers(guard.units) }));
  }

  public async guardsHosted(): Promise<{ from: string; soldiers: number }[]> {
    return (await this.kingdom()).garrisons.filter(guard => guard.turnsLeft === 0).map(guard => ({ from: guard.from, soldiers: soldiers(guard.units) }));
  }

  public async callGuardsHome(): Promise<Outcome> {
    const sent = (await this.kingdom()).reinforcements;
    if (!sent.length) return { done: false, message: "No soldiers stand guard elsewhere" };
    for (const guard of sent) {
      const recalled = await this.act("/kingdom/recall-reinforcements", guard.id);
      if (!recalled.done) return recalled;
    }
    return { done: true, message: "Our soldiers are home again" };
  }

  public async buy(resource: MarketResource, count: number): Promise<Outcome> {
    const name = await this.kingdomName();
    const offers = (await this.get<MarketOffer[]>(`/market/${resource}`)).filter(offer => offer.sellerName !== name && offer.count >= count);
    const cheapest = offers.sort((a, b) => a.price - b.price)[0];
    if (!cheapest) return { done: false, message: `Nobody offers ${count} ${resource}` };
    return this.act(`/market/${cheapest.id}/buy`, { count });
  }

  public async buyFrom(seller: string, resource: MarketResource, count: number): Promise<Outcome> {
    const offer = (await this.get<MarketOffer[]>(`/market/${resource}`)).find(candidate => candidate.sellerName === seller);
    return offer ? this.act(`/market/${offer.id}/buy`, { count }) : { done: false, message: `${seller} offers no ${resource}` };
  }

  public offer(resource: MarketResource, count: number, price: number): Promise<Outcome> {
    return this.act("/market/create", { resource, count, price });
  }

  public async ownOffers(): Promise<{ resource: MarketResource; count: number; price: number }[]> {
    return (await this.kingdom()).marketOffers.map(({ resource, count, price }) => ({ resource, count, price }));
  }

  public async withdrawOffer(resource: MarketResource): Promise<Outcome> {
    const offer = (await this.kingdom()).marketOffers.find(candidate => candidate.resource === resource);
    return offer ? this.act(`/market/${offer.id}/withdraw`) : { done: false, message: `No ${resource} on the market` };
  }

  public async askAlliesForGold(gold: number): Promise<Outcome> {
    const kingdom = await this.kingdom();
    const pledge = marketResources.find(resource => kingdom.resources[resource] > 0);
    if (!pledge) return { done: false, message: "Nothing to pledge" };
    return this.act("/market/create", { resource: pledge, count: 1, price: gold });
  }

  public async alliesAsks(): Promise<{ from: string; gold: number }[]> {
    return (await this.get<MarketOffer[]>("/market/allies")).filter(offer => offer.count === 1).map(offer => ({ from: offer.sellerName, gold: offer.price }));
  }

  public async payAsk(ally: string): Promise<Outcome> {
    const ask = (await this.get<MarketOffer[]>("/market/allies")).find(offer => offer.sellerName === ally && offer.count === 1);
    if (!ask) return { done: false, message: `${ally} asks for nothing` };
    return this.act(`/market/${ask.id}/buy`, { count: 1 });
  }

  public sendCarriers(to: string, resource: MarketResource, count: number): Promise<Outcome> {
    return this.act("/kingdom/send-carriers", { destinationKingdomName: to, resource, count });
  }

  public async carriersOut(): Promise<{ to: string; resource: MarketResource; count: number }[]> {
    const kingdom = await this.kingdom();
    return kingdom.carriersOnTheMove.filter(load => load.from === kingdom.name).map(({ to, resource, count }) => ({ to, resource, count }));
  }

  public async recallCarriers(to: string): Promise<Outcome> {
    const kingdom = await this.kingdom();
    const load = kingdom.carriersOnTheMove.find(candidate => candidate.from === kingdom.name && candidate.to === to);
    return load ? this.act("/kingdom/withdraw-carriers", load.id) : { done: false, message: `No carriers on the road to ${to}` };
  }

  public writeToAlliance(body: string): Promise<Outcome> {
    return this.act("/messages/send-alliance", { body });
  }

  public writeLetter(to: string, body: string): Promise<Outcome> {
    return this.act("/messages/send", { recipient: to, body });
  }

  public unreadLetters(): Promise<number> {
    return this.get("/messages/unread-count");
  }

  public async letters(): Promise<{ from: string; body: string }[]> {
    return (await this.get<Schemas["MessageDto"][]>("/messages/inbox")).map(letter => ({ from: letter.sender, body: letter.body }));
  }

  public async sentLetters(): Promise<{ to: string; body: string }[]> {
    return (await this.get<Schemas["MessageDto"][]>("/messages/sent")).map(letter => ({ to: letter.recipient, body: letter.body }));
  }

  public foundAlliance(name: string): Promise<Outcome> {
    return this.act("/alliance/create", { name });
  }

  public async invite(kingdomName: string): Promise<Outcome> {
    const alliance = await this.alliance();
    if (!alliance) return { done: false, message: "No alliance to invite to" };
    return this.act("/alliance/invite", { kingdomName, allianceName: alliance.name });
  }

  public async join(allianceName: string): Promise<Outcome> {
    const invitation = (await this.get<AllianceInvitation[]>("/alliance/invitations")).find(candidate => candidate.allianceName === allianceName);
    if (!invitation) return { done: false, message: `No invitation to ${allianceName}` };
    return this.act("/alliance/accept", { invitationId: invitation.id });
  }

  public async decline(allianceName: string): Promise<Outcome> {
    const invitation = (await this.get<AllianceInvitation[]>("/alliance/invitations")).find(candidate => candidate.allianceName === allianceName);
    return invitation ? this.act("/alliance/reject", { invitationId: invitation.id }) : { done: false, message: `No invitation to ${allianceName}` };
  }

  public leaveAlliance(): Promise<Outcome> {
    return this.act("/alliance/leave");
  }

  public removeMember(kingdomName: string): Promise<Outcome> {
    return this.act("/alliance/remove", { kingdomName });
  }

  public raiseVassal(name: string): Promise<Outcome> {
    return this.act("/alliance/add-bot", { botName: name });
  }

  public foundMonument(seat: string): Promise<Outcome> {
    return this.act("/alliance/monument/found", { seat });
  }

  public payIntoMonument(goods: Partial<MonumentGoods>): Promise<Outcome> {
    return this.act("/alliance/monument/contribute", { gold: 0, iron: 0, tools: 0, food: 0, ...goods });
  }

  public sendToMonument(units: Units): Promise<Outcome> {
    return this.act("/alliance/monument/garrison", { units });
  }

  public async monumentLedger(): Promise<MonumentLedger | null> {
    const ledger = await this.get<Schemas["MonumentLedgerDto"]>("/alliance/monument").catch((failure: unknown) => {
      if (failure instanceof ApiError && failure.status === 404) return null;
      throw failure;
    });
    return (
      ledger && {
        stages: ledger.monument.stages,
        store: ledger.store,
        crumbling: ledger.upkeepUnpaid,
        soldiersOnGuard: ledger.garrison.filter(guard => guard.turnsLeft === 0).reduce((sum, guard) => sum + soldiers(guard.units), 0),
      }
    );
  }

  public async monuments(): Promise<MonumentView[]> {
    return (await this.get<Schemas["MonumentDto"][]>("/game/monuments")).map(monument => ({ alliance: monument.allianceName, seat: monument.seat, stages: monument.stages, complete: monument.complete, ticksToWin: monument.ticksToWin ?? null, garrison: monument.garrison }));
  }

  public async alliance(): Promise<{ name: string; members: string[] } | null> {
    if (!(await this.kingdom()).allianceName) return null;
    const alliance = await this.get<Schemas["AllianceWithMembersDto"]>("/alliance");
    return { name: alliance.name, members: alliance.members };
  }

  public async warRoomMarches(): Promise<{ from: string; to: string }[]> {
    if (!(await this.kingdom()).allianceName) return [];
    return (await this.get<Schemas["AllianceWarRoomDto"]>("/alliance/war-room")).marching.map(({ from, to }) => ({ from, to }));
  }

  public async chronicle(): Promise<string[]> {
    return (await this.get<Notification[]>("/kingdom/notifications")).map(note => note.message);
  }

  public async pastEras(): Promise<PastEra[]> {
    return (await this.get<Era[]>("/game/eras")).map(past => ({
      era: past.number,
      winner: past.winningAlliance ?? null,
      largest: past.finalRankings?.kingdoms[0]?.kingdomName ?? null,
      endedBy: past.endedBy ?? null,
    }));
  }
}

/** a visitor through the HTTP API */
class ApiGate implements GateDriver {
  private readonly url: string;
  private token: string | null = null;

  public constructor(url: string) {
    this.url = url;
  }

  public async foundKingdom(kingdomName: string, credentials: Credentials): Promise<Outcome> {
    try {
      await call<ActionResult>(`${this.url}/auth/register`, { method: "POST", body: { ...credentials, kingdomName } });
    } catch (error) {
      // a refused registration answers 400 with the reason
      if (error instanceof ApiError && error.status === 400) return { done: false, message: error.message };
      throw error;
    }
    return this.signIn(credentials);
  }

  public async signIn(credentials: Credentials): Promise<Outcome> {
    try {
      const { token } = await call<SignedIn>(`${this.url}/auth/authenticate`, { method: "POST", body: credentials });
      this.token = token;
      return { done: true, message: "Signed in" };
    } catch (error) {
      if (error instanceof ApiError && error.status === 401) return { done: false, message: error.message };
      throw error;
    }
  }

  public async playWithOnlyAName(kingdomName: string): Promise<Outcome> {
    const started = await call<ActionResult<Schemas["TrialStartedDto"]>>(`${this.url}/auth/trial`, { method: "POST", body: { kingdomName } });
    if (started.success && started.data) this.token = started.data.token;
    return outcome(started);
  }

  public ruler(): RulerDriver {
    if (!this.token) throw new Error("Nobody is signed in");
    return new ApiRuler(this.url, this.token);
  }

  public async close(): Promise<void> {}
}

export const apiVisitors = (url: string): Visitors => ({
  arrive: async () => new ApiGate(url),
  keepEvidence: async () => {},
  close: async () => {},
});
