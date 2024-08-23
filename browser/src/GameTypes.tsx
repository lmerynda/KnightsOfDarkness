export const buildings = [
  "houses",
  "goldMines",
  "ironMines",
  "workshops",
  "farms",
  "markets",
  "barracks",
  "guardHouses",
  "spyGuilds",
  "towers",
  "castles",
] as const;
export type Building = (typeof buildings)[number];
export type KingdomBuildings = Record<Building, number>;

export const units = [
  "builders",
  "goldMiners",
  "ironMiners",
  "farmers",
  "blacksmiths",
  "carriers",
  "guards",
  "spies",
  "infantry",
  "bowmen",
  "cavalry",
] as const;
export type Unit = (typeof units)[number];
export type KingdomUnits = Record<Unit, number>;

export type Resource = "food" | "gold" | "iron" | "tools" | "weapons" | "land" | "usedLand" | "buildingPoints" | "unemployed" | "turns";
export type KingdomResources = Record<Resource, number>;

export const marketResources = ["food", "iron", "tools", "weapons"] as const;
export type MarketResource = (typeof marketResources)[number];

export const specialBuildingTypes = ["goldShaft", "ironShaft", "forge", "granary"] as const;
export type SpecialBuildingType = (typeof units)[number];
export type SpecialBuilding = {
  id: string;
  buildingType: SpecialBuildingType;
  level: number;
  buildingPointsPut: number;
  buildingPointsRequired: number;
  isMaxLevel: boolean;
};
export type SpecialBuildingBonus = {
  food: number;
  gold: number;
  iron: number;
  tools: number;
  weapons: number;
};

export type KingdomData = {
  name: string;
  resources: KingdomResources;
  buildings: KingdomBuildings;
  units: KingdomUnits;
  marketOffers: MarketOfferData[];
  specialBuildings: SpecialBuilding[];
  lastTurnReport: TurnReport;
};

export type MarketOfferData = {
  id: string;
  sellerName: string;
  resource: string;
  price: number;
  count: number;
};

export type OfferBuyer = {
  count: number;
};

export type TurnReport = {
  foodConsumed: number;
  resourcesProduced: KingdomResources;
  arrivingPeople: number;
  exiledPeople: number;
  kingdomSizeProductionBonus: number;
  nourishmentProductionFactor: number;
  specialBuildingBonus: SpecialBuildingBonus;
};

export type TrainingCost = Record<
  Unit,
  {
    gold: number;
    tools: number;
    weapons: number;
  }
>;

export const productionResources = ["food", "gold", "iron", "tools", "weapons"] as const;
export type ProductionResource = (typeof productionResources)[number];
export type Production = Record<
  Unit,
  {
    rate: number;
    resource: ProductionResource;
  }
>;
export type GameConfig = {
  buildingPointCosts: Record<Building, number>;
  buildingCapacity: Record<Building, number>;
  trainingCost: TrainingCost;
  production: Production;
  specialBuildingCosts: Record<SpecialBuildingType, number>; // not really, cost should be reworked
};

export const buildingOccupantsMap: Record<Building, readonly Unit[]> = {
  goldMines: ["goldMiners"],
  ironMines: ["ironMiners"],
  workshops: ["blacksmiths"],
  farms: ["farmers"],
  markets: ["carriers"],
  barracks: ["infantry", "bowmen", "cavalry"],
  guardHouses: ["guards"],
  spyGuilds: ["spies"],
  towers: ["bowmen"],
  castles: ["infantry", "bowmen", "cavalry"],
  houses: [
    "builders",
    "goldMiners",
    "ironMiners",
    "farmers",
    "blacksmiths",
    "carriers",
    "guards",
    "spies",
    "infantry",
    "bowmen",
    "cavalry",
  ],
} as const;
