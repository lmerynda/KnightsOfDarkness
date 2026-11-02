import type { BuildingName, Counts, GameConfig, UnitName } from "../api/types";
import { unitHome } from "./rules";

type Shares = Partial<Counts<BuildingName>>;

/** a share of the land for each building, the mixes the simulated rulers grow on */
export type BuildingPlan = { name: string; label: string; shares: (land: number, config: GameConfig) => Shares };

const economy: Shares = { house: 0.34, farm: 0.14, goldMine: 0.22, ironMine: 0.06, workshop: 0.08 };

export const buildingPlans: BuildingPlan[] = [
  { name: "economy", label: "Economy", shares: () => economy },
  { name: "war", label: "War", shares: () => ({ ...economy, house: 0.28, goldMine: 0.18, barracks: 0.12, spyGuild: 0.01 }) },
  {
    name: "fortress",
    label: "Fortress",
    shares: (land, config) => ({
      ...economy,
      house: 0.3,
      goldMine: 0.18,
      // towers up to the full bonus and no further
      tower: Math.min(0.12, Math.ceil(config.attack.maxTowerDefenseBonus / config.attack.towerDefenseBonusPerTower) / Math.max(1, land)),
      guardHouse: 0.04,
      barracks: 0.04,
    }),
  },
];

/**
 * what a plan still misses on this land, one building at a time into the largest remaining gap, so a short turn of points
 * spreads across the plan; within the building points and free land at hand
 */
export const fillPlan = (
  shares: Shares,
  buildings: Counts<BuildingName>,
  land: number,
  points: number,
  freeLand: number,
  config: GameConfig,
): Partial<Counts<BuildingName>> => {
  const missing = new Map(
    (Object.entries(shares) as [BuildingName, number][])
      .map(([building, share]) => [building, Math.floor(share * land) - buildings[building]] as const)
      .filter(([, gap]) => gap > 0),
  );
  const order: Partial<Counts<BuildingName>> = {};
  let pointsLeft = points;
  let landLeft = freeLand;
  while (landLeft > 0) {
    const next = [...missing.entries()]
      .filter(([building, gap]) => gap > 0 && config.buildingPointCosts[building] <= pointsLeft)
      .sort(
        ([one, oneGap], [other, otherGap]) => otherGap - oneGap || config.buildingPointCosts[one] - config.buildingPointCosts[other],
      )[0];
    if (!next) break;
    const [building, gap] = next;
    order[building] = (order[building] ?? 0) + 1;
    missing.set(building, gap - 1);
    pointsLeft -= config.buildingPointCosts[building];
    landLeft -= 1;
  }
  return order;
};

/** the workers who staff a building; soldiers, guards and spies are a ruler's own choice */
const staff: UnitName[] = ["farmer", "goldMiner", "ironMiner", "blacksmith", "carrier"];

/**
 * workers for every free place in their buildings, within the gold, tools and idle people at hand
 */
export const fillFreePlaces = (
  places: (unit: UnitName) => number,
  resources: { gold: number; tools: number; unemployed: number },
  config: GameConfig,
): Partial<Counts<UnitName>> => {
  const order: Partial<Counts<UnitName>> = {};
  let { gold, tools, unemployed } = resources;
  for (const unit of staff.filter(worker => unitHome[worker])) {
    const cost = config.trainingCost[unit];
    const count = Math.min(
      places(unit),
      unemployed,
      Math.floor(gold / cost.gold),
      cost.tools > 0 ? Math.floor(tools / cost.tools) : Infinity,
    );
    if (count <= 0) continue;
    order[unit] = count;
    gold -= count * cost.gold;
    tools -= count * cost.tools;
    unemployed -= count;
  }
  return order;
};
