
export type KingdomResources = {
    food: number;
    gold: number;
    iron: number;
    land: number;
    tools: number;
    weapons: number;
    buildingPoints: number;
    unemployed: number;
    turns: number;
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

export type KingdomData = {
    name: string;
    resources: KingdomResources;
    buildings: KingdomBuildings;
    units: KingdomUnits;
};

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