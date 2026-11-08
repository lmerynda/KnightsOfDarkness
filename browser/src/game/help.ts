import type { GameConfig } from "../api/types";

/** the rules a veteran knows and a newcomer can look up, shown on hover where the number they explain appears */

export const defenseHelp =
  "Soldiers and guards at home and allies' soldiers standing guard, with those a castle shelters counting for more. Soldiers on the march or standing guard elsewhere do not defend.";

export const counterIntelligenceHelp = (config: GameConfig): string =>
  `${config.espionage.counterIntelligencePerSpy} for each spy and ${config.espionage.counterIntelligencePerGuard} for each guard. Enemy spies succeed only when they outnumber it.`;

const every = (millis: number): string => {
  const units: [number, string][] = [
    [3600000, "hour"],
    [60000, "minute"],
    [1000, "second"],
  ];
  const [size, name] = units.find(([size]) => millis % size === 0) ?? [1, "millisecond"];
  const count = millis / size;
  return count === 1 ? `every ${name}` : `every ${count} ${name}s`;
};

/** turns come on the clock's whole periods, so the next one is the next whole hour for hourly turns */
export const turnsHelp = (config: GameConfig): string => {
  const period = config.common.newTurnPeriodicity;
  const next = new Date((Math.floor(Date.now() / period) + 1) * period);
  const at = next.toLocaleTimeString([], period < 60000 ? { timeStyle: "medium" } : { hour: "2-digit", minute: "2-digit" });
  return `A new turn ${every(period)}, the next at ${at}, stored up to ${config.common.maxTurns}; once full, new turns are lost.`;
};

export const protectionHelp =
  "Every kingdom starts at peace, and one whose defense was broken gets a short respite. Sending an army of your own ends the protection; armies already on the road still arrive.";
