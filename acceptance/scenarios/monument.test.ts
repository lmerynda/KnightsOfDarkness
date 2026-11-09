import assert from "node:assert/strict";
import type { Game } from "../src/dsl/game.ts";
import type { Ruler } from "../src/dsl/ruler.ts";
import { playedOn, scenario } from "../src/dsl/scenario.ts";
import { noPeace } from "../src/server.ts";

// An alliance raises one monument an era. Here it costs what a new kingdom holds, so that scenarios need no months of
// economy; the real prices are in gameconfig.json.
playedOn({
  ...noPeace,
  "monument.stages": 4,
  "monument.stageCost.gold": 1000,
  "monument.stageCost.iron": 10,
  "monument.stageCost.tools": 10,
  "monument.upkeepPerStage.gold": 10,
  "monument.upkeepPerStage.food": 2,
  "monument.decayRatio": 0.25,
  // no monument wins the era here: winning.test.ts has a world of its own, since a won era starts every kingdom anew
  "monument.holdTicks": 1_000_000,
});

const aStage = { gold: 1000, iron: 10, tools: 10 };

const alliance = async (game: Game, prefix: string): Promise<{ name: string; emperor: Ruler; squire: Ruler }> => {
  const squire = await game.foundsAKingdom("Squire");
  const emperor = await game.foundsAKingdom("Emperor");
  const name = `${prefix} ${Math.random().toString(36).slice(2, 7)}`;
  await emperor.foundsAlliance(name);
  await emperor.invites(squire);
  await squire.joins(name);
  return { name, emperor, squire };
};

scenario("an emperor founds a monument in a member's kingdom, and every kingdom hears of it", async game => {
  const { name, emperor, squire } = await alliance(game, "Founders");
  const rival = await game.foundsAKingdom("Rival");
  await squire.isRefusedFoundingAMonumentAt(squire, /only the emperor/i);
  await emperor.foundsAMonumentAt(squire);
  await rival.readsInTheChronicle(new RegExp(`${name} has founded a monument at ${squire}`));
  await rival.seesTheMonumentOf(name, squire, 0);
  // one an era
  await emperor.isRefusedFoundingAMonumentAt(emperor, /already has its monument/);
});

scenario("members pay goods into the monument until the store covers a stage", async game => {
  const { name, emperor, squire } = await alliance(game, "Payers");
  const rival = await game.foundsAKingdom("Rival");
  await rival.foundsAlliance(`Unbuilt ${Math.random().toString(36).slice(2, 7)}`);
  await rival.isRefusedPayingIntoTheMonument({ gold: 100 }, /no monument/);
  await emperor.foundsAMonumentAt(emperor);

  const goldBefore = await emperor.holds("gold");
  await emperor.paysIntoTheMonument({ gold: 1000, iron: 5 });
  assert.equal(await emperor.holds("gold"), goldBefore - 1000);
  await squire.seesOurMonumentAt(0, { gold: 1000, iron: 5, tools: 0, food: 0 });

  await emperor.isRefusedPayingIntoTheMonument({ gold: 1_000_000_000 }, /don't hold that much/);

  await squire.paysIntoTheMonument({ iron: 5, tools: 10 });
  await emperor.seesOurMonumentAt(1, { gold: 0, iron: 0, tools: 0, food: 0 });
  await rival.seesTheMonumentOf(name, emperor, 1);
});

scenario("the chronicle tells of the stages from the half-way mark, and of the monument complete", async game => {
  const { name, emperor } = await alliance(game, "Builders");
  const rival = await game.foundsAKingdom("Rival");
  await emperor.foundsAMonumentAt(emperor);
  await emperor.paysIntoTheMonument(aStage);
  await rival.hasNotReadInTheChronicle(new RegExp(`${name} raised its monument`));
  await emperor.paysIntoTheMonument(aStage);
  await rival.readsInTheChronicle(new RegExp(`${name} raised its monument at ${emperor} to stage 2 of 4`));
  await emperor.paysIntoTheMonument(aStage);
  await rival.readsInTheChronicle(new RegExp(`to stage 3 of 4`));
  await rival.hasNotReadInTheChronicle(new RegExp(`${name} has completed`));
  await emperor.paysIntoTheMonument(aStage);
  await rival.readsInTheChronicle(new RegExp(`${name} has completed its monument at ${emperor}`));
  await rival.seesTheMonumentComplete(name);
});

scenario("a monument eats its upkeep with every tick of the clock, and crumbles when the store runs dry", async game => {
  const { emperor, squire } = await alliance(game, "Keepers");
  await emperor.foundsAMonumentAt(emperor);
  await emperor.paysIntoTheMonument({ ...aStage, gold: 1020, food: 4 });
  await game.timePasses(2);
  await emperor.seesOurMonumentAt(1, { gold: 0, iron: 0, tools: 0, food: 0 });

  await game.timePasses(1);
  await emperor.seesOurMonumentAt(0.75, { gold: 0, iron: 0, tools: 0, food: 0 });
  await emperor.seesOurMonumentCrumbling(true);
  await squire.readsInTheChronicle(/cannot pay its upkeep, and the monument is crumbling/);

  // only the crumbled quarter of the stage needs paying again, and what is left over feeds it
  await squire.paysIntoTheMonument({ gold: 260, iron: 3, tools: 3, food: 2 });
  await emperor.seesOurMonumentAt(1, { gold: 10, iron: 0, tools: 0, food: 2 });
  await game.timePasses(1);
  await emperor.seesOurMonumentAt(1, { gold: 0, iron: 0, tools: 0, food: 0 });
  await emperor.seesOurMonumentCrumbling(false);
});

scenario("a monument's seat moves to the emperor when its member leaves", async game => {
  const { name, emperor, squire } = await alliance(game, "Movers");
  const rival = await game.foundsAKingdom("Rival");
  await emperor.foundsAMonumentAt(squire);
  await squire.leavesTheAlliance();
  await rival.readsInTheChronicle(new RegExp(`${name} moved the seat of its monument from ${squire} to ${emperor}`));
  await rival.seesTheMonumentOf(name, emperor, 0);
});

scenario("a monument falls with its alliance", async game => {
  const emperor = await game.foundsAKingdom("Emperor");
  const rival = await game.foundsAKingdom("Rival");
  const name = `Fallen ${Math.random().toString(36).slice(2, 7)}`;
  await emperor.foundsAlliance(name);
  await emperor.foundsAMonumentAt(emperor);
  await emperor.leavesTheAlliance();
  await rival.readsInTheChronicle(new RegExp(`The monument of ${name} fell with its alliance`));
  await rival.seesNoMonumentOf(name);
});
