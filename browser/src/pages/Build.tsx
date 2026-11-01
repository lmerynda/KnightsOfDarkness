import React, { useEffect, useState } from "react";
import { api } from "../api/endpoints";
import type { BuildingName, Counts, LandTransaction, SpecialBuilding, SpecialBuildingType } from "../api/types";
import { specialBuildingTypes } from "../api/types";
import { useGame } from "../game/useGame";
import { art, buildingGroups, buildingLore, specialBuildingLore } from "../game/lore";
import { Icon } from "../ui/Icon";
import { AmountField, Banner, Choice, Meter, Panel } from "../ui/parts";
import { formatNumber, plural } from "../ui/format";

const LandPurchase: React.FC = () => {
  const { kingdom, details, act } = useGame();
  const [count, setCount] = useState(0);
  const [quote, setQuote] = useState<LandTransaction | null>(null);

  useEffect(() => {
    if (count <= 0) {
      setQuote(null);
      return;
    }
    const timer = window.setTimeout(() => {
      api
        .landQuote(count)
        .then(setQuote)
        .catch(() => setQuote(null));
    }, 250);
    return () => window.clearTimeout(timer);
  }, [count, kingdom.resources.gold, kingdom.resources.land]);

  const buy = async (): Promise<void> => {
    const result = await act(
      () => api.buyLand(count),
      transaction => ({
        text:
          transaction.amount > 0
            ? `Bought ${plural(transaction.amount, "acre", "acres")} for ${formatNumber(transaction.cost)} gold`
            : "Not enough gold for any land",
        failure: transaction.amount === 0,
      }),
    );
    if (result) setCount(0);
  };

  const free = kingdom.resources.land - details.usedLand;

  return (
    <Panel
      title="Land"
      lede={`${formatNumber(kingdom.resources.land)} acres, ${formatNumber(free)} free. Next acre: ${formatNumber(details.nextLandPrice)} gold.`}
      help="Every building stands on an acre. Each acre costs more than the last: the square of your land, over ten, in gold."
    >
      <div className="row">
        <div style={{ width: 180 }}>
          <AmountField label="Acres to buy" value={count} onChange={setCount} />
        </div>
        <button className="button" type="button" disabled={count <= 0 || !quote || quote.amount === 0} onClick={buy}>
          Buy land
        </button>
        <span className="muted">
          {quote
            ? quote.amount < count
              ? `You can afford ${plural(quote.amount, "acre", "acres")}, for ${formatNumber(quote.cost)} gold`
              : `${plural(quote.amount, "acre costs", "acres cost")} ${formatNumber(quote.cost)} gold`
            : null}
        </span>
      </div>
    </Panel>
  );
};

const SpecialBuildingRow: React.FC<{ building: SpecialBuilding }> = ({ building }) => {
  const { kingdom, config, act } = useGame();
  const [points, setPoints] = useState(0);
  const lore = specialBuildingLore[building.buildingType];
  const remaining = building.buildingPointsRequired - building.buildingPointsPut;

  return (
    <li className="special">
      <img src={art(building.buildingType)} alt="" />
      <div className="special-body">
        <div className="special-head">
          <h3>{lore.label}</h3>
          <span className="pips" aria-label={`Level ${building.level} of ${config.common.specialBuildingMaxLevel}`}>
            {Array.from({ length: config.common.specialBuildingMaxLevel }, (_, level) => (
              <span key={level} className={level < building.level ? "on" : undefined} />
            ))}
          </span>
        </div>
        <p className="muted">{lore.effect}</p>
        {building.isMaxLevel ? (
          <p className="gain">Finished.</p>
        ) : (
          <>
            <div className="special-progress">
              <Meter value={building.buildingPointsPut} max={building.buildingPointsRequired} label={`${lore.label} progress`} />
              <span className="muted">
                {formatNumber(building.buildingPointsPut)} of {formatNumber(building.buildingPointsRequired)} points to level{" "}
                {building.level + 1}
              </span>
            </div>
            <div className="row">
              <div style={{ width: 170 }}>
                <AmountField
                  label="Points to invest"
                  hideLabel
                  value={points}
                  onChange={setPoints}
                  max={Math.min(remaining, kingdom.resources.buildingPoints)}
                />
              </div>
              <button
                className="button quiet"
                type="button"
                disabled={points <= 0}
                onClick={async () => {
                  await act(
                    () => api.investInSpecialBuilding(building.id, points),
                    spent => ({ text: `Invested ${formatNumber(spent)} building points in the ${lore.label.toLowerCase()}` }),
                  );
                  setPoints(0);
                }}
              >
                Invest
              </button>
            </div>
          </>
        )}
      </div>
      <button
        className="button danger small special-demolish"
        type="button"
        onClick={() => {
          if (window.confirm(`Tear down this ${lore.label.toLowerCase()}? Its levels are lost.`)) {
            void act(
              () => api.demolishSpecialBuilding(building.id),
              () => ({ text: `The ${lore.label.toLowerCase()} was torn down` }),
            );
          }
        }}
      >
        Tear down
      </button>
    </li>
  );
};

