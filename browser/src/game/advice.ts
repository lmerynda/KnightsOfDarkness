import type {
  BuildingName,
  Counts,
  GameConfig,
  Kingdom,
  KingdomDetails,
  MarketOffer,
  Monument,
  MonumentLedger,
  TurnReport,
  UnitName,
  WarRoom,
} from "../api/types";
import { militaryUnits } from "../api/types";
import { formatNumber, plural, signed } from "../ui/format";
import { art, buildingLore, unitLore } from "./lore";
import { buildingPlans, fillFreePlaces, fillPlan, rememberedPlan } from "./plans";
import { monumentGarrison, unitHome } from "./rules";

/** what one click carries out, an order the ruler could have written by hand */
export type Chore = { kind: "build"; order: Partial<Counts<BuildingName>> } | { kind: "train"; order: Partial<Counts<UnitName>> };

export type Counsel = {
  /** urgent counsel costs the ruler people or resources next turn if ignored */
  urgent: boolean;
  text: string;
  /** what it means, or what the one click would do */
  detail?: string;
  /** a painting beside the line, for what the ruler builds or trains */
  picture?: string;
  chore?: { label: string; chore: Chore };
  link?: { to: string; label: string };
};

/** what a ruler in an alliance knows about the coalition: who the allies are, the war room, the allies' market offers and the monument */
export type Coalition = { allies: string[]; warRoom: WarRoom; offers: MarketOffer[]; monument: MonumentLedger | null };

const theirTurns = (turns: number): string => plural(turns, "of their turns", "of their turns");

const inWords = <Name extends string>(
  order: Partial<Record<Name, number>>,
  lore: Record<Name, { label: string; plural: string }>,
): string =>
  (Object.entries(order) as [Name, number][])
    .filter(([, count]) => count > 0)
    .sort(([, one], [, other]) => other - one)
    .map(([name, count]) => `${formatNumber(count)} ${(count === 1 ? lore[name].label : lore[name].plural).toLowerCase()}`)
    .join(", ");

const countOf = (order: Partial<Record<string, number>>): number =>
  Object.values(order).reduce<number>((sum, count) => sum + (count ?? 0), 0);

/**
 * the work of every turn, each with its order worked out so that one click carries it out: what was already there to spend
 * and would otherwise go to waste; "Choose" leads to the page for a ruler who wants it otherwise
 */
const chores = (kingdom: Kingdom, details: KingdomDetails, config: GameConfig): Counsel[] => {
  const lines: Counsel[] = [];
  const resources = kingdom.resources;
  const freeLand = resources.land - details.usedLand;
  const points = resources.buildingPoints;
  const cheapest = Math.min(...Object.values(config.buildingPointCosts));
  if (points >= cheapest) {
    const lost = `${formatNumber(points)} building points are lost if the turn passes first.`;
    if (freeLand > 0) {
      const plan = buildingPlans.find(candidate => candidate.name === rememberedPlan()) ?? buildingPlans[0];
      const order = fillPlan(plan.shares(config), kingdom.buildings, resources.land, points, freeLand, config);
      const count = countOf(order);
      const first = (Object.keys(order) as BuildingName[])[0];
      lines.push({
        urgent: false,
        text: `${plural(freeLand, "acre stands", "acres stand")} empty`,
        detail:
          count > 0 ? `${lost} The ${plan.label} plan raises ${inWords(order, buildingLore)}.` : `${lost} The ${plan.label} plan is met.`,
        picture: art(first ?? "house"),
        chore: count > 0 ? { label: `Raise ${plural(count, "building", "buildings")}`, chore: { kind: "build", order } } : undefined,
        link: { to: "/kingdom/build", label: "Choose" },
      });
    } else {
      lines.push({
        urgent: false,
        text: `${formatNumber(points)} building points have no land to stand on`,
        detail: "They are lost when the turn passes. Buy land, or put them into a great work.",
        picture: art("house"),
        link: { to: "/kingdom/build", label: "Spend them" },
      });
    }
  }
  if (resources.unemployed > 0) {
    const places = (unit: UnitName): number => {
      const home = unitHome[unit];
      return home ? (details.buildingCapacity[home] ?? 0) - (details.buildingOccupancy[home] ?? 0) : 0;
    };
    const order = fillFreePlaces(places, resources, config);
    const count = countOf(order);
    const first = (Object.keys(order) as UnitName[])[0];
    lines.push({
      urgent: false,
      text: `${plural(resources.unemployed, "person stands", "people stand")} idle`,
      detail: count > 0 ? `There is work and gold for ${inWords(order, unitLore)}.` : "There is no free workplace they can be paid for.",
      picture: art(first ?? "farmer"),
      chore: count > 0 ? { label: `Train ${plural(count, "worker", "workers")}`, chore: { kind: "train", order } } : undefined,
      link: { to: "/kingdom/train", label: "Choose" },
    });
  }
  return lines;
};

