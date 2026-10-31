import type { Player, World } from "./client.ts";
import type { Strategy } from "./strategies.ts";
import type { BattleReport, GameConfig, Kingdom } from "./types.ts";
import { buildingNames, militaryUnits, resourceNames, unitNames } from "./types.ts";

export type Ruler = { player: Player; strategy: Strategy; memory: Record<string, unknown> };

export type Snapshot = {
  turn: number;
  kingdom: string;
  land: number;
  gold: number;
  food: number;
  buildings: number;
  people: number;
  army: number;
  defense: number;
  /** people who left for lack of food in the turn just played */
  starved: number;
};

export type WorldRun = {
  timeline: Snapshot[];
  final: Kingdom[];
  violations: string[];
  battles: BattleReport[];
};

const sum = (values: number[]): number => values.reduce((total, value) => total + value, 0);

/** rules that must hold for every kingdom after every turn, whatever the balance */
export const invariantViolations = (kingdoms: Kingdom[], config: GameConfig, turn: number): string[] => {
  const problems: string[] = [];
  for (const kingdom of kingdoms) {
    const where = `turn ${turn}, ${kingdom.name}`;
    for (const resource of resourceNames) {
      if (kingdom.resources[resource] < 0) problems.push(`${where}: ${resource} is ${kingdom.resources[resource]}`);
    }
    for (const unit of unitNames) {
      if (kingdom.units.availableUnits[unit] < 0) problems.push(`${where}: ${unit} at home is ${kingdom.units.availableUnits[unit]}`);
      if (kingdom.units.mobileUnits[unit] < 0) problems.push(`${where}: ${unit} away is ${kingdom.units.mobileUnits[unit]}`);
    }
    for (const building of buildingNames) {
      if (kingdom.buildings[building] < 0) problems.push(`${where}: ${building} is ${kingdom.buildings[building]}`);
    }
    const used = sum(buildingNames.map(building => kingdom.buildings[building]));
    if (used > kingdom.resources.land) problems.push(`${where}: ${used} buildings on ${kingdom.resources.land} land`);
    if (kingdom.resources.turns > config.common.maxTurns) problems.push(`${where}: ${kingdom.resources.turns} turns stored`);
    const marching = sum(kingdom.ongoingAttacks.flatMap(attack => militaryUnits.map(unit => attack.units[unit] ?? 0)));
    const away = sum(militaryUnits.map(unit => kingdom.units.mobileUnits[unit]));
    if (marching !== away) problems.push(`${where}: ${away} soldiers away but ${marching} in marching armies`);
  }
  return problems;
};

const snapshot = (kingdom: Kingdom, turn: number, defense: number): Snapshot => ({
  turn,
  kingdom: kingdom.name,
  land: kingdom.resources.land,
  gold: kingdom.resources.gold,
  food: kingdom.resources.food,
  buildings: sum(buildingNames.map(building => kingdom.buildings[building])),
  people: sum(unitNames.map(unit => kingdom.units.availableUnits[unit] + kingdom.units.mobileUnits[unit])) + kingdom.resources.unemployed,
  army: sum(militaryUnits.map(unit => kingdom.units.availableUnits[unit] + kingdom.units.mobileUnits[unit])),
  defense,
  starved: kingdom.lastTurnReport.starvedPeople,
});

/**
 * plays the given rulers for a number of turns: each turn the clock gives everyone a turn (bots act too), then every
 * ruler plays its strategy and passes the turn; the world is measured and checked after every turn
 */
export const runWorld = async (world: World, rulers: Ruler[], turns: number, options: { bots?: boolean } = {}): Promise<WorldRun> => {
  const config = await world.config();
  const timeline: Snapshot[] = [];
  const violations: string[] = [];

  for (let turn = 1; turn <= turns; turn++) {
    await world.advance(1, options.bots ?? true);
    const rankings = await world.rankings();
    for (const ruler of rulers) {
      const [kingdom, details] = await Promise.all([ruler.player.kingdom(), ruler.player.details(ruler.strategy.weaponsPercentage)]);
      await ruler.strategy.play({ player: ruler.player, world, config, kingdom, details, rankings, turn, memory: ruler.memory });
      const passed = await ruler.player.passTurn(ruler.strategy.weaponsPercentage);
      if (!passed.success) violations.push(`turn ${turn}, ${ruler.player.name}: could not pass the turn, ${passed.message}`);
    }

    const kingdoms = await world.kingdoms();
    violations.push(...invariantViolations(kingdoms, config, turn));
    for (const ruler of rulers) {
      const kingdom = kingdoms.find(candidate => candidate.name === ruler.player.name);
      if (kingdom) timeline.push(snapshot(kingdom, turn, (await ruler.player.details()).defensePower));
    }
  }

  const battles = new Map<string, BattleReport>();
  for (const ruler of rulers) {
    for (const report of await ruler.player.battleReports()) {
      battles.set(JSON.stringify([report.attacker, report.defender, report.attackPower, report.defensePower]), report);
    }
  }

  return { timeline, final: await world.kingdoms(), violations, battles: [...battles.values()] };
};

export const finalSnapshot = (run: WorldRun, kingdom: string): Snapshot => {
  const entries = run.timeline.filter(entry => entry.kingdom === kingdom);
  const last = entries[entries.length - 1];
  if (!last) throw new Error(`No snapshots for ${kingdom}`);
  return last;
};

export const foundRulers = async (world: World, strategies: Strategy[], prefix = "Sim"): Promise<Ruler[]> => {
  const rulers: Ruler[] = [];
  for (const [index, strategy] of strategies.entries()) {
    const player = await world.foundKingdom(`${prefix} ${strategy.name} ${index + 1}`);
    rulers.push({ player, strategy, memory: {} });
  }
  return rulers;
};
