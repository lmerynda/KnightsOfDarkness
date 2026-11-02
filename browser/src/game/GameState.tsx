import React, { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { errorText } from "../api/client";
import { api } from "../api/endpoints";
import type { GameConfig, Kingdom, KingdomDetails, Rankings, TurnReport } from "../api/types";
import { turnSummary } from "./advice";
import { GameContext } from "./useGame";

type Toast = { id: number; text: string; failure: boolean; topic?: string };

const weaponsKey = "weaponsPercentage";

// a new ruler's blacksmiths forge some weapons from the start, or the first soldiers cannot be armed
const defaultWeaponsPercentage = 20;

const readWeaponsPercentage = (): number => {
  try {
    const stored = localStorage.getItem(weaponsKey);
    const value = Number(stored);
    return stored !== null && Number.isFinite(value) ? Math.min(100, Math.max(0, value)) : defaultWeaponsPercentage;
  } catch {
    return defaultWeaponsPercentage;
  }
};

const Toasts: React.FC<{ toasts: Toast[] }> = ({ toasts }) => (
  <div className="toasts" role="status" aria-live="polite">
    {toasts.map(toast => (
      <div key={toast.id} className={`toast ${toast.failure ? "failure" : ""}`}>
        {toast.text}
      </div>
    ))}
  </div>
);

export const GameProvider: React.FC<{ children: React.ReactNode; fallback: (error: string | null) => React.ReactNode }> = ({
  children,
  fallback,
}) => {
  const [kingdom, setKingdom] = useState<Kingdom | null>(null);
  const [details, setDetails] = useState<KingdomDetails | null>(null);
  const [config, setConfig] = useState<GameConfig | null>(null);
  const [rankings, setRankings] = useState<Rankings | null>(null);
  const [unreadLetters, setUnreadLetters] = useState(0);
  const [unreadNotifications, setUnreadNotifications] = useState(0);
  const [weaponsPercentage, setWeaponsPercentageState] = useState(readWeaponsPercentage);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [toasts, setToasts] = useState<Toast[]>([]);
  const nextToast = useRef(1);
  const weaponsRef = useRef(weaponsPercentage);

  // a toast with a topic replaces the one before it on that topic, so passing turns quickly leaves one summary, not a pile
  const notify = useCallback((text: string, failure = false, topic?: string) => {
    const id = nextToast.current++;
    setToasts(current => [...current.filter(toast => !topic || toast.topic !== topic).slice(-3), { id, text, failure, topic }]);
    window.setTimeout(() => setToasts(current => current.filter(toast => toast.id !== id)), failure ? 7000 : 4500);
  }, []);

  const refreshCounters = useCallback(() => {
    api
      .unreadLetters()
      .then(setUnreadLetters)
      .catch(() => undefined);
    api
      .unreadNotifications()
      .then(setUnreadNotifications)
      .catch(() => undefined);
  }, []);

  const reload = useCallback(async () => {
    try {
      const [nextKingdom, nextDetails] = await Promise.all([api.kingdom(), api.details(weaponsRef.current)]);
      setKingdom(nextKingdom);
      setDetails(nextDetails);
      setLoadError(null);
    } catch (error) {
      setLoadError(errorText(error));
    }
    refreshCounters();
    api
      .rankings()
      .then(setRankings)
      .catch(() => undefined);
  }, [refreshCounters]);

  useEffect(() => {
    api
      .config()
      .then(setConfig)
      .catch(error => setLoadError(errorText(error)));
    void reload();
    // the server adds turns and resolves other kingdoms' moves on its own clock
    const timer = window.setInterval(() => void reload(), 30000);
    return () => window.clearInterval(timer);
  }, [reload]);

  const setWeaponsPercentage = useCallback(
    (value: number) => {
      weaponsRef.current = value;
      setWeaponsPercentageState(value);
      try {
        localStorage.setItem(weaponsKey, String(value));
      } catch {
        // remembered for this visit only
      }
      api
        .details(value)
        .then(setDetails)
        .catch(() => undefined);
    },
    [setDetails],
  );

  const act = useCallback(
    async <T,>(action: () => Promise<T>, describe: (result: T) => { text: string; failure?: boolean }) => {
      try {
        const result = await action();
        const { text, failure } = describe(result);
        notify(text, failure ?? false);
        await reload();
        return result;
      } catch (error) {
        notify(errorText(error), true);
        return undefined;
      }
    },
    [notify, reload],
  );

  const passTurn = useCallback(async () => {
    let report: TurnReport | undefined;
    try {
      const outcome = await api.passTurn(weaponsRef.current);
      if (outcome.success && outcome.turnReport) {
        report = outcome.turnReport;
        notify(turnSummary(report), false, "turn");
      } else notify(outcome.message, true);
    } catch (error) {
      notify(errorText(error), true);
    }
    await reload();
    return report;
  }, [notify, reload]);

  const value = useMemo(
    () =>
      kingdom && details && config
        ? {
            kingdom,
            details,
            config,
            rankings,
            unreadLetters,
            unreadNotifications,
            weaponsPercentage,
            setWeaponsPercentage,
            reload,
            act,
            notify,
            passTurn,
            refreshCounters,
          }
        : undefined,
    [
      kingdom,
      details,
      config,
      rankings,
      unreadLetters,
      unreadNotifications,
      weaponsPercentage,
      setWeaponsPercentage,
      reload,
      act,
      notify,
      passTurn,
      refreshCounters,
    ],
  );

  if (!value) {
    return <>{fallback(loadError)}</>;
  }

  return (
    <GameContext.Provider value={value}>
      {children}
      <Toasts toasts={toasts} />
    </GameContext.Provider>
  );
};