const SpecialBuildings: React.FC = () => {
  const { kingdom, config, act } = useGame();
  const [choice, setChoice] = useState<SpecialBuildingType>("goldShaft");
  const slotsLeft = config.common.specialBuildingMaxCount - kingdom.specialBuildings.length;

  return (
    <Panel
      title="Great works"
      help={`Up to ${config.common.specialBuildingMaxCount} great works, each raised level by level with building points. Every level costs twice the one before; tearing one down loses its levels.`}
    >
      {kingdom.specialBuildings.length > 0 ? (
        <ul className="specials">
          {kingdom.specialBuildings.map(building => (
            <SpecialBuildingRow key={building.id} building={building} />
          ))}
        </ul>
      ) : null}
      {slotsLeft > 0 ? (
        <div className="row special-start">
          <label className="field">
            <span>
              Begin a new great work, {slotsLeft} of {config.common.specialBuildingMaxCount} places left
            </span>
            <select className="select" value={choice} onChange={event => setChoice(event.target.value as SpecialBuildingType)}>
              {specialBuildingTypes.map(type => (
                <option key={type} value={type}>
                  {specialBuildingLore[type].label}, first level {formatNumber(config.specialBuildingCosts[type])} points
                </option>
              ))}
            </select>
          </label>
          <button
            className="button quiet"
            type="button"
            onClick={() =>
              act(
                () => api.startSpecialBuilding(choice),
                () => ({ text: `Ground broken for a ${specialBuildingLore[choice].label.toLowerCase()}` }),
              )
            }
          >
            Begin
          </button>
        </div>
      ) : null}
    </Panel>
  );
};

type Mode = "build" | "demolish";

const buildingFacts = (
  building: BuildingName,
  config: ReturnType<typeof useGame>["config"],
  occupancy: number | undefined,
  capacity: number | undefined,
): string[] => {
  const facts = [buildingLore[building].purpose];
  if (building === "house") facts[0] = `Shelters ${config.buildingCapacity.house} civilians`;
  const percent = (ratio: number): number => Math.round(ratio * 100);
  if (building === "tower")
    facts[0] = `Adds ${percent(config.attack.towerDefenseBonusPerTower)}% defense each, up to ${percent(config.attack.maxTowerDefenseBonus)}%`;
  if (building === "castle") facts[0] = `Adds ${percent(config.attack.castleDefenseBonusPerCastle)}% defense each`;
  if (capacity !== undefined && occupancy !== undefined) facts.push(`${formatNumber(occupancy)} of ${formatNumber(capacity)} places taken`);
  return facts;
};

