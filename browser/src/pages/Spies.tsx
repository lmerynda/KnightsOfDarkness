import React, { useCallback, useEffect, useState } from "react";
import { api } from "../api/endpoints";
import type { BuildingName, ResourceName, SpyReport, UnitName } from "../api/types";
import { buildingNames, unitNames } from "../api/types";
import { useGame } from "../game/useGame";
import { art, buildingLore, resourceLabel, unitLore } from "../game/lore";
import { Crest } from "../ui/Crest";
import { AmountField, Banner, Panel } from "../ui/parts";
import { ago, formatNumber, plural } from "../ui/format";

const shownResources: ResourceName[] = ["land", "gold", "food", "iron", "tools", "weapons", "unemployed", "turns"];

const IntelColumn: React.FC<{ title: string; rows: [string, number][] }> = ({ title, rows }) => (
  <div>
    <h4 className="intel-title">{title}</h4>
    <table className="ledger compact-ledger">
      <tbody>
        {rows.map(([label, value]) => (
          <tr key={label}>
            <td>{label}</td>
            <td className="number">{formatNumber(value)}</td>
          </tr>
        ))}
      </tbody>
    </table>
  </div>
);

const SpyReportCard: React.FC<{ report: SpyReport }> = ({ report }) => (
  <article className="intel">
    <header className="battle-head">
      <Crest name={report.targetKingdomName} size={34} />
      <div>
        <h3>{report.success ? `Inside ${report.targetKingdomName}` : `Caught in ${report.targetKingdomName}`}</h3>
        <span className="muted">
          {plural(report.spiesSent, "spy", "spies")} sent, {ago(report.date)}
          {report.success ? "" : `, ${plural(report.spiesLost, "spy", "spies")} never came back`}
        </span>
      </div>
    </header>
    {report.success ? (
      <div className="intel-grid">
        <IntelColumn title="Stores" rows={shownResources.map(resource => [resourceLabel[resource], report.resources[resource] ?? 0])} />
        <IntelColumn
          title="People at home"
          rows={unitNames
            .filter(unit => (report.units[unit] ?? 0) > 0)
            .map((unit: UnitName) => [unitLore[unit].plural, report.units[unit] ?? 0])}
        />
        <IntelColumn
          title="Buildings"
          rows={buildingNames
            .filter(building => (report.buildings[building] ?? 0) > 0)
            .map((building: BuildingName) => [buildingLore[building].plural, report.buildings[building] ?? 0])}
        />
      </div>
    ) : null}
  </article>
);

export const Spies: React.FC = () => {
  const { kingdom, details, config, rankings, act } = useGame();
  const [target, setTarget] = useState("");
  const [spies, setSpies] = useState(0);
  const [reports, setReports] = useState<SpyReport[] | null>(null);

  const load = useCallback(() => {
    api
      .spyReports()
      .then(setReports)
      .catch(() => setReports([]));
  }, []);
  useEffect(load, [load]);

  const available = kingdom.units.availableUnits.spy;
  const targets = (rankings?.kingdoms ?? []).filter(rank => rank.kingdomName !== kingdom.name);

  const send = async (): Promise<void> => {
    const result = await act(
      () => api.sendSpies(target, spies),
      outcome => ({ text: outcome.message, failure: !outcome.success || outcome.report?.success === false }),
    );
    if (result) {
      setSpies(0);
      load();
    }
  };

  return (
    <>
      <Banner image={art("spies")} title="Spies">
        Spies slip in and out at once, no turn needed. They succeed when they outnumber the target's counter intelligence:{" "}
        {config.espionage.counterIntelligencePerSpy} for each of its spies and {config.espionage.counterIntelligencePerGuard} for each
        guard. Yours stands at {formatNumber(details.counterIntelligence)}.
      </Banner>
      <div className="stack">
        <Panel
          title="Send spies"
          lede={`Caught spies are lost, about ${Math.round(config.espionage.failedMissionLossRatio * 100)}% of those sent, and the target learns who sent them.`}
        >
          <div className="row">
            <label className="field" style={{ minWidth: 260 }}>
              <span>Target</span>
              <select className="select" value={target} onChange={event => setTarget(event.target.value)}>
                <option value="">Choose a kingdom</option>
                {targets.map(rank => (
                  <option key={rank.kingdomName} value={rank.kingdomName}>
                    {rank.kingdomName}, {formatNumber(rank.land)} land
                  </option>
                ))}
              </select>
            </label>
            <div style={{ width: 180 }}>
              <AmountField label={`Spies, ${formatNumber(available)} at home`} value={spies} onChange={setSpies} max={available} />
            </div>
            <button className="button" type="button" disabled={!target || spies <= 0 || spies > available} onClick={send}>
              Send spies
            </button>
          </div>
          {available === 0 ? <p className="muted overview-note">You have no spies. Build a spy guild and train some first.</p> : null}
        </Panel>
        <Panel title="Reports">
          {reports === null ? (
            <p className="muted">Reading the reports</p>
          ) : reports.length === 0 ? (
            <p className="muted">No missions yet this round.</p>
          ) : (
            <div className="battles">
              {reports.map(report => (
                <SpyReportCard key={report.id} report={report} />
              ))}
            </div>
          )}
        </Panel>
      </div>
    </>
  );
};
