import type { Game } from "../src/dsl/game.ts";
import type { Ruler } from "../src/dsl/ruler.ts";
import { playedOn, scenario } from "../src/dsl/scenario.ts";
import { noPeace } from "../src/server.ts";

// A monument's only defense is the soldiers its members send, and rivals may strike it again and again. Here it costs what
// a new kingdom holds and eats nothing, so that scenarios need no months of economy; the real prices are in gameconfig.json.
playedOn({
  ...noPeace,
  "monument.stages": 4,
  "monument.stageCost.gold": 1000,
  "monument.stageCost.iron": 10,
  "monument.stageCost.tools": 10,
  "monument.upkeepPerStage.gold": 0,
  "monument.upkeepPerStage.food": 0,
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

const armed = async (game: Game, name: string, infantry = 10): Promise<Ruler> => {
  const ruler = await game.foundsAKingdom(name);
  await ruler.passesTurns(5);
  await ruler.trains({ infantry });
  return ruler;
};

scenario("members send soldiers to the monument, and everyone can count them once they stand guard", async game => {
  const { name, emperor, squire } = await alliance(game, "Wardens");
  const rival = await game.foundsAKingdom("Rival");
  await emperor.foundsAMonumentAt(emperor);
  await squire.passesTurns(5);
  await squire.trains({ infantry: 10 });

  await squire.sendsToTheMonument({ infantry: 10 });
  await rival.seesTheMonumentGuardedBy(name, 0);
  await squire.passesTurns(2);
  await rival.seesTheMonumentGuardedBy(name, 10);
  await emperor.seesOnGuardAtOurMonument(10);
  await squire.hasStandingGuardWith(emperor, { infantry: 10 });

  await squire.callsTheGuardsHome();
  await rival.seesTheMonumentGuardedBy(name, 0);
  await squire.hasAtHome(10, "infantry");
});

scenario("an unguarded monument falls to any army, whatever its seat has at home, and every kingdom hears", async game => {
  const { name, emperor } = await alliance(game, "Unguarded");
  const bystander = await game.foundsAKingdom("Bystander");
  await emperor.foundsAMonumentAt(emperor);
  await emperor.paysIntoTheMonument(fourStages);
  await emperor.passesTurns(5);
  await emperor.trains({ infantry: 20 });
  const raider = await armed(game, "Raider");

  await raider.marchesOnTheMonumentAt(emperor, { infantry: 10 });
  await emperor.seesAnArmyComing(raider, 10);
  await raider.passesTurns(4);

  // a quarter of the four stages, and no land or stores
  await raider.hasStormedTheMonumentAt(emperor, 1);
  await bystander.seesTheMonumentOf(name, emperor, 3);
  await bystander.readsInTheChronicle(new RegExp(`${raider} stormed the monument of ${name} at ${emperor} and knocked it down to stage 3 of 4`));
  await emperor.hasAtHome(20, "infantry");
});

scenario("a garrison holds against a smaller army, and the monument stands", async game => {
  const { name, emperor, squire } = await alliance(game, "Holders");
  await emperor.foundsAMonumentAt(emperor);
  await emperor.paysIntoTheMonument(fourStages);
  await squire.passesTurns(5);
  await squire.trains({ infantry: 20 });
  await squire.sendsToTheMonument({ infantry: 20 });
  await squire.passesTurns(2);
  const raider = await armed(game, "Raider", 5);

  await raider.marchesOnTheMonumentAt(emperor, { infantry: 5 });
  await raider.passesTurns(4);

  await raider.wasRepelledAtTheMonumentAt(emperor);
  await emperor.hasRepelled(raider);
  await raider.seesTheMonumentComplete(name);
  await emperor.readsInTheChronicle(new RegExp(`We repelled ${raider} at our monument`));
  await squire.readsInTheChronicle(new RegExp(`of our soldiers fell at the monument at ${emperor}, against ${raider}`));
});

scenario("a monument has no protection: the next army strikes it at once", async game => {
  const { name, emperor } = await alliance(game, "Battered");
  await emperor.foundsAMonumentAt(emperor);
  await emperor.paysIntoTheMonument(fourStages);
  const first = await armed(game, "First");
  const second = await armed(game, "Second");

  await first.marchesOnTheMonumentAt(emperor, { infantry: 10 });
  await first.passesTurns(4);
  await first.hasStormedTheMonumentAt(emperor, 1);

  await second.marchesOnTheMonumentAt(emperor, { infantry: 10 });
  await second.passesTurns(4);
  await second.hasStormedTheMonumentAt(emperor, 0.75);
  await emperor.seesTheMonumentOf(name, emperor, 2.25);
});

scenario("allies join an assault, and their armies land right after the first", async game => {
  const { name, emperor } = await alliance(game, "Besieged");
  await emperor.foundsAMonumentAt(emperor);
  await emperor.paysIntoTheMonument(fourStages);
  const leader = await armed(game, "Leader");
  const ally = await armed(game, "Ally");
  const besiegers = `Besiegers ${Math.random().toString(36).slice(2, 7)}`;
  await leader.foundsAlliance(besiegers);
  await leader.invites(ally);
  await ally.joins(besiegers);

  await leader.marchesOnTheMonumentAt(emperor, { infantry: 10 });
  await ally.joinsTheAssault(leader, emperor, { infantry: 10 });
  await ally.passesTurns(4);
  await ally.hasNotFoughtAt(emperor);

  await leader.passesTurns(4);
  await leader.hasStormedTheMonumentAt(emperor, 1);
  await ally.hasStormedTheMonumentAt(emperor, 0.75);
  await emperor.seesTheMonumentOf(name, emperor, 2.25);
});

scenario("what the store can cover is rebuilt with the next tick", async game => {
  const { name, emperor } = await alliance(game, "Rebuilders");
  await emperor.foundsAMonumentAt(emperor);
  await emperor.paysIntoTheMonument({ gold: 5000, iron: 50, tools: 50 });
  const raider = await armed(game, "Raider");
  await raider.marchesOnTheMonumentAt(emperor, { infantry: 10 });
  await raider.passesTurns(4);
  await raider.hasStormedTheMonumentAt(emperor, 1);
  await emperor.seesTheMonumentOf(name, emperor, 3);

  await game.timePasses(1);
  await emperor.seesOurMonumentAt(4, { gold: 0, iron: 0, tools: 0, food: 0 });
});

scenario("an army follows the monument when its seat moves", async game => {
  const { name, emperor, squire } = await alliance(game, "Dodgers");
  await emperor.foundsAMonumentAt(squire);
  await emperor.paysIntoTheMonument(fourStages);
  const raider = await armed(game, "Raider");
  await raider.marchesOnTheMonumentAt(squire, { infantry: 10 });

  await squire.leavesTheAlliance();
  await raider.passesTurns(4);

  await raider.hasStormedTheMonumentAt(emperor, 1);
  await emperor.seesTheMonumentOf(name, emperor, 3);
});

scenario("an army whose monument has fallen comes home without a fight", async game => {
  const emperor = await game.foundsAKingdom("Emperor");
  await emperor.foundsAlliance(`Doomed ${Math.random().toString(36).slice(2, 7)}`);
  await emperor.foundsAMonumentAt(emperor);
  const raider = await armed(game, "Raider");
  await raider.marchesOnTheMonumentAt(emperor, { infantry: 10 });

  await emperor.leavesTheAlliance();
  await raider.passesTurns(4);

  await raider.readsInTheChronicle(new RegExp(`found no enemy monument to assault at ${emperor}`));
  await raider.hasNoArmyOut();
  await raider.hasAtHome(10, "infantry");
  await raider.hasNotFoughtAt(emperor);
});

scenario("soldiers standing guard come home when their ruler leaves the alliance", async game => {
  const { name, emperor, squire } = await alliance(game, "Deserted");
  await emperor.foundsAMonumentAt(emperor);
  await squire.passesTurns(5);
  await squire.trains({ infantry: 10 });
  await squire.sendsToTheMonument({ infantry: 10 });
  await squire.passesTurns(2);
  await emperor.seesTheMonumentGuardedBy(name, 10);

  await squire.leavesTheAlliance();

  await emperor.seesTheMonumentGuardedBy(name, 0);
  await squire.hasAtHome(10, "infantry");
});
