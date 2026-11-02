// The shapes of the game server's HTTP API, kept here so the harness depends on the edge only, never on server code.

export const buildingNames = ["house", "goldMine", "ironMine", "workshop", "farm", "market", "barracks", "guardHouse", "spyGuild", "tower", "castle"] as const;
export type BuildingName = (typeof buildingNames)[number];

export const unitNames = ["goldMiner", "ironMiner", "farmer", "blacksmith", "builder", "carrier", "guard", "spy", "infantry", "bowman", "cavalry"] as const;
export type UnitName = (typeof unitNames)[number];

export type SpecialBuildingType = "goldShaft" | "ironShaft" | "granary" | "forge" | "warehouse";

export const militaryUnits = ["infantry", "bowman", "cavalry"] as const;

export const resourceNames = ["land", "buildingPoints", "unemployed", "gold", "iron", "food", "tools", "weapons", "turns"] as const;
export type ResourceName = (typeof resourceNames)[number];

export type MarketResource = "food" | "iron" | "tools" | "weapons";
export type AttackType = "land" | "economy";
export type Counts<K extends string> = Record<K, number>;

export type TurnReport = {
  foodConsumed: number;
  resourcesProduced: Partial<Counts<ResourceName>>;
  arrivingPeople: number;
  exiledPeople: number;
  starvedPeople: number;
  goldUpkeep: number;
  desertedSoldiers: number;
  nourishmentProductionFactor: number;
  professionalsLeaving: Partial<Counts<UnitName>>;
};

export type Kingdom = {
  name: string;
  resources: Counts<ResourceName>;
  buildings: Counts<BuildingName>;
  units: { availableUnits: Counts<UnitName>; mobileUnits: Counts<UnitName> };
  marketOffers: { id: string; resource: MarketResource; count: number; price: number }[];
  specialBuildings: { id: string; buildingType: SpecialBuildingType; level: number; isMaxLevel: boolean }[];
  lastTurnReport: TurnReport;
  ongoingAttacks: { id: string; to: string; turnsLeft: number; units: Partial<Counts<UnitName>>; joining: string | null }[];
  carriersOnTheMove: { id: string; to: string; turnsLeft: number }[];
  allianceName: string | null;
  protectionTicks: number;
  /** our soldiers on the way to, or standing guard in, allied kingdoms */
  reinforcements: Reinforcement[];
  /** allied soldiers on the way to, or standing guard in, this kingdom */
  garrisons: Reinforcement[];
  /** enemy armies marching on this kingdom, as its scouts see them */
  incoming: { from: string; to: string; turnsLeft: number; soldiers: number }[];
};

export type Reinforcement = { id: string; from: string; to: string; turnsLeft: number; units: Partial<Counts<UnitName>> };

export type KingdomDetails = {
  usedLand: number;
  housingCapacity: number;
  housedPeople: number;
  buildingCapacity: Partial<Counts<BuildingName>>;
  buildingOccupancy: Partial<Counts<BuildingName>>;
  foodUpkeep: number;
  defensePower: number;
  counterIntelligence: number;
  nextLandPrice: number;
  forecast: TurnReport;
};

export type GameConfig = {
  buildingPointCosts: Counts<BuildingName>;
  trainingCost: Record<UnitName, { gold: number; tools: number; weapons: number }>;
  common: { maxTurns: number; foodUpkeepPerUnit: number; specialBuildingMaxCount: number; turnsToReinforce: number; soldierUpkeepRatio: number };
  attack: {
    turnsToArrive: number;
    minTargetLandRatio: number;
    unitAttackPower: Partial<Counts<UnitName>>;
    unitDefensePower: Partial<Counts<UnitName>>;
    towerDefenseBonusPerTower: number;
    maxTowerDefenseBonus: number;
    castleDefenseBonusPerCastle: number;
  };
  bots: { maxLand: number; maxStock: Partial<Counts<ResourceName>>; maxOffer: Partial<Counts<MarketResource>> };
  [section: string]: unknown;
};

/** what every action answers: whether it went through, a message for the player, and what it produced, if anything */
export type ActionResult<T = never> = { message: string; success: boolean; data: T | null };

export type BattleReport = {
  attacker: string;
  defender: string;
  attackType: AttackType;
  attackPower: number;
  defensePower: number;
  attackerWon: boolean;
  landCaptured: number;
  plunder: Partial<Counts<ResourceName>>;
  unitsSent: Partial<Counts<UnitName>>;
  attackerLosses: Partial<Counts<UnitName>>;
  defenderLosses: Partial<Counts<UnitName>>;
  attackerArrowLosses: Partial<Counts<UnitName>>;
  defenderArrowLosses: Partial<Counts<UnitName>>;
};

export type SpyReport = {
  targetKingdomName: string;
  success: boolean;
  units: Partial<Counts<UnitName>>;
  buildings: Partial<Counts<BuildingName>>;
  resources: Partial<Counts<ResourceName>>;
};

export type KingdomRank = { rank: number; kingdomName: string; allianceName: string | null; land: number; buildings: number; bot: boolean; protectionTicks: number };
export type Rankings = { kingdoms: KingdomRank[]; alliances: { rank: number; allianceName: string; members: number; land: number }[] };
export type Round = { number: number; ticksElapsed: number; lengthInTicks: number; endedAt: string | null; finalRankings: Rankings | null };
export type MarketOffer = { id: string; sellerName: string; resource: MarketResource; count: number; price: number };
