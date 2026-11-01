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

test("several turns pass with one order", async ({ page }) => {
  await foundKingdom(page);
  const passed = page.waitForResponse(response => response.url().endsWith("/kingdom/pass-turn") && response.ok());
  await page.getByRole("button", { name: "Pass 5 turns" }).click();
  await passed;

  await expect(toast(page, /5 turns have passed: \+[\d,]+ gold/)).toBeVisible();
});

test("a new ruler can arm soldiers after a few turns", async ({ page }) => {
  await foundKingdom(page);
  const passed = page.waitForResponse(response => response.url().endsWith("/kingdom/pass-turn") && response.ok());
  await page.getByRole("button", { name: "Pass 5 turns" }).click();
  await passed;
  await expect(toast(page, /5 turns have passed/)).toBeVisible();

  await goTo(page, "Train");
  await page.getByLabel("Infantry to train", { exact: true }).fill("10");
  await page.getByRole("button", { name: "Train them" }).click();
  await expect(toast(page, "Trained 10 recruits")).toBeVisible();
  await expect(page.locator("article").filter({ hasText: "Infantry" }).first()).toContainText("Paid 150 gold a turn");

  // soldiers are paid every turn, and the forecast says what they cost
  await goTo(page, "Overview");
  await expect(page.locator("tr").filter({ hasText: "Soldiers' pay" })).toContainText("-1,500");
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

test("an army marches, fights and leaves a battle report", async ({ page, browser }) => {
  // two fresh kingdoms, so a beaten kingdom's protection or a grown attacker from an earlier run cannot get in the way
  const other = await browser.newPage();
  const { kingdomName: target } = await foundKingdom(other, "Target");
  const { kingdomName: attacker } = await foundKingdom(page);
  const passed = page.waitForResponse(response => response.url().endsWith("/kingdom/pass-turn") && response.ok());
  await page.getByRole("button", { name: "Pass 5 turns" }).click();
  await passed;
  await expect(toast(page, /5 turns have passed/)).toBeVisible();
  await goTo(page, "Train");
  await page.getByLabel("Infantry to train", { exact: true }).fill("10");
  await page.getByRole("button", { name: "Train them" }).click();
  await expect(toast(page, "Trained 10 recruits")).toBeVisible();

  await goTo(page, "Army");
  await page.getByRole("combobox", { name: /^Target/ }).selectOption(target);
  await page.getByLabel("Infantry to send", { exact: true }).fill("10");
  await page.getByRole("button", { name: "March" }).click();
  await expect(toast(page, `The army marches on ${target}`)).toBeVisible();
  await expect(page.getByRole("heading", { name: "Armies on the march" })).toBeVisible();

  // the target's scouts see the army coming
  await goTo(other, "Army");
  await expect(other.locator("main")).toContainText(`${attacker} with about 10 soldiers`);
  await goTo(other, "Overview");
  await expect(other.locator(".counsel li.urgent")).toContainText(`${attacker} marches on us`);
  await other.close();

  for (let turn = 0; turn < 4; turn++) {
    await passTurn(page);
  }

  await expect(page.locator(".battle").first()).toContainText(`Victory over ${target}`);
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
  await page
    .getByRole("button", { name: new RegExp(sender.kingdomName) })
    .first()
    .click();
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

test("an ally's soldiers stand guard and go home when called", async ({ page, browser }) => {
  const squire = await foundKingdom(page, "Squire");
  await signOut(page);
  const emperor = await foundKingdom(page, "Emperor");
  await goTo(page, "Alliance");
  await page.getByLabel("Alliance name").fill(`Shield ${Date.now() % 100000}`);
  await page.getByRole("button", { name: "Found it" }).click();
  await page.getByRole("combobox", { name: /^Kingdom/ }).selectOption(squire.kingdomName);
  await page.getByRole("button", { name: "Send invitation" }).click();
  await expect(toast(page, `Invitation sent to ${squire.kingdomName}`)).toBeVisible();

  const squirePage = await browser.newPage();
  await signIn(squirePage, squire.email, password);
  await goTo(squirePage, "Alliance");
  await squirePage.getByRole("button", { name: "Join" }).click();
  await goTo(squirePage, "Overview");
  const passed = squirePage.waitForResponse(response => response.url().endsWith("/kingdom/pass-turn") && response.ok());
  await squirePage.getByRole("button", { name: "Pass 5 turns" }).click();
  await passed;
  await expect(toast(squirePage, /5 turns have passed/)).toBeVisible();
  await goTo(squirePage, "Train");
  await squirePage.getByLabel("Infantry to train", { exact: true }).fill("10");
  await squirePage.getByRole("button", { name: "Train them" }).click();
  await expect(toast(squirePage, "Trained 10 recruits")).toBeVisible();
  await goTo(squirePage, "Army");
  await squirePage.getByRole("combobox", { name: "Ally" }).selectOption(emperor.kingdomName);
  await squirePage.getByLabel(/^Infantry to send, \d+ at home/).fill("10");
  await squirePage.getByRole("button", { name: "Send to stand guard" }).click();
  await expect(toast(squirePage, `10 soldiers march to stand guard in ${emperor.kingdomName}`)).toBeVisible();
  for (let turn = 0; turn < 2; turn++) {
    await passTurn(squirePage);
  }
  await expect(squirePage.locator("main")).toContainText(`10 infantry stand guard in ${emperor.kingdomName}`);

  await goTo(page, "Overview");
  await goTo(page, "Army");
  await expect(page.locator("main")).toContainText(`10 infantry from ${squire.kingdomName} stand guard with us`);
  await goTo(page, "Alliance");
  await expect(page.locator("main")).toContainText(`${squire.kingdomName} → ${emperor.kingdomName}, 10 soldiers`);

  await squirePage.getByRole("button", { name: "Call home" }).click();
  await expect(toast(squirePage, "Our soldiers are home again")).toBeVisible();
  await squirePage.close();
});

test("allies see each other's armies and join the strike", async ({ page, browser }) => {
  const other = await browser.newPage();
  const { kingdomName: target } = await foundKingdom(other, "Target");
  await other.close();
  const squire = await foundKingdom(page, "Squire");
  await signOut(page);
  await foundKingdom(page, "Emperor");
  await goTo(page, "Alliance");
  const allianceName = `Pact ${Date.now() % 100000}`;
  await page.getByLabel("Alliance name").fill(allianceName);
  await page.getByRole("button", { name: "Found it" }).click();
  await page.getByRole("combobox", { name: /^Kingdom/ }).selectOption(squire.kingdomName);
  await page.getByRole("button", { name: "Send invitation" }).click();
  await expect(toast(page, `Invitation sent to ${squire.kingdomName}`)).toBeVisible();
  const emperorPage = page;

  const squirePage = await browser.newPage();
  await signIn(squirePage, squire.email, password);
  await goTo(squirePage, "Alliance");
  await squirePage.getByRole("button", { name: "Join" }).click();
  await expect(squirePage.locator(".rail-alliance")).toHaveText(allianceName);
  await goTo(squirePage, "Overview");
  const passed = squirePage.waitForResponse(response => response.url().endsWith("/kingdom/pass-turn") && response.ok());
  await squirePage.getByRole("button", { name: "Pass 5 turns" }).click();
  await passed;
  await expect(toast(squirePage, /5 turns have passed/)).toBeVisible();
  await goTo(squirePage, "Train");
  await squirePage.getByLabel("Infantry to train", { exact: true }).fill("10");
  await squirePage.getByRole("button", { name: "Train them" }).click();
  await expect(toast(squirePage, "Trained 10 recruits")).toBeVisible();
  await goTo(squirePage, "Army");
  await squirePage.getByRole("combobox", { name: /^Target/ }).selectOption(target);
  await squirePage.getByLabel("Infantry to send", { exact: true }).fill("10");
  await squirePage.getByRole("button", { name: "March" }).click();
  await expect(toast(squirePage, `The army marches on ${target}`)).toBeVisible();
  await squirePage.close();

  // the steward points an emperor with soldiers at home to the strike
  await goTo(emperorPage, "Overview");
  const emperorPassed = emperorPage.waitForResponse(response => response.url().endsWith("/kingdom/pass-turn") && response.ok());
  await emperorPage.getByRole("button", { name: "Pass 5 turns" }).click();
  await emperorPassed;
  await expect(toast(emperorPage, /5 turns have passed/)).toBeVisible();
  await goTo(emperorPage, "Train");
  await emperorPage.getByLabel("Infantry to train", { exact: true }).fill("10");
  await emperorPage.getByRole("button", { name: "Train them" }).click();
  await expect(toast(emperorPage, "Trained 10 recruits")).toBeVisible();
  await goTo(emperorPage, "Overview");
  await expect(emperorPage.locator(".counsel")).toContainText(`${squire.kingdomName} marches on ${target}`);

  await goTo(emperorPage, "Alliance");
  await expect(emperorPage.getByRole("heading", { name: "War room" })).toBeVisible();
  await expect(emperorPage.locator("main")).toContainText(`${squire.kingdomName} → ${target}`);
  await goTo(emperorPage, "Army");
  const march = emperorPage.locator("li").filter({ hasText: `${squire.kingdomName} marches` });
  await expect(march).toContainText(target);
  await march.getByRole("button", { name: "Join the strike" }).click();
  await expect(emperorPage.getByRole("combobox", { name: /^Target/ })).toHaveValue(target);
});

test("an ally asks for gold and another sends it through the market", async ({ page, browser }) => {
  const squire = await foundKingdom(page, "Squire");
  await signOut(page);
  await foundKingdom(page, "Emperor");
  await goTo(page, "Alliance");
  await page.getByLabel("Alliance name").fill(`Purse ${Date.now() % 100000}`);
  await page.getByRole("button", { name: "Found it" }).click();
  await page.getByRole("combobox", { name: /^Kingdom/ }).selectOption(squire.kingdomName);
  await page.getByRole("button", { name: "Send invitation" }).click();
  await expect(toast(page, `Invitation sent to ${squire.kingdomName}`)).toBeVisible();

  const squirePage = await browser.newPage();
  await signIn(squirePage, squire.email, password);
  await goTo(squirePage, "Alliance");
  await squirePage.getByRole("button", { name: "Join" }).click();
  await squirePage.getByLabel("Gold to ask for").fill("50000");
  await squirePage.getByRole("button", { name: "Ask allies for 50,000 gold" }).click();
  await expect(toast(squirePage, "Your allies can send you 50,000 gold now")).toBeVisible();
  await expect(squirePage.locator("main")).toContainText("You ask for 50,000 gold");

  await goTo(page, "Overview");
  await expect(page.locator(".counsel")).toContainText(`${squire.kingdomName} asks the alliance for 50,000 gold`);
  await goTo(page, "Market");
  await expect(page.locator("tbody")).toContainText(`${squire.kingdomName} (ally)`);
  await goTo(page, "Alliance");
  const ask = page.locator("li").filter({ hasText: `${squire.kingdomName} asks for 50,000 gold` });
  await ask.getByRole("button", { name: "Send 50,000 gold" }).click();
  await expect(toast(page, `${squire.kingdomName} has 50,000 more gold`)).toBeVisible();
  await expect(page.locator("main")).toContainText("Our allies have nothing on the market.");

  await goTo(squirePage, "Chronicle");
  await expect(squirePage.locator("main")).toContainText("for 50,000 gold");
  await squirePage.close();
});
