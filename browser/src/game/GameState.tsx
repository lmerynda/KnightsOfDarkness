import React, { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { errorText } from "../api/client";
import { api } from "../api/endpoints";
import type { GameConfig, Kingdom, KingdomDetails, Rankings } from "../api/types";
import { turnSummary } from "./advice";
import { GameContext } from "./useGame";

type Toast = { id: number; text: string; failure: boolean };

const weaponsKey = "weaponsPercentage";

const readWeaponsPercentage = (): number => {
  try {
    const stored = Number(localStorage.getItem(weaponsKey));
    return Number.isFinite(stored) ? Math.min(100, Math.max(0, stored)) : 0;
  } catch {
    return 0;
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

  const notify = useCallback((text: string, failure = false) => {
    const id = nextToast.current++;
    setToasts(current => [...current.slice(-3), { id, text, failure }]);
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
    const result = await act(
      () => api.passTurn(weaponsRef.current),
      outcome => ({
        text: outcome.success ? (outcome.turnReport ? turnSummary(outcome.turnReport) : "A turn has passed") : outcome.message,
        failure: !outcome.success,
      }),
    );
    return result?.turnReport ?? undefined;
  }, [act]);

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
