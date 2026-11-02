import { ApiError, get, getTrial, post, setToken, setTrial } from "./client";
import type {
  Account,
  ActionResult,
  Alliance,
  AllianceInvitation,
  AllianceSummary,
  AttackType,
  BattleReport,
  BuildingName,
  Counts,
  GameConfig,
  Kingdom,
  KingdomDetails,
  LandTransaction,
  Letter,
  MarketOffer,
  MarketPurchase,
  MarketResource,
  Notification,
  PassTurnResult,
  Rankings,
  Round,
  SpecialBuildingType,
  SignedIn,
  SpyReport,
  TrialStarted,
  UnitName,
  WarRoom,
} from "./types";

type UnitsResult = ActionResult<Counts<UnitName>>;
type BuildingsResult = ActionResult<Counts<BuildingName>>;

export const api = {
  signIn: async (email: string, password: string): Promise<void> => {
    const { token } = await post<SignedIn>("/auth/authenticate", { email, password });
    setToken(token);
  },
  register: (email: string, kingdomName: string, password: string): Promise<ActionResult> =>
    post("/auth/register", { email, kingdomName, password }),
  /** founds a kingdom with only its name and keeps the trial account in this browser */
  startTrial: async (kingdomName: string): Promise<ActionResult<TrialStarted>> => {
    const started = await post<ActionResult<TrialStarted>>("/auth/trial", {
      kingdomName,
    });
    if (started.data) {
      setTrial({ email: started.data.email, password: started.data.password });
      setToken(started.data.token);
    }
    return started;
  },
  /** signs back in to the trial kingdom this browser keeps */
  resumeTrial: async (): Promise<void> => {
    const trial = getTrial();
    if (!trial) throw new Error("This browser keeps no kingdom");
    const { token } = await post<SignedIn>("/auth/authenticate", trial);
    setToken(token);
  },
  /** a trial ruler keeps the kingdom with an address and password of their own */
  keepKingdom: async (email: string, password: string): Promise<ActionResult<string>> => {
    const kept = await post<ActionResult<string>>("/auth/keep", { email, password });
    if (kept.success && kept.data) {
      setTrial(null);
      setToken(kept.data);
    }
    return kept;
  },
  account: (): Promise<Account> => get("/auth/account"),
  signOut: (): void => {
    setTrial(null);
    setToken(null);
  },

  config: (): Promise<GameConfig> => post("/config"),
  round: (): Promise<Round> => get("/game/round"),
  finishedRounds: (): Promise<Round[]> => get("/game/rounds"),
  rankings: (): Promise<Rankings> => get("/game/rankings"),

  kingdom: (): Promise<Kingdom> => get("/kingdom"),
  details: (weaponsPercentage: number): Promise<KingdomDetails> => get(`/kingdom/details?weaponsProductionPercentage=${weaponsPercentage}`),
  passTurn: (weaponsPercentage: number): Promise<PassTurnResult> => post("/kingdom/pass-turn", weaponsPercentage),
  build: (buildings: Partial<Counts<BuildingName>>): Promise<BuildingsResult> => post("/kingdom/build", buildings),
  demolish: (buildings: Partial<Counts<BuildingName>>): Promise<BuildingsResult> => post("/kingdom/demolish", buildings),
  train: (units: Partial<Counts<UnitName>>): Promise<UnitsResult> => post("/kingdom/train", units),
  dismiss: (units: Partial<Counts<UnitName>>): Promise<UnitsResult> => post("/kingdom/fire", units),
  landQuote: (count: number): Promise<LandTransaction> => get(`/kingdom/land-quote?count=${count}`),
  buyLand: (count: number): Promise<ActionResult<LandTransaction>> => post("/kingdom/buy-land", count),
  startSpecialBuilding: (type: SpecialBuildingType): Promise<ActionResult<string>> =>
    post("/kingdom/start-special-building", { name: type }),
  investInSpecialBuilding: (id: string, buildingPoints: number): Promise<ActionResult<number>> =>
    post("/kingdom/build-special-building", { id, buildingPoints }),
  demolishSpecialBuilding: (id: string): Promise<ActionResult> => post("/kingdom/demolish-special-building", { id }),

  sendCarriers: (destinationKingdomName: string, resource: MarketResource, count: number): Promise<ActionResult> =>
    post("/kingdom/send-carriers", { destinationKingdomName, resource, count }),
  recallCarriers: (id: string): Promise<ActionResult> => post("/kingdom/withdraw-carriers", id),
  /** joins is an ally's army on the march against the same target, whose strike ours joins */
  sendAttack: (
    destinationKingdomName: string,
    attackType: AttackType,
    units: Partial<Counts<UnitName>>,
    joins?: string,
  ): Promise<ActionResult> => post("/kingdom/send-attack", { destinationKingdomName, attackType, units, joins }),
  recallAttack: (id: string): Promise<ActionResult> => post("/kingdom/withdraw-attack", id),
  sendReinforcements: (hostKingdomName: string, units: Partial<Counts<UnitName>>): Promise<ActionResult> =>
    post("/kingdom/send-reinforcements", { hostKingdomName, units }),
  recallReinforcements: (id: string): Promise<ActionResult> => post("/kingdom/recall-reinforcements", id),

  sendSpies: (targetKingdomName: string, spies: number): Promise<ActionResult<SpyReport>> =>
    post("/war/send-spies", { targetKingdomName, spies }),
  battleReports: (): Promise<BattleReport[]> => get("/war/battle-reports"),
  spyReports: (): Promise<SpyReport[]> => get("/war/spy-reports"),

  offers: (resource: MarketResource): Promise<MarketOffer[]> => get(`/market/${resource}`),
  /** every offer our allies keep on the market, dear ones included */
  alliesOffers: (): Promise<MarketOffer[]> => get("/market/allies"),
  postOffer: (resource: MarketResource, count: number, price: number): Promise<ActionResult> =>
    post("/market/create", { resource, count, price }),
  buyOffer: (id: string, count: number): Promise<ActionResult<MarketPurchase>> => post(`/market/${id}/buy`, { count }),
  withdrawOffer: (id: string): Promise<ActionResult> => post(`/market/${id}/withdraw`),

  notifications: (): Promise<Notification[]> => get("/kingdom/notifications"),
  unreadNotifications: (): Promise<number> => get("/kingdom/notifications/count"),
  markNotificationRead: (id: string): Promise<unknown> => post(`/kingdom/notifications/${id}/mark-as-read`),

  inbox: (): Promise<Letter[]> => get("/messages/inbox"),
  sentLetters: (): Promise<Letter[]> => get("/messages/sent"),
  unreadLetters: (): Promise<number> => get("/messages/unread-count"),
  sendLetter: (recipient: string, body: string): Promise<ActionResult<number>> => post("/messages/send", { recipient, body }),
  sendAllianceLetter: (body: string): Promise<ActionResult<number>> => post("/messages/send-alliance", { body }),
  markLetterRead: (id: string): Promise<ActionResult> => post(`/messages/${id}/mark-as-read`),

  alliance: async (): Promise<Alliance | undefined> => {
    try {
      return await get<Alliance>("/alliance");
    } catch (error) {
      if (error instanceof ApiError && error.status === 404) {
        return undefined;
      }
      throw error;
    }
  },
  warRoom: async (): Promise<WarRoom | undefined> => {
    try {
      return await get<WarRoom>("/alliance/war-room");
    } catch (error) {
      if (error instanceof ApiError && error.status === 404) {
        return undefined;
      }
      throw error;
    }
  },
  alliances: (): Promise<AllianceSummary[]> => get("/alliance/all"),
  invitations: (): Promise<AllianceInvitation[]> => get("/alliance/invitations"),
  createAlliance: (name: string): Promise<ActionResult> => post("/alliance/create", { name }),
  leaveAlliance: (): Promise<ActionResult> => post("/alliance/leave"),
  invite: (kingdomName: string, allianceName: string): Promise<ActionResult> => post("/alliance/invite", { kingdomName, allianceName }),
  acceptInvitation: (invitationId: string): Promise<ActionResult> => post("/alliance/accept", { invitationId }),
  rejectInvitation: (invitationId: string): Promise<ActionResult> => post("/alliance/reject", { invitationId }),
  removeMember: (kingdomName: string): Promise<ActionResult> => post("/alliance/remove", { kingdomName }),
  addBot: (botName: string): Promise<ActionResult> => post("/alliance/add-bot", { botName }),
};
