import React, { useEffect, useState } from "react";
import { api } from "../api/endpoints";
import type { BuildingName, Counts, LandTransaction, SpecialBuilding, SpecialBuildingType } from "../api/types";
import { specialBuildingTypes } from "../api/types";
import { useGame } from "../game/useGame";
import { art, buildingGroups, buildingLore, specialBuildingLore } from "../game/lore";
import { Icon } from "../ui/Icon";
import { CountField, Banner, Choice, Meter, Panel } from "../ui/parts";
import { Tip } from "../ui/Tip";
import { buildingPlans, fillPlan } from "../game/plans";
import { formatNumber, plural } from "../ui/format";

const LandPurchase: React.FC = () => {
  const { kingdom, details, act } = useGame();
  const [count, setCount] = useState(0);
  const [fetchedQuote, setQuote] = useState<LandTransaction | null>(null);
  const quote = count > 0 ? fetchedQuote : null;

  useEffect(() => {
    if (count <= 0) return;
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
          transaction.success && transaction.data
            ? `Bought ${plural(transaction.data.count, "acre", "acres")} for ${formatNumber(transaction.data.cost)} gold`
            : transaction.message,
        failure: !transaction.success,
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
          <CountField label="Acres to buy" value={count} onChange={setCount} />
        </div>
        <button className="button" type="button" disabled={count <= 0 || !quote || quote.count === 0} onClick={buy}>
          Buy land
        </button>
        <span className="muted">
          {quote
            ? quote.count < count
              ? `You can afford ${plural(quote.count, "acre", "acres")}, for ${formatNumber(quote.cost)} gold`
              : `${plural(quote.count, "acre costs", "acres cost")} ${formatNumber(quote.cost)} gold`
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
                <CountField
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
                    spent => ({
                      text: spent.success
                        ? `Invested ${formatNumber(spent.data ?? 0)} building points in the ${lore.label.toLowerCase()}`
                        : spent.message,
                      failure: !spent.success,
                    }),
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
              outcome => ({
                text: outcome.success ? `The ${lore.label.toLowerCase()} was torn down` : outcome.message,
                failure: !outcome.success,
              }),
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
                outcome => ({
                  text: outcome.success ? `Ground broken for a ${specialBuildingLore[choice].label.toLowerCase()}` : outcome.message,
                  failure: !outcome.success,
                }),
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
  const holds: Partial<Record<BuildingName, number>> = config.buildingCapacity;
  const facts = [buildingLore[building].purpose(holds[building] ?? 0)];
  const percent = (ratio: number): number => Math.round(ratio * 100);
  if (building === "tower")
    facts[0] = `Adds ${percent(config.attack.towerDefenseBonusPerTower)}% defense each, up to ${percent(config.attack.maxTowerDefenseBonus)}%`;
  if (building === "castle") facts[0] = `Adds ${percent(config.attack.castleDefenseBonusPerCastle)}% defense each`;
  if (capacity !== undefined && occupancy !== undefined) facts.push(`${formatNumber(occupancy)} of ${formatNumber(capacity)} places taken`);
  return facts;
};

export const Build: React.FC = () => {
  const { kingdom, details, config, act, notify } = useGame();
  const [mode, setMode] = useState<Mode>("build");
  const [plan, setPlan] = useState(buildingPlans[0].name);
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

  const chosen = (Object.keys(order) as BuildingName[]).filter(building => (order[building] ?? 0) > 0);

  // one plate's order alone, or every plate's at once
  const submit = async (only?: BuildingName): Promise<void> => {
    const part = only ? { [only]: order[only] ?? 0 } : order;
    const result = await act(
      () => (mode === "build" ? api.build(part) : api.demolish(part)),
      outcome => {
        const total = Object.values(outcome.data ?? {}).reduce((sum, count) => sum + count, 0);
        return {
          text:
            mode === "build" ? `Raised ${plural(total, "building", "buildings")}` : `Tore down ${plural(total, "building", "buildings")}`,
          failure: total === 0,
        };
      },
    );
    if (result) setOrder(current => (only ? { ...current, [only]: 0 } : {}));
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
        {mode === "build" ? (
          <div className="row plan-row">
            <Choice
              label="Plan"
              value={plan}
              onChange={setPlan}
              options={buildingPlans.map(option => ({ value: option.name, label: option.label }))}
            />
            <button
              className="button quiet"
              type="button"
              onClick={() => {
                const chosen = buildingPlans.find(option => option.name === plan) ?? buildingPlans[0];
                const filled = fillPlan(
                  chosen.shares(kingdom.resources.land, config),
                  kingdom.buildings,
                  kingdom.resources.land,
                  points,
                  freeLand,
                  config,
                );
                setOrder(filled);
                if (Object.keys(filled).length === 0) notify("Nothing to fill in: the plan is met, or points or free land ran out");
              }}
            >
              Fill in the plan
            </button>
            <Tip about="plans">
              Each plan holds a share of your land for every building: Economy for mines and farms, War with barracks and a spy guild,
              Fortress with towers and guard houses. Filling it in enters what is still missing, the largest gap first, within this turn's
              points and free land; nothing is raised until you say so.
            </Tip>
          </div>
        ) : null}
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
                    <CountField
                      label={`${buildingLore[building].plural} to ${mode === "build" ? "raise" : "tear down"}`}
                      hideLabel
                      value={order[building] ?? 0}
                      onChange={count => setCount(building, count)}
                      max={maxFor(building)}
                      action={
                        mode === "build"
                          ? {
                              label: `Raise ${buildingLore[building].plural.toLowerCase()}`,
                              icon: "hammer",
                              onClick: () => submit(building),
                            }
                          : {
                              label: `Tear down ${buildingLore[building].plural.toLowerCase()}`,
                              icon: "pickaxe",
                              danger: true,
                              onClick: () => submit(building),
                            }
                      }
                    />
                    {(order[building] ?? 0) > maxFor(building) ? (
                      <p className="warning plate-warning">
                        {mode === "build" ? `Room for ${formatNumber(maxFor(building))}.` : `Only ${formatNumber(maxFor(building))} stand.`}
                      </p>
                    ) : null}
                  </div>
                </article>
              ))}
            </div>
          </section>
        ))}
        {chosen.length > 1 ? (
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
              <button className={`button ${mode === "demolish" ? "danger" : ""}`} type="button" onClick={() => submit()}>
                {mode === "build" ? "Raise all" : "Tear all down"}
              </button>
            </span>
          </div>
        ) : null}
        <SpecialBuildings />
      </div>
    </>
  );
};
