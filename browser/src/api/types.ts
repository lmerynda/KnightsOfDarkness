export const resourceNames = ["land", "buildingPoints", "unemployed", "gold", "iron", "food", "tools", "weapons", "turns"] as const;
export type ResourceName = (typeof resourceNames)[number];

export const buildingNames = [
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
] as const;
export type BuildingName = (typeof buildingNames)[number];

export const unitNames = [
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
] as const;
export type UnitName = (typeof unitNames)[number];

export const militaryUnits = ["infantry", "bowman", "cavalry"] as const;
export type MilitaryUnit = (typeof militaryUnits)[number];

export const marketResources = ["food", "iron", "tools", "weapons"] as const;
export type MarketResource = (typeof marketResources)[number];

export const specialBuildingTypes = ["goldShaft", "ironShaft", "granary", "forge", "warehouse"] as const;
export type SpecialBuildingType = (typeof specialBuildingTypes)[number];

export type AttackType = "land" | "economy";

export type Counts<K extends string> = Record<K, number>;

export type TurnReport = {
  foodConsumed: number;
  resourcesProduced: Partial<Counts<ResourceName>>;
  arrivingPeople: number;
  exiledPeople: number;
  starvedPeople: number;
  weaponsProductionPercentage: number;
  kingdomSizeProductionBonus: number;
  nourishmentProductionFactor: number;
  specialBuildingBonus: Partial<Counts<ResourceName>>;
  professionalsLeaving: Partial<Counts<UnitName>>;
};

export type MarketOffer = { id: string; sellerName: string; resource: MarketResource; count: number; price: number };

export type SpecialBuilding = {
  id: string;
  buildingType: SpecialBuildingType;
  level: number;
  buildingPointsPut: number;
  buildingPointsRequired: number;
  isMaxLevel: boolean;
};

export type CarriersOnTheMove = {
  id: string;
  from: string;
  to: string;
  turnsLeft: number;
  carriersCount: number;
  resource: MarketResource;
  amount: number;
};

export type OngoingAttack = {
  id: string;
  from: string;
  to: string;
  turnsLeft: number;
  attackType: AttackType;
  units: Partial<Counts<UnitName>>;
};

export type Kingdom = {
  name: string;
  resources: Counts<ResourceName>;
  buildings: Counts<BuildingName>;
  units: { availableUnits: Counts<UnitName>; mobileUnits: Counts<UnitName> };
  marketOffers: MarketOffer[];
  specialBuildings: SpecialBuilding[];
  lastTurnReport: TurnReport;
  carriersOnTheMove: CarriersOnTheMove[];
  ongoingAttacks: OngoingAttack[];
  allianceName: string | null;
};

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
  buildingCapacity: Counts<Exclude<BuildingName, "tower" | "castle">>;
  trainingCost: Record<UnitName, { gold: number; tools: number; weapons: number }>;
  production: Partial<Record<UnitName, { rate: number; resource: ResourceName }>>;
  specialBuildingCosts: Counts<SpecialBuildingType>;
  market: { visibleMarketOffersCap: number; maxKingdomOffers: number; minPrice: number };
  common: {
    specialBuildingPerLevelProductionBonus: number;
    maxTurns: number;
    foodUpkeepPerUnit: number;
    specialBuildingMaxCount: number;
    specialBuildingMaxLevel: number;
    turnsToDeliverResources: number;
    allianceMaxMembers: number;
    newTurnPeriodicity: number;
  };
  carrierCapacity: Counts<MarketResource>;
  attack: {
    turnsToArrive: number;
    minTargetLandRatio: number;
    maxLandCaptureRatio: number;
    maxPlunderRatio: number;
    towerDefenseBonusPerTower: number;
    maxTowerDefenseBonus: number;
    castleDefenseBonusPerCastle: number;
    warehousePlunderProtectionPerLevel: number;
    unitAttackPower: Partial<Counts<UnitName>>;
    unitDefensePower: Partial<Counts<UnitName>>;
  };
  espionage: { counterIntelligencePerSpy: number; counterIntelligencePerGuard: number; failedMissionLossRatio: number };
};

export type ActionResult = { message: string; success: boolean };

export type PassTurnResult = ActionResult & { turnReport: TurnReport | null };

export type LandTransaction = { amount: number; cost: number };

export type BattleReport = {
  id: string;
  date: string;
  attacker: string;
  defender: string;
  attackType: AttackType;
  unitsSent: Partial<Counts<UnitName>>;
  attackerLosses: Partial<Counts<UnitName>>;
  defenderLosses: Partial<Counts<UnitName>>;
  attackPower: number;
  defensePower: number;
  attackerWon: boolean;
  landCaptured: number;
  plunder: Partial<Counts<ResourceName>>;
};

export type SpyReport = {
  id: string;
  date: string;
  kingdomName: string;
  targetKingdomName: string;
  success: boolean;
  spiesSent: number;
  spiesLost: number;
  resources: Partial<Counts<ResourceName>>;
  buildings: Partial<Counts<BuildingName>>;
  units: Partial<Counts<UnitName>>;
};

export type Notification = { id: string; kingdomName: string; message: string; date: string; isRead: boolean };

export type Letter = { id: string; sender: string; recipient: string; body: string; date: string; isRead: boolean; toAlliance: boolean };

/** bots have rank 0, they trade and can be attacked but do not compete */
export type KingdomRank = { rank: number; kingdomName: string; allianceName: string | null; land: number; buildings: number; bot: boolean };
export type AllianceRank = { rank: number; allianceName: string; members: number; land: number };
export type Rankings = { kingdoms: KingdomRank[]; alliances: AllianceRank[] };

export type Round = {
  number: number;
  startedAt: string;
  endedAt: string | null;
  ticksElapsed: number;
  lengthInTicks: number;
  tickPeriodMillis: number;
  finalRankings: Rankings | null;
};

export type Alliance = { name: string; emperor: string; members: string[] };
export type AllianceSummary = { name: string; emperor: string };
export type AllianceInvitation = { id: string; kingdomName: string; allianceName: string };
