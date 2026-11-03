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

scenario("buildings torn down free their land", async game => {
  const ruler = await game.foundsAKingdom();
  const houses = await ruler.buildingsOf("house");
  await ruler.raises({ house: 2 });
  await ruler.owns(houses + 2, "house");
  await ruler.tearsDown({ house: 2 });
  await ruler.owns(houses, "house");
});

scenario("dismissed workers go back to the idle", async game => {
  const ruler = await game.foundsAKingdom();
  const farmers = await ruler.unitsAtHome("farmer");
  await ruler.trains({ farmer: 2 });
  await ruler.hasAtHome(farmers + 2, "farmer");
  await ruler.dismisses({ farmer: 2 });
  await ruler.hasAtHome(farmers, "farmer");
});

scenario("a great work rises level by level and is lost when torn down", async game => {
  const ruler = await game.foundsAKingdom();
  await ruler.beginsAGreatWork("warehouse");
  await ruler.hasAGreatWork("warehouse", 0);
  // the builders bring a turn's worth of points, more than the first level takes
  await ruler.passesTurns(1);
  await ruler.investsIn("warehouse", 30000);
  await ruler.hasAGreatWork("warehouse", 1);
  await ruler.tearsDownTheGreatWork("warehouse");
  await ruler.hasNoGreatWorks();
});

scenario("land is priced and bought", async game => {
  const ruler = await game.foundsAKingdom();
  assert.ok((await ruler.priceOfLand(5)) > 0, "land is free");
  await ruler.buysLand(5);
});
