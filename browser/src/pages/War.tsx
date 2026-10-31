import React, { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api/endpoints";
import type { AttackType, BattleReport, Counts, MilitaryUnit, ResourceName, SpyReport, UnitName, WarRoom } from "../api/types";
import { militaryUnits } from "../api/types";
import { useGame } from "../game/useGame";
import { art, resourceLabel, unitLore } from "../game/lore";
import { estimatedDefense, targetProblem } from "../game/rules";
import { Crest } from "../ui/Crest";
import { Icon } from "../ui/Icon";
import { AmountField, Banner, Choice, Panel } from "../ui/parts";
import { ago, formatNumber, plural } from "../ui/format";

const unitList = (units: Partial<Counts<UnitName>>): string => {
  const parts = [...militaryUnits, "guard" as const]
    .filter(unit => (units[unit] ?? 0) > 0)
    .map(
      unit =>
        `${formatNumber(units[unit] ?? 0)} ${(units[unit] ?? 0) === 1 ? unitLore[unit].label.toLowerCase() : unitLore[unit].plural.toLowerCase()}`,
    );
  return parts.length ? parts.join(", ") : "nobody";
};

/** how the fallen fell: soldiers to arrows or in melee, guards on the walls once the battle was decided */
const howTheyFell = (losses: Partial<Counts<UnitName>>, arrowLosses: Partial<Counts<UnitName>>): string => {
  const soldiers = militaryUnits.reduce((sum, unit) => sum + (losses[unit] ?? 0), 0);
  const toArrows = militaryUnits.reduce((sum, unit) => sum + (arrowLosses[unit] ?? 0), 0);
  const guards = losses.guard ?? 0;
  return [
    toArrows > 0 ? `${formatNumber(toArrows)} to arrows` : "",
    soldiers - toArrows > 0 ? `${formatNumber(soldiers - toArrows)} in melee` : "",
    guards > 0 ? `${formatNumber(guards)} on the walls` : "",
  ]
    .filter(Boolean)
    .join(", ");
};

const Losses: React.FC<{ losses: Partial<Counts<UnitName>>; arrowLosses: Partial<Counts<UnitName>>; tone: "loss" | "gain" }> = ({
  losses,
  arrowLosses,
  tone,
}) => {
  const fallen = unitList(losses);
  if (fallen === "nobody") return <dd className="muted">nobody</dd>;
  return (
    <dd className={tone}>
      {fallen}
      <span className="muted block">{howTheyFell(losses, arrowLosses)}</span>
    </dd>
  );
};

const SendArmy: React.FC<{ spyReports: SpyReport[]; target: string; setTarget: (target: string) => void; onSent: () => void }> = ({
  spyReports,
  target,
  setTarget,
  onSent,
}) => {
  const { kingdom, config, rankings, act } = useGame();
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
      lede={`Armies arrive after ${plural(config.attack.turnsToArrive, "turn", "turns")} of your own. Archers on both sides loose first, then infantry and cavalry cut into the whole enemy army; bowmen strike nobody in melee but still fall. Win and you take up to ${Math.round(config.attack.maxLandCaptureRatio * 100)}% of their land, or plunder up to ${Math.round(config.attack.maxPlunderRatio * 100)}% of their stores; crushing victories take the most.`}
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
                ? `, ${target} defended with about ${formatNumber(defense)} when ${intel?.kingdomName === kingdom.name ? "your" : `${intel?.kingdomName}'s`} spies last looked (${ago(intel?.date ?? "")})`
                : `, ${target}'s defense is unknown; `
              : ""}
            {target && defense === null ? <Link to="/kingdom/spies">send spies first</Link> : null}
          </span>
        </div>
        {kingdom.protectionTicks > 0 ? (
          <p className="notice">
            <Icon name="shield" size={18} /> You are protected for {plural(kingdom.protectionTicks, "more turn", "more turns")}. Marching
            out ends your protection.
          </p>
        ) : null}
      </div>
    </Panel>
  );
};

const soldiersIn = (units: Partial<Counts<UnitName>>): number => militaryUnits.reduce((sum, unit) => sum + (units[unit] ?? 0), 0);

