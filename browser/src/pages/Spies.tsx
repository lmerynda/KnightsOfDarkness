import React, { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api/endpoints";
import type { BattleReport, BuildingName, ResourceName, SpyReport, UnitName } from "../api/types";
import { buildingNames, unitNames } from "../api/types";
import { useGame } from "../game/useGame";
import { art, buildingLore, resourceLabel, unitLore } from "../game/lore";
import { alliesUnseen, estimatedDefense } from "../game/rules";
import { Crest } from "../ui/Crest";
import { CountField, Banner, Panel } from "../ui/parts";
import { ago, formatNumber, plural, signed } from "../ui/format";
import { Term } from "../ui/Tip";
import { counterIntelligenceHelp } from "../game/help";

const shownResources: ResourceName[] = ["iron", "tools", "weapons", "unemployed", "turns"];

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

/** everything our spies know of one kingdom: their newest look, the newest that got inside, and the one before it */
type Dossier = { target: string; newest: SpyReport; seen: SpyReport | undefined; before: SpyReport | undefined; looks: number };

/** one dossier a kingdom, the one looked at last first; the reports come newest first */
const dossiersOf = (reports: SpyReport[]): Dossier[] => {
  const targets = [...new Set(reports.map(report => report.targetKingdomName))];
  return targets.map(target => {
    const looks = reports.filter(report => report.targetKingdomName === target);
    const inside = looks.filter(report => report.success);
    return { target, newest: looks[0], seen: inside[0], before: inside[1], looks: looks.length };
  });
};

/** a figure from the newest look that got inside, and how it moved since the look before */
const Watched: React.FC<{ label: string; resource: ResourceName; dossier: Dossier }> = ({ label, resource, dossier }) => {
  const now = dossier.seen?.resources[resource];
  const change = dossier.before && now !== undefined ? now - (dossier.before.resources[resource] ?? 0) : 0;
  return (
    <td className="number" data-label={label}>
      {now === undefined ? <span className="muted">unknown</span> : formatNumber(now)}
      {change ? <span className={`dossier-change ${change > 0 ? "gain" : "loss"}`}>{signed(change)}</span> : null}
    </td>
  );
};

const DossierRows: React.FC<{ dossier: Dossier; battles: BattleReport[]; open: boolean; onToggle: () => void }> = ({
  dossier,
  battles,
  open,
  onToggle,
}) => {
  const { kingdom, config } = useGame();
  const { target, newest, seen, looks } = dossier;
  const estimate = seen ? estimatedDefense(seen, config) : null;
  const unseen = estimate === null ? null : alliesUnseen(target, estimate, battles);
  return (
    <>
      <tr className={`dossier ${open ? "open" : ""}`}>
        <td>
          <span className="figure">
            <Crest name={target} size={26} /> <strong className="dossier-name">{target}</strong>
          </span>
          <span className="muted block small-note">
            {newest.success
              ? `looked ${ago(newest.date)}`
              : `caught ${ago(newest.date)}, ${plural(newest.spiesLost, "spy", "spies")} never came back`}
          </span>
        </td>
        <Watched label="Land" resource="land" dossier={dossier} />
        <Watched label="Gold" resource="gold" dossier={dossier} />
        <Watched label="Food" resource="food" dossier={dossier} />
        <td className={`number ${unseen ? "warning" : ""}`} data-label="Defends with">
          {estimate === null ? <span className="muted">unknown</span> : `about ${formatNumber(estimate)}`}
        </td>
        <td className="number" data-label="Looks">
          {looks}
        </td>
        <td className="number dossier-actions">
          {seen ? (
            <button className="button quiet small" type="button" aria-expanded={open} onClick={onToggle}>
              {open ? "Less" : "More"}
            </button>
          ) : null}
          {target === kingdom.name ? null : (
            <Link className="button quiet small" to={`/kingdom/war?target=${encodeURIComponent(target)}`}>
              Target
            </Link>
          )}
        </td>
      </tr>
      {unseen ? (
        <tr className="dossier-note">
          <td colSpan={7}>
            <p className="warning">
              Spies see only who is at home. An army here last met {formatNumber(unseen.defensePower)} defense: allies stand guard.
            </p>
          </td>
        </tr>
      ) : null}
      {open && seen ? (
        <tr className="dossier-detail">
          <td colSpan={7}>
            <div className="intel-grid">
              <IntelColumn title="Stores" rows={shownResources.map(resource => [resourceLabel[resource], seen.resources[resource] ?? 0])} />
              <IntelColumn
                title="People at home"
                rows={unitNames
                  .filter(unit => (seen.units[unit] ?? 0) > 0)
                  .map((unit: UnitName) => [unitLore[unit].plural, seen.units[unit] ?? 0])}
              />
              <IntelColumn
                title="Buildings"
                rows={buildingNames
                  .filter(building => (seen.buildings[building] ?? 0) > 0)
                  .map((building: BuildingName) => [buildingLore[building].plural, seen.buildings[building] ?? 0])}
              />
            </div>
          </td>
        </tr>
      ) : null}
    </>
  );
};

export const Spies: React.FC = () => {
  const { kingdom, details, config, rankings, act } = useGame();
  const [target, setTarget] = useState("");
  const [spies, setSpies] = useState(0);
  const [reports, setReports] = useState<SpyReport[] | null>(null);
  const [battles, setBattles] = useState<BattleReport[]>([]);
  // the newest dossier stands open, until the ruler chooses another or closes it
  const [chosen, setChosen] = useState<string | null | undefined>(undefined);

  const load = useCallback(() => {
    api
      .spyReports()
      .then(setReports)
      .catch(() => setReports([]));
    // what an army of ours or an ally's met shows what spies cannot see
    Promise.all([api.battleReports(), api.warRoom().catch(() => undefined)])
      .then(([own, room]) => setBattles([...own, ...(room?.battles ?? [])]))
      .catch(() => undefined);
  }, []);
  useEffect(load, [load]);

  const available = kingdom.units.availableUnits.spy;
  const targets = (rankings?.kingdoms ?? []).filter(rank => rank.kingdomName !== kingdom.name);
  const dossiers = dossiersOf(reports ?? []);
  const opened = chosen === undefined ? dossiers[0]?.target : chosen;

  const send = async (): Promise<void> => {
    const result = await act(
      () => api.sendSpies(target, spies),
      outcome => ({ text: outcome.message, failure: !outcome.success || outcome.data?.success === false }),
    );
    if (result) {
      setSpies(0);
      setChosen(undefined);
      load();
    }
  };

  return (
    <>
      <Banner image={art("spies")} title="Spies" slim>
        <Term tip={counterIntelligenceHelp(config)}>Counter intelligence</Term> {formatNumber(details.counterIntelligence)}
      </Banner>
      <div className="stack">
        <Panel
          title="Send spies"
          help={`Spies slip in and out at once, no turn needed, and succeed when they outnumber the target's counter intelligence. Caught spies are lost, about ${Math.round(config.espionage.failedMissionLossRatio * 100)}% of those sent, and the target learns who sent them. Spies see only who is at home, never allies standing guard.`}
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
              <CountField label={`Spies, ${formatNumber(available)} at home`} value={spies} onChange={setSpies} max={available} />
            </div>
            <button className="button" type="button" disabled={!target || spies <= 0 || spies > available} onClick={send}>
              Send spies
            </button>
          </div>
          {available === 0 ? <p className="muted overview-note">No spies at home.</p> : null}
        </Panel>
        <Panel title="What our spies saw">
          {reports === null ? (
            <p className="muted">Reading the reports</p>
          ) : dossiers.length === 0 ? (
            <p className="muted">No missions yet this era.</p>
          ) : (
            <table className="ledger dossiers">
              <thead>
                <tr>
                  <th>Kingdom</th>
                  <th className="number">Land</th>
                  <th className="number">Gold</th>
                  <th className="number">Food</th>
                  <th className="number">Defends with</th>
                  <th className="number">Looks</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {dossiers.map(dossier => (
                  <DossierRows
                    key={dossier.target}
                    dossier={dossier}
                    battles={battles}
                    open={opened === dossier.target}
                    onToggle={() => setChosen(opened === dossier.target ? null : dossier.target)}
                  />
                ))}
              </tbody>
            </table>
          )}
        </Panel>
      </div>
    </>
  );
};
