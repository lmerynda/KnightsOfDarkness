import { playedOn, scenario } from "../src/dsl/scenario.ts";
import { noPeace } from "../src/server.ts";

// An era ends when the clock runs out, four months as the game is configured. The alliance holding the most land wins it
// and every kingdom in the world starts again, so this file keeps a world of its own, with an era short enough to play
// through here; long/eras.test.ts plays the full length.
playedOn({ ...noPeace, "era.maxLengthInTicks": 120 });

// first in this file: it needs a world without any alliance
scenario("an era runs out with no alliance sworn: only the largest kingdom is remembered", async game => {
  const largest = await game.foundsAKingdom("Loner");
  await largest.buysLand(20);
  const ended = await game.theEraRunsOut();
  await largest.readsInTheChronicle(new RegExp(`Era ${ended} has ended as the clock ran out. No alliance was sworn, ${largest} was the largest kingdom`));
  await largest.seesInTheHallOfFame(ended, null, largest);
});

scenario("an era runs out: the alliance with the most land wins it, and every kingdom starts anew", async game => {
  const largest = await game.foundsAKingdom("Largest");
  const lone = `Lasting ${Math.random().toString(36).slice(2, 7)}`;
  await largest.foundsAlliance(lone);
  await largest.buysLand(20);
  const smaller = await game.foundsAKingdom("Smaller");
  const ally = await game.foundsAKingdom("Ally");
  const sworn = `Sworn ${Math.random().toString(36).slice(2, 7)}`;
  await smaller.foundsAlliance(sworn);
  await smaller.invites(ally);
  await ally.joins(sworn);
  await smaller.passesTurns(5);
  await smaller.trains({ infantry: 10 });
  await smaller.foundsAMonumentAt(ally);
  await largest.seesTheMonumentOf(sworn, ally, 0);

  const ended = await game.theEraRunsOut();

  // two ordinary kingdoms hold more land together than the largest one alone
  await smaller.readsInTheChronicle(new RegExp(`Era ${ended} has ended as the clock ran out. ${sworn} held the most land, ${largest} was the largest kingdom`));
  await smaller.seesInTheHallOfFame(ended, sworn, largest);
  const newcomer = await game.foundsAKingdom("Newcomer");
  await largest.standsLevelWith(newcomer);
  await smaller.standsLevelWith(newcomer);
  await largest.seesNoMonumentOf(sworn);
  // alliances outlast the era
  await largest.leadsAllianceOf(lone);
});
