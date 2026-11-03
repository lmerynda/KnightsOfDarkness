import { scenario } from "../src/dsl/scenario.ts";

scenario("a newcomer founds a kingdom and comes back to it", async game => {
  const ruler = await game.foundsAKingdom();
  await ruler.ownsTheKingdom();
  await ruler.comesBack();
});

scenario("a kingdom name is taken only once", async game => {
  const first = await game.foundsAKingdom();
  await game.refusesToFound(first.name, "A kingdom with this name already exists");
});

scenario("a wrong password is refused", async game => {
  const ruler = await game.foundsAKingdom();
  await game.refusesToSignIn({ email: ruler.credentials.email, password: "not-the-password" }, "Wrong email or password");
});

scenario("a ruler plays with only a kingdom name, then keeps the kingdom", async game => {
  const ruler = await game.playsWithOnlyAName();
  await ruler.playsOnATrial();
  await ruler.passesTurns(1);
  await ruler.keepsTheKingdom({ email: `kept-${Date.now()}@acceptance.test`, password: "kept-password" });
  await ruler.ownsTheKingdom();
  await ruler.comesBack();
});
