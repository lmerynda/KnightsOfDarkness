import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api/endpoints";
import type { Notification, ResourceName, TurnReport, UnitName } from "../api/types";
import { militaryUnits } from "../api/types";
import { useGame } from "../game/useGame";
import { art, marketResourceLabel, resourceLabel, unitLore } from "../game/lore";
import type { IconName } from "../ui/Icon";
import { Icon } from "../ui/Icon";
import { Banner, Meter, Panel } from "../ui/parts";
import { ago, formatNumber, plural, signed } from "../ui/format";

const producedResources: { resource: ResourceName; icon: IconName }[] = [
  { resource: "gold", icon: "gold" },
  { resource: "food", icon: "food" },
  { resource: "iron", icon: "iron" },
  { resource: "tools", icon: "tools" },
  { resource: "weapons", icon: "weapons" },
  { resource: "buildingPoints", icon: "buildingPoints" },
];

const departures = (report: TurnReport): string[] => {
  const leaving = Object.entries(report.professionalsLeaving).filter(([, count]) => (count ?? 0) > 0) as [UnitName, number][];
  return [
    report.starvedPeople > 0 ? `${plural(report.starvedPeople, "person leaves", "people leave")} for lack of food` : null,
    report.exiledPeople > 0 ? `${plural(report.exiledPeople, "person leaves", "people leave")} for lack of houses` : null,
    ...leaving.map(
      ([unit, count]) =>
        `${formatNumber(count)} ${count === 1 ? unitLore[unit].label.toLowerCase() : unitLore[unit].plural.toLowerCase()} quit, there is no room for them`,
    ),
  ].filter((line): line is string => line !== null);
};

const TurnLines: React.FC<{ report: TurnReport }> = ({ report }) => (
  <table className="ledger">
    <tbody>
      {producedResources.map(({ resource, icon }) => {
        const produced = report.resourcesProduced[resource] ?? 0;
        const bonus = report.specialBuildingBonus[resource] ?? 1;
        return (
          <tr key={resource}>
            <td>
              <span className="figure">
                <Icon name={icon} /> {resourceLabel[resource]}
              </span>
            </td>
            <td className="number">{signed(produced)}</td>
            <td className="number muted">{bonus > 1 ? `special buildings ×${bonus.toFixed(1)}` : ""}</td>
          </tr>
        );
      })}
      <tr>
        <td>
          <span className="figure">
            <Icon name="food" /> Eaten
          </span>
        </td>
        <td className="number loss">{signed(-report.foodConsumed)}</td>
        <td className="number muted">
          {report.nourishmentProductionFactor < 1 ? `only ${Math.round(report.nourishmentProductionFactor * 100)}% fed` : ""}
        </td>
      </tr>
      <tr>
        <td>
          <span className="figure">
            <Icon name="people" /> Newcomers
          </span>
        </td>
        <td className="number">{signed(report.arrivingPeople)}</td>
        <td />
      </tr>
    </tbody>
  </table>
);

const Forecast: React.FC = () => {
  const { details, weaponsPercentage, setWeaponsPercentage, kingdom } = useGame();
  const forecast = details.forecast;
  const warnings = departures(forecast);
  const foodBalance = (forecast.resourcesProduced.food ?? 0) - forecast.foodConsumed;
  const blacksmiths = kingdom.units.availableUnits.blacksmith;

  return (
    <Panel
      title="If the turn passed now"
      lede={`Your realm grows ${forecast.kingdomSizeProductionBonus.toFixed(1)} times faster while it is small, the bonus fades as it reaches 1,000 land.`}
    >
      <TurnLines report={forecast} />
      {foodBalance < 0 ? (
        <p className="warning overview-note">
          Your people eat more than the farms grow. Train farmers or buy food before the stores run out.
        </p>
      ) : null}
      {warnings.map(line => (
        <p key={line} className="warning overview-note">
          {line}
        </p>
      ))}
      {blacksmiths > 0 ? (
        <label className="field overview-slider">
          <span>
            Blacksmiths forge {weaponsPercentage}% weapons and {100 - weaponsPercentage}% tools
          </span>
          <input
            type="range"
            min={0}
            max={100}
            step={5}
            value={weaponsPercentage}
            onChange={event => setWeaponsPercentage(Number(event.target.value))}
          />
        </label>
      ) : null}
    </Panel>
  );
};

