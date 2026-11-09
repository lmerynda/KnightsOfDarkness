import React, { useEffect, useState } from "react";
import { Link, NavLink, Outlet, useLocation } from "react-router-dom";
import { getTrial } from "../api/client";
import { api } from "../api/endpoints";
import type { ResourceName } from "../api/types";
import { useGame } from "../game/useGame";
import { resourceLabel } from "../game/lore";
import { Crest } from "../ui/Crest";
import type { IconName } from "../ui/Icon";
import { Icon } from "../ui/Icon";
import { formatNumber, signed } from "../ui/format";
import { Term } from "../ui/Tip";

type NavItem = { to: string; label: string; badge?: number; alert?: boolean };

/** whether this browser holds a kingdom started with only its name, one its ruler has not kept yet */
const useTrial = (): boolean => {
  const [trial, setTrial] = useState(() => getTrial() !== null);
  useEffect(() => {
    const check = (): void => setTrial(getTrial() !== null);
    window.addEventListener("auth-changed", check);
    return () => window.removeEventListener("auth-changed", check);
  }, []);
  return trial;
};

const signOut = (trial: boolean): void => {
  if (
    trial &&
    !window.confirm("This kingdom lives only in this browser. Signing out loses it for good unless you keep it first. Sign out anyway?")
  ) {
    return;
  }
  api.signOut();
};

type NavGroup = { title: string; items: NavItem[] };

const useNavGroups = (): NavGroup[] => {
  const { kingdom, unreadLetters, unreadNotifications } = useGame();
  const marching = kingdom.ongoingAttacks.length;
  return [
    {
      title: "Realm",
      items: [
        { to: "/kingdom/overview", label: "Overview" },
        { to: "/kingdom/build", label: "Build" },
        { to: "/kingdom/train", label: "Train" },
      ],
    },
    {
      title: "War",
      items: [
        { to: "/kingdom/war", label: "Army", badge: marching || undefined },
        { to: "/kingdom/spies", label: "Spies" },
      ],
    },
    {
      title: "Trade",
      items: [
        { to: "/kingdom/market", label: "Market" },
        { to: "/kingdom/carriers", label: "Carriers", badge: kingdom.carriersOnTheMove.length || undefined },
      ],
    },
    {
      title: "Council",
      items: [
        { to: "/kingdom/alliance", label: "Alliance" },
        { to: "/kingdom/letters", label: "Letters", badge: unreadLetters || undefined, alert: unreadLetters > 0 },
        { to: "/kingdom/chronicle", label: "Chronicle", badge: unreadNotifications || undefined, alert: unreadNotifications > 0 },
        { to: "/kingdom/rankings", label: "Rankings" },
      ],
    },
  ];
};

const Rail: React.FC = () => {
  const trial = useTrial();
  const { kingdom } = useGame();
  const groups = useNavGroups();

  return (
    <nav className="rail" aria-label="Kingdom">
      <div className="rail-crest">
        <Crest name={kingdom.name} size={56} />
        <div>
          <div className="rail-name">{kingdom.name}</div>
          <div className="rail-alliance">{kingdom.allianceName ?? "No alliance"}</div>
        </div>
      </div>
      {groups.map(group => (
        <div key={group.title} className="rail-group">
          <div className="rail-group-title">{group.title}</div>
          {group.items.map(item => (
            <NavLink key={item.to} to={item.to} className="rail-link">
              <span>{item.label}</span>
              {item.badge ? <span className={item.alert ? "badge" : "rail-count"}>{item.badge}</span> : null}
            </NavLink>
          ))}
        </div>
      ))}
      <button type="button" className="rail-signout" onClick={() => signOut(trial)}>
        Sign out
      </button>
    </nav>
  );
};

const resourceIcon: Partial<Record<ResourceName, IconName>> = {
  gold: "gold",
  food: "food",
  iron: "iron",
  tools: "tools",
  weapons: "weapons",
  buildingPoints: "buildingPoints",
};

/** a figure in the top bar, under its name; what the next turn brings on hover or tap */
const LedgerItem: React.FC<{
  icon: IconName;
  label: string;
  /** the name on the bar, when the full one is too long for it */
  short?: string;
  value: number;
  change?: number;
  note?: string;
  detail: React.ReactNode;
}> = ({ icon, label, short, value, change, note, detail }) => (
  <Term
    className="ledger-item"
    label={`${label} ${formatNumber(value)}`}
    tip={
      <>
        <strong>{label}</strong> {detail}
      </>
    }
  >
    <Icon name={icon} size={18} />
    <span className="ledger-text">
      <span className="ledger-label">{short ?? label}</span>
      <span className="ledger-figures">
        <span className="ledger-value">{formatNumber(value)}</span>
        {change ? <span className={`ledger-change ${change > 0 ? "gain" : "loss"}`}>{signed(change)}</span> : null}
        {note ? <span className="ledger-note">{note}</span> : null}
      </span>
    </span>
  </Term>
);

