import type { Monument, MonumentLedger } from "../api/types.ts";
import { militaryUnits, type Counts, type UnitName } from "../api/types.ts";
import { armyPower, type TurnContext } from "./strategies.ts";

/**
 * What one coalition does about monuments, standing in for players who agree on it by letter. A builder founds its
 * monument once its land leads the other alliances' average by the given share, and then every member pays what it can
 * spare towards the next stage and two days of upkeep, and keeps a share of its soldiers standing guard there. A rival's
 * warriors march on any other alliance's monument from the given stage on, whenever their army beats the garrison as
 * everyone counts it, on attack turns shifted by the coalition's offset so that rival alliances land a few turns apart.
 */
export type MonumentPolicy = {
  alliance: string;
  /** the emperor, who founds the monument in its own kingdom */
  emperor: string;
  /** found once the alliance's land is this share above the other alliances' average; null never founds */
  foundAtLead: number | null;
  /** found no earlier than this turn */
  foundFromTurn: number;
  /** the share of what a member can spare that it pays in each turn */
  payShare: number;
  /** the share of each member's soldiers kept at the monument */
  guardShare: number;
  /** assault other alliances' monuments from this many stages on; null never assaults */
  assaultFromStage: number | null;
  /** attack turns of this coalition's assaults fall this many turns after the others' */
  assaultOffset: number;
  /** rivals banded together: they share attack turns and march when all their armies together beat the garrison */
  band?: Band;
  /** what its warriors learned from their own assaults, when they do not share a band's */
  lessons: Lessons;
  /** what each member could march with, as it last said */
  armies: Map<string, number>;
  /** the assault the coalition set off this turn, which its other members join */
  assault?: { turn: number; lead: Promise<{ seat: string; id: string } | null> };
  /** what happened, for the report */
  events: MonumentEvent[];
  /** the monuments as the world saw them this turn, fetched once a turn for the whole coalition */
  seen?: { turn: number; monuments: Monument[] };
};

export type MonumentEvent = { turn: number; alliance: string; what: string };

/** what each banded warrior can march with, as it last told the others, and what the band learned together */
export type Band = { armies: Map<string, { alliance: string; power: number }>; lessons: Lessons };

/**
 * how strong armies must be against each alliance's monument, as a share of its garrison counted at the best defense any
 * soldier has, and the battle reports already read: archers loose first and a repelled army is crushed, so no count of
 * soldiers says what an assault needs, and rivals learn it from their reports as players do
 */
export type Lessons = { bars: Map<string, number>; read: Set<string> };

export const freshLessons = (): Lessons => ({ bars: new Map(), read: new Set() });

/**
 * the share of its land each member of a builder turns into barracks once the monument stands, for soldiers to guard it:
 * a lead in land is gold, not soldiers, and a garrison of the members' 1% home garrisons fell to rivals at any lead
 */
const guardBarracksShare = 0.02;

/** the share of its land a member keeps in barracks for the monument's guard, as of the monuments it last saw */
export const monumentBarracks = (policy: MonumentPolicy | undefined): number =>
  policy?.seen?.monuments.some(monument => monument.allianceName === policy.alliance && monument.stages > 0) ? guardBarracksShare : 0;

/** upkeep a builder keeps in the store beyond the next stage's cost: two days of hourly turns */
const upkeepBufferTurns = 48;
/**
 * the share of the garrison counted at the best defense that armies must match before rivals know better, about one
 * soldier for each soldier on guard; a repelled assault raises it and a won one lowers it
 */
const firstBar = 0.5;
const barAfterDefeat = 1.2;
const barAfterVictory = 0.9;
/**
 * the most a bar rises to, every soldier on guard at the best defense: rivals who raised it with every repelled wave until
 * their armies could never reach it stopped marching for good, where players rebuild and come back
 */
const highestBar = 1;
const attackEvery = 6;

