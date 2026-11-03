import type { Game } from "../src/dsl/game.ts";
import type { Ruler } from "../src/dsl/ruler.ts";
import { scenario } from "../src/dsl/scenario.ts";

/** an emperor and a squire in an alliance of their own */
const alliance = async (game: Game, prefix: string): Promise<{ name: string; emperor: Ruler; squire: Ruler }> => {
  const squire = await game.foundsAKingdom("Squire");
  const emperor = await game.foundsAKingdom("Emperor");
  const name = `${prefix} ${Math.random().toString(36).slice(2, 7)}`;
  await emperor.foundsAlliance(name);
  await emperor.invites(squire);
  await squire.joins(name);
  return { name, emperor, squire };
};

scenario("letters reach the other kingdom", async game => {
  const writer = await game.foundsAKingdom("Writer");
  const reader = await game.foundsAKingdom("Reader");
  const body = `Greetings from ${writer}`;
  await writer.writesTo(reader, body);
  await writer.hasNoLetters();
  await writer.hasSentALetterTo(reader, body);
  await reader.hasUnreadLetters();
  await reader.hasALetterFrom(writer, body);
});

scenario("an alliance is founded and a second kingdom joins it", async game => {
  const { name, emperor, squire } = await alliance(game, "Order");
  await squire.isInAllianceWith(name, emperor);
  await emperor.isInAllianceWith(name, squire);
});

scenario("an ally's soldiers stand guard and go home when called", async game => {
  const { emperor, squire } = await alliance(game, "Shield");
  await squire.passesTurns(5);
  await squire.trains({ infantry: 10 });
  await squire.sendsToStandGuard(emperor, { infantry: 10 });
  await squire.passesTurns(2);
  await squire.hasStandingGuardWith(emperor, { infantry: 10 });
  await emperor.isGuardedBy(squire, { infantry: 10 });
  await squire.callsTheGuardsHome();
  await squire.hasNoGuardsAway();
});

scenario("allies see each other's armies and strike together", async game => {
  const target = await game.foundsAKingdom("Target");
  const { emperor, squire } = await alliance(game, "Pact");
  await squire.passesTurns(5);
  await squire.trains({ infantry: 10 });
  await squire.marchesOn(target, { infantry: 10 });

  await emperor.passesTurns(5);
  await emperor.trains({ infantry: 10 });
  await emperor.seesInTheWarRoom(squire, target);
  await emperor.joinsTheStrike(squire, target, { infantry: 10 });
  // the joined army waits at the border however many turns the emperor passes, and fights right after the squire's lands
  await emperor.passesTurns(5);
  await emperor.waitsAtTheBorderOf(target, squire);
  await squire.passesTurns(4);
  await squire.hasFoughtAt(target);
  await squire.readsInTheChronicle(new RegExp(`${emperor} struck ${target} right after our army`));
  await emperor.hasFoughtAt(target);
  await emperor.hasNoArmyOut();
});

scenario("an ally asks for gold and another sends it through the market", async game => {
  const { emperor, squire } = await alliance(game, "Purse");
  await squire.asksAlliesFor(50000);
  await emperor.seesTheAsk(squire, 50000);
  await emperor.paysTheAskOf(squire);
  await emperor.seesNoAsks();
  await squire.readsInTheChronicle(/for 50,000 gold/);
});

scenario("a letter to the alliance reaches every other member", async game => {
  const { emperor, squire } = await alliance(game, "Herald");
  const body = `To all of us, from ${emperor}`;
  await emperor.writesToTheAlliance(body);
  await squire.hasALetterFrom(emperor, body);
});

scenario("an invitation may be declined", async game => {
  const squire = await game.foundsAKingdom("Squire");
  const emperor = await game.foundsAKingdom("Emperor");
  const name = `Lonely ${Math.random().toString(36).slice(2, 7)}`;
  await emperor.foundsAlliance(name);
  await emperor.invites(squire);
  await squire.declinesTheInvitationTo(name);
  await squire.isInNoAlliance();
  await emperor.leadsAllianceOf(name);
});

scenario("a member leaves, and the emperor sends another away", async game => {
  const { name, emperor, squire } = await alliance(game, "Brittle");
  const knight = await game.foundsAKingdom("Knight");
  await emperor.invites(knight);
  await knight.joins(name);
  await emperor.leadsAllianceOf(name, squire.name, knight.name);
  await squire.leavesTheAlliance();
  await squire.isInNoAlliance();
  await emperor.removesFromTheAlliance(knight);
  await knight.isInNoAlliance();
  await emperor.leadsAllianceOf(name);
});

scenario("an emperor raises a vassal that fills a place in the alliance", async game => {
  const emperor = await game.foundsAKingdom("Emperor");
  const name = `Liege ${Math.random().toString(36).slice(2, 7)}`;
  await emperor.foundsAlliance(name);
  const vassal = `Vassal ${Math.random().toString(36).slice(2, 7)}`;
  await emperor.raisesAVassal(vassal);
  await emperor.leadsAllianceOf(name, vassal);
});
