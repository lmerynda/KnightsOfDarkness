import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api/endpoints";
import type { MarketOffer, Notification, ResourceName, TurnReport, UnitName, WarRoom } from "../api/types";
import { militaryUnits } from "../api/types";
import { useGame } from "../game/useGame";
import { counsel } from "../game/advice";
import { ChronicleEntry } from "../game/chronicle";
import { art, marketResourceLabel, realmStage, resourceLabel, unitLore } from "../game/lore";
import type { IconName } from "../ui/Icon";
import { Icon } from "../ui/Icon";
import { Banner, Meter, Panel } from "../ui/parts";
import { Term } from "../ui/Tip";
import { counterIntelligenceHelp, defenseHelp, protectionHelp, turnsHelp } from "../game/help";
import { formatNumber, plural, signed } from "../ui/format";

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
    report.desertedSoldiers > 0 ? `${plural(report.desertedSoldiers, "soldier deserts", "soldiers desert")} for lack of pay` : null,
    report.exiledPeople > 0 ? `${plural(report.exiledPeople, "person leaves", "people leave")} for lack of houses` : null,
    ...leaving.map(
      ([unit, count]) =>
        `${formatNumber(count)} ${count === 1 ? unitLore[unit].label.toLowerCase() : unitLore[unit].plural.toLowerCase()} quit for lack of room`,
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
      {report.goldUpkeep > 0 || report.desertedSoldiers > 0 ? (
        <tr>
          <td>
            <span className="figure">
              <Icon name="gold" /> Soldiers' pay
            </span>
          </td>
          <td className="number loss">{signed(-report.goldUpkeep)}</td>
          <td className="number muted">
            {report.desertedSoldiers > 0 ? `${plural(report.desertedSoldiers, "soldier", "soldiers")} unpaid, leaving` : ""}
          </td>
        </tr>
      ) : null}
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
      help={`The server's own forecast; fortune never shows in it. Small realms produce faster, ×${forecast.kingdomSizeProductionBonus.toFixed(1)} now, fading to nothing at 1,000 acres.`}
    >
      <TurnLines report={forecast} />
      {foodBalance < 0 ? <p className="warning overview-note">The people eat more than the farms grow.</p> : null}
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
    // newcomers fill every empty house place each turn, so houses are always full; what matters is how many of them stand idle
    {
      label: "Idle people",
      value: kingdom.resources.unemployed,
      max: details.housedPeople,
      link: "/kingdom/train",
      note: `${formatNumber(details.housedPeople - kingdom.resources.unemployed)} at work`,
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
          <Icon name="shield" size={18} /> <Term tip={protectionHelp}>Protected</Term> from attack for{" "}
          {plural(kingdom.protectionTicks, "more turn", "more turns")}.
        </p>
      ) : null}
      <div className="realm-stats">
        <div>
          <Icon name="shield" size={22} />
          <strong>{formatNumber(details.defensePower)}</strong>
          <span className="muted">
            <Term tip={defenseHelp}>defense</Term> at home
          </span>
        </div>
        <div>
          <Icon name="sword" size={22} />
          <strong>{formatNumber(athome)}</strong>
          <span className="muted">soldiers home, {formatNumber(away)} marching</span>
        </div>
        <div>
          <Icon name="eye" size={22} />
          <strong>{formatNumber(details.counterIntelligence)}</strong>
          <span className="muted">
            <Term tip={counterIntelligenceHelp(config)}>counter intelligence</Term>
          </span>
        </div>
        <div>
          <Icon name="turns" size={22} />
          <strong>
            {kingdom.resources.turns} / {config.common.maxTurns}
          </strong>
          <span className="muted">
            <Term tip={turnsHelp(config)}>turns stored</Term>
          </span>
        </div>
      </div>
    </Panel>
  );
};

const Movements: React.FC = () => {
  const { kingdom } = useGame();
  const moves: { id: string; text: string; turns: number; status?: string; link: string }[] = [
    ...kingdom.incoming.map((army, index) => ({
      id: `incoming-${index}`,
      text: `${army.from} marches on us with about ${formatNumber(army.soldiers)} soldiers`,
      turns: army.turnsLeft,
      link: "/kingdom/war",
    })),
    ...kingdom.ongoingAttacks.map(attack =>
      attack.joining && attack.turnsLeft === 0
        ? {
            id: attack.id,
            text: `Army at the border of ${attack.to}, waiting for ${attack.joining}'s army`,
            turns: 0,
            status: "waiting",
            link: "/kingdom/war",
          }
        : {
            id: attack.id,
            text: `Army marching on ${attack.to} to ${attack.attackType === "land" ? "take land" : "plunder"}`,
            turns: attack.turnsLeft,
            link: "/kingdom/war",
          },
    ),
    ...kingdom.reinforcements
      .filter(sent => sent.turnsLeft > 0)
      .map(sent => ({
        id: sent.id,
        text: `Soldiers on the way to stand guard in ${sent.to}`,
        turns: sent.turnsLeft,
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
        <p className="muted">Nobody is on the move.</p>
      ) : (
        <ul className="plain-list">
          {moves.map(move => (
            <li key={move.id}>
              <Link to={move.link}>{move.text}</Link>
              <span className="muted">{move.status ?? `${plural(move.turns, "turn", "turns")} left`}</span>
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
        <p className="muted">Nothing yet.</p>
      ) : (
        <ol className="chronicle compact">
          {entries.map(entry => (
            <ChronicleEntry key={entry.id} entry={entry} />
          ))}
        </ol>
      )}
      <p className="overview-note">
        <Link to="/kingdom/chronicle">Read the whole chronicle</Link>
      </p>
    </Panel>
  );
};

const Counsel: React.FC = () => {
  const { kingdom, details, config, rankings } = useGame();
  const [warRoom, setWarRoom] = useState<WarRoom | undefined>(undefined);
  const [offers, setOffers] = useState<MarketOffer[]>([]);
  useEffect(() => {
    if (!kingdom.allianceName) return;
    api
      .warRoom()
      .then(setWarRoom)
      .catch(() => undefined);
    api
      .alliesOffers()
      .then(setOffers)
      .catch(() => undefined);
  }, [kingdom]);
  const allies = (rankings?.kingdoms ?? [])
    .filter(rank => kingdom.allianceName && rank.allianceName === kingdom.allianceName && rank.kingdomName !== kingdom.name)
    .map(rank => rank.kingdomName);
  const lines = counsel(kingdom, details, config, warRoom && kingdom.allianceName ? { allies, warRoom, offers } : undefined);
  if (!lines.length) return null;
  return (
    <Panel title="The steward warns">
      <ul className="counsel">
        {lines.map(line => (
          <li key={line.text} className={line.urgent ? "urgent" : undefined}>
            <Icon name={line.urgent ? "warning" : "scroll"} size={18} />
            <span>{line.text}</span>
            {line.link ? (
              <Link className="button quiet small" to={line.link.to}>
                {line.link.label}
              </Link>
            ) : null}
          </li>
        ))}
      </ul>
    </Panel>
  );
};

export const Overview: React.FC = () => {
  const { kingdom } = useGame();
  const stage = realmStage(kingdom.resources.land);
  return (
    <>
      <Banner image={art(stage.art)} title={kingdom.name}>
        {stage.title} of {formatNumber(kingdom.resources.land)} acres · {kingdom.allianceName ?? "no alliance"}
      </Banner>
      <div className="stack">
        <Counsel />
        <div className="two-columns">
          <Forecast />
          <div className="stack">
            <Realm />
            <Movements />
            <RecentChronicle />
          </div>
        </div>
      </div>
    </>
  );
};
