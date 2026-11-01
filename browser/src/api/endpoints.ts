import { ApiError, get, post, setToken } from "./client";
import type {
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
  MarketResource,
  Notification,
  PassTurnResult,
  Rankings,
  Round,
  SpecialBuildingType,
  SpyReport,
  UnitName,
  WarRoom,
} from "./types";

type UnitsResult = { message: string; units: Counts<UnitName> };
type BuildingsResult = { message: string; buildings: Counts<BuildingName> };

export const api = {
  signIn: async (email: string, password: string): Promise<void> => {
    const { token } = await post<{ token: string }>("/auth/authenticate", { email, password });
    setToken(token);
  },
  register: (email: string, kingdomName: string, password: string): Promise<{ message: string }> =>
    post("/auth/register", { email, kingdomName, password }),
  signOut: (): void => setToken(null),

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
  buyLand: (count: number): Promise<LandTransaction> => post("/kingdom/buy-land", count),
  startSpecialBuilding: (type: SpecialBuildingType): Promise<unknown> => post("/kingdom/start-special-building", { name: type }),
  investInSpecialBuilding: (id: string, buildingPoints: number): Promise<number> =>
    post("/kingdom/build-special-building", { id, buildingPoints }),
  demolishSpecialBuilding: (id: string): Promise<boolean> => post("/kingdom/demolish-special-building", { id }),

  sendCarriers: (destinationKingdomName: string, resource: MarketResource, amount: number): Promise<ActionResult> =>
    post("/kingdom/send-carriers", { destinationKingdomName, resource, amount }),
  recallCarriers: (id: string): Promise<boolean> => post("/kingdom/withdraw-carriers", id),
  sendAttack: (destinationKingdomName: string, attackType: AttackType, units: Partial<Counts<UnitName>>): Promise<ActionResult> =>
    post("/kingdom/send-attack", { destinationKingdomName, attackType, units }),
  recallAttack: (id: string): Promise<boolean> => post("/kingdom/withdraw-attack", id),
  sendReinforcements: (hostKingdomName: string, units: Partial<Counts<UnitName>>): Promise<ActionResult> =>
    post("/kingdom/send-reinforcements", { hostKingdomName, units }),
  recallReinforcements: (id: string): Promise<boolean> => post("/kingdom/recall-reinforcements", id),

  sendSpies: (targetKingdomName: string, spies: number): Promise<ActionResult & { report: SpyReport | null }> =>
    post("/war/send-spies", { targetKingdomName, spies }),
  battleReports: (): Promise<BattleReport[]> => get("/war/battle-reports"),
  spyReports: (): Promise<SpyReport[]> => get("/war/spy-reports"),

  offers: (resource: MarketResource): Promise<MarketOffer[]> => get(`/market/${resource}`),
  /** every offer our allies keep on the market, dear ones included */
  alliesOffers: (): Promise<MarketOffer[]> => get("/market/allies"),
  postOffer: (resource: MarketResource, count: number, price: number): Promise<ActionResult> =>
    post("/market/create", { resource, count, price }),
  buyOffer: (id: string, count: number): Promise<{ resource: MarketResource; count: number; pricePerUnit: number; totalCost: number }> =>
    post(`/market/${id}/buy`, { count }),
  withdrawOffer: (id: string): Promise<ActionResult> => post(`/market/${id}/withdraw`),

  notifications: (): Promise<Notification[]> => get("/kingdom/notifications"),
  unreadNotifications: (): Promise<number> => get("/kingdom/notifications/count"),
  markNotificationRead: (id: string): Promise<unknown> => post(`/kingdom/notifications/${id}/mark-as-read`),

  inbox: (): Promise<Letter[]> => get("/messages/inbox"),
  sentLetters: (): Promise<Letter[]> => get("/messages/sent"),
  unreadLetters: (): Promise<number> => get("/messages/unread-count"),
  sendLetter: (recipient: string, body: string): Promise<ActionResult> => post("/messages/send", { recipient, body }),
  sendAllianceLetter: (body: string): Promise<ActionResult> => post("/messages/send-alliance", { body }),
  markLetterRead: (id: string): Promise<boolean> => post(`/messages/${id}/mark-as-read`),

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
