import type { BuildingName, GameConfig, Kingdom, KingdomDetails, TurnReport, UnitName } from "../api/types";
import { militaryUnits } from "../api/types";
import { formatNumber, plural, signed } from "../ui/format";
import { unitHome } from "./rules";

export type Counsel = {
  /** urgent counsel costs the ruler people or resources next turn if ignored */
  urgent: boolean;
  text: string;
  link?: { to: string; label: string };
};

const workers: UnitName[] = ["farmer", "goldMiner", "ironMiner", "blacksmith", "carrier"];

/**
 * what a steward would tell the ruler this turn, most pressing first; every line is read from the kingdom and the
 * server's forecast of the next turn
 */
export const counsel = (kingdom: Kingdom, details: KingdomDetails, config: GameConfig, limit = 4): Counsel[] => {
  const lines: Counsel[] = [];
  const resources = kingdom.resources;
  const forecast = details.forecast;
  const foodBalance = (forecast.resourcesProduced.food ?? 0) - forecast.foodConsumed;
  const freeLand = resources.land - details.usedLand;
  const cheapestBuilding = Math.min(...Object.values(config.buildingPointCosts));
  const freeWorkplaces = workers.reduce((sum, unit) => {
    const home = unitHome[unit] as BuildingName;
    return sum + Math.max(0, (details.buildingCapacity[home] ?? 0) - (details.buildingOccupancy[home] ?? 0));
  }, 0);
  const soldiers = militaryUnits.reduce((sum, unit) => sum + kingdom.units.availableUnits[unit] + kingdom.units.mobileUnits[unit], 0);

  if (forecast.starvedPeople > 0) {
    lines.push({
      urgent: true,
      text: `${plural(forecast.starvedPeople, "person leaves", "people leave")} hungry next turn. Farmers feed the realm.`,
      link: { to: "/kingdom/train", label: "Train farmers" },
    });
  } else if (foodBalance < 0 && resources.food < -foodBalance * 5) {
    lines.push({
      urgent: true,
      text: `The granaries empty in about ${plural(Math.floor(resources.food / -foodBalance), "turn", "turns")}. More farms and farmers, or buy food.`,
      link: { to: "/kingdom/market", label: "Buy food" },
    });
  }
  if (forecast.exiledPeople > 0) {
    lines.push({
      urgent: true,
      text: `${plural(forecast.exiledPeople, "person has", "people have")} no house and will leave.`,
      link: { to: "/kingdom/build", label: "Build houses" },
    });
  }
  if (Object.values(forecast.professionalsLeaving).some(count => (count ?? 0) > 0)) {
    lines.push({
      urgent: true,
      text: "Some workers have no room in their workplaces and will quit.",
      link: { to: "/kingdom/build", label: "Build" },
    });
  }
  if (freeLand > 0 && resources.buildingPoints >= cheapestBuilding) {
    lines.push({
      urgent: false,
      text: `${plural(freeLand, "acre stands", "acres stand")} empty, and this turn's ${formatNumber(resources.buildingPoints)} building points are lost when it passes.`,
      link: { to: "/kingdom/build", label: "Build" },
    });
  }
  if (resources.unemployed > 0 && freeWorkplaces > 0) {
    lines.push({
      urgent: false,
      text: `${plural(resources.unemployed, "person stands", "people stand")} idle while ${plural(freeWorkplaces, "workplace waits", "workplaces wait")}.`,
      link: { to: "/kingdom/train", label: "Train workers" },
    });
  }
  if (details.housedPeople >= details.housingCapacity && forecast.arrivingPeople === 0) {
    lines.push({
      urgent: false,
      text: "Every house is full, so no newcomers arrive.",
      link: { to: "/kingdom/build", label: "Build houses" },
    });
  }
  if (freeLand <= 0) {
    lines.push({
      urgent: false,
      text: `Every acre is built on. The next acre costs ${formatNumber(details.nextLandPrice)} gold.`,
      link: { to: "/kingdom/build", label: "Buy land" },
    });
  }
  if (resources.turns >= config.common.maxTurns) {
    lines.push({ urgent: false, text: `Your turn store is full at ${config.common.maxTurns}; new turns are lost until you pass one.` });
  }
  if (soldiers === 0) {
    lines.push({
      urgent: false,
      text: "No soldiers stand in your realm. Raiders take land from kingdoms that cannot fight back.",
      link: { to: "/kingdom/train", label: "Train soldiers" },
    });
  }
  if (kingdom.units.availableUnits.spy === 0) {
    lines.push({
      urgent: false,
      text: "Without spies you march blind: they show what an enemy keeps at home.",
      link: { to: "/kingdom/train", label: "Train spies" },
    });
  }

  return [...lines.filter(line => line.urgent), ...lines.filter(line => !line.urgent)].slice(0, limit);
};

/** one line on what the turns just passed brought, for the toast */
export const turnSummary = (reports: TurnReport[]): string => {
  const total = (pick: (report: TurnReport) => number): number => reports.reduce((sum, report) => sum + pick(report), 0);
  const starved = total(report => report.starvedPeople);
  const exiled = total(report => report.exiledPeople);
  const parts = [
    `${signed(total(report => report.resourcesProduced.gold ?? 0))} gold`,
    `${signed(total(report => (report.resourcesProduced.food ?? 0) - report.foodConsumed))} food`,
    total(report => report.arrivingPeople) > 0
      ? plural(
          total(report => report.arrivingPeople),
          "newcomer",
          "newcomers",
        )
      : null,
    starved > 0 ? `${formatNumber(starved)} left hungry` : null,
    exiled > 0 ? `${formatNumber(exiled)} left for lack of houses` : null,
  ].filter(Boolean);
  return `${reports.length === 1 ? "A turn has passed" : `${reports.length} turns have passed`}: ${parts.join(", ")}`;
};
