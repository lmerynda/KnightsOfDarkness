import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api/endpoints";
import type { Rankings, Round } from "../api/types";
import { art } from "../game/lore";
import { roundTimeLeft } from "../game/rules";
import { Crest } from "../ui/Crest";
import { formatNumber } from "../ui/format";

const pillars = [
  {
    image: "castle",
    title: "Raise a kingdom",
    text: "Clear land, raise houses and mines, and keep your people fed. Every turn your miners, farmers and smiths work, and every turn the land grows dearer.",
  },
  {
    image: "cavalry",
    title: "Take what others hold",
    text: "March infantry, bowmen and cavalry on a rival. A victory takes their land or plunders their stores; towers, guards and warehouses are how you keep yours.",
  },
  {
    image: "alliance",
    title: "Swear to an alliance",
    text: "No kingdom outlasts the dark alone. Trade on the market, send carriers to allies, write to your council, and rise together in the rankings until the round ends.",
  },
];

export const Landing: React.FC = () => {
  const [round, setRound] = useState<Round | null>(null);
  const [rankings, setRankings] = useState<Rankings | null>(null);

  useEffect(() => {
    api
      .round()
      .then(setRound)
      .catch(() => undefined);
    api
      .rankings()
      .then(setRankings)
      .catch(() => undefined);
  }, []);

  return (
    <div className="landing">
      <section className="landing-hero" style={{ backgroundImage: `url(${art("hero")})` }}>
        <div className="landing-hero-inner">
          <h1 className="landing-title">Knights of Darkness</h1>
          <p className="landing-intro">
            The western sky never clears. Caravans vanish, outposts burn, and old enemies hold torchlit councils because they must. Build a
            kingdom that lasts the round, and an alliance that does not betray it.
          </p>
          <div className="row">
            <Link className="button" to="/register">
              Found a kingdom
            </Link>
            <Link className="button quiet" to="/sign-in">
              Return to your kingdom
            </Link>
          </div>
        </div>
      </section>

      <section className="landing-round">
        <div>
          <h2>{round ? `Round ${round.number}` : "The current round"}</h2>
          <p className="muted">
            {round
              ? `Ends in ${roundTimeLeft(round)}. When it does, the kingdom with the most land is remembered, and every realm starts again from nothing.`
              : "Rounds end on a fixed day; the largest kingdom is remembered and every realm starts again."}
          </p>
        </div>
        <ol className="landing-standings">
          {(rankings?.kingdoms ?? [])
            .filter(rank => !rank.bot)
            .slice(0, 5)
            .map(rank => (
              <li key={rank.kingdomName}>
                <span className="standing-rank">{rank.rank}</span>
                <Crest name={rank.kingdomName} size={28} />
                <span className="standing-name">{rank.kingdomName}</span>
                <span className="muted">{formatNumber(rank.land)} land</span>
              </li>
            ))}
        </ol>
      </section>

      <section className="landing-pillars">
        {pillars.map(pillar => (
          <article key={pillar.title}>
            <img src={art(pillar.image)} alt="" loading="lazy" />
            <h3>{pillar.title}</h3>
            <p>{pillar.text}</p>
          </article>
        ))}
      </section>
    </div>
  );
};