/**
 * what waits on the ruler this turn, most pressing first: only what costs something or will not wait, never how the game
 * works; every line is read from the kingdom and the server's forecast of the next turn
 */
export const counsel = (
  kingdom: Kingdom,
  details: KingdomDetails,
  config: GameConfig,
  coalition?: Coalition,
  monuments: Monument[] = [],
  limit = 6,
): Counsel[] => {
  const lines: Counsel[] = [];
  const resources = kingdom.resources;
  const forecast = details.forecast;
  const foodBalance = (forecast.resourcesProduced.food ?? 0) - forecast.foodConsumed;
  const goldBalance = (forecast.resourcesProduced.gold ?? 0) - forecast.goldUpkeep;
  const goldFrom = kingdom.allianceName
    ? { to: "/kingdom/alliance", label: "Ask allies for gold" }
    : { to: "/kingdom/train", label: "Train gold miners" };

  // a rival's monument that stands complete ends the era for everyone unless an army knocks it down
  for (const rival of monuments.filter(monument => monument.complete && monument.allianceName !== kingdom.allianceName)) {
    const garrison = monumentGarrison(rival);
    lines.push({
      urgent: true,
      text: `The monument of ${rival.allianceName} wins the era in ${plural(rival.ticksToWin ?? 0, "turn", "turns")}`,
      detail: `Only a won assault knocks it down and makes it stand the whole hold again. ${garrison[0].toUpperCase()}${garrison.slice(1)} at ${rival.seat}.`,
      link: { to: `/kingdom/war?assault=${encodeURIComponent(rival.seat)}`, label: "March on it" },
    });
  }

  for (const army of kingdom.incoming) {
    lines.push(
      army.attackType === "monument"
        ? {
            urgent: true,
            text: `${army.from} marches on our monument: about ${formatNumber(army.soldiers)} soldiers, landing in ${theirTurns(army.turnsLeft)}.`,
            link: { to: "/kingdom/alliance", label: "Send soldiers" },
          }
        : {
            urgent: true,
            text: `${army.from} marches on us: about ${formatNumber(army.soldiers)} soldiers, landing in ${theirTurns(army.turnsLeft)}. Our defense: ${formatNumber(details.defensePower)}.`,
            link: { to: "/kingdom/war", label: "Prepare" },
          },
    );
  }
  if (forecast.starvedPeople > 0) {
    lines.push({
      urgent: true,
      text: `${plural(forecast.starvedPeople, "person leaves", "people leave")} hungry next turn.`,
      link: { to: "/kingdom/train", label: "Train farmers" },
    });
  } else if (foodBalance < 0 && resources.food < -foodBalance * 5) {
    lines.push({
      urgent: true,
      text: `The granaries empty in ${plural(Math.floor(resources.food / -foodBalance), "turn", "turns")}.`,
      link: { to: "/kingdom/market", label: "Buy food" },
    });
  }
  if (forecast.desertedSoldiers > 0) {
    lines.push({
      urgent: true,
      text: `The treasury falls short: ${plural(forecast.desertedSoldiers, "soldier deserts", "soldiers desert")} next turn.`,
      link: goldFrom,
    });
  } else if (goldBalance < 0 && resources.gold < -goldBalance * 5) {
    lines.push({
      urgent: true,
      text: `Soldiers' pay outruns the mines: the treasury empties in ${plural(Math.floor(resources.gold / -goldBalance), "turn", "turns")}.`,
      link: goldFrom,
    });
  }
  if (forecast.exiledPeople > 0) {
    lines.push({
      urgent: true,
      text: `${plural(forecast.exiledPeople, "person has", "people have")} no house and leave next turn.`,
      link: { to: "/kingdom/build", label: "Build houses" },
    });
  }
  if (Object.values(forecast.professionalsLeaving).some(count => (count ?? 0) > 0)) {
    lines.push({
      urgent: true,
      text: "Workers without room quit next turn.",
      link: { to: "/kingdom/build", label: "Build" },
    });
  }
  const monument = coalition?.monument;
  if (monument?.upkeepUnpaid) {
    lines.push({
      urgent: true,
      text: `Our monument is crumbling: its store cannot pay the upkeep of ${formatNumber(monument.upkeep.gold)} gold and ${formatNumber(monument.upkeep.food)} food a turn.`,
      link: { to: "/kingdom/alliance", label: "Pay in" },
    });
  }
  if (monument?.monument.complete) {
    lines.push({
      urgent: false,
      text: `Our monument wins the era in ${plural(monument.monument.ticksToWin ?? 0, "turn", "turns")}`,
      detail: "If its store pays the upkeep every turn and no army knocks it down. Either one makes it stand the whole hold again.",
      link: { to: "/kingdom/alliance", label: "Guard it" },
    });
  }
  if (monument && monument.monument.stages > 0 && monument.garrison.every(guard => guard.turnsLeft > 0)) {
    lines.push({
      urgent: false,
      text: "Nobody guards our monument",
      detail: monument.monument.complete
        ? `It stands complete. Any army that marches there wins, knocks down up to ${Math.round(config.monument.maxRazeRatio * 100)}% of it, and makes it stand the whole hold again.`
        : `Stage ${monument.monument.stages} of ${monument.monument.totalStages}. Any army that marches there wins and knocks down up to ${Math.round(config.monument.maxRazeRatio * 100)}% of it.`,
      link: { to: "/kingdom/alliance", label: "Send soldiers" },
    });
  }
  if (coalition) {
    // allies wait at the border for our army: the strike lands when we pass the turns
    const led = kingdom.ongoingAttacks.find(attack =>
      coalition.warRoom.marching.some(march => march.joining === kingdom.name && march.to === attack.to),
    );
    if (led) {
      const waiting = coalition.warRoom.marching.filter(march => march.joining === kingdom.name && march.to === led.to);
      const atBorder = waiting.filter(march => march.turnsLeft === 0).map(march => march.from);
      const names = waiting.map(march => march.from).join(", ");
      lines.push({
        urgent: false,
        text: atBorder.length
          ? `${atBorder.join(", ")} ${atBorder.length === 1 ? "waits" : "wait"} at the border of ${led.to}; they fight right after our army lands, in ${plural(led.turnsLeft, "turn", "turns")}.`
          : `${names} joined our strike on ${led.to}; they fight right after our army lands, in ${plural(led.turnsLeft, "turn", "turns")}.`,
        link: { to: "/kingdom/war", label: "See the march" },
      });
    }
  }
  const soldiersHome = militaryUnits.reduce((sum, unit) => sum + kingdom.units.availableUnits[unit], 0);
  if (coalition && soldiersHome > 0) {
    const threat = coalition.warRoom.threats.find(army => army.to !== kingdom.name);
    if (threat) {
      lines.push(
        threat.attackType === "monument"
          ? {
              urgent: false,
              text: `${threat.from} marches on our monument at ${threat.to}: about ${formatNumber(threat.soldiers)} soldiers, landing in ${theirTurns(threat.turnsLeft)}.`,
              link: { to: "/kingdom/alliance", label: "Send soldiers" },
            }
          : {
              urgent: false,
              text: `${threat.from} marches on ${threat.to}: about ${formatNumber(threat.soldiers)} soldiers, landing in ${theirTurns(threat.turnsLeft)}.`,
              link: { to: "/kingdom/war", label: "Stand guard" },
            },
      );
    }
    // a strike's lead, one we have not joined yet
    const strike = coalition.warRoom.marching.find(
      march =>
        march.from !== kingdom.name &&
        !march.joining &&
        !kingdom.ongoingAttacks.some(attack => attack.joining === march.from && attack.to === march.to),
    );
    if (strike) {
      lines.push({
        urgent: false,
        text: `${strike.from} marches on ${strike.to}, landing in ${theirTurns(strike.turnsLeft)}.`,
        link: { to: `/kingdom/war?join=${strike.id}`, label: "Join the strike" },
      });
    }
  }
  // an ally asks for gold by offering a single piece at the sum it needs
  const ask = coalition?.offers.find(offer => offer.count === 1 && offer.price <= resources.gold);
  if (ask) {
    lines.push({
      urgent: false,
      text: `${ask.sellerName} asks the alliance for ${formatNumber(ask.price)} gold.`,
      link: { to: "/kingdom/alliance", label: "Send gold" },
    });
  }
  if (resources.turns >= config.common.maxTurns) {
    lines.push({ urgent: false, text: `The turn store is full at ${config.common.maxTurns}; new turns are lost.` });
  }

  // what threatens first, what asks for an answer next, and the turn's own work last
  return [...lines.filter(line => line.urgent), ...lines.filter(line => !line.urgent), ...chores(kingdom, details, config)].slice(0, limit);
};

/** one line on what the turns just passed brought, for the toast */
export const turnSummary = (report: TurnReport): string => {
  const parts = [
    `${signed((report.resourcesProduced.gold ?? 0) - report.goldUpkeep)} gold`,
    `${signed((report.resourcesProduced.food ?? 0) - report.foodConsumed)} food`,
    report.arrivingPeople > 0 ? plural(report.arrivingPeople, "newcomer", "newcomers") : null,
    report.starvedPeople > 0 ? `${formatNumber(report.starvedPeople)} left hungry` : null,
    report.exiledPeople > 0 ? `${formatNumber(report.exiledPeople)} left for lack of houses` : null,
    report.desertedSoldiers > 0 ? `${plural(report.desertedSoldiers, "soldier", "soldiers")} deserted unpaid` : null,
  ].filter(Boolean);
  return `A turn has passed: ${parts.join(", ")}${report.event ? `. ${report.event}` : ""}`;
};
