import type { BuildingName, MarketResource, ResourceName, SpecialBuildingType, UnitName } from "../api/types";

export const art = (name: string): string => `/art/${name}.webp`;

/** what a realm looks like as it grows, from a frontier hamlet to the seat of a mighty realm */
const realmStages = [
  { below: 200, title: "hamlet", art: "realmHamlet" },
  { below: 500, title: "town", art: "realmTown" },
  { below: 1200, title: "city", art: "realmCity" },
  { below: Infinity, title: "citadel", art: "realmCitadel" },
];

export const realmStage = (land: number): { title: string; art: string } => realmStages.find(stage => land < stage.below) ?? realmStages[0];

export const resourceLabel: Record<ResourceName, string> = {
  land: "Land",
  buildingPoints: "Building points",
  unemployed: "Unemployed",
  gold: "Gold",
  iron: "Iron",
  food: "Food",
  tools: "Tools",
  weapons: "Weapons",
  turns: "Turns",
};

export const marketResourceLabel: Record<MarketResource, string> = {
  food: "Food",
  iron: "Iron",
  tools: "Tools",
  weapons: "Weapons",
};

type BuildingLore = { label: string; plural: string; purpose: string };

export const buildingLore: Record<BuildingName, BuildingLore> = {
  house: { label: "House", plural: "Houses", purpose: "Shelters your civilians" },
  goldMine: { label: "Gold mine", plural: "Gold mines", purpose: "Employs gold miners" },
  ironMine: { label: "Iron mine", plural: "Iron mines", purpose: "Employs iron miners" },
  workshop: { label: "Workshop", plural: "Workshops", purpose: "Employs blacksmiths" },
  farm: { label: "Farm", plural: "Farms", purpose: "Employs farmers" },
  market: { label: "Market", plural: "Markets", purpose: "Houses carriers" },
  barracks: { label: "Barracks", plural: "Barracks", purpose: "Quarters infantry, bowmen and cavalry" },
  guardHouse: { label: "Guard house", plural: "Guard houses", purpose: "Quarters the town guard" },
  spyGuild: { label: "Spy guild", plural: "Spy guilds", purpose: "Hides your spies" },
  tower: { label: "Tower", plural: "Towers", purpose: "Strengthens your defense" },
  castle: { label: "Castle", plural: "Castles", purpose: "Strengthens your defense" },
};

type UnitLore = { label: string; plural: string; duty: string };

export const unitLore: Record<UnitName, UnitLore> = {
  goldMiner: { label: "Gold miner", plural: "Gold miners", duty: "Digs gold" },
  ironMiner: { label: "Iron miner", plural: "Iron miners", duty: "Digs iron ore" },
  farmer: { label: "Farmer", plural: "Farmers", duty: "Grows food" },
  blacksmith: { label: "Blacksmith", plural: "Blacksmiths", duty: "Forges tools and weapons from iron" },
  builder: { label: "Builder", plural: "Builders", duty: "Raises buildings each turn" },
  carrier: { label: "Carrier", plural: "Carriers", duty: "Hauls goods to other kingdoms" },
  guard: { label: "Guard", plural: "Guards", duty: "Defends at home and catches spies" },
  spy: { label: "Spy", plural: "Spies", duty: "Scouts other kingdoms" },
  infantry: { label: "Infantry", plural: "Infantry", duty: "Steady in melee" },
  bowman: { label: "Bowman", plural: "Bowmen", duty: "Strikes first with arrows, strong on walls, helpless in melee" },
  cavalry: { label: "Cavalry", plural: "Cavalry", duty: "Hits hardest when attacking" },
};

export const unitGroups: { title: string; units: UnitName[] }[] = [
  { title: "Workers", units: ["goldMiner", "ironMiner", "farmer", "blacksmith", "builder", "carrier"] },
  { title: "Watch", units: ["guard", "spy"] },
  { title: "Army", units: ["infantry", "bowman", "cavalry"] },
];

export const buildingGroups: { title: string; buildings: BuildingName[] }[] = [
  { title: "Homes and work", buildings: ["house", "farm", "goldMine", "ironMine", "workshop", "market"] },
  { title: "Defense and war", buildings: ["barracks", "guardHouse", "spyGuild", "tower", "castle"] },
];

export const specialBuildingLore: Record<SpecialBuildingType, { label: string; effect: string }> = {
  goldShaft: { label: "Gold shaft", effect: "Each level adds 20% gold production" },
  ironShaft: { label: "Iron shaft", effect: "Each level adds 20% iron production" },
  granary: { label: "Granary", effect: "Each level adds 20% food production" },
  forge: { label: "Forge", effect: "Each level adds 20% tools and weapons production" },
  warehouse: { label: "Warehouse", effect: "Each level shields 5% of your stores from plunder" },
};