/** allies stand guard in each other's kingdoms: send soldiers, see who stands where, and call them home */
const StandGuard: React.FC = () => {
  const { kingdom, rankings, act } = useGame();
  const [host, setHost] = useState("");
  const [guard, setGuard] = useState<Partial<Counts<MilitaryUnit>>>({});
  if (!kingdom.allianceName) return null;
  const allies = (rankings?.kingdoms ?? []).filter(rank => rank.allianceName === kingdom.allianceName && rank.kingdomName !== kingdom.name);
  const sending = soldiersIn(guard);

  const send = async (): Promise<void> => {
    const result = await act(
      () => api.sendReinforcements(host, guard),
      outcome => ({ text: outcome.message, failure: !outcome.success }),
    );
    if (result?.success) setGuard({});
  };
  const recall = (id: string): void =>
    void act(
      () => api.recallReinforcements(id),
      () => ({ text: "Our soldiers are home again" }),
    );

  return (
    <Panel
      title="Stand guard with allies"
      lede="Soldiers sent to an ally reach it after two of your turns and defend it beside its own, sharing its losses. They stay yours: you feed them and can call them home at once."
    >
      <div className="stack">
        <label className="field">
          <span>Ally</span>
          <select className="select" value={host} onChange={event => setHost(event.target.value)}>
            <option value="">Choose an ally</option>
            {allies.map(ally => (
              <option key={ally.kingdomName} value={ally.kingdomName}>
                {ally.kingdomName}, {formatNumber(ally.land)} land
              </option>
            ))}
          </select>
        </label>
        <div className="army-picker">
          {militaryUnits.map(unit => (
            <AmountField
              key={unit}
              label={`${unitLore[unit].plural} to send, ${formatNumber(kingdom.units.availableUnits[unit])} at home`}
              value={guard[unit] ?? 0}
              onChange={count => setGuard(current => ({ ...current, [unit]: count }))}
              max={kingdom.units.availableUnits[unit]}
            />
          ))}
        </div>
        <div className="row">
          <button className="button" type="button" disabled={!host || sending === 0} onClick={send}>
            Send to stand guard
          </button>
        </div>
        {kingdom.reinforcements.length ? (
          <ul className="plain-list">
            {kingdom.reinforcements.map(sent => (
              <li key={sent.id}>
                <span>
                  {unitList(sent.units)}{" "}
                  {sent.turnsLeft > 0
                    ? `on the way to ${sent.to}, ${plural(sent.turnsLeft, "turn", "turns")} left`
                    : `stand guard in ${sent.to}`}
                </span>
                <button className="button quiet small" type="button" onClick={() => recall(sent.id)}>
                  Call home
                </button>
              </li>
            ))}
          </ul>
        ) : null}
        {kingdom.garrisons.length ? (
          <ul className="plain-list">
            {kingdom.garrisons.map(garrison => (
              <li key={garrison.id}>
                <span>
                  {unitList(garrison.units)} from {garrison.from}{" "}
                  {garrison.turnsLeft > 0
                    ? `arrive in ${plural(garrison.turnsLeft, "of their turns", "of their turns")}`
                    : "stand guard with us"}
                </span>
              </li>
            ))}
          </ul>
        ) : null}
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
              <th>Army</th>
              <th className="number">Arrives in</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {kingdom.ongoingAttacks.map(attack => (
              <tr key={attack.id}>
                <td>
                  {attack.to}
                  <span className="muted block">{attack.attackType === "land" ? "to take land" : "to plunder"}</span>
                </td>
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
  // a defender's losses include the allied garrisons that stood with it; those are told apart
  const alliedLosses = report.alliedLosses ?? {};
  const ownDefenderLosses = Object.fromEntries(
    Object.entries(report.defenderLosses).map(([unit, count]) => [unit, (count ?? 0) - (alliedLosses[unit as UnitName] ?? 0)]),
  ) as Partial<Counts<UnitName>>;
  const ourLosses = attacking ? report.attackerLosses : ownDefenderLosses;
  const theirLosses = attacking ? report.defenderLosses : report.attackerLosses;
  const ourArrowLosses = attacking ? report.attackerArrowLosses : report.defenderArrowLosses;
  const theirArrowLosses = attacking ? report.defenderArrowLosses : report.attackerArrowLosses;
  const plunder = (Object.entries(report.plunder) as [ResourceName, number][]).filter(([, amount]) => amount > 0);

  return (
    <article className={`battle ${won ? "won" : "lost"}`}>
      <div className="battle-art" style={{ backgroundImage: `url(${art(won ? "victory" : attacking ? "defeat" : "breached")})` }} />
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
            <Losses losses={ourLosses} arrowLosses={ourArrowLosses} tone="loss" />
          </div>
          <div>
            <dt>They lost</dt>
            <Losses losses={theirLosses} arrowLosses={theirArrowLosses} tone="gain" />
          </div>
          {soldiersIn(alliedLosses) > 0 ? (
            <div>
              <dt>{attacking ? "Of them, allies" : "Allies lost"}</dt>
              <dd className={attacking ? "gain" : "loss"}>{unitList(alliedLosses)}</dd>
            </div>
          ) : null}
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

/** allies' armies on the march, so a ruler can send theirs to land in the same strike */
const AlliedMarches: React.FC<{ warRoom: WarRoom | undefined; onJoin: (target: string) => void }> = ({ warRoom, onJoin }) => {
  const { kingdom } = useGame();
  const marches = (warRoom?.marching ?? []).filter(march => march.from !== kingdom.name);
  if (!marches.length) return null;
  return (
    <Panel
      title="Allies on the march"
      lede="Armies that land on the same turn strike together, and a beaten kingdom is protected only from armies sent after its defeat."
    >
      <ul className="plain-list">
        {marches.map(march => (
          <li key={march.id}>
            <span>
              {march.from} marches {unitList(march.units)} on <strong>{march.to}</strong> to{" "}
              {march.attackType === "land" ? "take land" : "plunder"}, arriving in{" "}
              {plural(march.turnsLeft, "of their turns", "of their turns")}
            </span>
            <button className="button quiet small" type="button" onClick={() => onJoin(march.to)}>
              Join the strike
            </button>
          </li>
        ))}
      </ul>
    </Panel>
  );
};

export const War: React.FC = () => {
  const { kingdom, details } = useGame();
  const [reports, setReports] = useState<BattleReport[] | null>(null);
  const [spyReports, setSpyReports] = useState<SpyReport[]>([]);
  const [warRoom, setWarRoom] = useState<WarRoom | undefined>(undefined);
  const [target, setTarget] = useState("");

  const load = useCallback(() => {
    api
      .battleReports()
      .then(setReports)
      .catch(() => setReports([]));
    api
      .spyReports()
      .then(setSpyReports)
      .catch(() => undefined);
    api
      .warRoom()
      .then(setWarRoom)
      .catch(() => undefined);
  }, []);
  // the freshest intel first, whether our spies or an ally's brought it back
  const intel = useMemo(
    () => [...spyReports, ...(warRoom?.intel ?? [])].sort((one, other) => other.date.localeCompare(one.date)),
    [spyReports, warRoom],
  );
  const join = (to: string): void => {
    setTarget(to);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  // battles resolve when turns pass, so reload whenever the kingdom changes
  const turnsKey = useMemo(
    () => `${kingdom.resources.turns}-${kingdom.ongoingAttacks.length}`,
    [kingdom.resources.turns, kingdom.ongoingAttacks.length],
  );
  useEffect(load, [load, turnsKey]);

  return (
    <>
      <Banner image={art("war")} title="Army">
        Your defense at home is {formatNumber(details.defensePower)}: soldiers, guards, allies standing guard, towers and castles. Soldiers
        sent away do not defend.
      </Banner>
      <div className="stack">
        <SendArmy spyReports={intel} target={target} setTarget={setTarget} onSent={load} />
        <AlliedMarches warRoom={warRoom} onJoin={join} />
        <Marching />
        <StandGuard />
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
