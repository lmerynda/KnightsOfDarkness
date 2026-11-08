import { playedOn, scenario } from "../src/dsl/scenario.ts";

// the shipped peace, on a world that is not spared it as scenarios about war are
playedOn({});

scenario("every kingdom starts at peace, and no army may march on it", async game => {
  const target = await game.foundsAKingdom("Target");
  const attacker = await game.foundsAKingdom("Attacker");
  await attacker.passesTurns(5);
  await attacker.trains({ infantry: 10 });
  await attacker.mayNotMarchOn(target, { infantry: 10 }, /protected/);
});

scenario("the peace ends when its turns have passed, and the armies march", async game => {
  const target = await game.foundsAKingdom("Target");
  const attacker = await game.foundsAKingdom("Attacker");
  await attacker.passesTurns(5);
  await attacker.trains({ infantry: 10 });
  await game.timePasses(335);
  await attacker.mayNotMarchOn(target, { infantry: 10 }, /protected for 1 more turn/);
  await game.timePasses(1);
  await attacker.marchesOn(target, { infantry: 10 });
  await attacker.passesTurns(4);
  await attacker.hasDefeated(target);
});
