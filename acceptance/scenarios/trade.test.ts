import { scenario } from "../src/dsl/scenario.ts";

scenario("food is bought from a merchant on the market", async game => {
  const buyer = await game.foundsAKingdom("Buyer");
  // the merchants put their goods on the market as time passes
  await game.timePasses(1);
  await buyer.buys(10, "food");
});

scenario("goods offered on the market are bought, and the seller is paid", async game => {
  const seller = await game.foundsAKingdom("Seller");
  const buyer = await game.foundsAKingdom("Buyer");
  await seller.offersForSale(100, "food", 5);
  await seller.hasOnTheMarket(100, "food");
  await buyer.buysFrom(seller, 100, "food");
  await seller.hasNothingOnTheMarket();
  await seller.readsInTheChronicle(new RegExp(`${buyer} bought 100 food from us for 500 gold`));
});

scenario("goods withdrawn from the market come home", async game => {
  const seller = await game.foundsAKingdom("Seller");
  await seller.offersForSale(50, "iron", 40);
  await seller.hasOnTheMarket(50, "iron");
  await seller.withdrawsTheOfferOf("iron");
  await seller.hasNothingOnTheMarket();
});

scenario("carriers called back never deliver", async game => {
  const receiver = await game.foundsAKingdom("Receiver");
  const hauler = await game.foundsAKingdom("Hauler");
  await hauler.raises({ market: 1 });
  await hauler.trains({ carrier: 1 });
  await hauler.sendsCarriers(receiver, 100, "food");
  await hauler.callsBackTheCarriersFrom(receiver);
  await hauler.hasNoCarriersOut();
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
