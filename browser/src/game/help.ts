import type { GameConfig } from "../api/types";

/** the rules a veteran knows and a newcomer can look up, shown on hover where the number they explain appears */

export const defenseHelp =
  "Soldiers and guards at home, allies' soldiers standing guard, towers and castles. Soldiers on the march or standing guard elsewhere do not defend.";

export const counterIntelligenceHelp = (config: GameConfig): string =>
  `${config.espionage.counterIntelligencePerSpy} for each spy and ${config.espionage.counterIntelligencePerGuard} for each guard. Enemy spies succeed only when they outnumber it.`;

export const turnsHelp = (config: GameConfig): string =>
  `A new turn every ${config.common.newTurnPeriodicity / 1000} seconds, stored up to ${config.common.maxTurns}; once full, new turns are lost.`;

export const protectionHelp =
  "Your defense was broken. Sending an army of your own ends the protection; armies already on the road still arrive.";
