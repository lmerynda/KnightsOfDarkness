import assert from "node:assert/strict";
import { after, test } from "node:test";
import { ApiError, World, type Player } from "../src/api/client.ts";
import type { ActionResult } from "../src/api/types.ts";
import { startServer, type GameServer } from "../src/server.ts";
import { invariantViolations } from "../src/simulation/world.ts";

// Players who click twice, play from two tabs, or reach for the same thing as someone else at the same moment: every
// request below is sent at once with the others, and whatever order the server takes them in, nothing may be spent
// twice, made out of nothing, or lost. A request may lose the race; it must then say so (a refusal, or 409 when the
// kingdom changed under it), never fail with a server error.

let started: Promise<GameServer> | undefined;

// the first check starts the world: node does not wait for a before hook when a name filter skipped every check here
const sharedWorld = async (): Promise<World> => new World((await (started ??= startServer())).url);

after(async () => {
  if (started) await (await started).stop();
});

type Outcome<T> = { answered: T } | { conflict: true };

/** the answer to one of several requests sent at once, or that it lost the race to a change of the same kingdom */
const raced = async <T>(request: Promise<T>): Promise<Outcome<T>> => {
  try {
    return { answered: await request };
  } catch (error) {
    if (error instanceof ApiError && error.status === 409) return { conflict: true };
    throw error;
  }
};

const all = <T>(count: number, request: (index: number) => Promise<T>): Promise<Outcome<T>[]> =>
  Promise.all(Array.from({ length: count }, (_, index) => raced(request(index))));

const succeeded = <T>(outcomes: Outcome<ActionResult<T>>[]): ActionResult<T>[] =>
  outcomes.flatMap(outcome => ("answered" in outcome && outcome.answered.success ? [outcome.answered] : []));

const brokenRules = async (world: World): Promise<string[]> => invariantViolations(await world.kingdoms(), await world.config(), 0);

let founded = 0;
const found = (world: World, name: string): Promise<Player> => world.foundKingdom(`${name} ${++founded}`);

const foodOfferOf = async (buyer: Player, seller: Player): Promise<{ id: string; count: number } | undefined> =>
  (await buyer.offers("food")).find(offer => offer.sellerName === seller.name);

test("five buyers racing for one offer never take more than it holds, and every coin reaches the seller", async () => {
  const world = await sharedWorld();
  const seller = await found(world, "Race Seller");
  const buyers = await Promise.all([1, 2, 3, 4, 5].map(index => found(world, `Race Buyer ${index}`)));
  assert.ok((await seller.postOffer("food", 300, 5)).success);
  const offer = await foodOfferOf(buyers[0], seller);
  assert.ok(offer, "the offer is not on the market");
  const sellerBefore = (await seller.kingdom()).resources;
  const buyersBefore = await Promise.all(buyers.map(async buyer => (await buyer.kingdom()).resources));

  await all(buyers.length, index => buyers[index].buyOffer(offer.id, 300));

  const sellerAfter = (await seller.kingdom()).resources;
  const buyersAfter = await Promise.all(buyers.map(async buyer => (await buyer.kingdom()).resources));
  const foodBought = buyersAfter.reduce((total, after, index) => total + after.food - buyersBefore[index].food, 0);
  const goldPaid = buyersAfter.reduce((total, after, index) => total + buyersBefore[index].gold - after.gold, 0);
  const left = (await foodOfferOf(buyers[0], seller))?.count ?? 0;
  assert.equal(foodBought + left, 300, `${foodBought} food bought and ${left} left of an offer of 300`);
  assert.equal(goldPaid, foodBought * 5, `${goldPaid} gold paid for ${foodBought} food at 5`);
  assert.equal(sellerAfter.gold - sellerBefore.gold, goldPaid, "the seller was not paid what the buyers spent");
  assert.deepEqual(await brokenRules(world), []);
});

test("a buyer racing the seller's withdrawal gets the food only if the seller does not get it back", async () => {
  const world = await sharedWorld();
  const seller = await found(world, "Withdraw Seller");
  const buyer = await found(world, "Withdraw Buyer");
  assert.ok((await seller.postOffer("food", 200, 5)).success);
  const offer = await foodOfferOf(buyer, seller);
  assert.ok(offer, "the offer is not on the market");
  const foodBefore = (await seller.kingdom()).resources.food + (await buyer.kingdom()).resources.food + offer.count;

  await Promise.all([raced(seller.withdrawOffer(offer.id)), raced(buyer.buyOffer(offer.id, 200))]);

  const foodAfter = (await seller.kingdom()).resources.food + (await buyer.kingdom()).resources.food + ((await foodOfferOf(buyer, seller))?.count ?? 0);
  assert.equal(foodAfter, foodBefore, "food appeared or vanished");
  assert.deepEqual(await brokenRules(world), []);
});

