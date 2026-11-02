import type { GameConfig, Kingdom, KingdomDetails, MarketOffer, TurnReport, WarRoom } from "../api/types";
import { militaryUnits } from "../api/types";
import { formatNumber, plural, signed } from "../ui/format";

export type Counsel = {
  /** urgent counsel costs the ruler people or resources next turn if ignored */
  urgent: boolean;
  text: string;
  link?: { to: string; label: string };
};

/** what a ruler in an alliance knows about the coalition: who the allies are, the war room and the allies' market offers */
export type Coalition = { allies: string[]; warRoom: WarRoom; offers: MarketOffer[] };

const theirTurns = (turns: number): string => plural(turns, "of their turns", "of their turns");

/**
 * what the steward warns of this turn, most pressing first: only what costs the ruler something or will not wait,
 * never how the game works; every line is read from the kingdom and the server's forecast of the next turn
 */
export const counsel = (kingdom: Kingdom, details: KingdomDetails, config: GameConfig, coalition?: Coalition, limit = 4): Counsel[] => {
  const lines: Counsel[] = [];
  const resources = kingdom.resources;
  const forecast = details.forecast;
  const foodBalance = (forecast.resourcesProduced.food ?? 0) - forecast.foodConsumed;
  const goldBalance = (forecast.resourcesProduced.gold ?? 0) - forecast.goldUpkeep;
  const goldFrom = kingdom.allianceName
    ? { to: "/kingdom/alliance", label: "Ask allies for gold" }
    : { to: "/kingdom/train", label: "Train gold miners" };

  for (const army of kingdom.incoming) {
    lines.push({
      urgent: true,
      text: `${army.from} marches on us: about ${formatNumber(army.soldiers)} soldiers, landing in ${theirTurns(army.turnsLeft)}. Our defense: ${formatNumber(details.defensePower)}.`,
      link: { to: "/kingdom/war", label: "Prepare" },
    });
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
      lines.push({
        urgent: false,
        text: `${threat.from} marches on ${threat.to}: about ${formatNumber(threat.soldiers)} soldiers, landing in ${theirTurns(threat.turnsLeft)}.`,
        link: { to: "/kingdom/war", label: "Stand guard" },
      });
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

  return [...lines.filter(line => line.urgent), ...lines.filter(line => !line.urgent)].slice(0, limit);
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
