import React, { useCallback, useEffect, useState } from "react";
import { api } from "../api/endpoints";
import type { Alliance, MonumentLedger } from "../api/types";
import { useGame } from "../game/useGame";
import { monumentStanding } from "../game/rules";
import { CountField, Meter, Panel } from "../ui/parts";
import { formatNumber } from "../ui/format";

const goods = ["gold", "iron", "tools", "food"] as const;
type Good = (typeof goods)[number];

const goodLabel: Record<Good, string> = { gold: "Gold", iron: "Iron", tools: "Tools", food: "Food" };

const nothing: Record<Good, number> = { gold: 0, iron: 0, tools: 0, food: 0 };

const help =
  "An alliance raises one monument an era. Members pay goods into its store, and a stage rises as soon as the store covers it. Every stage eats gold and food each turn; when the store cannot pay, the monument crumbles and only the crumbled part needs paying again. Everyone can see how far it stands.";

/** the alliance's monument: founding it, paying into it, and what it costs to keep */
export const MonumentPanel: React.FC<{ alliance: Alliance }> = ({ alliance }) => {
  const { kingdom, config, act } = useGame();
  const [ledger, setLedger] = useState<MonumentLedger | null | undefined>(undefined);
  const [seat, setSeat] = useState(kingdom.name);
  const [payment, setPayment] = useState(nothing);
  const load = useCallback(() => {
    api
      .monument()
      .then(setLedger)
      .catch(() => setLedger(null));
  }, []);
  // allies pay in and the clock takes the upkeep while the page is open
  useEffect(load, [load, kingdom]);

  if (ledger === undefined) return null;

  if (ledger === null) {
    const { stages, stageCost, upkeepPerStage } = config.monument;
    return (
      <Panel
        title="Monument"
        help={help}
        lede={`Your alliance has no monument. It takes ${stages} stages, each for ${formatNumber(stageCost.gold)} gold, ${formatNumber(stageCost.iron)} iron and ${formatNumber(stageCost.tools)} tools, and each eating ${formatNumber(upkeepPerStage.gold)} gold and ${formatNumber(upkeepPerStage.food)} food a turn.`}
      >
        {alliance.emperor === kingdom.name ? (
          <div className="row">
            <label className="field" style={{ minWidth: 220 }}>
              <span>Seat of the monument</span>
              <select className="select" value={seat} onChange={event => setSeat(event.target.value)}>
                {alliance.members.map(member => (
                  <option key={member} value={member}>
                    {member}
                  </option>
                ))}
              </select>
            </label>
            <button
              className="button"
              type="button"
              onClick={async () => {
                await act(
                  () => api.foundMonument(seat),
                  outcome => ({ text: outcome.message, failure: !outcome.success }),
                );
                load();
              }}
            >
              Found the monument
            </button>
          </div>
        ) : (
          <p className="muted">Only the emperor can found the monument.</p>
        )}
      </Panel>
    );
  }

  const { monument, store, nextStageCost, upkeep } = ledger;
  const paying = goods.some(good => payment[good] > 0);
  const short = goods.some(good => payment[good] > kingdom.resources[good]);

  return (
    <Panel title="Monument" help={help} lede={`${monumentStanding(monument)} at ${monument.seat}.`}>
      <Meter value={monument.stages} max={monument.totalStages} label="Monument stages" />
      {ledger.upkeepUnpaid ? <p className="warning">The store cannot pay the upkeep, and the monument is crumbling.</p> : null}
      <div className="table-scroll">
        <table className="ledger">
          <thead>
            <tr>
              <th>Good</th>
              <th className="number">In the store</th>
              <th className="number">Next stage needs</th>
              <th className="number">Eaten each turn</th>
            </tr>
          </thead>
          <tbody>
            {goods.map(good => (
              <tr key={good}>
                <td>{goodLabel[good]}</td>
                <td className="number">{formatNumber(store[good])}</td>
                <td className="number">{nextStageCost ? formatNumber(nextStageCost[good]) : ""}</td>
                <td className="number">{formatNumber(upkeep[good])}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <div className="row overview-note">
        {goods.map(good => (
          <div key={good} style={{ width: 150 }}>
            <CountField label={`${goodLabel[good]} to pay`} value={payment[good]} onChange={value => setPayment({ ...payment, [good]: value })} />
          </div>
        ))}
        <button
          className="button"
          type="button"
          disabled={!paying || short}
          onClick={async () => {
            const result = await act(
              () => api.payIntoMonument(payment),
              outcome => ({ text: outcome.message, failure: !outcome.success }),
            );
            if (result?.success) setPayment(nothing);
            load();
          }}
        >
          Pay in
        </button>
      </div>
      {short ? <p className="warning">You don&apos;t hold that much.</p> : null}
    </Panel>
  );
};
