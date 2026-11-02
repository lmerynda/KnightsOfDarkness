import React, { useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api/endpoints";
import type { BuildingName, Counts, GameConfig, UnitName } from "../api/types";
import { militaryUnits, unitNames } from "../api/types";
import { useGame } from "../game/useGame";
import { art, buildingLore, resourceLabel, unitGroups, unitLore } from "../game/lore";
import { unitHome } from "../game/rules";
import { Icon } from "../ui/Icon";
import { AmountField, Banner, Choice } from "../ui/parts";
import { formatNumber, plural } from "../ui/format";

type Order = Partial<Counts<UnitName>>;

const orderCost = (order: Order, config: GameConfig): { gold: number; tools: number; weapons: number; people: number } =>
  (Object.entries(order) as [UnitName, number][]).reduce(
    (sum, [unit, count]) => ({
      gold: sum.gold + count * config.trainingCost[unit].gold,
      tools: sum.tools + count * config.trainingCost[unit].tools,
      weapons: sum.weapons + count * config.trainingCost[unit].weapons,
      people: sum.people + count,
    }),
    { gold: 0, tools: 0, weapons: 0, people: 0 },
  );

const productionText = (unit: UnitName, config: GameConfig): string | null => {
  const production = config.production[unit];
  if (!production) return null;
  if (unit === "blacksmith") return `Makes ${production.rate} tools or weapons a turn, using as much iron`;
  return `Makes ${formatNumber(production.rate)} ${resourceLabel[production.resource].toLowerCase()} a turn, before bonuses`;
};

type Mode = "train" | "dismiss";

export const Train: React.FC = () => {
  const { kingdom, details, config, act } = useGame();
  const [mode, setMode] = useState<Mode>("train");
  const [order, setOrder] = useState<Order>({});
  const resources = kingdom.resources;
  const units = kingdom.units;

  const spent = orderCost(order, config);
  const ordered = spent.people;

  const placesLeft = (unit: UnitName): number | null => {
    const home = unitHome[unit];
    if (!home) return null;
    const capacity = details.buildingCapacity[home] ?? 0;
    const occupancy = details.buildingOccupancy[home] ?? 0;
    const orderedForHome = unitNames.filter(other => unitHome[other] === home).reduce((sum, other) => sum + (order[other] ?? 0), 0);
    return capacity - occupancy - orderedForHome;
  };

  const maxFor = (unit: UnitName): number => {
    const current = order[unit] ?? 0;
    if (mode === "dismiss") return units.availableUnits[unit];
    const cost = config.trainingCost[unit];
    const limits = [
      Math.floor((resources.gold - spent.gold + current * cost.gold) / cost.gold),
      resources.unemployed - ordered + current,
      cost.tools > 0 ? Math.floor((resources.tools - spent.tools + current * cost.tools) / cost.tools) : Infinity,
      cost.weapons > 0 ? Math.floor((resources.weapons - spent.weapons + current * cost.weapons) / cost.weapons) : Infinity,
    ];
    const places = placesLeft(unit);
    if (places !== null) limits.push(places + current);
    return Math.max(0, Math.min(...limits));
  };

  const short =
    mode === "train"
      ? [
          spent.gold > resources.gold ? "gold" : null,
          spent.tools > resources.tools ? "tools" : null,
          spent.weapons > resources.weapons ? "weapons" : null,
          spent.people > resources.unemployed ? "idle people" : null,
        ].filter(Boolean)
      : [];

  const chosen = (Object.keys(order) as UnitName[]).filter(unit => (order[unit] ?? 0) > 0);

  // one plate's order alone, or every plate's at once
  const submit = async (only?: UnitName): Promise<void> => {
    const part: Order = only ? { [only]: order[only] ?? 0 } : order;
    const asked = only ? (order[only] ?? 0) : ordered;
    const lacking = only ? [] : short;
    const result = await act(
      () => (mode === "train" ? api.train(part) : api.dismiss(part)),
      outcome => {
        const total = Object.values(outcome.units).reduce((sum, count) => sum + count, 0);
        const partly = total < asked;
        const reason = partly && lacking.length ? `, the rest lacked ${lacking.join(" and ")}` : "";
        return {
          text:
            mode === "train"
              ? partly
                ? `Trained ${formatNumber(total)} of ${formatNumber(asked)} recruits${reason}`
                : `Trained ${plural(total, "recruit", "recruits")}`
              : `Dismissed ${plural(total, "person", "people")}, they are idle now`,
          failure: total === 0,
        };
      },
    );
    if (result) setOrder(current => (only ? { ...current, [only]: 0 } : {}));
  };
  return (
    <>
      <Banner
        image={art("barracks")}
        title="Train"
        help={`Idle people become workers, watchmen and soldiers. Each needs gold, its tools or weapons, and a place in its building. Soldiers are paid every turn, those away too; when the gold falls short, soldiers at home desert.`}
      />
      <div className="row build-mode">
        <Choice
          label="Train or dismiss"
          value={mode}
          onChange={next => {
            setMode(next);
            setOrder({});
          }}
          options={[
            { value: "train", label: "Train" },
            { value: "dismiss", label: "Dismiss" },
          ]}
        />
        <span className="figure muted">
          <Icon name="people" /> {plural(resources.unemployed, "idle person", "idle people")}
        </span>
      </div>
      <div className="stack">
        {unitGroups.map(group => (
          <section key={group.title}>
            <h2 className="section-title">{group.title}</h2>
            <div className="plates">
              {group.units.map(unit => {
                const cost = config.trainingCost[unit];
                const home = unitHome[unit];
                const away = units.mobileUnits[unit];
                const production = productionText(unit, config);
                return (
                  <article key={unit} className={`plate ${(order[unit] ?? 0) > 0 ? "chosen" : ""}`}>
                    <div className="plate-art" style={{ backgroundImage: `url(${art(unit)})` }}>
                      <span className="plate-count" title={`${unitLore[unit].plural} at home`}>
                        {formatNumber(units.availableUnits[unit])}
                      </span>
                    </div>
                    <div className="plate-body">
                      <h3>{unitLore[unit].label}</h3>
                      <div className="facts">
                        <span>{production ?? unitLore[unit].duty}</span>
                        {config.attack.unitDefensePower[unit] !== undefined ? (
                          <span>
                            Attack {config.attack.unitAttackPower[unit] ?? 0}, defense {config.attack.unitDefensePower[unit]}
                          </span>
                        ) : null}
                        {(militaryUnits as readonly UnitName[]).includes(unit) ? (
                          <span>Paid {formatNumber(Math.round(cost.gold * config.common.soldierUpkeepRatio))} gold a turn</span>
                        ) : null}
                        {away > 0 ? <span className="warning">{formatNumber(away)} away from home</span> : null}
                        {home ? (
                          <span>
                            {buildingLore[home].plural}: {formatNumber(details.buildingOccupancy[home] ?? 0)} of{" "}
                            {formatNumber(details.buildingCapacity[home] ?? 0)} places,{" "}
                            {formatNumber((config.buildingCapacity as Partial<Record<BuildingName, number>>)[home] ?? 0)} each
                          </span>
                        ) : (
                          <span>Lives in a house</span>
                        )}
                        <span className="cost">
                          <span className="figure">
                            <Icon name="gold" size={13} /> {formatNumber(cost.gold)}
                          </span>
                          {cost.tools > 0 ? (
                            <span className="figure">
                              <Icon name="tools" size={13} /> {cost.tools}
                            </span>
                          ) : null}
                          {cost.weapons > 0 ? (
                            <span className="figure">
                              <Icon name="weapons" size={13} /> {cost.weapons}
                            </span>
                          ) : null}
                        </span>
                      </div>
                    </div>
                    <div className="plate-input">
                      <AmountField
                        label={`${unitLore[unit].plural} to ${mode}`}
                        hideLabel
                        value={order[unit] ?? 0}
                        onChange={count => setOrder(current => ({ ...current, [unit]: count }))}
                        max={maxFor(unit)}
                        action={
                          mode === "train"
                            ? { label: `Train ${unitLore[unit].plural.toLowerCase()}`, icon: "recruit", onClick: () => submit(unit) }
                            : {
                                label: `Dismiss ${unitLore[unit].plural.toLowerCase()}`,
                                icon: "dismiss",
                                danger: true,
                                onClick: () => submit(unit),
                              }
                        }
                      />
                      {(order[unit] ?? 0) > maxFor(unit) ? (
                        <p className="warning plate-warning">
                          Only {formatNumber(maxFor(unit))} within reach.
                          {mode === "train" && (order[unit] ?? 0) * cost.weapons > resources.weapons ? (
                            <>
                              {" "}
                              <Link to="/kingdom/overview">Set the forge</Link>
                            </>
                          ) : null}
                        </p>
                      ) : null}
                    </div>
                  </article>
                );
              })}
            </div>
          </section>
        ))}
      </div>
      {chosen.length > 1 ? (
        <div className="order-bar">
          <span>
            {mode === "train" ? (
              <>
                Train {plural(ordered, "recruit", "recruits")} for {formatNumber(spent.gold)} gold
                {spent.tools ? `, ${formatNumber(spent.tools)} tools` : ""}
                {spent.weapons ? `, ${formatNumber(spent.weapons)} weapons` : ""}
                {short.length ? <span className="warning"> Not enough {short.join(", ")}.</span> : null}
                {short.includes("weapons") ? (
                  <>
                    {" "}
                    <Link to="/kingdom/overview">Set the forge</Link>
                  </>
                ) : null}
              </>
            ) : (
              `Dismiss ${plural(ordered, "person", "people")} to idle`
            )}
          </span>
          <span className="row">
            <button className="button quiet" type="button" onClick={() => setOrder({})}>
              Clear
            </button>
            <button className={`button ${mode === "dismiss" ? "danger" : ""}`} type="button" onClick={() => submit()}>
              {mode === "train" ? "Train all" : "Dismiss all"}
            </button>
          </span>
        </div>
      ) : null}
    </>
  );
};
