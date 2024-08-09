
export type KingdomResources = {
    food: number;
    gold: number;
    iron: number;
    usedLand: number;
    land: number;
    tools: number;
    weapons: number;
    buildingPoints: number;
    unemployed: number;
    turns: number;
}

export enum MarketResource {
    food = "food",
    iron = "iron",
    tools = "tools",
    weapons = "weapons",
}

export type MarketResourcesMap = {
    [key in (MarketResource)]: number;
}

export type KingdomBuildings = {
    houses: number,
    goldMines: number,
    ironMines: number,
    workshops: number,
    farms: number,
    markets: number,
    barracks: number,
    guardHouses: number,
    spyGuilds: number,
    towers: number,
    castles: number
}

export type KingdomUnits = {
    goldMiners: number,
    ironMiners: number,
    farmers: number,
    blacksmiths: number,
    builders: number,
    carriers: number,
    guards: number,
    spies: number,
    infantry: number,
    bowmen: number,
    cavalry: number
}

export type SpecialBuilding = {
    id: string,
    buildingType: string,
    level: number,
    buildingPointsPut: number,
    buildingPointsRequired: number,
    isMaxLevel: boolean
}

export type KingdomData = {
    name: string;
    resources: KingdomResources;
    buildings: KingdomBuildings;
    units: KingdomUnits;
    marketOffers: MarketData[];
    specialBuildings: SpecialBuilding[];
    lastTurnReport: TurnReport;
};

export type MarketData = {
    id: string;
    sellerName: string;
    resource: string;
    price: number;
    count: number;
};

export type OfferBuyer = {
    count: number;
}

export const buildingList = [
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
    "castles"
];

export const unitList = [
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
    "cavalry"
];

export const specialBuildingList = [
    "goldShaft",
    "granary"
] as const;

export type SpecialBuildingBonus = {
    food: number;
    gold: number;
    iron: number;
    tools: number;
    weapons: number;
}

export type TurnReport = {
    foodConsumed: number;
    resourcesProduced: KingdomResources;
    arrivingPeople: number;
    exiledPeople: number;
    kingdomSizeProductionBonus: number;
    nourishmentProductionFactor: number;
    specialBuildingBonus: SpecialBuildingBonus;
}