/** what passing the turn takes, wherever the button is: the turns left, what it would cost, and the act itself */
const usePassTurn = (): { turns: number; warnings: string[]; passing: boolean; pass: () => Promise<void> } => {
  const { kingdom, details, passTurn } = useGame();
  const [passing, setPassing] = useState(false);
  const forecast = details.forecast;
  const warnings = [
    forecast.starvedPeople > 0 ? `${formatNumber(forecast.starvedPeople)} people will leave hungry` : null,
    forecast.exiledPeople > 0 ? `${formatNumber(forecast.exiledPeople)} people lack houses and will leave` : null,
    Object.values(forecast.professionalsLeaving).some(count => (count ?? 0) > 0) ? "Some workers have no room and will quit" : null,
  ].filter((warning): warning is string => warning !== null);

  const pass = async (): Promise<void> => {
    setPassing(true);
    await passTurn();
    setPassing(false);
  };

  return { turns: kingdom.resources.turns, warnings, passing, pass };
};

const TurnSeal: React.FC = () => {
  const { config, details } = useGame();
  const { turns, warnings, passing, pass: onPass } = usePassTurn();
  const forecast = details.forecast;

  return (
    <div className="seal-wrap">
      <button
        type="button"
        className={`seal ${warnings.length ? "warn" : ""}`}
        onClick={onPass}
        disabled={passing || turns <= 0}
        aria-label={turns > 0 ? `Pass turn, ${turns} stored` : "No turns left"}
        aria-describedby="seal-forecast"
      >
        <span className="seal-glass">
          <Icon name="turns" size={26} />
        </span>
        <span className="seal-label">{turns > 0 ? "Pass turn" : "No turns"}</span>
        <span className="seal-turns">{turns}</span>
      </button>
      <div className="seal-forecast" id="seal-forecast" role="tooltip">
        <div className="seal-forecast-title">
          Next turn, {formatNumber(turns)} of {config.common.maxTurns} turns stored
        </div>
        <ul>
          {(["gold", "food", "iron", "tools", "weapons", "buildingPoints"] as ResourceName[]).map(resource => {
            const produced = forecast.resourcesProduced[resource] ?? 0;
            const net =
              resource === "food" ? produced - forecast.foodConsumed : resource === "gold" ? produced - forecast.goldUpkeep : produced;
            return (
              <li key={resource}>
                <Icon name={resourceIcon[resource] ?? "gold"} size={16} />
                <span>{resourceLabel[resource]}</span>
                <span className={net < 0 ? "loss" : "gain"}>{signed(net)}</span>
              </li>
            );
          })}
          <li>
            <Icon name="people" size={16} />
            <span>Newcomers</span>
            <span className="gain">{signed(forecast.arrivingPeople)}</span>
          </li>
        </ul>
        {warnings.map(warning => (
          <p key={warning} className="warning">
            {warning}
          </p>
        ))}
      </div>
    </div>
  );
};