const monuments = async (context: TurnContext, policy: MonumentPolicy): Promise<Monument[]> => {
  if (policy.seen?.turn !== context.turn) policy.seen = { turn: context.turn, monuments: await context.world.monuments() };
  return policy.seen.monuments;
};

const ours = async (context: TurnContext, policy: MonumentPolicy): Promise<Monument | undefined> =>
  (await monuments(context, policy)).find(monument => monument.allianceName === policy.alliance);

/** the alliance's land against the average of the other alliances', 0.2 for a lead of 20% */
export const leadOf = (context: TurnContext, alliance: string): number => {
  const alliances = context.rankings.alliances;
  const own = alliances.find(entry => entry.allianceName === alliance);
  const others = alliances.filter(entry => entry.allianceName !== alliance);
  if (!own || others.length === 0) return 0;
  return own.land / (others.reduce((sum, entry) => sum + entry.land, 0) / others.length) - 1;
};

/** the emperor founds the monument in its own kingdom once the alliance leads by the policy's share */
export const foundWhenAhead = async (context: TurnContext, policy: MonumentPolicy): Promise<void> => {
  if (policy.foundAtLead === null || context.turn < policy.foundFromTurn || context.kingdom.name !== policy.emperor) return;
  if (await ours(context, policy)) return;
  const lead = leadOf(context, policy.alliance);
  if (lead < policy.foundAtLead) return;
  const founded = await context.player.foundMonument(context.kingdom.name);
  if (!founded.success) return;
  policy.events.push({ turn: context.turn, alliance: policy.alliance, what: `founded at a lead of ${Math.round(lead * 100)}%` });
  policy.seen = undefined;
};

/**
 * pays the share of what the member can spare, as far as the store still lacks it: gold from the treasury, and iron,
 * tools and food beyond what the member wants for itself
 */
export const payIn = async (context: TurnContext, policy: MonumentPolicy, want: Partial<Record<"food" | "iron" | "tools", number>>): Promise<void> => {
  if (!(await ours(context, policy))) return;
  let ledger: MonumentLedger;
  try {
    ledger = await context.player.monument();
  } catch {
    return;
  }
  const kingdom = await context.player.kingdom();
  const goods = ["gold", "iron", "tools", "food"] as const;
  const payment = Object.fromEntries(
    goods.map(good => {
      const lacking = (ledger.nextStageCost?.[good] ?? 0) + upkeepBufferTurns * ledger.upkeep[good] - ledger.store[good];
      const spare = good === "gold" ? kingdom.resources.gold : kingdom.resources[good] - (want[good] ?? 0);
      return [good, Math.max(0, Math.min(Math.floor(spare * policy.payShare), lacking, 2_000_000_000))];
    }),
  ) as Record<(typeof goods)[number], number>;
  if (goods.every(good => payment[good] <= 0)) return;
  await context.player.payIntoMonument(payment);
};

/** keeps the policy's share of the member's soldiers at the monument, sending more from home when it falls short */
export const standGuard = async (context: TurnContext, policy: MonumentPolicy): Promise<void> => {
  if (policy.guardShare <= 0 || context.turn % attackEvery !== 1) return;
  const monument = await ours(context, policy);
  if (!monument || monument.stages <= 0) return;
  const kingdom = await context.player.kingdom();
  const posted = kingdom.reinforcements.filter(guard => guard.atMonument);
  const units: Partial<Counts<UnitName>> = {};
  for (const unit of militaryUnits) {
    const away = posted.reduce((sum, guard) => sum + (guard.units[unit] ?? 0), 0);
    const total = kingdom.units.availableUnits[unit] + kingdom.units.mobileUnits[unit];
    const send = Math.min(kingdom.units.availableUnits[unit], Math.floor(total * policy.guardShare) - away);
    if (send > 0) units[unit] = send;
  }
  if (Object.keys(units).length === 0) return;
  await context.player.sendToMonument(units);
};

