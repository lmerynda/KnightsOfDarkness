import { createContext, useContext } from "react";
import type { GameConfig, Kingdom, KingdomDetails, Rankings, TurnReport } from "../api/types";

export type GameState = {
  kingdom: Kingdom;
  details: KingdomDetails;
  config: GameConfig;
  rankings: Rankings | null;
  unreadLetters: number;
  unreadNotifications: number;
  weaponsPercentage: number;
  setWeaponsPercentage: (value: number) => void;
  reload: () => Promise<void>;
  /** runs an action against the server, reports the outcome and refreshes the kingdom */
  act: <T>(action: () => Promise<T>, describe: (result: T) => { text: string; failure?: boolean }) => Promise<T | undefined>;
  notify: (text: string, failure?: boolean) => void;
  passTurn: () => Promise<TurnReport | undefined>;
  refreshCounters: () => void;
};

export const GameContext = createContext<GameState | undefined>(undefined);

export const useGame = (): GameState => {
  const context = useContext(GameContext);
  if (!context) {
    throw new Error("useGame is used outside of the game");
  }
  return context;
};
