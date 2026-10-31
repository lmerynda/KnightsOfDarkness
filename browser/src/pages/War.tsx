import React, { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api/endpoints";
import type { AttackType, BattleReport, Counts, GameConfig, MilitaryUnit, ResourceName, SpyReport, UnitName } from "../api/types";
import { militaryUnits } from "../api/types";
import { useGame } from "../game/useGame";
import { art, resourceLabel, unitLore } from "../game/lore";
import { targetProblem } from "../game/rules";
import { Crest } from "../ui/Crest";
import { AmountField, Banner, Choice, Panel } from "../ui/parts";
import { ago, formatNumber, plural } from "../ui/format";

/** defense estimated from a spy report, using the same rules the battle uses */
const estimatedDefense = (report: SpyReport, config: GameConfig): number => {
  const units = report.units;
  const base =
    militaryUnits.reduce((sum, unit) => sum + (units[unit] ?? 0) * (config.attack.unitDefensePower[unit] ?? 0), 0) +
    (units.guard ?? 0) * (config.attack.unitDefensePower.guard ?? 0);
  const towers = Math.min(config.attack.maxTowerDefenseBonus, (report.buildings.tower ?? 0) * config.attack.towerDefenseBonusPerTower);
  const castles = (report.buildings.castle ?? 0) * config.attack.castleDefenseBonusPerCastle;
  return base * (1 + towers + castles);
};

const unitList = (units: Partial<Counts<UnitName>>): string => {
  const parts = militaryUnits
    .filter(unit => (units[unit] ?? 0) > 0)
    .map(
      unit =>
        `${formatNumber(units[unit] ?? 0)} ${(units[unit] ?? 0) === 1 ? unitLore[unit].label.toLowerCase() : unitLore[unit].plural.toLowerCase()}`,
    );
  return parts.length ? parts.join(", ") : "nobody";
};

const SendArmy: React.FC<{ spyReports: SpyReport[]; onSent: () => void }> = ({ spyReports, onSent }) => {
  const { kingdom, config, rankings, act } = useGame();
  const [target, setTarget] = useState("");
  const [attackType, setAttackType] = useState<AttackType>("land");
  const [army, setArmy] = useState<Partial<Counts<MilitaryUnit>>>({});

  const targets = (rankings?.kingdoms ?? []).filter(rank => rank.kingdomName !== kingdom.name);
  const chosen = targets.find(rank => rank.kingdomName === target);
  const problem = chosen ? targetProblem(chosen, kingdom.name, kingdom.allianceName, kingdom.resources.land, config) : null;
  const sending = militaryUnits.reduce((sum, unit) => sum + (army[unit] ?? 0), 0);
  const attackPower = militaryUnits.reduce((sum, unit) => sum + (army[unit] ?? 0) * (config.attack.unitAttackPower[unit] ?? 0), 0);
  const intel = spyReports.find(report => report.success && report.targetKingdomName === target);
  const defense = intel ? estimatedDefense(intel, config) : null;

  const send = async (): Promise<void> => {
    const result = await act(
      () => api.sendAttack(target, attackType, army),
      outcome => ({
        text: outcome.success
          ? `The army marches on ${target}, it arrives in ${plural(config.attack.turnsToArrive, "turn", "turns")}`
          : outcome.message,
        failure: !outcome.success,
      }),
    );
    if (result?.success) {
      setArmy({});
      onSent();
    }
  };

  return (
    <Panel
      title="Send an army"
      lede={`Armies arrive after ${plural(config.attack.turnsToArrive, "turn", "turns")} of your own. Archers on both sides loose first, then the lines meet. Win and you take up to ${Math.round(config.attack.maxLandCaptureRatio * 100)}% of their land, or plunder up to ${Math.round(config.attack.maxPlunderRatio * 100)}% of their stores; crushing victories take the most.`}
    >
      <div className="stack">
        <div className="two-columns compact">
          <label className="field">
            <span>Target</span>
            <select className="select" value={target} onChange={event => setTarget(event.target.value)}>
              <option value="">Choose a kingdom</option>
              {targets.map(rank => {
                const reason = targetProblem(rank, kingdom.name, kingdom.allianceName, kingdom.resources.land, config);
                return (
                  <option key={rank.kingdomName} value={rank.kingdomName} disabled={reason !== null}>
                    {rank.kingdomName}, {formatNumber(rank.land)} land{reason ? `, ${reason}` : ""}
                  </option>
                );
              })}
            </select>
          </label>
          <div className="field">
            <span>Goal</span>
            <Choice
              label="Goal of the attack"
              value={attackType}
              onChange={setAttackType}
              options={[
                { value: "land", label: "Take land" },
                { value: "economy", label: "Plunder stores" },
              ]}
            />
          </div>
        </div>
        <div className="army-picker">
          {militaryUnits.map(unit => (
            <div key={unit} className="army-unit">
              <img src={art(unit)} alt="" />
              <div>
                <strong>{unitLore[unit].plural}</strong>
                <span className="muted">
                  {formatNumber(kingdom.units.availableUnits[unit])} at home, attack {config.attack.unitAttackPower[unit]} each
                </span>
              </div>
              <AmountField
                label={`${unitLore[unit].plural} to send`}
                hideLabel
                value={army[unit] ?? 0}
                onChange={count => setArmy(current => ({ ...current, [unit]: count }))}
                max={kingdom.units.availableUnits[unit]}
              />
            </div>
          ))}
        </div>
        <div className="row">
          <button className="button" type="button" disabled={!chosen || problem !== null || sending === 0} onClick={send}>
            March
          </button>
          <span className="muted">
            Attack strength {formatNumber(attackPower)}
            {target
              ? defense !== null
                ? `, ${target} defended with about ${formatNumber(defense)} when your spies last looked (${ago(intel?.date ?? "")})`
                : `, ${target}'s defense is unknown; `
              : ""}
            {target && defense === null ? <Link to="/kingdom/spies">send spies first</Link> : null}
          </span>
        </div>
      </div>
    </Panel>
  );
};

const Marching: React.FC = () => {
  const { kingdom, act } = useGame();
  if (kingdom.ongoingAttacks.length === 0) {
    return null;
  }
  return (
    <Panel title="Armies on the march">
      <div className="table-scroll">
        <table className="ledger">
          <thead>
            <tr>
              <th>Target</th>
              <th>Goal</th>
              <th>Army</th>
              <th className="number">Arrives in</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {kingdom.ongoingAttacks.map(attack => (
              <tr key={attack.id}>
                <td>{attack.to}</td>
                <td>{attack.attackType === "land" ? "Take land" : "Plunder"}</td>
                <td>{unitList(attack.units)}</td>
                <td className="number">{plural(attack.turnsLeft, "turn", "turns")}</td>
                <td className="number">
                  <button
                    className="button quiet small"
                    type="button"
                    onClick={() =>
                      act(
                        () => api.recallAttack(attack.id),
                        () => ({ text: "The army turned back home" }),
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
    </Panel>
  );
};

const BattleReportCard: React.FC<{ report: BattleReport; own: string }> = ({ report, own }) => {
  const attacking = report.attacker === own;
  const won = attacking === report.attackerWon;
  const other = attacking ? report.defender : report.attacker;
  const ourLosses = attacking ? report.attackerLosses : report.defenderLosses;
  const theirLosses = attacking ? report.defenderLosses : report.attackerLosses;
  const plunder = (Object.entries(report.plunder) as [ResourceName, number][]).filter(([, amount]) => amount > 0);

  return (
    <article className={`battle ${won ? "won" : "lost"}`}>
      <div className="battle-art" style={{ backgroundImage: `url(${art(won ? "victory" : "aftermath")})` }} />
      <div className="battle-body">
        <header className="battle-head">
          <Crest name={other} size={34} />
          <div>
            <h3>
              {won
                ? attacking
                  ? `Victory over ${other}`
                  : `${other} was repelled`
                : attacking
                  ? `Defeat at ${other}`
                  : `${other} broke through`}
            </h3>
            <span className="muted">
              {attacking ? "We attacked" : "We were attacked"} to {report.attackType === "land" ? "take land" : "plunder"},{" "}
              {ago(report.date)}
            </span>
          </div>
        </header>
        <dl className="battle-facts">
          <div>
            <dt>Strength</dt>
            <dd>
              {formatNumber(report.attackPower)} attacking against {formatNumber(report.defensePower)} defending
            </dd>
          </div>
          {attacking ? (
            <div>
              <dt>We sent</dt>
              <dd>{unitList(report.unitsSent)}</dd>
            </div>
          ) : null}
          <div>
            <dt>We lost</dt>
            <dd className={unitList(ourLosses) === "nobody" ? "muted" : "loss"}>{unitList(ourLosses)}</dd>
          </div>
          <div>
            <dt>They lost</dt>
            <dd className={unitList(theirLosses) === "nobody" ? "muted" : "gain"}>{unitList(theirLosses)}</dd>
          </div>
          {report.attackerWon && report.attackType === "land" ? (
            <div>
              <dt>Land</dt>
              <dd className={attacking ? "gain" : "loss"}>
                {attacking ? "Took" : "Lost"} {plural(report.landCaptured, "acre", "acres")}
              </dd>
            </div>
          ) : null}
          {report.attackerWon && plunder.length ? (
            <div>
              <dt>{attacking ? "Plundered" : "Stolen"}</dt>
              <dd className={attacking ? "gain" : "loss"}>
                {plunder.map(([resource, amount]) => `${formatNumber(amount)} ${resourceLabel[resource].toLowerCase()}`).join(", ")}
              </dd>
            </div>
          ) : null}
        </dl>
      </div>
    </article>
  );
};

export const War: React.FC = () => {
  const { kingdom, details } = useGame();
  const [reports, setReports] = useState<BattleReport[] | null>(null);
  const [spyReports, setSpyReports] = useState<SpyReport[]>([]);

  const load = useCallback(() => {
    api
      .battleReports()
      .then(setReports)
      .catch(() => setReports([]));
    api
      .spyReports()
      .then(setSpyReports)
      .catch(() => undefined);
  }, []);

  // battles resolve when turns pass, so reload whenever the kingdom changes
  const turnsKey = useMemo(
    () => `${kingdom.resources.turns}-${kingdom.ongoingAttacks.length}`,
    [kingdom.resources.turns, kingdom.ongoingAttacks.length],
  );
  useEffect(load, [load, turnsKey]);

  return (
    <>
      <Banner image={art("war")} title="Army">
        Your defense at home is {formatNumber(details.defensePower)}: soldiers, guards, towers and castles. Soldiers sent away do not
        defend.
      </Banner>
      <div className="stack">
        <SendArmy spyReports={spyReports} onSent={load} />
        <Marching />
        <Panel title="Battle reports">
          {reports === null ? (
            <p className="muted">Reading the reports</p>
          ) : reports.length === 0 ? (
            <p className="muted">No battles yet this round. When your army arrives, or someone attacks you, the outcome is written here.</p>
          ) : (
            <div className="battles">
              {reports.map(report => (
                <BattleReportCard key={report.id} report={report} own={kingdom.name} />
              ))}
            </div>
          )}
        </Panel>
      </div>
    </>
  );
};