/** raises the bar for a monument after each of the warrior's repelled assaults on it, and lowers it after each won one */
const learn = async (context: TurnContext, lessons: Lessons, known: Monument[]): Promise<void> => {
  for (const report of await context.player.battleReports()) {
    if (report.attackType !== "monument" || report.attacker !== context.kingdom.name || lessons.read.has(report.id)) continue;
    lessons.read.add(report.id);
    const alliance = known.find(monument => monument.seat === report.defender)?.allianceName;
    if (!alliance) continue;
    lessons.bars.set(alliance, Math.min(highestBar, (lessons.bars.get(alliance) ?? firstBar) * (report.attackerWon ? barAfterVictory : barAfterDefeat)));
  }
};

/**
 * on its coalition's assault turns every member marches on the most advanced rival monument from the policy's stage on,
 * as one army: the first member to play sets off the assault and the others join its strike. It marches if the armies
 * match the bar learned for that monument against the garrison everyone sees, counting the whole coalition, or in a band
 * every banded coalition outside the monument's alliance, since they land on the same turn
 *
 * @return whether the member's army marched
 */
export const assaultRivals = async (context: TurnContext, policy: MonumentPolicy, army: Partial<Counts<UnitName>>): Promise<boolean> => {
  if (policy.assaultFromStage === null) return false;
  const power = armyPower(army, context.config);
  policy.armies.set(context.kingdom.name, power);
  policy.band?.armies.set(context.kingdom.name, { alliance: policy.alliance, power });
  if ((context.turn + policy.assaultOffset) % attackEvery !== 0) return false;
  if (power <= 0 || context.kingdom.ongoingAttacks.length > 0) return false;
  if (policy.assault?.turn !== context.turn) {
    // decided before anything is awaited, so that members playing at the same moment join rather than lead
    const lead = leadAssault(context, policy, army, power);
    policy.assault = { turn: context.turn, lead };
    return (await lead) !== null;
  }
  const lead = await policy.assault.lead;
  if (!lead) return false;
  const joined = await context.player.sendAttack(lead.seat, "monument", army, lead.id);
  return joined.success;
};

/** sets off the coalition's assault, if the armies it can count match the bar */
const leadAssault = async (context: TurnContext, policy: MonumentPolicy, army: Partial<Counts<UnitName>>, power: number): Promise<{ seat: string; id: string } | null> => {
  const fromStage = policy.assaultFromStage ?? Infinity;
  const targets = (await monuments(context, policy))
    .filter(monument => monument.allianceName !== policy.alliance && monument.allianceName !== context.kingdom.allianceName)
    .filter(monument => monument.complete || monument.stages >= fromStage)
    .sort((one, other) => other.stages - one.stages);
  const target = targets[0];
  if (!target) return null;
  const lessons = policy.band?.lessons ?? policy.lessons;
  await learn(context, lessons, await monuments(context, policy));
  const bar = lessons.bars.get(target.allianceName) ?? firstBar;
  const bestDefense = Math.max(...militaryUnits.map(unit => context.config.attack.unitDefensePower[unit] ?? 0));
  const together = policy.band
    ? [...policy.band.armies.values()].filter(entry => entry.alliance !== target.allianceName).reduce((sum, entry) => sum + entry.power, 0)
    : [...policy.armies.values()].reduce((sum, each) => sum + each, 0);
  if (together <= target.garrison * bestDefense * bar) return null;
  const sent = await context.player.sendAttack(target.seat, "monument", army);
  if (!sent.success) return null;
  const id = (await context.player.kingdom()).ongoingAttacks.find(attack => attack.to === target.seat && attack.attackType === "monument")?.id;
  if (!id) return null;
  policy.events.push({ turn: context.turn, alliance: policy.alliance, what: `${context.kingdom.name} led an assault on the monument of ${target.allianceName} at stage ${target.stages} with power ${power} of ${together} counted against about ${target.garrison} on guard` });
  return { seat: target.seat, id };
};
