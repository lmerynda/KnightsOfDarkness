import React, { useEffect, useState } from "react";
import { api } from "../api/endpoints";
import type { BuildingName, Counts, LandTransaction, SpecialBuilding, SpecialBuildingType } from "../api/types";
import { specialBuildingTypes } from "../api/types";
import { useGame } from "../game/useGame";
import { art, buildingGroups, buildingLore, specialBuildingLore } from "../game/lore";
import { Icon } from "../ui/Icon";
import { CountField, Banner, Choice, Meter, Panel } from "../ui/parts";
import { Tip } from "../ui/Tip";
import { buildingPlans, fillPlan, rememberedPlan, rememberPlan } from "../game/plans";
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
  // one great work of each kind; warehouses alone may be raised more than once, as the server rules
  const raised = (type: SpecialBuildingType): boolean =>
    type !== "warehouse" && kingdom.specialBuildings.some(building => building.buildingType === type);
  const open = specialBuildingTypes.filter(type => !raised(type));
  const [picked, setChoice] = useState<SpecialBuildingType>("goldShaft");
  const choice = open.includes(picked) ? picked : open[0];
  const slotsLeft = config.common.specialBuildingMaxCount - kingdom.specialBuildings.length;

  return (
    <Panel
      title="Great works"
      help={`Up to ${config.common.specialBuildingMaxCount} great works, one of each kind, though warehouses may be raised more than once. Each is raised level by level with building points; every level costs twice the one before, and tearing one down loses its levels.`}
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
                <option key={type} value={type} disabled={raised(type)}>
                  {raised(type)
                    ? `${specialBuildingLore[type].label}, already raised: one of each kind`
                    : `${specialBuildingLore[type].label}, first level ${formatNumber(config.specialBuildingCosts[type])} points`}
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

/** what one building gives beyond its places, behind a mark so that a row stays one line */
const buildingBonus = (building: BuildingName, config: ReturnType<typeof useGame>["config"]): string | null => {
  const percent = (ratio: number): number => Math.round(ratio * 100);
  if (building === "tower") return `They loose ${percent(config.attack.towerFireBonus)}% more arrows when defending.`;
  if (building === "castle")
    return `They defend ${percent(config.attack.castleSoldierBonus)}% stronger and take as many more blows to fall.`;
  return null;
};

export const Build: React.FC = () => {
  const { kingdom, details, config, act, notify } = useGame();
  const [mode, setMode] = useState<Mode>("build");
  const [plan, setPlan] = useState(rememberedPlan);
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
        slim
        help="Builders bring building points each turn. Points not spent are lost when the turn passes."
      />
      <div className="stack">
        <div className="row works-tools">
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
          {mode === "build" ? (
            <span className="row plan-row">
              <Choice
                label="Plan"
                value={plan}
                onChange={next => {
                  setPlan(next);
                  rememberPlan(next);
                }}
                options={buildingPlans.map(option => ({ value: option.name, label: option.label }))}
              />
              <button
                className="button quiet"
                type="button"
                onClick={() => {
                  const chosen = buildingPlans.find(option => option.name === plan) ?? buildingPlans[0];
                  const filled = fillPlan(chosen.shares(config), kingdom.buildings, kingdom.resources.land, points, freeLand, config);
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
            </span>
          ) : null}
          <span className="figure muted works-budget">
            <Icon name="buildingPoints" /> {formatNumber(points)} building points, {formatNumber(freeLand)} free acres
          </span>
          <span className="works-jumps">
            <a href="#land">Buy land</a>
            <a href="#great-works">Great works</a>
          </span>
        </div>
        <table className="ledger works">
          <thead>
            <tr>
              <th aria-label="Picture" />
              <th>Building</th>
              <th>What one gives</th>
              <th className="number">Standing</th>
              <th>Places taken</th>
              <th className="number">Points each</th>
              <th>{mode === "build" ? "Raise" : "Tear down"}</th>
            </tr>
          </thead>
          {buildingGroups.map(group => (
            <tbody key={group.title}>
              <tr className="works-group">
                <th colSpan={7} scope="rowgroup">
                  {group.title}
                </th>
              </tr>
              {group.buildings.map(building => {
                const holds = (config.buildingCapacity as Partial<Record<BuildingName, number>>)[building] ?? 0;
                const capacity = details.buildingCapacity[building];
                const occupancy = details.buildingOccupancy[building];
                const bonus = buildingBonus(building, config);
                const lore = buildingLore[building];
                return (
                  <tr key={building} className={(order[building] ?? 0) > 0 ? "chosen" : undefined}>
                    <td className="work-art">
                      <img src={art(building)} alt="" loading="lazy" />
                    </td>
                    <td className="work-name">
                      <h3>{lore.label}</h3>
                    </td>
                    <td className="work-purpose">
                      {lore.purpose(holds)}
                      {bonus ? <Tip about={lore.label}>{bonus}</Tip> : null}
                    </td>
                    <td className="number work-count" data-label="Standing">
                      <span title={`${lore.plural} you own`}>{formatNumber(kingdom.buildings[building])}</span>
                    </td>
                    <td className="work-places" data-label="Places taken">
                      {capacity !== undefined && occupancy !== undefined ? (
                        <>
                          <span>
                            {formatNumber(occupancy)} of {formatNumber(capacity)}
                          </span>
                          <Meter value={occupancy} max={capacity} label={`${lore.plural}: places taken`} />
                        </>
                      ) : null}
                    </td>
                    <td className="number work-cost" data-label="Points each">
                      {formatNumber(config.buildingPointCosts[building])}
                    </td>
                    <td className="work-input">
                      <CountField
                        label={`${lore.plural} to ${mode === "build" ? "raise" : "tear down"}`}
                        hideLabel
                        value={order[building] ?? 0}
                        onChange={count => setCount(building, count)}
                        max={maxFor(building)}
                        action={
                          mode === "build"
                            ? { label: `Raise ${lore.plural.toLowerCase()}`, icon: "hammer", onClick: () => submit(building) }
                            : {
                                label: `Tear down ${lore.plural.toLowerCase()}`,
                                icon: "pickaxe",
                                danger: true,
                                onClick: () => submit(building),
                              }
                        }
                      />
                      {(order[building] ?? 0) > maxFor(building) ? (
                        <p className="warning work-warning">
                          {mode === "build"
                            ? `Room for ${formatNumber(maxFor(building))}.`
                            : `Only ${formatNumber(maxFor(building))} stand.`}
                        </p>
                      ) : null}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          ))}
        </table>
        {chosen.length > 0 ? (
          <div className="order-bar">
            <span>
              {mode === "build"
                ? `Raise ${plural(ordered, "building", "buildings")} for ${formatNumber(cost)} of ${formatNumber(points)} building points, on ${formatNumber(ordered)} of ${plural(freeLand, "free acre", "free acres")}`
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
        <div id="land" className="jump-target">
          <LandPurchase />
        </div>
        <div id="great-works" className="jump-target">
          <SpecialBuildings />
        </div>
      </div>
    </>
  );
};
