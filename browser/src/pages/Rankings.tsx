import React, { useEffect, useState } from "react";
import { api } from "../api/endpoints";
import type { Round } from "../api/types";
import { useGame } from "../game/useGame";
import { art } from "../game/lore";
import { Crest } from "../ui/Crest";
import { Banner, Meter, Panel } from "../ui/parts";
import { formatNumber } from "../ui/format";
import { roundTimeLeft } from "../game/rules";

export const RankingsPage: React.FC = () => {
  const { kingdom, rankings } = useGame();
  const [round, setRound] = useState<Round | null>(null);
  const [history, setHistory] = useState<Round[]>([]);

  useEffect(() => {
    api
      .round()
      .then(setRound)
      .catch(() => undefined);
    api
      .finishedRounds()
      .then(setHistory)
      .catch(() => undefined);
  }, []);

  return (
    <>
      <Banner image={art("victory")} title="Rankings">
        Land decides the round. When it ends, the largest kingdom and the largest alliance are remembered, and every realm starts again.
      </Banner>
      <div className="stack">
        {round ? (
          <Panel
            title={`Round ${round.number}`}
            lede={`Ends in about ${roundTimeLeft(round)}, after ${formatNumber(round.lengthInTicks)} turns have been handed out.`}
          >
            <Meter value={round.ticksElapsed} max={round.lengthInTicks} label="Round progress" />
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
            <Panel title="Past rounds">
              {history.length === 0 ? (
                <p className="muted">This is the first round. Its winner will be remembered here.</p>
              ) : (
                <ul className="plain-list">
                  {history.map(past => {
                    const winner = past.finalRankings?.kingdoms[0];
                    const alliance = past.finalRankings?.alliances[0];
                    return (
                      <li key={past.number}>
                        <span>
                          Round {past.number}: {winner ? `${winner.kingdomName} with ${formatNumber(winner.land)} land` : "no kingdoms"}
                          {alliance ? <span className="muted">, alliance {alliance.allianceName}</span> : null}
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
