import { scenario } from "../src/dsl/scenario.ts";

// The clock brings every kingdom a turn each hour, whether its ruler is there or not; a ruler plays them when they come
// back. Here a tick of the clock stands for the hour.

scenario("the clock brings a turn every hour, and keeps at most 48 waiting", async game => {
  const ruler = await game.foundsAKingdom("Waiting");
  await ruler.hasTurnsStored(20);
  await game.timePasses(1);
  await ruler.hasTurnsStored(21);
  // away for two days, the ruler finds no more than 48 turns waiting
  await game.timePasses(48);
  await ruler.hasTurnsStored(48);
});

scenario("a ruler who has played every turn waits for the clock", async game => {
  const ruler = await game.foundsAKingdom("Eager");
  await ruler.passesTurns(20);
  await ruler.hasTurnsStored(0);
  await ruler.mayNotPassATurn("No turns left");
  await game.timePasses(1);
  await ruler.passesTurns(1);
  await ruler.hasTurnsStored(0);
});

scenario("a beaten kingdom is spared for a day of the clock", async game => {
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
  await game.timePasses(23);
  await second.mayNotMarchOn(target, { infantry: 10 }, /protected/);
  await game.timePasses(1);
  await second.marchesOn(target, { infantry: 10 });
});