test("kingdoms buying each other's offers at the same moment trade or are told to try again, and nothing is lost", async () => {
  const world = await sharedWorld();
  const pairs = await Promise.all([1, 2, 3, 4, 5, 6, 7, 8].map(async index => [await found(world, `Swap West ${index}`), await found(world, `Swap East ${index}`)] as const));
  const everyone = pairs.flat();
  const holdings = async (): Promise<{ gold: number; food: number }> => {
    const kingdoms = await Promise.all(everyone.map(player => player.kingdom()));
    const onOffer = kingdoms.flatMap(kingdom => kingdom.marketOffers).reduce((sum, offer) => sum + offer.count, 0);
    return { gold: kingdoms.reduce((sum, kingdom) => sum + kingdom.resources.gold, 0), food: kingdoms.reduce((sum, kingdom) => sum + kingdom.resources.food, 0) + onOffer };
  };
  const before = await holdings();
  for (let round = 0; round < 6; round++) {
    const offerOf = async (player: Player): Promise<string> => {
      const posted = await player.postOffer("food", 10, 5);
      assert.ok(posted.success && posted.data, posted.message);
      return posted.data.id;
    };
    // each kingdom buys from the other in its pair at once, so each purchase touches both kingdoms, in opposite orders
    const offers = await Promise.all(pairs.map(async ([west, east]) => [await offerOf(west), await offerOf(east)] as const));
    const outcomes = await Promise.all(pairs.flatMap(([west, east], index) => [raced(west.buyOffer(offers[index][1], 10)), raced(east.buyOffer(offers[index][0], 10))]));
    assert.ok(outcomes.some(outcome => "answered" in outcome && outcome.answered.success), "nobody bought anything");
  }
  assert.deepEqual(await holdings(), before, "gold or food appeared or vanished");
  assert.deepEqual(await brokenRules(world), []);
});

test("a ruler passing turns from ten tabs at once spends each stored turn once", async () => {
  const world = await sharedWorld();
  const ruler = await found(world, "Many Tabs");
  const turnsBefore = (await ruler.kingdom()).resources.turns;

  const passed = succeeded(await all(10, () => ruler.passTurn()));

  assert.ok(passed.length >= 1, "no turn was passed");
  assert.equal((await ruler.kingdom()).resources.turns, turnsBefore - passed.length);
  assert.deepEqual(await brokenRules(world), []);
});

test("training from ten tabs at once never spends gold the kingdom does not have", async () => {
  const world = await sharedWorld();
  const ruler = await found(world, "Spendthrift");
  const before = await ruler.kingdom();
  const cost = (await world.config()).trainingCost.builder.gold;
  // each order alone would spend most of the treasury
  const each = Math.floor((before.resources.gold * 0.6) / cost);

  await all(10, () => ruler.train({ builder: each }));

  const after = await ruler.kingdom();
  const trained = after.units.availableUnits.builder - before.units.availableUnits.builder;
  assert.ok(after.resources.gold >= 0, `${after.resources.gold} gold left`);
  assert.equal(before.resources.gold - after.resources.gold, trained * cost, `${trained} builders trained for ${before.resources.gold - after.resources.gold} gold`);
  assert.deepEqual(await brokenRules(world), []);
});

