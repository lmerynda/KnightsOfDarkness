import { scenario } from "../src/dsl/scenario.ts";

// A round lasts four weeks of the clock. When it runs out, the kingdom with the most land is remembered and every
// kingdom in the world starts again, so this file keeps a world of its own.

scenario("a round runs out: the largest kingdom is remembered, and every kingdom starts anew", async game => {
  const largest = await game.foundsAKingdom("Largest");
  const smaller = await game.foundsAKingdom("Smaller");
  const pact = `Lasting ${Math.random().toString(36).slice(2, 7)}`;
  await largest.foundsAlliance(pact);
  await largest.buysLand(20);
  await smaller.passesTurns(5);
  await smaller.trains({ infantry: 10 });
  await game.theRoundRunsOut();
  await smaller.readsInTheChronicle(new RegExp(`Round 1 has ended, ${largest} ruled the most land`));
  await smaller.seesInTheHallOfFame(1, largest);
  const newcomer = await game.foundsAKingdom("Newcomer");
  await largest.standsLevelWith(newcomer);
  await smaller.standsLevelWith(newcomer);
  // alliances outlast the round
  await largest.leadsAllianceOf(pact);
});
