import { Building, buildingOccupantsMap, GameConfig, KingdomData, Unit } from "./GameTypes";

export function getBuildingOccupants(building: Building, kingdom: KingdomData): number {
  const units = buildingOccupantsMap[building];
  return units.reduce((sum, unit) => sum + kingdom.units[unit], 0);
}

export function getTotalCapacity(building: Building, kingdom: KingdomData, gameConfig: GameConfig): number {
  return kingdom.buildings[building] * gameConfig.buildingCapacity[building];
}

export function getOpenPositions(unit: Unit, kingdom: KingdomData, gameConfig: GameConfig): number | undefined {
  if (unit === "builder") {
    return undefined;
  }
  const building = getBuildingForUnit(unit);
  return getTotalCapacity(building, kingdom, gameConfig) - getBuildingOccupants(building, kingdom);
}

export function getBuildingForUnit(unit: Unit): Building {
  for (const buildingString in buildingOccupantsMap) {
    const building = buildingString as Building;
    if (buildingOccupantsMap[building].includes(unit)) {
      return building;
    }
  }

  throw new Error(`No building found for unit ${unit}, should never happen`);
}
