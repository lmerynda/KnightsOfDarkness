import type { Player, World } from "./client.ts";
import type { BuildingName, Counts, GameConfig, Kingdom, KingdomDetails, Rankings, SpyReport, UnitName } from "./types.ts";
import { militaryUnits } from "./types.ts";

/** everything a ruler can know at the start of their turn, gathered through the public API */
export type TurnContext = {
  player: Player;
  world: World;
  config: GameConfig;
  kingdom: Kingdom;
  details: KingdomDetails;
  rankings: Rankings;
  turn: number;
  /** a strategy's own notes between turns */
  memory: Record<string, unknown>;
};

export type Strategy = {
  name: string;
  /** share of blacksmith output that becomes weapons */
  weaponsPercentage: number;
  play: (context: TurnContext) => Promise<void>;
};

/** shares of the land each building type should cover */
type Plan = Partial<Counts<BuildingName>>;

const workerHomes: [UnitName, BuildingName][] = [
  ["farmer", "farm"],
  ["goldMiner", "goldMine"],
  ["ironMiner", "ironMine"],
  ["blacksmith", "workshop"],
  ["carrier", "market"],
  ["guard", "guardHouse"],
  ["spy", "spyGuild"],
];

const foodBalance = (details: KingdomDetails): number => (details.forecast.resourcesProduced.food ?? 0) - details.forecast.foodConsumed;

/** buys up to a fifth more land each turn, spending at most half of the treasury, since land is what every plan grows on */
const growLand = async ({ player, kingdom }: TurnContext): Promise<void> => {
  let acres = Math.max(1, Math.floor(kingdom.resources.land / 5));
  while (acres > 0) {
    const quote = await player.landQuote(acres);
    if (quote.amount === acres && quote.cost <= kingdom.resources.gold / 2) {
      await player.buyLand(acres);
      return;
    }
    acres = Math.floor(acres / 2);
  }
};

/** raises the buildings furthest behind their share first; the server stops when points or land run out */
const buildTowardsPlan = async ({ player, kingdom }: TurnContext, plan: Plan, urgent: BuildingName[] = []): Promise<void> => {
  const land = kingdom.resources.land;
  const deficits = (Object.entries(plan) as [BuildingName, number][])
    .map(([building, share]) => ({ building, missing: Math.floor(share * land) - kingdom.buildings[building] }))
    .filter(entry => entry.missing > 0)
    .sort((a, b) => Number(urgent.includes(b.building)) - Number(urgent.includes(a.building)) || b.missing - a.missing);
  for (const { building, missing } of deficits) {
    await player.build({ [building]: missing });
  }
};

/** fills every workplace, farmers first when the kingdom eats more than it grows */
const staffBuildings = async ({ player, details }: TurnContext, units: UnitName[]): Promise<void> => {
  const order = foodBalance(details) < 0 ? ["farmer" as UnitName, ...units.filter(unit => unit !== "farmer")] : units;
  for (const unit of order) {
    const home = workerHomes.find(([worker]) => worker === unit)?.[1];
    if (!home) continue;
    const free = (details.buildingCapacity[home] ?? 0) - (details.buildingOccupancy[home] ?? 0);
    if (free > 0) {
      await player.train({ [unit]: free });
    }
  }
};

/** builders raise one turn's worth of building points each, keep them at about a tenth of the land */
const keepBuilders = async ({ player, kingdom }: TurnContext): Promise<void> => {
  const missing = Math.floor(kingdom.resources.land / 10) - kingdom.units.availableUnits.builder;
  if (missing > 0) {
    await player.train({ builder: missing });
  }
};

const economyPlan: Plan = { house: 0.34, farm: 0.14, goldMine: 0.22, ironMine: 0.06, workshop: 0.08 };

const developEconomy = async (start: TurnContext, plan: Plan, workers: UnitName[]): Promise<void> => {
  await growLand(start);
  const context = { ...start, kingdom: await start.player.kingdom() };
  const urgent: BuildingName[] = foodBalance(context.details) < 0 ? ["farm"] : [];
  if (context.details.housedPeople >= context.details.housingCapacity) urgent.push("house");
  await buildTowardsPlan(context, plan, urgent);
  const refreshed = { ...context, details: await context.player.details(), kingdom: await context.player.kingdom() };
  await staffBuildings(refreshed, workers);
  await keepBuilders(refreshed);
};

const economyWorkers: UnitName[] = ["farmer", "goldMiner", "ironMiner", "blacksmith"];

/** passes every turn without doing anything, the baseline every other strategy should beat */
export const idle: Strategy = {
  name: "idle",
  weaponsPercentage: 0,
  play: async () => undefined,
};

/** grows land and production, never fights */
export const economist: Strategy = {
  name: "economist",
  weaponsPercentage: 0,
  play: context => developEconomy(context, economyPlan, economyWorkers),
};

/** walls, towers and guards on top of a smaller economy, bowmen behind the walls */
export const turtle: Strategy = {
  name: "turtle",
  weaponsPercentage: 50,
  play: async context => {
    const towersForFullBonus = Math.ceil(context.config.attack.maxTowerDefenseBonus / context.config.attack.towerDefenseBonusPerTower);
    const towerShare = Math.min(0.12, towersForFullBonus / Math.max(1, context.kingdom.resources.land));
    // soldiers live in barracks, so recruiting them before the workers frees house space for newcomers
    await fillBarracks(context, { bowman: 1 });
    await developEconomy(context, { ...economyPlan, house: 0.3, goldMine: 0.18, tower: towerShare, guardHouse: 0.04, barracks: 0.04 }, [
      ...economyWorkers,
      "guard",
    ]);
  },
};

