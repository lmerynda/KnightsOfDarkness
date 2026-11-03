import { scenario } from "../src/dsl/scenario.ts";

scenario("food is bought from a merchant on the market", async game => {
  const buyer = await game.foundsAKingdom("Buyer");
  // the merchants put their goods on the market as time passes
  await game.timePasses(1);
  await buyer.buys(10, "food");
});

scenario("carriers haul goods to another kingdom", async game => {
  const receiver = await game.foundsAKingdom("Receiver");
  const hauler = await game.foundsAKingdom("Hauler");
  await hauler.raises({ market: 1 });
  await hauler.trains({ carrier: 2 });
  await hauler.sendsCarriers(receiver, 100, "food");
  await hauler.hasCarriersOnTheWayTo(receiver, 100, "food");
  await hauler.passesTurns(2);
  await hauler.hasNoCarriersOut();
  await receiver.readsInTheChronicle(/carriers delivered 100 food/);
});
