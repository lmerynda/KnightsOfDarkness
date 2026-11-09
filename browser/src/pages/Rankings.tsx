import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api/endpoints";
import type { Era, Monument } from "../api/types";
import { useGame } from "../game/useGame";
import { art } from "../game/lore";
import { Crest } from "../ui/Crest";
import { Banner, Meter, Panel } from "../ui/parts";
import { formatNumber, plural } from "../ui/format";
import { eraEndings, eraTimeLeft, monumentGarrison, monumentStanding } from "../game/rules";

export const RankingsPage: React.FC = () => {
  const { kingdom, rankings } = useGame();
  const [era, setEra] = useState<Era | null>(null);
  const [history, setHistory] = useState<Era[]>([]);
  const [monuments, setMonuments] = useState<Monument[] | null>(null);

  useEffect(() => {
    api
      .era()
      .then(setEra)
      .catch(() => undefined);
    api
      .monuments()
      .then(setMonuments)
      .catch(() => undefined);
    api
      .finishedEras()
      .then(setHistory)
      .catch(() => undefined);
  }, []);

  return (
    <>
      <Banner
        image={art("victory")}
        title="Rankings"
        help="Land decides the era. When the clock runs out, the alliance holding the most land wins it, the largest kingdom is remembered too, and every realm starts again."
      />
      <div className="stack">
        {era ? (
          <Panel
            title={`Era ${era.number}`}
            lede={`The clock ends it in about ${eraTimeLeft(era)}.`}
            help={`An era lasts at most ${formatNumber(era.maxLengthInTicks)} turns.`}
          >
            <Meter value={era.ticksElapsed} max={era.maxLengthInTicks} label="Era progress" />
          </Panel>
        ) : null}
        <div className="two-columns">
          <Panel title="Kingdoms">
            <div className="table-scroll">
              <table className="ledger">
                <thead>
                  <tr>
                    <th className="number">#</th>
                    <th>Kingdom</th>
                    <th className="number">Land</th>
                    <th className="number narrow-hidden">Buildings</th>
                  </tr>
                </thead>
                <tbody>
                  {(rankings?.kingdoms ?? []).map(rank => (
                    <tr key={rank.kingdomName} className={rank.kingdomName === kingdom.name ? "mine" : undefined}>
                      <td className="number rank-number">{rank.bot ? "" : rank.rank}</td>
                      <td>
                        <span className="figure">
                          <Crest name={rank.kingdomName} size={26} />
                          <span>
                            {rank.kingdomName}
                            {rank.allianceName ? <span className="muted"> of {rank.allianceName}</span> : null}
                            {rank.bot ? <span className="muted">, a merchant realm, not ranked</span> : null}
                            {rank.protectionTicks > 0 ? (
                              <span className="muted block">protected for {plural(rank.protectionTicks, "more turn", "more turns")}</span>
                            ) : null}
                          </span>
                        </span>
                      </td>
                      <td className="number">{formatNumber(rank.land)}</td>
                      <td className="number muted narrow-hidden">{formatNumber(rank.buildings)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </Panel>
          <div className="stack">
            <Panel title="Alliances">
              {(rankings?.alliances ?? []).length === 0 ? (
                <p className="muted">No alliances yet.</p>
              ) : (
                <table className="ledger">
                  <thead>
                    <tr>
                      <th className="number">#</th>
                      <th>Alliance</th>
                      <th className="number">Members</th>
                      <th className="number">Land</th>
                    </tr>
                  </thead>
                  <tbody>
                    {(rankings?.alliances ?? []).map(rank => (
                      <tr key={rank.allianceName} className={rank.allianceName === kingdom.allianceName ? "mine" : undefined}>
                        <td className="number rank-number">{rank.rank}</td>
                        <td>
                          <span className="figure">
                            <Crest name={rank.allianceName} size={26} /> {rank.allianceName}
                          </span>
                        </td>
                        <td className="number">{rank.members}</td>
                        <td className="number">{formatNumber(rank.land)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </Panel>
            {monuments ? (
              <Panel
                title="Monuments"
                help="Every alliance's monument, for all to see. Rivals may want to answer before one stands complete: only the garrison its members send defends it, and every won assault knocks stages down."
              >
                {monuments.length === 0 ? (
                  <p className="muted">No alliance has founded a monument this era.</p>
                ) : (
                  <ul className="plain-list">
                    {monuments.map(monument => (
                      <li key={monument.allianceName}>
                        <span className="figure">
                          <Crest name={monument.allianceName} size={26} /> {monument.allianceName} at {monument.seat}
                        </span>
                        <span className="muted">
                          {monumentStanding(monument)}, {monumentGarrison(monument)}
                        </span>
                        {monument.allianceName !== kingdom.allianceName ? (
                          <Link className="button quiet small" to={`/kingdom/war?assault=${encodeURIComponent(monument.seat)}`}>
                            March on it
                          </Link>
                        ) : null}
                      </li>
                    ))}
                  </ul>
                )}
              </Panel>
            ) : null}
            <Panel title="Past eras">
              {history.length === 0 ? (
                <p className="muted">This is the first era. Its winner will be remembered here.</p>
              ) : (
                <ul className="plain-list">
                  {history.map(past => {
                    const largest = past.finalRankings?.kingdoms[0];
                    const winner = past.finalRankings?.alliances[0];
                    return (
                      <li key={past.number}>
                        <span>
                          Era {past.number}:{" "}
                          {winner ? `${winner.allianceName} won with ${formatNumber(winner.land)} land` : "no alliance sworn"}{" "}
                          {past.endedBy ? eraEndings[past.endedBy] : ""}
                          {largest ? (
                            <span className="muted">
                              , largest kingdom {largest.kingdomName} with {formatNumber(largest.land)} land
                            </span>
                          ) : null}
                        </span>
                        <span className="muted">
                          {past.endedAt
                            ? new Date(past.endedAt).toLocaleDateString("en-GB", { day: "numeric", month: "short", year: "numeric" })
                            : ""}
                        </span>
                      </li>
                    );
                  })}
                </ul>
              )}
            </Panel>
          </div>
        </div>
      </div>
    </>
  );
};
