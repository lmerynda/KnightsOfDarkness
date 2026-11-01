import React, { useEffect, useState } from "react";
import { NavLink, Outlet, useLocation } from "react-router-dom";
import { api } from "../api/endpoints";
import type { ResourceName } from "../api/types";
import { useGame } from "../game/useGame";
import { resourceLabel } from "../game/lore";
import { Crest } from "../ui/Crest";
import type { IconName } from "../ui/Icon";
import { Icon } from "../ui/Icon";
import { formatNumber, signed } from "../ui/format";

type NavItem = { to: string; label: string; badge?: number; alert?: boolean };

const Rail: React.FC<{ open: boolean; onNavigate: () => void }> = ({ open, onNavigate }) => {
  const { kingdom, unreadLetters, unreadNotifications } = useGame();
  const marching = kingdom.ongoingAttacks.length;

  const groups: { title: string; items: NavItem[] }[] = [
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

  return (
    <nav className={`rail ${open ? "open" : ""}`} aria-label="Kingdom">
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
            <NavLink key={item.to} to={item.to} className="rail-link" onClick={onNavigate}>
              <span>{item.label}</span>
              {item.badge ? <span className={item.alert ? "badge" : "rail-count"}>{item.badge}</span> : null}
            </NavLink>
          ))}
        </div>
      ))}
      <button type="button" className="rail-signout" onClick={() => api.signOut()}>
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

const LedgerItem: React.FC<{ icon: IconName; label: string; value: React.ReactNode; change?: number; hint?: string }> = ({
  icon,
  label,
  value,
  change,
  hint,
}) => (
  <div className="ledger-item" title={hint ?? label}>
    <Icon name={icon} size={20} />
    <div>
      <div className="ledger-value">
        {value}
        {change ? <span className={change > 0 ? "gain" : "loss"}> {signed(change)}</span> : null}
      </div>
      <div className="ledger-label">{label}</div>
    </div>
  </div>
);

const TurnSeal: React.FC = () => {
  const { kingdom, config, details, passTurn } = useGame();
  const [passing, setPassing] = useState(false);
  const turns = kingdom.resources.turns;
  const forecast = details.forecast;
  const warnings = [
    forecast.starvedPeople > 0 ? `${formatNumber(forecast.starvedPeople)} people will leave hungry` : null,
    forecast.exiledPeople > 0 ? `${formatNumber(forecast.exiledPeople)} people lack houses and will leave` : null,
    Object.values(forecast.professionalsLeaving).some(count => (count ?? 0) > 0) ? "Some workers have no room and will quit" : null,
  ].filter(Boolean);

  const onPass = async (): Promise<void> => {
    setPassing(true);
    await passTurn();
    setPassing(false);
  };

  return (
    <div className="seal-wrap">
      <button
        type="button"
        className={`seal ${warnings.length ? "warn" : ""}`}
        onClick={onPass}
        disabled={passing || turns <= 0}
        aria-describedby="seal-forecast"
      >
        <span className="seal-turns">{turns}</span>
        <span className="seal-label">{turns > 0 ? "Pass the turn" : "No turns left"}</span>
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

const Ledger: React.FC<{ onMenu: () => void }> = ({ onMenu }) => {
  const { kingdom, details } = useGame();
  const resources = kingdom.resources;
  const forecast = details.forecast;
  const produced = (resource: ResourceName): number => forecast.resourcesProduced[resource] ?? 0;
  const freeLand = resources.land - details.usedLand;

  return (
    <header className="ledger-bar">
      <button type="button" className="menu-button" onClick={onMenu} aria-label="Open the kingdom menu">
        <span />
        <span />
        <span />
      </button>
      <div className="ledger-items">
        <LedgerItem
          icon="gold"
          label="Gold"
          value={formatNumber(resources.gold)}
          change={produced("gold") - forecast.goldUpkeep}
          hint="Gold, and what the next turn brings after the soldiers are paid"
        />
        <LedgerItem
          icon="food"
          label="Food"
          value={formatNumber(resources.food)}
          change={produced("food") - forecast.foodConsumed}
          hint="Food, and the harvest minus what your people eat next turn"
        />
        <LedgerItem icon="iron" label="Iron" value={formatNumber(resources.iron)} />
        <LedgerItem icon="tools" label="Tools" value={formatNumber(resources.tools)} />
        <LedgerItem icon="weapons" label="Weapons" value={formatNumber(resources.weapons)} />
        <LedgerItem
          icon="buildingPoints"
          label="Building points"
          value={formatNumber(resources.buildingPoints)}
          hint="Building points are spent on buildings this turn; unspent points are lost when the turn passes"
        />
        <LedgerItem icon="land" label={`Land, ${formatNumber(freeLand)} free`} value={formatNumber(resources.land)} />
        <LedgerItem
          icon="people"
          label="Idle people"
          value={formatNumber(resources.unemployed)}
          hint={`${formatNumber(details.housedPeople)} of ${formatNumber(details.housingCapacity)} house places taken`}
        />
      </div>
      <TurnSeal />
    </header>
  );
};

export const GameShell: React.FC = () => {
  const [menuOpen, setMenuOpen] = useState(false);
  const location = useLocation();
  const { reload } = useGame();

  // allies and enemies change the kingdom between our own actions, so every page opens on fresh state
  useEffect(() => {
    window.scrollTo({ top: 0 });
    void reload();
  }, [location.pathname, reload]);

  return (
    <div className="shell">
      <Rail open={menuOpen} onNavigate={() => setMenuOpen(false)} />
      {menuOpen ? <button type="button" className="rail-scrim" aria-label="Close the menu" onClick={() => setMenuOpen(false)} /> : null}
      <div className="shell-main">
        <Ledger onMenu={() => setMenuOpen(true)} />
        <main className="content">
          <Outlet />
        </main>
      </div>
    </div>
  );
};
