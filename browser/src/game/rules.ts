import type { BuildingName, Era, EraEnding, GameConfig, KingdomRank, SpyReport, UnitName } from "../api/types";
import { militaryUnits } from "../api/types";

/** defense estimated from a spy report, using the same rules the battle uses */
export const estimatedDefense = (report: SpyReport, config: GameConfig): number => {
  const units = report.units;
  const base =
    militaryUnits.reduce((sum, unit) => sum + (units[unit] ?? 0) * (config.attack.unitDefensePower[unit] ?? 0), 0) +
    (units.guard ?? 0) * (config.attack.unitDefensePower.guard ?? 0);
  // a castle's places go to infantry first, then cavalry
  const places = (report.buildings.castle ?? 0) * config.buildingCapacity.castle;
  const infantry = Math.min(units.infantry ?? 0, places);
  const cavalry = Math.min(units.cavalry ?? 0, places - infantry);
  const sheltered = infantry * (config.attack.unitDefensePower.infantry ?? 0) + cavalry * (config.attack.unitDefensePower.cavalry ?? 0);
  return base + sheltered * config.attack.castleSoldierBonus;
};

/** the building that houses each kind of unit, builders live in houses */
export const unitHome: Partial<Record<UnitName, BuildingName>> = {
  goldMiner: "goldMine",
  ironMiner: "ironMine",
  farmer: "farm",
  blacksmith: "workshop",
  carrier: "market",
  guard: "guardHouse",
  spy: "spyGuild",
  infantry: "barracks",
  bowman: "barracks",
  cavalry: "barracks",
};

/**
 * why a kingdom cannot be attacked, the server enforces the same rules
 */
export const targetProblem = (
  target: KingdomRank,
  ownName: string,
  ownAlliance: string | null,
  ownLand: number,
  config: GameConfig,
): string | null => {
  if (target.kingdomName === ownName) return "your own kingdom";
  if (ownAlliance && target.allianceName === ownAlliance) return "an ally";
  if (target.land < ownLand * config.attack.minTargetLandRatio) return "too small to attack";
  if (target.protectionTicks > 0) return `protected for ${target.protectionTicks} more ${target.protectionTicks === 1 ? "turn" : "turns"}`;
  return null;
};

/** the most land a crushing land victory takes: the full share of a target at least our size, less of a smaller one */
export const landAtStake = (targetLand: number, ownLand: number, config: GameConfig): { acres: number; sizeFactor: number } => {
  const sizeFactor = Math.min(1, targetLand / Math.max(1, ownLand)) ** config.attack.smallerTargetCaptureExponent;
  return { acres: Math.max(1, Math.floor(targetLand * config.attack.maxLandCaptureRatio * sizeFactor)), sizeFactor };
};

/** how the hall of fame tells the way an era ended */
export const eraEndings: Record<EraEnding, string> = { clock: "when the clock ran out" };

export const eraTimeLeft = (era: Era): string => {
  const millis = Math.max(0, era.maxLengthInTicks - era.ticksElapsed) * era.tickPeriodMillis;
  const hours = Math.floor(millis / 3600000);
  const minutes = Math.floor((millis % 3600000) / 60000);
  if (hours >= 48) return `${Math.floor(hours / 24)} days`;
  if (hours > 0) return `${hours} h ${minutes} min`;
  return `${minutes} min`;
};