const Realm: React.FC = () => {
  const { kingdom, details, config } = useGame();
  const land = kingdom.resources.land;
  const athome = militaryUnits.reduce((sum, unit) => sum + kingdom.units.availableUnits[unit], 0);
  const away = militaryUnits.reduce((sum, unit) => sum + kingdom.units.mobileUnits[unit], 0);
  const barracks = details.buildingCapacity.barracks ?? 0;

  const rows = [
    {
      label: "Land built on",
      value: details.usedLand,
      max: land,
      link: "/kingdom/build",
      note: `${formatNumber(land - details.usedLand)} free`,
    },
    {
      label: "House places",
      value: details.housedPeople,
      max: details.housingCapacity,
      link: "/kingdom/build",
      note: `${formatNumber(details.housingCapacity - details.housedPeople)} empty`,
    },
    {
      label: "Barracks places",
      value: athome + away,
      max: barracks,
      link: "/kingdom/train",
      note: `${formatNumber(Math.max(0, barracks - athome - away))} empty`,
    },
  ];

  return (
    <Panel title="The realm">
      <div className="realm-meters">
        {rows.map(row => (
          <div key={row.label}>
            <div className="realm-meter-head">
              <Link to={row.link}>{row.label}</Link>
              <span>
                {formatNumber(row.value)} of {formatNumber(row.max)} <span className="muted">({row.note})</span>
              </span>
            </div>
            <Meter value={row.value} max={row.max} label={row.label} />
          </div>
        ))}
      </div>
      {kingdom.protectionTicks > 0 ? (
        <p className="notice">
          <Icon name="shield" size={18} /> Your walls were breached, so no army may march on you for{" "}
          {plural(kingdom.protectionTicks, "more turn", "more turns")}. Sending an army of your own ends this protection.
        </p>
      ) : null}
      <div className="realm-stats">
        <div>
          <Icon name="shield" size={22} />
          <strong>{formatNumber(details.defensePower)}</strong>
          <span className="muted">defense at home</span>
        </div>
        <div>
          <Icon name="sword" size={22} />
          <strong>{formatNumber(athome)}</strong>
          <span className="muted">soldiers home, {formatNumber(away)} marching</span>
        </div>
        <div>
          <Icon name="eye" size={22} />
          <strong>{formatNumber(details.counterIntelligence)}</strong>
          <span className="muted">counter intelligence</span>
        </div>
        <div>
          <Icon name="turns" size={22} />
          <strong>
            {kingdom.resources.turns} / {config.common.maxTurns}
          </strong>
          <span className="muted">turns stored</span>
        </div>
      </div>
    </Panel>
  );
};

const Movements: React.FC = () => {
  const { kingdom } = useGame();
  const moves = [
    ...kingdom.ongoingAttacks.map(attack => ({
      id: attack.id,
      text: `Army marching on ${attack.to} to ${attack.attackType === "land" ? "take land" : "plunder"}`,
      turns: attack.turnsLeft,
      link: "/kingdom/war",
    })),
    ...kingdom.carriersOnTheMove.map(carriers => ({
      id: carriers.id,
      text: `${formatNumber(carriers.amount)} ${marketResourceLabel[carriers.resource].toLowerCase()} on the road to ${carriers.to}`,
      turns: carriers.turnsLeft,
      link: "/kingdom/carriers",
    })),
  ];

  return (
    <Panel title="On the roads">
      {moves.length === 0 ? (
        <p className="muted">Nobody is on the move. Armies and carriers you send out will show here until they arrive.</p>
      ) : (
        <ul className="plain-list">
          {moves.map(move => (
            <li key={move.id}>
              <Link to={move.link}>{move.text}</Link>
              <span className="muted">{plural(move.turns, "turn", "turns")} left</span>
            </li>
          ))}
        </ul>
      )}
    </Panel>
  );
};

const RecentChronicle: React.FC = () => {
  const { unreadNotifications } = useGame();
  const [entries, setEntries] = useState<Notification[]>([]);

  useEffect(() => {
    api
      .notifications()
      .then(list => setEntries(list.slice(0, 5)))
      .catch(() => undefined);
  }, [unreadNotifications]);

  return (
    <Panel title="Chronicle">
      {entries.length === 0 ? (
        <p className="muted">Battles, trades and alliance news will be written here.</p>
      ) : (
        <ul className="plain-list">
          {entries.map(entry => (
            <li key={entry.id} className={entry.isRead ? "muted" : undefined}>
              <span>{entry.message}</span>
              <span className="muted">{ago(entry.date)}</span>
            </li>
          ))}
        </ul>
      )}
      <p className="overview-note">
        <Link to="/kingdom/chronicle">Read the whole chronicle</Link>
      </p>
    </Panel>
  );
};

export const Overview: React.FC = () => {
  const { kingdom } = useGame();
  return (
    <>
      <Banner image={art("overview")} title={kingdom.name}>
        {kingdom.allianceName ? `Sworn to ${kingdom.allianceName}.` : "A kingdom without allies."} Spend this turn's building points, then
        pass the turn.
      </Banner>
      <div className="two-columns">
        <Forecast />
        <div className="stack">
          <Realm />
          <Movements />
          <RecentChronicle />
        </div>
      </div>
    </>
  );
};