const armyPower = (units: Partial<Counts<UnitName>>, config: GameConfig): number =>
  militaryUnits.reduce((sum, unit) => sum + (units[unit] ?? 0) * (config.attack.unitAttackPower[unit] ?? 0), 0);

const estimatedDefense = (report: SpyReport, config: GameConfig): number => {
  const base =
    militaryUnits.reduce((sum, unit) => sum + (report.units[unit] ?? 0) * (config.attack.unitDefensePower[unit] ?? 0), 0) +
    (report.units.guard ?? 0) * (config.attack.unitDefensePower.guard ?? 0);
  const towers = Math.min(config.attack.maxTowerDefenseBonus, (report.buildings.tower ?? 0) * config.attack.towerDefenseBonusPerTower);
  return base * (1 + towers + (report.buildings.castle ?? 0) * config.attack.castleDefenseBonusPerCastle);
};

/** trains soldiers into free barracks places in the given proportions */
const fillBarracks = async ({ player }: TurnContext, mix: Partial<Counts<UnitName>>): Promise<void> => {
  const details = await player.details();
  const free = (details.buildingCapacity.barracks ?? 0) - (details.buildingOccupancy.barracks ?? 0);
  if (free <= 0) return;
  const total = Object.values(mix).reduce((sum, share) => sum + (share ?? 0), 0);
  const order = Object.fromEntries(Object.entries(mix).map(([unit, share]) => [unit, Math.floor((free * (share ?? 0)) / total)]));
  await player.train(order);
};

/** a smaller economy feeding barracks, a few spies for intel */
const raiseArmy = async (context: TurnContext): Promise<void> => {
  // a few spies first, they are cheap and without intel the army never marches
  const missingSpies = 12 - context.kingdom.units.availableUnits.spy;
  if (missingSpies > 0) await context.player.train({ spy: missingSpies });
  await fillBarracks(context, { infantry: 2, cavalry: 1 });
  await developEconomy(context, { ...economyPlan, house: 0.28, goldMine: 0.18, barracks: 0.12, spyGuild: 0.01 }, economyWorkers);
};

/** ranked kingdoms large enough to attack and outside the ruler's alliance, largest first */
const attackableKingdoms = ({ kingdom, config, rankings }: TurnContext): Rankings["kingdoms"] =>
  rankings.kingdoms
    .filter(rank => rank.kingdomName !== kingdom.name && rank.land >= kingdom.resources.land * config.attack.minTargetLandRatio)
    .filter(rank => !kingdom.allianceName || rank.allianceName !== kingdom.allianceName)
    .sort((a, b) => b.land - a.land);

const armyAtHome = (kingdom: Kingdom): Partial<Counts<UnitName>> =>
  Object.fromEntries(militaryUnits.map(unit => [unit, kingdom.units.availableUnits[unit]])) as Partial<Counts<UnitName>>;

/** scouts the largest kingdom it may attack and marches when its army is clearly stronger than the defense its spies saw */
export const warlord = (options: { attackEvery?: number; margin?: number } = {}): Strategy => ({
  name: "warlord",
  weaponsPercentage: 70,
  play: async context => {
    const { player, kingdom, config, turn } = context;
    await raiseArmy(context);

    if (kingdom.ongoingAttacks.length > 0 || turn % (options.attackEvery ?? 6) !== 0) return;
    const target = attackableKingdoms(context)[0];
    if (!target) return;

    const army = armyAtHome(kingdom);
    const power = armyPower(army, config);
    if (power <= 0) return;

    let defense = Infinity;
    if (kingdom.units.availableUnits.spy > 0) {
      const { report } = await player.sendSpies(target.kingdomName, kingdom.units.availableUnits.spy);
      if (report?.success) defense = estimatedDefense(report, config);
    }
    if (power > defense * (options.margin ?? 1.3)) {
      await player.sendAttack(target.kingdomName, "land", army);
    }
  },
});

/** the warlord's army, but it seeks out the kingdom with the most soldiers it may attack and charges whatever the odds */
export const raider: Strategy = {
  name: "raider",
  weaponsPercentage: 70,
  play: async context => {
    const { player, kingdom, config, turn } = context;
    await raiseArmy(context);

    if (kingdom.ongoingAttacks.length > 0 || turn < 12 || turn % 6 !== 0) return;
    const army = armyAtHome(kingdom);
    if (armyPower(army, config) <= 0) return;

    let target: { name: string; soldiers: number } | undefined;
    for (const candidate of attackableKingdoms(context)) {
      const spies = (await player.kingdom()).units.availableUnits.spy;
      if (spies <= 0) break;
      const { report } = await player.sendSpies(candidate.kingdomName, spies);
      if (!report?.success) continue;
      const soldiers = militaryUnits.reduce((sum, unit) => sum + (report.units[unit] ?? 0), 0);
      if (!target || soldiers > target.soldiers) target = { name: candidate.kingdomName, soldiers };
    }
    if (target && target.soldiers > 0) {
      await player.sendAttack(target.name, "land", army);
    }
  },
};

export const strategies: Record<string, () => Strategy> = {
  idle: () => idle,
  economist: () => economist,
  turtle: () => turtle,
  warlord: () => warlord(),
  raider: () => raider,
};
