import type { components } from "./schema";

/**
 * the API's types, named for the browser; every shape comes from schema.ts, which is generated from the server's own
 * description (api/openapi.json, npm run api:types), so a field the server renames or drops breaks the build here
 */
type Schemas = components["schemas"];

export type ResourceName = Schemas["ResourceName"];
export type BuildingName = Schemas["BuildingName"];
export type UnitName = Schemas["UnitName"];
export type MarketResource = Schemas["MarketResource"];
export type SpecialBuildingType = Schemas["SpecialBuildingType"];
export type AttackType = Schemas["AttackType"];
export type NotificationKind = Schemas["NotificationKind"];

/** a list of an enum's values in the order the pages show them, which must name every value the server knows */
const everyValue =
  <T extends string>() =>
  <const List extends readonly T[]>(values: List & ([T] extends [List[number]] ? unknown : { missing: Exclude<T, List[number]> })): List =>
    values;

export const resourceNames = everyValue<ResourceName>()([
  "land",
  "buildingPoints",
  "unemployed",
  "gold",
  "iron",
  "food",
  "tools",
  "weapons",
  "turns",
]);
export const buildingNames = everyValue<BuildingName>()([
  "house",
  "goldMine",
  "ironMine",
  "workshop",
  "farm",
  "market",
  "barracks",
  "guardHouse",
  "spyGuild",
  "tower",
  "castle",
]);
export const unitNames = everyValue<UnitName>()([
  "goldMiner",
  "ironMiner",
  "farmer",
  "blacksmith",
  "builder",
  "carrier",
  "guard",
  "spy",
  "infantry",
  "bowman",
  "cavalry",
]);
export const marketResources = everyValue<MarketResource>()(["food", "iron", "tools", "weapons"]);
export const specialBuildingTypes = everyValue<SpecialBuildingType>()(["goldShaft", "ironShaft", "granary", "forge", "warehouse"]);

export const militaryUnits = ["infantry", "bowman", "cavalry"] as const satisfies readonly UnitName[];
export type MilitaryUnit = (typeof militaryUnits)[number];

export type Counts<K extends string> = Record<K, number>;

export type TurnReport = Schemas["KingdomTurnReport"];
export type MarketOffer = Schemas["MarketOfferDto"];
export type MarketPurchase = Schemas["MarketPurchaseDto"];
export type SpecialBuilding = Schemas["KingdomSpecialBuildingDto"];
export type CarriersOnTheMove = Schemas["CarriersOnTheMoveDto"];
export type OngoingAttack = Schemas["OngoingAttackDto"];
export type IncomingArmy = Schemas["IncomingArmyDto"];
export type Reinforcement = Schemas["ReinforcementDto"];
export type Kingdom = Schemas["KingdomDto"];
export type KingdomDetails = Schemas["KingdomDetailsDto"];
export type GameConfig = Schemas["GameConfig"];
export type LandTransaction = Schemas["LandTransaction"];
export type BattleReport = Schemas["BattleReportDto"];
export type SpyReport = Schemas["SpyReportDto"];
export type WarRoom = Schemas["AllianceWarRoomDto"];
export type Notification = Schemas["NotificationDto"];
export type Letter = Schemas["MessageDto"];
export type KingdomRank = Schemas["KingdomRankDto"];
export type AllianceRank = Schemas["AllianceRankDto"];
export type Rankings = Schemas["RankingsDto"];
export type Round = Schemas["RoundDto"];
export type Alliance = Schemas["AllianceWithMembersDto"];
export type AllianceSummary = Schemas["AllianceDto"];
export type AllianceInvitation = Schemas["AllianceInvitationDto"];
export type Account = Schemas["AccountDto"];
export type TrialStarted = Schemas["TrialStartedDto"];
export type SignedIn = Schemas["AuthResponseDto"];

/** what every action answers: whether it went through, a message for the player, and what it produced, if anything */
export type ActionResult<T = never> = Omit<Schemas["ActionResultVoid"], "data"> & { data: T | null };
export type PassTurnResult = Schemas["ActionResultKingdomTurnReport"];
