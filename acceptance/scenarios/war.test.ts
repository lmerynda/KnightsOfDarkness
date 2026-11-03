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
  await attacker.hasTakenLandFrom(target);
  await attacker.hasNoArmyOut();
});

scenario("an army sent to plunder carries off stores and takes no land", async game => {
  const target = await game.foundsAKingdom("Target");
  const raider = await game.foundsAKingdom("Raider");
  await raider.passesTurns(5);
  await raider.trains({ infantry: 10 });
  await raider.marchesToPlunder(target, { infantry: 10 });
  await raider.passesTurns(4);
  await raider.hasPlundered(target);
});

scenario("an army called back never fights", async game => {
  const target = await game.foundsAKingdom("Target");
  const attacker = await game.foundsAKingdom("Attacker");
  await attacker.passesTurns(5);
  await attacker.trains({ infantry: 10 });
  await attacker.marchesOn(target, { infantry: 10 });
  await attacker.callsBackTheArmyFrom(target);
  await attacker.hasNoArmyOut();
  await attacker.hasAtHome(10, "infantry");
});

scenario("a beaten kingdom is spared until it marches itself", async game => {
  const target = await game.foundsAKingdom("Target");
  const first = await game.foundsAKingdom("First");
  const second = await game.foundsAKingdom("Second");
  for (const ruler of [first, second]) {
    await ruler.passesTurns(5);
    await ruler.trains({ infantry: 10 });
  }
  await first.marchesOn(target, { infantry: 10 });
  await first.passesTurns(4);
  await first.hasDefeated(target);
  await second.mayNotMarchOn(target, { infantry: 10 }, /protected/);
  // a kingdom that marches gives up its own protection
  await target.passesTurns(5);
  await target.trains({ infantry: 5 });
  await target.marchesOn(first, { infantry: 5 });
  await second.marchesOn(target, { infantry: 10 });
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
