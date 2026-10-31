import React from "react";
import { Link } from "react-router-dom";
import type { Notification, NotificationKind } from "../api/types";
import type { IconName } from "../ui/Icon";
import { Icon } from "../ui/Icon";
import { ago } from "../ui/format";

const kinds: Record<NotificationKind, { icon: IconName; tone: "gain" | "loss" | "news"; page: string }> = {
  victory: { icon: "sword", tone: "gain", page: "/kingdom/war" },
  defeat: { icon: "sword", tone: "loss", page: "/kingdom/war" },
  repelled: { icon: "shield", tone: "gain", page: "/kingdom/war" },
  breached: { icon: "shield", tone: "loss", page: "/kingdom/war" },
  spies: { icon: "eye", tone: "news", page: "/kingdom/spies" },
  trade: { icon: "gold", tone: "news", page: "/kingdom/market" },
  delivery: { icon: "food", tone: "news", page: "/kingdom/carriers" },
  alliance: { icon: "people", tone: "news", page: "/kingdom/alliance" },
  round: { icon: "turns", tone: "news", page: "/kingdom/rankings" },
  event: { icon: "scroll", tone: "news", page: "/kingdom/overview" },
};

/** one line of the chronicle: what kind of news it is, the news itself leading to its page, and when it happened */
export const ChronicleEntry: React.FC<{ entry: Notification }> = ({ entry }) => {
  const kind = kinds[entry.kind];
  return (
    <li className={`chronicle-entry tone-${kind.tone} ${entry.isRead ? "read" : ""}`}>
      <Icon name={kind.icon} size={18} />
      <Link to={kind.page}>{entry.message}</Link>
      <time className="muted" dateTime={entry.date}>
        {ago(entry.date)}
      </time>
    </li>
  );
};
