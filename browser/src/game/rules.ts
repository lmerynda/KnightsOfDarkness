import type {
  AttackType,
  BattleReport,
  BuildingName,
  Era,
  EraEnding,
  GameConfig,
  KingdomRank,
  Monument,
  SpyReport,
  UnitName,
} from "../api/types";
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

/**
 * the last battle at a kingdom that met allied soldiers and more defense than our spies counted, for spies see only who is
 * at home; the battles may be our own and our allies', in any order
 */
export const alliesUnseen = (target: string, estimate: number, battles: BattleReport[]): BattleReport | null => {
  const last = battles
    .filter(battle => battle.defender === target && battle.attackType !== "monument")
    .sort((one, other) => other.date.localeCompare(one.date))[0];
  const fellThere = last ? militaryUnits.some(unit => (last.alliedLosses[unit] ?? 0) > 0) : false;
  return last && fellThere && last.defensePower > estimate ? last : null;
};

/** battles one after another between the same two, fought for the same thing and ending the same way, the newest first */
export const inARow = (battles: BattleReport[]): BattleReport[][] => {
  const runs: BattleReport[][] = [];
  for (const battle of battles) {
    const run = runs[runs.length - 1] as BattleReport[] | undefined;
    const same =
      run?.[0].attacker === battle.attacker &&
      run[0].defender === battle.defender &&
      run[0].attackType === battle.attackType &&
      run[0].attackerWon === battle.attackerWon;
    if (same) run.push(battle);
    else runs.push([battle]);
  }
  return runs;
};

/** what an army marches for, to follow "to" */
export const attackGoal: Record<AttackType, string> = { land: "take land", economy: "plunder", monument: "assault the monument" };

/** who guards a monument, as everyone's scouts count them */
export const monumentGarrison = (monument: Monument): string =>
  monument.garrison > 0
    ? `about ${monument.garrison.toLocaleString("en-US")} ${monument.garrison === 1 ? "soldier" : "soldiers"} on guard`
    : "no garrison";

/** how far a monument stands, in everyone's words */
export const monumentStanding = (monument: Monument): string =>
  monument.complete
    ? `Stands complete, ${monument.ticksToWin === 1 ? "1 turn" : `${monument.ticksToWin ?? 0} turns`} from winning the era`
    : `Stage ${monument.stages} of ${monument.totalStages}`;

/** how the hall of fame tells the way an era ended */
export const eraEndings: Record<EraEnding, string> = { clock: "when the clock ran out", monument: "by completing its monument" };

/** what ends the era first: a complete monument whose hold runs out, or else the clock */
export const eraOutlook = (era: Era, monuments: Monument[]): string => {
  const first = monuments.filter(monument => monument.complete).sort((one, other) => (one.ticksToWin ?? 0) - (other.ticksToWin ?? 0))[0];
  if (!first) return `The clock ends it in about ${eraTimeLeft(era)}.`;
  const turns = first.ticksToWin === 1 ? "1 turn" : `${first.ticksToWin ?? 0} turns`;
  return `The monument of ${first.allianceName} wins it in ${turns} unless an army knocks it down; otherwise the clock ends it in about ${eraTimeLeft(era)}.`;
};

export const eraTimeLeft = (era: Era): string => {
  const millis = Math.max(0, era.maxLengthInTicks - era.ticksElapsed) * era.tickPeriodMillis;
  const hours = Math.floor(millis / 3600000);
  const minutes = Math.floor((millis % 3600000) / 60000);
  if (hours >= 48) return `${Math.floor(hours / 24)} days`;
  if (hours > 0) return `${hours} h ${minutes} min`;
  return `${minutes} min`;
};
