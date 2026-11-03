import assert from "node:assert/strict";
import { scenario } from "../src/dsl/scenario.ts";

scenario("a ruler raises buildings, trains workers and passes the turn", async game => {
  const ruler = await game.foundsAKingdom();
  const houses = await ruler.buildingsOf("house");
  await ruler.raises({ house: 3 });
  await ruler.owns(houses + 3, "house");
  await ruler.raises({ house: 1, farm: 1 });
  await ruler.owns(houses + 4, "house");
  await ruler.trains({ farmer: 2 });
  await ruler.passesTurns(1);
});

scenario("a new ruler arms soldiers after a few turns and pays them every turn", async game => {
  const ruler = await game.foundsAKingdom();
  await ruler.passesTurns(5);
  await ruler.trains({ infantry: 10 });
  await ruler.paysSoldiers(1500);
});

scenario("land is priced and bought", async game => {
  const ruler = await game.foundsAKingdom();
  assert.ok((await ruler.priceOfLand(5)) > 0, "land is free");
  await ruler.buysLand(5);
});
