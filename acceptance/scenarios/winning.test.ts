import type { Game } from "../src/dsl/game.ts";
import type { Ruler } from "../src/dsl/ruler.ts";
import { playedOn, scenario } from "../src/dsl/scenario.ts";
import { noPeace } from "../src/server.ts";

// A complete monument that stands fed through the hold wins the era for its alliance, whatever the land, and every kingdom
// starts anew. Every scenario here ends its era, so the world is its own; the monument costs what a new kingdom holds,
// and the hold is longer than a day so that the last call is heard.
playedOn({
  ...noPeace,
  "monument.stages": 4,
  "monument.stageCost.gold": 1000,
  "monument.stageCost.iron": 10,
  "monument.stageCost.tools": 10,
  "monument.upkeepPerStage.gold": 10,
  "monument.upkeepPerStage.food": 0,
  "monument.holdTicks": 30,
});

const fourStages = { gold: 4000, iron: 40, tools: 40 };

const alliance = async (game: Game, prefix: string): Promise<{ name: string; emperor: Ruler; squire: Ruler }> => {
  const squire = await game.foundsAKingdom("Squire");
  const emperor = await game.foundsAKingdom("Emperor");
  const name = `${prefix} ${Math.random().toString(36).slice(2, 7)}`;
  await emperor.foundsAlliance(name);
  await emperor.invites(squire);
  await squire.joins(name);
  return { name, emperor, squire };
};

scenario("a monument that stands complete and fed through the hold wins the era, whatever the land", async game => {
  const mason = await game.foundsAKingdom("Mason");
  const name = `Masons ${Math.random().toString(36).slice(2, 7)}`;
  await mason.foundsAlliance(name);
  // a larger alliance, which would win if the clock ran out
  const rival = await game.foundsAKingdom("Rival");
  await rival.foundsAlliance(`Landed ${Math.random().toString(36).slice(2, 7)}`);
  await rival.buysLand(50);

  await mason.foundsAMonumentAt(mason);
  // four stages, and the upkeep of the whole hold in the store
  await mason.paysIntoTheMonument({ ...fourStages, gold: fourStages.gold + 40 * 30 });
  await rival.readsInTheChronicle(new RegExp(`${name} has completed its monument at ${mason}. If it stands complete and fed for 30 turns, ${name} wins the era`));
  await rival.seesTheMonumentWinIn(name, 30);

  await game.timePasses(6);
  await rival.seesTheMonumentWinIn(name, 24);
  await rival.readsInTheChronicle(new RegExp(`The monument of ${name} at ${mason} wins the era in 24 turns unless an army knocks it down`));

  const era = await game.currentEra();
  await game.timePasses(23);
  await rival.seesTheMonumentWinIn(name, 1);
  await game.timePasses(1);

  await rival.readsInTheChronicle(new RegExp(`Era ${era} has ended: the monument of ${name} at ${mason} stood complete for 30 turns, and ${name} wins the era. ${rival} was the largest kingdom`));
  await mason.seesInTheHallOfFame(era, name, rival, "monument");
  const newcomer = await game.foundsAKingdom("Newcomer");
  await rival.standsLevelWith(newcomer);
  await mason.standsLevelWith(newcomer);
  await rival.seesNoMonumentOf(name);
});

scenario("a won assault during the hold knocks the monument down, and once rebuilt it must stand the whole hold again", async game => {
  const { name, emperor } = await alliance(game, "Toppled");
  // the largest kingdom, for the hall of fame
  const rival = await game.foundsAKingdom("Rival");
  await rival.buysLand(20);
  await emperor.foundsAMonumentAt(emperor);
  // four stages, a stage to spare for repairs, and upkeep for more than the hold
  await emperor.paysIntoTheMonument({ gold: 5000 + 40 * 45, iron: 50, tools: 50 });
  await game.timePasses(10);
  await rival.seesTheMonumentWinIn(name, 20);

  const raider = await game.foundsAKingdom("Raider");
  await raider.passesTurns(5);
  await raider.trains({ infantry: 10 });
  await raider.marchesOnTheMonumentAt(emperor, { infantry: 10 });
  await raider.passesTurns(4);
  await raider.hasStormedTheMonumentAt(emperor, 1);
  await rival.readsInTheChronicle(
    new RegExp(`${raider} stormed the monument of ${name} at ${emperor} and knocked it down to stage 3 of 4. Rebuilt, it must stand complete 30 turns again to win the era`),
  );
  await rival.seesTheMonumentOf(name, emperor, 3);

  // the store rebuilds it with the next tick, and the hold starts from the beginning
  await game.timePasses(1);
  await rival.seesTheMonumentWinIn(name, 30);
  const era = await game.currentEra();
  await game.timePasses(29);
  await rival.seesTheMonumentWinIn(name, 1);
  await game.timePasses(1);
  await emperor.seesInTheHallOfFame(era, name, rival, "monument");
});
