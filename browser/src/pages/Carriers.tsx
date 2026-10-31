import React, { useState } from "react";
import { api } from "../api/endpoints";
import type { MarketResource } from "../api/types";
import { marketResources } from "../api/types";
import { useGame } from "../game/useGame";
import { art, marketResourceLabel } from "../game/lore";
import { AmountField, Banner, Panel } from "../ui/parts";
import { formatNumber, plural } from "../ui/format";

export const Carriers: React.FC = () => {
  const { kingdom, config, rankings, act } = useGame();
  const [target, setTarget] = useState("");
  const [resource, setResource] = useState<MarketResource>("food");
  const [amount, setAmount] = useState(0);

  const carriers = kingdom.units.availableUnits.carrier;
  const perCarrier = config.carrierCapacity[resource];
  const canCarry = Math.min(kingdom.resources[resource], carriers * perCarrier);
  const needed = Math.ceil(amount / perCarrier);
  const allies = new Set(
    (rankings?.kingdoms ?? [])
      .filter(rank => kingdom.allianceName && rank.allianceName === kingdom.allianceName)
      .map(rank => rank.kingdomName),
  );
  const targets = (rankings?.kingdoms ?? [])
    .filter(rank => rank.kingdomName !== kingdom.name)
    .sort((a, b) => Number(allies.has(b.kingdomName)) - Number(allies.has(a.kingdomName)));

  const send = async (): Promise<void> => {
    const result = await act(
      () => api.sendCarriers(target, resource, amount),
      outcome => ({
        text: outcome.success
          ? `Carriers set out for ${target}, they arrive in ${plural(config.common.turnsToDeliverResources, "turn", "turns")}`
          : outcome.message,
        failure: !outcome.success,
      }),
    );
    if (result?.success) setAmount(0);
  };

  return (
    <>
      <Banner image={art("transfers")} title="Carriers">
        Carriers haul goods to any kingdom, usually an ally in need. They come home when the goods are delivered.
      </Banner>
      <div className="stack">
        <Panel
          title="Send goods"
          lede={`You have ${plural(carriers, "carrier", "carriers")} at home. Each carries ${config.carrierCapacity.food} food or ${config.carrierCapacity.iron} iron, tools or weapons.`}
        >
          <div className="row">
            <label className="field" style={{ minWidth: 240 }}>
              <span>To</span>
              <select className="select" value={target} onChange={event => setTarget(event.target.value)}>
                <option value="">Choose a kingdom</option>
                {targets.map(rank => (
                  <option key={rank.kingdomName} value={rank.kingdomName}>
                    {rank.kingdomName}
                    {allies.has(rank.kingdomName) ? ", ally" : ""}
                  </option>
                ))}
              </select>
            </label>
            <label className="field">
              <span>Goods</span>
              <select className="select" value={resource} onChange={event => setResource(event.target.value as MarketResource)}>
                {marketResources.map(name => (
                  <option key={name} value={name}>
                    {marketResourceLabel[name]}, {formatNumber(kingdom.resources[name])} in store
                  </option>
                ))}
              </select>
            </label>
            <div style={{ width: 170 }}>
              <AmountField label="Amount" value={amount} onChange={setAmount} max={canCarry} />
            </div>
            <button className="button" type="button" disabled={!target || amount <= 0 || needed > carriers} onClick={send}>
              Send {needed > 0 ? plural(needed, "carrier", "carriers") : "carriers"}
            </button>
          </div>
          {needed > carriers ? (
            <p className="warning overview-note">That needs {plural(needed, "carrier", "carriers")}; train more in a market.</p>
          ) : null}
        </Panel>
        <Panel title="On the road">
          {kingdom.carriersOnTheMove.length === 0 ? (
            <p className="muted">No carriers are out.</p>
          ) : (
            <div className="table-scroll">
              <table className="ledger">
                <thead>
                  <tr>
                    <th>To</th>
                    <th className="number">Goods</th>
                    <th className="number">Carriers</th>
                    <th className="number">Arrive in</th>
                    <th />
                  </tr>
                </thead>
                <tbody>
                  {kingdom.carriersOnTheMove.map(move => (
                    <tr key={move.id}>
                      <td>{move.to}</td>
                      <td className="number">
                        {formatNumber(move.amount)} {marketResourceLabel[move.resource].toLowerCase()}
                      </td>
                      <td className="number">{formatNumber(move.carriersCount)}</td>
                      <td className="number">{plural(move.turnsLeft, "turn", "turns")}</td>
                      <td className="number">
                        <button
                          className="button quiet small"
                          type="button"
                          onClick={() =>
                            act(
                              () => api.recallCarriers(move.id),
                              () => ({ text: "The carriers turned back with the goods" }),
                            )
                          }
                        >
                          Call back
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </Panel>
      </div>
    </>
  );
};
