import { Building, buildingOccupantsMap, GameConfig, KingdomData } from "./GameTypes";

export function getBuildingOccupants(building: Building, kingdom: KingdomData): number {
    const units = buildingOccupantsMap[building];
    return units.reduce((sum, unit) => sum + kingdom.units[unit], 0);
}

export function getTotalCapacity(building: Building, kingdom: KingdomData, gameConfig: GameConfig): number {
    return kingdom.buildings[building] * gameConfig.buildingCapacity[building];
}