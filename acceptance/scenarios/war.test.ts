import { scenario } from "../src/dsl/scenario.ts";

scenario("an army marches, is seen coming, and wins", async game => {
  const target = await game.foundsAKingdom("Target");
  const attacker = await game.foundsAKingdom("Attacker");
  await attacker.passesTurns(5);
  await attacker.trains({ infantry: 10 });
  await attacker.marchesOn(target, { infantry: 10 });
  await target.seesAnArmyComing(attacker, 10);
  await attacker.passesTurns(4);
  await attacker.hasDefeated(target);
  await attacker.hasNoArmyOut();
});

scenario("spies bring back what another kingdom holds", async game => {
  const target = await game.foundsAKingdom("Target");
  const spymaster = await game.foundsAKingdom("Spymaster");
  await spymaster.raises({ spyGuild: 1 });
  // a new kingdom has weapons for two spies, more than the one a target's five guards can catch
  await spymaster.trains({ spy: 2 });
  await spymaster.sendsSpies(target, 2);
  await spymaster.hasIntelOnTheStoresOf(target);
});
