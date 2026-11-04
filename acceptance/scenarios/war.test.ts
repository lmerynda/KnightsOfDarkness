import assert from "node:assert/strict";
import type { Ruler } from "../src/dsl/ruler.ts";
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

scenario("a kingdom under half the attacker's land is too small to attack", async game => {
  const small = await game.foundsAKingdom("Small");
  const large = await game.foundsAKingdom("Large");
  await large.passesTurns(6);
  await large.trains({ infantry: 5 });
  // a new kingdom holds 100 acres; at 201 it is more than twice as large
  await large.buysLand(101);
  await large.mayNotMarchOn(small, { infantry: 5 }, /too small/);
});

scenario("a warehouse keeps a twentieth of the stores from raiders per level", async game => {
  const open = await game.foundsAKingdom("Open");
  const guarded = await game.foundsAKingdom("Guarded");
  await guarded.beginsAGreatWork("warehouse");
  await guarded.passesTurns(1);
  await guarded.investsIn("warehouse", 30000);
  await guarded.hasAGreatWork("warehouse", 1);

  // twin raids, so the only difference between them is the warehouse
  const shareOfGoldTaken = async (target: Ruler): Promise<number> => {
    const raider = await game.foundsAKingdom("Raider");
    await raider.passesTurns(5);
    await raider.trains({ infantry: 10 });
    await raider.marchesToPlunder(target, { infantry: 10 });
    await raider.passesTurns(4);
    const taken = await raider.goldPlunderedFrom(target);
    return taken / (taken + (await target.holds("gold")));
  };
  const fromOpen = await shareOfGoldTaken(open);
  const fromGuarded = await shareOfGoldTaken(guarded);
  assert.ok(Math.abs(fromGuarded / fromOpen - 0.95) < 0.001, `raiders took ${fromGuarded} of the guarded gold and ${fromOpen} of the open`);
});

scenario("a lone spy is caught by the guards and never comes back", async game => {
  const target = await game.foundsAKingdom("Target");
  const spymaster = await game.foundsAKingdom("Spymaster");
  await spymaster.raises({ spyGuild: 1 });
  await spymaster.trains({ spy: 2 });
  // five guards watch as well as one spy, and the spies must outnumber the watch
  await spymaster.sendsSpies(target, 1);
  await spymaster.hasLostSpiesIn(target, 1);
  await spymaster.hasAtHome(1, "spy");
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

scenario("an army too weak is beaten back, takes nothing, and both sides hear of it", async game => {
  const target = await game.foundsAKingdom("Target");
  const attacker = await game.foundsAKingdom("Attacker");
  await attacker.passesTurns(5);
  // two foot soldiers strike with 4; a new kingdom's five guards stand behind its castle and towers with about 6
  await attacker.trains({ infantry: 2 });
  await attacker.marchesOn(target, { infantry: 2 });
  await attacker.passesTurns(4);
  await attacker.hasBeenBeatenBackBy(target);
  await target.hasRepelled(attacker);
  await attacker.readsInTheChronicle(new RegExp(`Our army was repelled by ${target}`));
  await target.readsInTheChronicle(new RegExp(`We repelled an attack from ${attacker}`));
  await attacker.hasNoArmyOut();
});