const Ledger: React.FC = () => {
  const { kingdom, details } = useGame();
  const resources = kingdom.resources;
  const forecast = details.forecast;
  const produced = (resource: ResourceName): number => forecast.resourcesProduced[resource] ?? 0;
  const freeLand = resources.land - details.usedLand;

  return (
    <header className="ledger-bar">
      <div className="ledger-who">
        <Crest name={kingdom.name} size={26} />
        <span className="ledger-name">{kingdom.name}</span>
      </div>
      <div className="ledger-items">
        <LedgerItem
          icon="gold"
          label="Gold"
          value={resources.gold}
          change={produced("gold") - forecast.goldUpkeep}
          detail={`${signed(produced("gold"))} from the mines next turn${forecast.goldUpkeep ? `, ${formatNumber(forecast.goldUpkeep)} paid to the soldiers` : ""}.`}
        />
        <LedgerItem
          icon="food"
          label="Food"
          value={resources.food}
          change={produced("food") - forecast.foodConsumed}
          detail={`${signed(produced("food"))} harvested next turn, ${formatNumber(forecast.foodConsumed)} eaten.`}
        />
        <LedgerItem
          icon="land"
          label="Land"
          value={resources.land}
          note={`${formatNumber(freeLand)} free`}
          detail={`${formatNumber(freeLand)} acres free. The next acre costs ${formatNumber(details.nextLandPrice)} gold.`}
        />
        <LedgerItem
          icon="buildingPoints"
          label="Building points"
          short="Points"
          value={resources.buildingPoints}
          detail="Spent on buildings this turn; what is left is lost when the turn passes."
        />
        <LedgerItem
          icon="people"
          label="Idle people"
          short="Idle"
          value={resources.unemployed}
          detail={`Ready to train; ${formatNumber(details.housedPeople - resources.unemployed)} others at work. ${forecast.arrivingPeople > 0 ? `${signed(forecast.arrivingPeople)} arrive next turn.` : "The houses are full, so nobody arrives next turn."}`}
        />
        {(["iron", "tools", "weapons"] as const).map(resource => (
          <LedgerItem
            key={resource}
            icon={resource}
            label={resourceLabel[resource]}
            value={resources[resource]}
            detail={`${signed(produced(resource))} next turn.`}
          />
        ))}
      </div>
      <TurnSeal />
    </header>
  );
};

/**
 * a phone's way around the kingdom, within reach of a thumb: the four parts of the rail, each opening its pages, and the
 * turn in the middle
 */
const ThumbBar: React.FC = () => {
  const trial = useTrial();
  const groups = useNavGroups();
  const location = useLocation();
  const { turns, warnings, passing, pass } = usePassTurn();
  const [opened, setOpened] = useState<string | null>(null);
  const sheet = groups.find(group => group.title === opened);

  const tab = (group: NavGroup): React.ReactElement => {
    const unread = group.items.reduce((sum, item) => sum + (item.alert ? (item.badge ?? 0) : 0), 0);
    return (
      <button
        key={group.title}
        type="button"
        className={`thumb-tab ${group.items.some(item => item.to === location.pathname) ? "here" : ""}`}
        aria-expanded={opened === group.title}
        onClick={() => setOpened(opened === group.title ? null : group.title)}
      >
        {group.title}
        {unread ? <span className="badge">{unread}</span> : null}
      </button>
    );
  };

  return (
    <>
      {sheet ? (
        <>
          <button type="button" className="rail-scrim" aria-label="Close the menu" onClick={() => setOpened(null)} />
          <div className="thumb-sheet">
            {sheet.items.map(item => (
              <NavLink key={item.to} to={item.to} className="rail-link" onClick={() => setOpened(null)}>
                <span>{item.label}</span>
                {item.badge ? <span className={item.alert ? "badge" : "rail-count"}>{item.badge}</span> : null}
              </NavLink>
            ))}
            {sheet.title === "Council" ? (
              <button type="button" className="rail-signout" onClick={() => signOut(trial)}>
                Sign out
              </button>
            ) : null}
          </div>
        </>
      ) : null}
      <nav className="thumb-bar" aria-label="Parts of the kingdom">
        {groups.slice(0, 2).map(tab)}
        <button
          type="button"
          className={`thumb-turn ${warnings.length ? "warn" : ""}`}
          onClick={() => void pass()}
          disabled={passing || turns <= 0}
          aria-label={turns > 0 ? `Pass turn, ${turns} stored` : "No turns left"}
        >
          <span>{turns > 0 ? "Pass turn" : "No turns"}</span>
          <strong>{turns}</strong>
        </button>
        {groups.slice(2).map(tab)}
      </nav>
    </>
  );
};

export const GameShell: React.FC = () => {
  const location = useLocation();
  const { reload } = useGame();
  const trial = useTrial();

  // allies and enemies change the kingdom between our own actions, so every page opens on fresh state
  useEffect(() => {
    window.scrollTo({ top: 0 });
    void reload();
  }, [location.pathname, reload]);

  return (
    <div className="shell">
      <Rail />
      <div className="shell-main">
        <Ledger />
        <main className="content">
          {trial && location.pathname !== "/kingdom/keep" ? (
            <p className="trial-notice">
              <span>This kingdom lives only in this browser.</span>
              <Link className="button quiet small" to="/kingdom/keep">
                Keep it
              </Link>
            </p>
          ) : null}
          <Outlet />
        </main>
      </div>
      <ThumbBar />
    </div>
  );
};