test("two players founding a kingdom of one name at once: one gets it, the other is told", async () => {
  const world = await sharedWorld();
  const attempts = await Promise.all(
    [1, 2].map(index =>
      fetch(`${world.url}/auth/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: `twin-${index}@simulation.test`, kingdomName: "Twin Crowns", password: "simulation-password" }),
      }).then(response => response.status),
    ),
  );
  // the second hears that the name is taken (400), or that it was claimed at the same moment (409)
  const [first, second] = attempts.toSorted();
  assert.equal(first, 200, `the two sign-ups answered ${attempts.join(" and ")}`);
  assert.ok(second === 400 || second === 409, `the two sign-ups answered ${attempts.join(" and ")}`);
  assert.equal((await world.kingdoms()).filter(kingdom => kingdom.name === "Twin Crowns").length, 1);
});

test("two emperors founding an alliance of one name at once: one gets it, the other is told", async () => {
  const world = await sharedWorld();
  const emperors = await Promise.all([1, 2].map(index => found(world, `Rival Emperor ${index}`)));

  const created = succeeded(await all(2, index => emperors[index].createAlliance("Twin Banners")));

  assert.equal(created.length, 1, `${created.length} alliances named Twin Banners`);
  const members = (await world.kingdoms()).filter(kingdom => kingdom.allianceName === "Twin Banners");
  assert.equal(members.length, 1);
});

test("a kingdom accepting two alliances at once joins only one", async () => {
  const world = await sharedWorld();
  const [left, right, joiner] = await Promise.all(["Left Emperor", "Right Emperor", "Torn Joiner"].map(name => found(world, name)));
  assert.ok((await left.createAlliance(`Left ${founded}`)).success);
  assert.ok((await right.createAlliance(`Right ${founded}`)).success);
  assert.ok((await left.invite(joiner.name, `Left ${founded}`)).success);
  assert.ok((await right.invite(joiner.name, `Right ${founded}`)).success);
  const invitations = await joiner.invitations();
  assert.equal(invitations.length, 2);

  const joined = succeeded(await all(2, index => joiner.acceptInvitation(invitations[index].id)));

  assert.equal(joined.length, 1, `joined ${joined.length} alliances`);
  const alliance = (await world.kingdoms()).find(kingdom => kingdom.name === joiner.name)?.allianceName;
  assert.ok(alliance === `Left ${founded}` || alliance === `Right ${founded}`, `the joiner is in ${alliance}`);
});

test("members paying into a monument at once while the clock ticks lose nothing and make nothing", async () => {
  const world = await sharedWorld();
  const emperor = await found(world, "Mason Emperor");
  const alliance = `Masons ${founded}`;
  assert.ok((await emperor.createAlliance(alliance)).success);
  const masons = [emperor];
  for (let member = 0; member < 3; member++) {
    const mason = await found(world, "Mason");
    assert.ok((await emperor.invite(mason.name, alliance)).success);
    assert.ok((await mason.acceptInvitation((await mason.invitations())[0].id)).success);
    masons.push(mason);
  }
  assert.ok((await emperor.foundMonument(emperor.name)).success);
  const before = await Promise.all(masons.map(async mason => (await mason.kingdom()).resources.gold));

  const [outcomes] = await Promise.all([all(masons.length, index => masons[index].payIntoMonument({ gold: 100 })), world.advance(1, false)]);

  const after = await Promise.all(masons.map(async mason => (await mason.kingdom()).resources.gold));
  const given = before.reduce((sum, gold, index) => sum + gold - after[index], 0);
  assert.equal(given, 100 * succeeded(outcomes).length, "a payment that was refused or lost the race cost gold, or one that went through cost none");
  assert.equal((await emperor.monument()).store.gold, given);
});

test("an assault landing while members call soldiers home, send more and pay in loses nobody and spends nothing twice", async () => {
  const world = await sharedWorld();
  const emperor = await found(world, "Besieged Emperor");
  const mason = await found(world, "Besieged Mason");
  const raider = await found(world, "Besieger");
  for (const ruler of [emperor, mason, raider]) {
    for (let turn = 0; turn < 5; turn++) await ruler.passTurn(100);
    assert.ok((await ruler.train({ infantry: 10 })).success);
  }
  const alliance = `Besieged ${founded}`;
  assert.ok((await emperor.createAlliance(alliance)).success);
  assert.ok((await emperor.invite(mason.name, alliance)).success);
  assert.ok((await mason.acceptInvitation((await mason.invitations())[0].id)).success);
  assert.ok((await emperor.foundMonument(emperor.name)).success);
  assert.ok((await mason.sendToMonument({ infantry: 4 })).success);
  for (let turn = 0; turn < 2; turn++) await mason.passTurn(100);
  const standing = (await mason.kingdom()).reinforcements[0].id;
  assert.ok((await raider.sendAttack(emperor.name, "monument", { infantry: 10 })).success);
  for (let turn = 0; turn < 3; turn++) await raider.passTurn(100);
  const goldBefore = (await emperor.kingdom()).resources.gold;

  const [, , , payment] = await Promise.all([raced(raider.passTurn(100)), raced(mason.recallReinforcements(standing)), raced(mason.sendToMonument({ infantry: 2 })), raced(emperor.payIntoMonument({ gold: 100 }))]);

  // the army may have lost its race to the garrison's owner: it lands with the next try
  for (let attempt = 0; attempt < 5 && (await raider.kingdom()).ongoingAttacks.length > 0; attempt++) await raced(raider.passTurn(100));
  assert.equal((await raider.kingdom()).ongoingAttacks.length, 0, "the army never landed");
  assert.deepEqual(await brokenRules(world), []);
  const given = goldBefore - (await emperor.kingdom()).resources.gold;
  assert.equal(given, succeeded([payment]).length * 100, "the payment cost gold though it failed, or none though it went through");
  assert.equal((await emperor.monument()).store.gold, given);
});

test("the last places in an alliance go to as many kingdoms as there are places", async () => {
  const world = await sharedWorld();
  const maxMembers = (await world.config()).common.allianceMaxMembers;
  const emperor = await found(world, "Crowded Emperor");
  const alliance = `Crowded ${founded}`;
  assert.ok((await emperor.createAlliance(alliance)).success);
  // the alliance fills up to two places short, then three kingdoms take the invitations they hold at once
  for (let member = 2; member <= maxMembers - 2; member++) {
    const joiner = await found(world, "Early Member");
    assert.ok((await emperor.invite(joiner.name, alliance)).success);
    assert.ok((await joiner.acceptInvitation((await joiner.invitations())[0].id)).success);
  }
  const latecomers = await Promise.all([1, 2, 3].map(index => found(world, `Latecomer ${index}`)));
  for (const latecomer of latecomers) assert.ok((await emperor.invite(latecomer.name, alliance)).success);
  const invitations = await Promise.all(latecomers.map(async latecomer => (await latecomer.invitations())[0].id));

  const joined = succeeded(await all(latecomers.length, index => latecomers[index].acceptInvitation(invitations[index])));

  const members = (await world.kingdoms()).filter(kingdom => kingdom.allianceName === alliance);
  assert.equal(members.length, maxMembers, `${members.length} members in an alliance of at most ${maxMembers}`);
  assert.equal(joined.length, 2);
});
