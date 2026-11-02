// The shapes of the game server's HTTP API, generated into schema.ts from the server's own description (api/openapi.json,
// npm run api:types), so the harness depends on the edge only, never on server code.
import type { components } from "./schema.ts";

type Schemas = components["schemas"];

export type BuildingName = Schemas["BuildingName"];
export type UnitName = Schemas["UnitName"];
export type ResourceName = Schemas["ResourceName"];
export type SpecialBuildingType = Schemas["SpecialBuildingType"];
export type MarketResource = Schemas["MarketResource"];
export type AttackType = Schemas["AttackType"];

/** a list of an enum's values, which must name every value the server knows */
const everyValue =
  <T extends string>() =>
  <const List extends readonly T[]>(values: List & ([T] extends [List[number]] ? unknown : { missing: Exclude<T, List[number]> })): List =>
    values;

export const buildingNames = everyValue<BuildingName>()(["house", "goldMine", "ironMine", "workshop", "farm", "market", "barracks", "guardHouse", "spyGuild", "tower", "castle"]);
export const unitNames = everyValue<UnitName>()(["goldMiner", "ironMiner", "farmer", "blacksmith", "builder", "carrier", "guard", "spy", "infantry", "bowman", "cavalry"]);
export const resourceNames = everyValue<ResourceName>()(["land", "buildingPoints", "unemployed", "gold", "iron", "food", "tools", "weapons", "turns"]);
export const militaryUnits = ["infantry", "bowman", "cavalry"] as const satisfies readonly UnitName[];

export type Counts<K extends string> = Record<K, number>;

export type TurnReport = Schemas["KingdomTurnReport"];
export type Kingdom = Schemas["KingdomDto"];
export type Reinforcement = Schemas["ReinforcementDto"];
export type KingdomDetails = Schemas["KingdomDetailsDto"];
export type GameConfig = Schemas["GameConfig"];
export type BattleReport = Schemas["BattleReportDto"];
export type SpyReport = Schemas["SpyReportDto"];
export type KingdomRank = Schemas["KingdomRankDto"];
export type Rankings = Schemas["RankingsDto"];
export type Round = Schemas["RoundDto"];
export type MarketOffer = Schemas["MarketOfferDto"];
export type LandTransaction = Schemas["LandTransaction"];
export type MarketPurchase = Schemas["MarketPurchaseDto"];
export type Notification = Schemas["NotificationDto"];
export type AllianceInvitation = Schemas["AllianceInvitationDto"];
export type SignedIn = Schemas["AuthResponseDto"];

/** what every action answers: whether it went through, a message for the player, and what it produced, if anything */
export type ActionResult<T = never> = Omit<Schemas["ActionResultVoid"], "data"> & { data: T | null };
