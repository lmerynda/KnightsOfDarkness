import React, { useCallback, useEffect, useState } from "react";
import { api } from "../api/endpoints";
import type { Notification } from "../api/types";
import { useGame } from "../game/useGame";
import { art } from "../game/lore";
import { ChronicleEntry } from "../game/chronicle";
import { Banner, Panel } from "../ui/parts";

export const Chronicle: React.FC = () => {
  const { refreshCounters, unreadNotifications } = useGame();
  const [entries, setEntries] = useState<Notification[] | null>(null);

  const load = useCallback(() => {
    api
      .notifications()
      .then(setEntries)
      .catch(() => setEntries([]));
  }, []);
  useEffect(load, [load, unreadNotifications]);

  const markAllRead = async (): Promise<void> => {
    await Promise.all(
      (entries ?? []).filter(entry => !entry.isRead).map(entry => api.markNotificationRead(entry.id).catch(() => undefined)),
    );
    load();
    refreshCounters();
  };

  const unread = (entries ?? []).filter(entry => !entry.isRead).length;

  return (
    <>
      <Banner image={art("rankings")} title="Chronicle">
        What happened to your kingdom while you were away: battles, caught spies, sales, deliveries and alliance news.
      </Banner>
      <Panel>
        <div className="row chronicle-head">
          <span className="muted">{unread > 0 ? `${unread} new` : "Nothing new"}</span>
          {unread > 0 ? (
            <button className="button quiet small" type="button" onClick={markAllRead}>
              Mark everything read
            </button>
          ) : null}
        </div>
        {entries === null ? (
          <p className="muted">Unrolling the chronicle</p>
        ) : entries.length === 0 ? (
          <p className="empty">Nothing has happened yet. It will.</p>
        ) : (
          <ol className="chronicle">
            {entries.map(entry => (
              <ChronicleEntry key={entry.id} entry={entry} />
            ))}
          </ol>
        )}
      </Panel>
    </>
  );
};
