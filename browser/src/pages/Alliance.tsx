import React, { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api/endpoints";
import type { Alliance, AllianceInvitation, AllianceSummary, MarketOffer, WarRoom } from "../api/types";
import { marketResources } from "../api/types";
import { useGame } from "../game/useGame";
import { art, marketResourceLabel } from "../game/lore";
import { estimatedDefense } from "../game/rules";
import { Crest } from "../ui/Crest";
import { CountField, Banner, Panel } from "../ui/parts";
import { ago, formatNumber, plural } from "../ui/format";

const Members: React.FC<{ alliance: Alliance; onChange: () => void }> = ({ alliance, onChange }) => {
  const { kingdom, config, rankings, act } = useGame();
  const [invitee, setInvitee] = useState("");
  const [botName, setBotName] = useState("");
  const emperor = alliance.emperor === kingdom.name;
  const land = (name: string): number => rankings?.kingdoms.find(rank => rank.kingdomName === name)?.land ?? 0;
  const members = [...alliance.members].sort((a, b) => land(b) - land(a));
  const candidates = (rankings?.kingdoms ?? []).filter(rank => !rank.allianceName);
  const full = alliance.members.length >= config.common.allianceMaxMembers;

  const run = async (action: () => Promise<{ message: string; success: boolean }>): Promise<void> => {
    await act(action, outcome => ({ text: outcome.message, failure: !outcome.success }));
    onChange();
  };

  return (
    <>
      <Panel
        title={alliance.name}
        lede={`${alliance.members.length} of ${config.common.allianceMaxMembers} members, led by ${alliance.emperor}.`}
        help="Allies cannot attack each other and rise together in the alliance rankings. Write to everyone at once from the letters page."
      >
        <div className="table-scroll">
          <table className="ledger">
            <thead>
              <tr>
                <th>Member</th>
                <th className="number">Land</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {members.map(member => (
                <tr key={member} className={member === kingdom.name ? "mine" : undefined}>
                  <td>
                    <span className="figure">
                      <Crest name={member} size={26} /> {member}
                      {member === alliance.emperor ? <span className="muted">emperor</span> : null}
                    </span>
                  </td>
                  <td className="number">{formatNumber(land(member))}</td>
                  <td className="number">
                    {emperor && member !== kingdom.name ? (
                      <button
                        className="button danger small"
                        type="button"
                        onClick={() => window.confirm(`Remove ${member} from ${alliance.name}?`) && run(() => api.removeMember(member))}
                      >
                        Remove
                      </button>
                    ) : null}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <div className="row overview-note">
          <Link className="button quiet" to="/kingdom/letters">
            Write to the alliance
          </Link>
          <button
            className="button danger"
            type="button"
            disabled={emperor && alliance.members.length > 1}
            title={emperor && alliance.members.length > 1 ? "The emperor leaves last" : undefined}
            onClick={() =>
              window.confirm(emperor ? "Leave and disband the alliance?" : `Leave ${alliance.name}?`) && run(() => api.leaveAlliance())
            }
          >
            {emperor ? "Leave and disband" : "Leave the alliance"}
          </button>
        </div>
      </Panel>
      {emperor ? (
        <div className="two-columns">
          <Panel title="Invite a kingdom" help="Only kingdoms without an alliance can be invited.">
            <div className="row">
              <label className="field" style={{ minWidth: 220 }}>
                <span>Kingdom</span>
                <select className="select" value={invitee} onChange={event => setInvitee(event.target.value)}>
                  <option value="">Choose a kingdom</option>
                  {candidates.map(rank => (
                    <option key={rank.kingdomName} value={rank.kingdomName}>
                      {rank.kingdomName}, {formatNumber(rank.land)} land
                    </option>
                  ))}
                </select>
              </label>
              <button
                className="button"
                type="button"
                disabled={!invitee || full}
                onClick={() => run(() => api.invite(invitee, alliance.name))}
              >
                Send invitation
              </button>
            </div>
          </Panel>
          <Panel
            title="Raise a vassal"
            help="A vassal is a kingdom run by the game that trades on the market and fills a place in your alliance."
          >
            <div className="row">
              <label className="field" style={{ minWidth: 200 }}>
                <span>Vassal name</span>
                <input className="input" value={botName} onChange={event => setBotName(event.target.value)} />
              </label>
              <button
                className="button quiet"
                type="button"
                disabled={!botName.trim() || full}
                onClick={() => run(() => api.addBot(botName.trim()))}
              >
                Raise vassal
              </button>
            </div>
          </Panel>
        </div>
      ) : null}
    </>
  );
};

const NoAlliance: React.FC<{ onChange: () => void }> = ({ onChange }) => {
  const { act } = useGame();
  const [name, setName] = useState("");
  const [invitations, setInvitations] = useState<AllianceInvitation[]>([]);
  const [alliances, setAlliances] = useState<AllianceSummary[]>([]);

  useEffect(() => {
    api
      .invitations()
      .then(setInvitations)
      .catch(() => undefined);
    api
      .alliances()
      .then(setAlliances)
      .catch(() => undefined);
  }, []);

  const run = async (action: () => Promise<{ message: string; success: boolean }>): Promise<void> => {
    await act(action, outcome => ({ text: outcome.message, failure: !outcome.success }));
    onChange();
  };

  return (
    <div className="stack">
      {invitations.length > 0 ? (
        <Panel title="Invitations">
          <ul className="plain-list">
            {invitations.map(invitation => (
              <li key={invitation.id}>
                <span className="figure">
                  <Crest name={invitation.allianceName} size={26} /> {invitation.allianceName} wants you among them
                </span>
                <span className="row">
                  <button className="button small" type="button" onClick={() => run(() => api.acceptInvitation(invitation.id))}>
                    Join
                  </button>
                  <button className="button quiet small" type="button" onClick={() => run(() => api.rejectInvitation(invitation.id))}>
                    Decline
                  </button>
                </span>
              </li>
            ))}
          </ul>
        </Panel>
      ) : null}
      <div className="two-columns">
        <Panel title="Found an alliance" help="You become its emperor: you invite members, and you leave last.">
          <div className="row">
            <label className="field" style={{ minWidth: 220 }}>
              <span>Alliance name</span>
              <input className="input" value={name} onChange={event => setName(event.target.value)} maxLength={32} />
            </label>
            <button
              className="button"
              type="button"
              disabled={name.trim().length < 3}
              onClick={() => run(() => api.createAlliance(name.trim()))}
            >
              Found it
            </button>
          </div>
        </Panel>
        <Panel title="Alliances of this round" help="Write to an emperor to ask for an invitation.">
          {alliances.length === 0 ? (
            <p className="muted">No alliances yet.</p>
          ) : (
            <ul className="plain-list">
              {alliances.map(alliance => (
                <li key={alliance.name}>
                  <span className="figure">
                    <Crest name={alliance.name} size={26} /> {alliance.name}
                  </span>
                  <span className="muted">led by {alliance.emperor}</span>
                </li>
              ))}
            </ul>
          )}
        </Panel>
      </div>
    </div>
  );
};

/** what the alliance knows together: armies on the march, the freshest intel on each enemy, recent battles */
const WarRoomPanel: React.FC = () => {
  const { config, kingdom } = useGame();
  const [warRoom, setWarRoom] = useState<WarRoom | undefined>(undefined);
  useEffect(() => {
    api
      .warRoom()
      .then(setWarRoom)
      .catch(() => undefined);
    // the kingdom reloads every turn and every half minute, and allies act meanwhile
  }, [kingdom]);
  if (!warRoom) return null;
  const soldiers = (units: Partial<Record<string, number>>): number =>
    Object.values(units).reduce<number>((sum, count) => sum + (count ?? 0), 0);

  return (
    <Panel title="War room" help="What the alliance knows together. Armies that land on the same turn strike together.">
      <div className="two-columns">
        <section>
          {warRoom.threats.length ? (
            <>
              <h3 className="section-title small">Enemies marching on us</h3>
              <ul className="plain-list">
                {warRoom.threats.map((threat, index) => (
                  <li key={`${threat.from}-${threat.to}-${index}`}>
                    <span className="warning">
                      {threat.from} → <strong>{threat.to}</strong>, about {plural(threat.soldiers, "soldier", "soldiers")}
                    </span>
                    <span className="muted">in {plural(threat.turnsLeft, "turn", "turns")}</span>
                  </li>
                ))}
              </ul>
            </>
          ) : null}
          <h3 className="section-title small">Armies on the march</h3>
          {warRoom.marching.length === 0 ? (
            <p className="muted">No allied army is on the road.</p>
          ) : (
            <ul className="plain-list">
              {warRoom.marching.map(march => (
                <li key={march.id}>
                  <span>
                    {march.from} → <strong>{march.to}</strong>, {plural(soldiers(march.units), "soldier", "soldiers")}
                  </span>
                  <span className="muted">
                    {march.joining ? `joins ${march.joining}, ` : ""}
                    {march.joining && march.turnsLeft === 0 ? "at the border" : `in ${plural(march.turnsLeft, "turn", "turns")}`}
                  </span>
                </li>
              ))}
            </ul>
          )}
          <h3 className="section-title small">Standing guard</h3>
          {warRoom.guards.length === 0 ? (
            <p className="muted">No ally keeps soldiers with another.</p>
          ) : (
            <ul className="plain-list">
              {warRoom.guards.map(guard => (
                <li key={guard.id}>
                  <span>
                    {guard.from} → <strong>{guard.to}</strong>, {plural(soldiers(guard.units), "soldier", "soldiers")}
                  </span>
                  <span className="muted">
                    {guard.turnsLeft > 0 ? `arrive in ${plural(guard.turnsLeft, "turn", "turns")}` : "standing"}
                  </span>
                </li>
              ))}
            </ul>
          )}
          <h3 className="section-title small">Recent battles</h3>
          {warRoom.battles.length === 0 ? (
            <p className="muted">No battles yet.</p>
          ) : (
            <ul className="plain-list">
              {warRoom.battles.map(battle => (
                <li key={battle.id}>
                  <span>
                    {battle.attackerWon
                      ? `${battle.attacker} beat ${battle.defender}`
                      : `${battle.defender} held against ${battle.attacker}`}
                    {battle.landCaptured > 0 ? `, ${plural(battle.landCaptured, "acre", "acres")} changed hands` : ""}
                  </span>
                  <span className="muted">{ago(battle.date)}</span>
                </li>
              ))}
            </ul>
          )}
        </section>
        <section>
          <h3 className="section-title small">Members, weakest defended first</h3>
          <ul className="plain-list">
            {warRoom.members.map(member => (
              <li key={member.kingdomName}>
                <span>{member.kingdomName}</span>
                <span className="muted">
                  defense {formatNumber(member.defense)} on {formatNumber(member.land)} acres
                </span>
              </li>
            ))}
          </ul>
          <h3 className="section-title small">What our spies saw</h3>
          {warRoom.intel.length === 0 ? (
            <p className="muted">No ally has spied on anyone yet.</p>
          ) : (
            <ul className="plain-list">
              {warRoom.intel.map(report => (
                <li key={report.id}>
                  <span>
                    <strong>{report.targetKingdomName}</strong> defends with about {formatNumber(estimatedDefense(report, config))}
                    <span className="muted"> by {report.kingdomName}&apos;s spies</span>
                  </span>
                  <span className="muted">{ago(report.date)}</span>
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </Panel>
  );
};

/** allies pass gold through the market: one asks by offering a single piece at a dear price, another buys it */
const GoldPanel: React.FC = () => {
  const { kingdom, config, act } = useGame();
  const [offers, setOffers] = useState<MarketOffer[]>([]);
  const [ask, setAsk] = useState(0);
  const load = useCallback(() => {
    api
      .alliesOffers()
      .then(setOffers)
      .catch(() => setOffers([]));
  }, []);
  useEffect(load, [load, kingdom]);
  const pledge = marketResources.find(resource => kingdom.resources[resource] > 0);
  const asking = kingdom.marketOffers.filter(offer => offer.count === 1);

  return (
    <Panel
      title="Gold between allies"
      help="Carriers cannot carry gold, so allies pass it on the market: ask by offering one piece of goods at the price you need, and an ally sends the gold by buying it. Your allies always see your offers, however dear."
    >
      <div className="row">
        <div style={{ width: 180 }}>
          <CountField label="Gold to ask for" value={ask} onChange={setAsk} />
        </div>
        <button
          className="button"
          type="button"
          disabled={!pledge || ask < config.market.minPrice}
          onClick={async () => {
            if (!pledge) return;
            const result = await act(
              () => api.postOffer(pledge, 1, ask),
              outcome => ({
                text: outcome.success ? `Your allies can send you ${formatNumber(ask)} gold now` : outcome.message,
                failure: !outcome.success,
              }),
            );
            if (result?.success) setAsk(0);
          }}
        >
          Ask allies for {ask > 0 ? `${formatNumber(ask)} ` : ""}gold
        </button>
      </div>
      {asking.length ? (
        <ul className="plain-list">
          {asking.map(offer => (
            <li key={offer.id}>
              <span>
                You ask for <strong>{formatNumber(offer.price)} gold</strong> against one piece of{" "}
                {marketResourceLabel[offer.resource].toLowerCase()}
              </span>
              <button
                className="button quiet small"
                type="button"
                onClick={() =>
                  act(
                    () => api.withdrawOffer(offer.id),
                    outcome => ({ text: outcome.message, failure: !outcome.success }),
                  )
                }
              >
                Withdraw
              </button>
            </li>
          ))}
        </ul>
      ) : null}
      <h3 className="section-title small">What our allies offer</h3>
      {offers.length ? (
        <ul className="plain-list">
          {offers.map(offer => {
            const total = offer.count * offer.price;
            const label = marketResourceLabel[offer.resource].toLowerCase();
            return (
              <li key={offer.id}>
                <span>
                  <strong>{offer.sellerName}</strong>{" "}
                  {offer.count === 1
                    ? `asks for ${formatNumber(offer.price)} gold`
                    : `sells ${formatNumber(offer.count)} ${label} at ${formatNumber(offer.price)} each`}
                </span>
                <button
                  className="button small"
                  type="button"
                  disabled={kingdom.resources.gold < total}
                  onClick={async () => {
                    await act(
                      () => api.buyOffer(offer.id, offer.count),
                      result => ({
                        text:
                          result.success && result.data
                            ? `${offer.sellerName} has ${formatNumber(result.data.totalCost)} more gold`
                            : result.message,
                        failure: !result.success,
                      }),
                    );
                    load();
                  }}
                >
                  {offer.count === 1 ? `Send ${formatNumber(total)} gold` : `Buy all for ${formatNumber(total)}`}
                </button>
              </li>
            );
          })}
        </ul>
      ) : (
        <p className="muted">Our allies have nothing on the market.</p>
      )}
    </Panel>
  );
};

export const AlliancePage: React.FC = () => {
  const { kingdom, reload } = useGame();
  const [alliance, setAlliance] = useState<Alliance | undefined | null>(null);

  const load = useCallback(() => {
    api
      .alliance()
      .then(setAlliance)
      .catch(() => setAlliance(undefined));
  }, []);
  // members join and leave while the page is open, so it refreshes whenever the kingdom does
  useEffect(load, [load, kingdom]);

  const onChange = (): void => {
    load();
    void reload();
  };

  return (
    <>
      <Banner image={art("alliance")} title="Alliance" />
      {alliance === null ? (
        <p className="muted">Calling the council</p>
      ) : alliance ? (
        <div className="stack">
          <WarRoomPanel />
          <GoldPanel />
          <Members alliance={alliance} onChange={onChange} />
        </div>
      ) : (
        <NoAlliance onChange={onChange} />
      )}
    </>
  );
};