export const Build: React.FC = () => {
  const { kingdom, details, config, act } = useGame();
  const [mode, setMode] = useState<Mode>("build");
  const [order, setOrder] = useState<Partial<Counts<BuildingName>>>({});

  const points = kingdom.resources.buildingPoints;
  const freeLand = kingdom.resources.land - details.usedLand;
  const ordered = Object.entries(order).reduce((sum, [, count]) => sum + (count ?? 0), 0);
  const cost = (Object.entries(order) as [BuildingName, number][]).reduce(
    (sum, [building, count]) => sum + count * config.buildingPointCosts[building],
    0,
  );
  const pointsLeft = points - cost;
  const landLeft = freeLand - ordered;

  const setCount = (building: BuildingName, count: number): void => setOrder(current => ({ ...current, [building]: count }));

  const maxFor = (building: BuildingName): number => {
    const current = order[building] ?? 0;
    if (mode === "demolish") return kingdom.buildings[building];
    const byPoints = Math.floor((pointsLeft + current * config.buildingPointCosts[building]) / config.buildingPointCosts[building]);
    return Math.max(0, Math.min(byPoints, landLeft + current));
  };

  const submit = async (): Promise<void> => {
    const result = await act(
      () => (mode === "build" ? api.build(order) : api.demolish(order)),
      outcome => {
        const total = Object.values(outcome.buildings).reduce((sum, count) => sum + count, 0);
        return {
          text:
            mode === "build" ? `Raised ${plural(total, "building", "buildings")}` : `Tore down ${plural(total, "building", "buildings")}`,
          failure: total === 0,
        };
      },
    );
    if (result) setOrder({});
  };

  const overBudget = mode === "build" && (pointsLeft < 0 || landLeft < 0);

  return (
    <>
      <Banner
        image={art("castle")}
        title="Build"
        help="Builders bring building points each turn. Points not spent are lost when the turn passes."
      />
      <div className="stack">
        <LandPurchase />
        <div className="row build-mode">
          <Choice
            label="Build or tear down"
            value={mode}
            onChange={next => {
              setMode(next);
              setOrder({});
            }}
            options={[
              { value: "build", label: "Raise buildings" },
              { value: "demolish", label: "Tear down" },
            ]}
          />
          <span className="figure muted">
            <Icon name="buildingPoints" /> {formatNumber(points)} building points this turn
          </span>
        </div>
        {buildingGroups.map(group => (
          <section key={group.title}>
            <h2 className="section-title">{group.title}</h2>
            <div className="plates">
              {group.buildings.map(building => (
                <article key={building} className={`plate ${(order[building] ?? 0) > 0 ? "chosen" : ""}`}>
                  <div className="plate-art" style={{ backgroundImage: `url(${art(building)})` }}>
                    <span className="plate-count" title={`${buildingLore[building].plural} you own`}>
                      {formatNumber(kingdom.buildings[building])}
                    </span>
                  </div>
                  <div className="plate-body">
                    <h3>{buildingLore[building].label}</h3>
                    <div className="facts">
                      {buildingFacts(building, config, details.buildingOccupancy[building], details.buildingCapacity[building]).map(
                        fact => (
                          <span key={fact}>{fact}</span>
                        ),
                      )}
                      <span>
                        <Icon name="buildingPoints" size={13} /> {formatNumber(config.buildingPointCosts[building])} points each
                      </span>
                    </div>
                  </div>
                  <div className="plate-input">
                    <AmountField
                      label={`${buildingLore[building].plural} to ${mode === "build" ? "raise" : "tear down"}`}
                      hideLabel
                      value={order[building] ?? 0}
                      onChange={count => setCount(building, count)}
                      max={maxFor(building)}
                    />
                  </div>
                </article>
              ))}
            </div>
          </section>
        ))}
        {ordered > 0 ? (
          <div className="order-bar">
            <span>
              {mode === "build"
                ? `Raise ${plural(ordered, "building", "buildings")} for ${formatNumber(cost)} building points, on ${plural(ordered, "acre", "acres")}`
                : `Tear down ${plural(ordered, "building", "buildings")}, freeing their land`}
              {overBudget ? (
                <span className="warning"> {pointsLeft < 0 ? "More points than you have." : "More than your free land."}</span>
              ) : null}
            </span>
            <span className="row">
              <button className="button quiet" type="button" onClick={() => setOrder({})}>
                Clear
              </button>
              <button className={`button ${mode === "demolish" ? "danger" : ""}`} type="button" onClick={submit}>
                {mode === "build" ? "Raise them" : "Tear them down"}
              </button>
            </span>
          </div>
        ) : null}
        <SpecialBuildings />
      </div>
    </>
  );
};
