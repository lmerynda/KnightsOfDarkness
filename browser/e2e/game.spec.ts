import { expect, test } from "@playwright/test";
import { foundKingdom, goTo, passTurn, password, signIn, signOut, toast } from "./helpers";

test("the landing page shows the round and lets a new player found a kingdom", async ({ page }) => {
  await page.goto("/");
  await expect(page.getByRole("heading", { name: "Knights of Darkness" })).toBeVisible();
  await expect(page.getByRole("heading", { name: /Round \d+/ })).toBeVisible();

  const { kingdomName } = await foundKingdom(page);

  await expect(page.locator(".rail-name")).toHaveText(kingdomName);
  await expect(page.getByText("If the turn passed now")).toBeVisible();
});

test("registration explains what is wrong", async ({ page }) => {
  await page.goto("/register");
  await page.getByLabel("Kingdom name").fill("uprzejmy");
  await page.getByLabel("Email").fill(`taken-${Date.now()}@example.com`);
  await page.getByLabel("Password, at least 8 characters").fill(password);
  await page.getByLabel("Password again").fill(password);
  await page.getByRole("button", { name: "Found the kingdom" }).click();
  await expect(page.getByText("A kingdom with this name already exists")).toBeVisible();

  await page.getByLabel("Password again").fill("something-else");
  await page.getByRole("button", { name: "Found the kingdom" }).click();
  await expect(page.getByText("The two passwords are different")).toBeVisible();
});

test("a wrong password is refused with a message", async ({ page }) => {
  await page.goto("/sign-in");
  await page.getByLabel("Email").fill("uprzejmy");
  await page.getByLabel("Password").fill("not-the-password");
  await page.getByRole("button", { name: "Enter" }).click();
  await expect(page.getByText("Wrong email or password")).toBeVisible();
});

test("a ruler raises buildings, trains workers and passes the turn", async ({ page }) => {
  await foundKingdom(page);

  await goTo(page, "Build");
  const housesBefore = Number(await page.locator(".plate").filter({ hasText: "House" }).first().locator(".plate-count").textContent());
  await page.getByLabel("Houses to raise", { exact: true }).fill("3");
  await expect(page.locator(".order-bar")).toContainText("Raise 3 buildings");
  await page.getByRole("button", { name: "Raise them" }).click();
  await expect(toast(page, "Raised 3 buildings")).toBeVisible();
  await expect(page.locator(".plate").filter({ hasText: "House" }).first().locator(".plate-count")).toHaveText(String(housesBefore + 3));

  await goTo(page, "Train");
  await page.getByLabel("Farmers to train", { exact: true }).fill("2");
  await page.getByRole("button", { name: "Train them" }).click();
  await expect(toast(page, "Trained 2 recruits")).toBeVisible();

  await passTurn(page);
  await expect(toast(page, /A turn has passed: \+[\d,]+ gold/).first()).toBeVisible();
});

test("the steward tells a newcomer what to do and leads there", async ({ page }) => {
  await foundKingdom(page);

  const advice = page.locator(".counsel li").filter({ hasText: "acres stand empty" });
  await expect(advice).toBeVisible();
  await advice.getByRole("link", { name: "Build" }).click();
  await expect(page).toHaveURL(/\/kingdom\/build$/);
});

test("land can be priced and bought", async ({ page }) => {
  await foundKingdom(page);
  await goTo(page, "Build");

  await page.getByLabel("Acres to buy").fill("5");
  await expect(page.getByText(/5 acres cost [\d,]+ gold/)).toBeVisible();
  await page.getByRole("button", { name: "Buy land" }).click();
  await expect(toast(page, /Bought 5 acres for [\d,]+ gold/)).toBeVisible();
});

test("an army marches, fights and leaves a battle report", async ({ page }) => {
  await signIn(page, "umbar", "umbar_password");
  await goTo(page, "Army");

  await page.getByRole("combobox", { name: /^Target/ }).selectOption("IronMinerBot");
  await page.getByLabel("Cavalry to send", { exact: true }).fill("20");
  await page.getByRole("button", { name: "March" }).click();
  await expect(toast(page, /The army marches on IronMinerBot/)).toBeVisible();
  await expect(page.getByRole("heading", { name: "Armies on the march" })).toBeVisible();

  for (let turn = 0; turn < 4; turn++) {
    await passTurn(page);
  }

  await expect(page.locator(".battle").first()).toContainText(/IronMinerBot/);
  await expect(page.getByRole("heading", { name: "Armies on the march" })).toHaveCount(0);
});

test("spies bring back intel about another kingdom", async ({ page }) => {
  await signIn(page, "uprzejmy", "uprzejmy_password");
  await goTo(page, "Spies");

  await page.getByRole("combobox", { name: /^Target/ }).selectOption("FarmerBot");
  await page.getByLabel(/Spies, \d+ at home/).fill("3");
  await page.getByRole("button", { name: "Send spies" }).click();

  await expect(page.locator(".intel").first()).toContainText("FarmerBot");
  await expect(page.locator(".intel").first()).toContainText("Stores");
});

test("food can be bought from a merchant on the market", async ({ page }) => {
  await foundKingdom(page);
  await goTo(page, "Market");

  const offer = page.locator("tbody tr").filter({ hasNotText: "(you)" }).first();
  await expect(offer).toBeVisible();
  await offer.getByRole("textbox").or(offer.getByRole("spinbutton")).fill("10");
  await offer.getByRole("button", { name: /^Buy/ }).click();
  await expect(toast(page, /Bought 10 food for [\d,]+ gold/)).toBeVisible();
});

test("letters reach the other kingdom", async ({ page }) => {
  const sender = await foundKingdom(page, "Writer");
  await goTo(page, "Letters");
  await page.getByRole("combobox", { name: /^To/ }).selectOption("Umbar");
  const body = `Greetings from ${sender.kingdomName}`;
  await page.getByLabel(/Letter, \d+ of 2000 characters/).fill(body);
  await page.getByRole("button", { name: "Seal and send" }).click();
  await expect(toast(page, "Message sent to Umbar")).toBeVisible();

  await signOut(page);
  await signIn(page, "umbar", "umbar_password");
  await expect(page.getByRole("link", { name: /Letters/ }).locator(".badge")).toBeVisible();
  await goTo(page, "Letters");
  await page.getByRole("button", { name: new RegExp(sender.kingdomName) }).first().click();
  await expect(page.locator(".letter-body")).toContainText(body);
});

test("an alliance is founded and a second kingdom joins it", async ({ page }) => {
  const invitee = await foundKingdom(page, "Squire");
  await signOut(page);

  const emperor = await foundKingdom(page, "Emperor");
  await goTo(page, "Alliance");
  const allianceName = `Order ${Date.now() % 100000}`;
  await page.getByLabel("Alliance name").fill(allianceName);
  await page.getByRole("button", { name: "Found it" }).click();
  await expect(page.getByRole("heading", { name: allianceName })).toBeVisible();
  await page.getByRole("combobox", { name: /^Kingdom/ }).selectOption(invitee.kingdomName);
  await page.getByRole("button", { name: "Send invitation" }).click();
  await expect(toast(page, `Invitation sent to ${invitee.kingdomName}`)).toBeVisible();

  await signOut(page);
  await signIn(page, invitee.email, password);
  await goTo(page, "Alliance");
  await page.getByRole("button", { name: "Join" }).click();
  await expect(page.getByRole("heading", { name: allianceName })).toBeVisible();
  await expect(page.locator(".rail-alliance")).toHaveText(allianceName);
  await expect(page.locator("tbody")).toContainText(emperor.kingdomName);
});
