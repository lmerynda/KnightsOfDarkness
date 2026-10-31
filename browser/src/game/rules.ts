import type { BuildingName, GameConfig, KingdomRank, Round, UnitName } from "../api/types";

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
  return null;
};

export const roundTimeLeft = (round: Round): string => {
  const millis = Math.max(0, round.lengthInTicks - round.ticksElapsed) * round.tickPeriodMillis;
  const hours = Math.floor(millis / 3600000);
  const minutes = Math.floor((millis % 3600000) / 60000);
  if (hours >= 48) return `${Math.floor(hours / 24)} days`;
  if (hours > 0) return `${hours} h ${minutes} min`;
  return `${minutes} min`;
};
