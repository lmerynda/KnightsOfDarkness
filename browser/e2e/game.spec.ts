import { expect, test } from "@playwright/test";
import { foundKingdom, goTo, passTurn, passTurns, password, signIn, signOut, toast } from "./helpers";

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
  await expect(page.locator(".plate").filter({ hasText: "Farm" }).first()).toContainText(/Each employs \d+ farmers/);
  const housesBefore = Number(await page.locator(".plate").filter({ hasText: "House" }).first().locator(".plate-count").textContent());
  // one plate is ordered from its own button; the bar to order them all appears only for two or more
  await page.getByLabel("Houses to raise", { exact: true }).fill("3");
  await expect(page.locator(".order-bar")).toHaveCount(0);
  await page.getByRole("button", { name: "Raise houses", exact: true }).click();
  await expect(toast(page, "Raised 3 buildings")).toBeVisible();
  await expect(page.locator(".plate").filter({ hasText: "House" }).first().locator(".plate-count")).toHaveText(String(housesBefore + 3));
  await page.getByLabel("Houses to raise", { exact: true }).fill("1");
  await page.getByLabel("Farms to raise", { exact: true }).fill("1");
  await expect(page.locator(".order-bar")).toContainText("Raise 2 buildings");
  await page.getByRole("button", { name: "Raise all" }).click();
  await expect(toast(page, "Raised 2 buildings")).toBeVisible();
  await expect(page.locator(".plate").filter({ hasText: "House" }).first().locator(".plate-count")).toHaveText(String(housesBefore + 4));

  await goTo(page, "Train");
  await expect(page.locator(".plate").filter({ hasText: "Farmer" }).first()).toContainText(/Farms: \d+ of \d+ places, \d+ each/);
  await page.getByLabel("Farmers to train", { exact: true }).fill("2");
  await page.getByRole("button", { name: "Train farmers", exact: true }).click();
  await expect(toast(page, "Trained 2 recruits")).toBeVisible();

  await passTurn(page);
  await expect(toast(page, /A turn has passed: \+[\d,]+ gold/).first()).toBeVisible();
});

test("a new ruler can arm soldiers after a few turns", async ({ page }) => {
  await foundKingdom(page);
  await passTurns(page, 5);
  // each turn's summary replaces the one before, so quick turns leave one message, not a pile
  await expect(toast(page, /A turn has passed/)).toHaveCount(1);

  await goTo(page, "Train");
  await page.getByLabel("Infantry to train", { exact: true }).fill("10");
  await page.getByRole("button", { name: "Train infantry", exact: true }).click();
  await expect(toast(page, "Trained 10 recruits")).toBeVisible();
  await expect(page.locator("article").filter({ hasText: "Infantry" }).first()).toContainText("Paid 150 gold a turn");

  // soldiers are paid every turn, and the forecast says what they cost
  await goTo(page, "Overview");
  await expect(page.locator("tr").filter({ hasText: "Soldiers' pay" })).toContainText("-1,500");
});

test("the rules wait behind a mark until hovered or tapped", async ({ page }) => {
  await foundKingdom(page);
  await goTo(page, "Build");

  const rule = page.getByRole("tooltip");
  await expect(page.locator("main")).not.toContainText("Every building stands on an acre");
  const mark = page.getByRole("button", { name: "About Land" });
  await mark.hover();
  await expect(rule).toContainText("Every building stands on an acre");
  await page.mouse.move(0, 0);
  await expect(rule).toHaveCount(0);

  // a tap keeps it open until the next tap
  await mark.click();
  await page.mouse.move(0, 0);
  await expect(rule).toContainText("Every building stands on an acre");
  await mark.click();
  await expect(rule).toHaveCount(0);

  // words explain themselves the same way
  await goTo(page, "Overview");
  // houses always fill, so the realm shows how many of their people stand idle
  await expect(page.locator(".realm-meters")).toContainText(/Idle people[\s\S]*at work/);
  await page.locator(".term", { hasText: "counter intelligence" }).hover();
  await expect(rule).toContainText("for each spy");

  // and so do the figures in the top bar
  await page.getByRole("button", { name: /^Gold [\d,]+$/ }).hover();
  await expect(rule).toContainText("from the mines next turn");
  await page.getByRole("button", { name: /^Idle people [\d,]+$/ }).hover();
  await expect(rule).toContainText(/Ready to train; [\d,]+ others at work/);
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
  await passTurns(page, 5);
  await goTo(page, "Train");
  await page.getByLabel("Infantry to train", { exact: true }).fill("10");
  await page.getByRole("button", { name: "Train infantry", exact: true }).click();
  await expect(toast(page, "Trained 10 recruits")).toBeVisible();

  await goTo(page, "Army");
  await page.getByRole("combobox", { name: /^Target/ }).selectOption(target);
  await expect(page.locator("main")).toContainText("At stake: up to 15 acres");
  await page.getByLabel("Infantry to send", { exact: true }).fill("10");
  await page.getByRole("button", { name: "March" }).click();
  await expect(toast(page, `The army marches on ${target}`)).toBeVisible();
  await expect(page.getByRole("heading", { name: "Armies on the march" })).toBeVisible();

  // the target's scouts see the army coming
  await goTo(other, "Army");
  await expect(other.locator("main")).toContainText(`${attacker} with about 10 soldiers`);
  await goTo(other, "Overview");
  const warning = other.locator(".counsel li.urgent").filter({ hasText: `${attacker} marches on us` });
  await warning.getByRole("link", { name: "Prepare" }).click();
  await expect(other).toHaveURL(/\/kingdom\/war$/);
  await other.close();

  await passTurns(page, 4);

  await expect(page.locator(".battle").first()).toContainText(`Victory over ${target}`);
  await expect(page.getByRole("heading", { name: "Armies on the march" })).toHaveCount(0);
});

test("spies bring back intel about another kingdom", async ({ page }) => {
  await signIn(page, "uprzejmy", "uprzejmy_password");
  await goTo(page, "Spies");

  await page.getByRole("combobox", { name: /^Target/ }).selectOption("FarmerBot");
  await page.getByLabel(/Spies, \d+ at home/).fill("3");
  await page.getByRole("button", { name: "Send spies", exact: true }).click();

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

test("on a phone the market's buy field stays on screen", async ({ page }) => {
  await foundKingdom(page);
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto("/kingdom/market");

  const buy = page.getByRole("button", { name: /^Buy/ }).first();
  await expect(buy).toBeVisible();
  const box = await buy.boundingBox();
  expect((box?.x ?? Infinity) + (box?.width ?? 0)).toBeLessThanOrEqual(390);
  await page
    .getByRole("spinbutton", { name: /^Amount of food to buy/ })
    .first()
    .fill("10");
  await buy.click();
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

test("carriers haul goods to another kingdom", async ({ page, browser }) => {
  const other = await browser.newPage();
  const { kingdomName: receiver } = await foundKingdom(other, "Receiver");
  await foundKingdom(page, "Hauler");
  await goTo(page, "Build");
  await page.getByLabel("Markets to raise", { exact: true }).fill("1");
  await page.getByRole("button", { name: "Raise markets", exact: true }).click();
  await expect(toast(page, "Raised 1 building")).toBeVisible();
  await goTo(page, "Train");
  await page.getByLabel("Carriers to train", { exact: true }).fill("2");
  await page.getByRole("button", { name: "Train carriers", exact: true }).click();
  await expect(toast(page, "Trained 2 recruits")).toBeVisible();

  await goTo(page, "Carriers");
  await page.getByRole("combobox", { name: /^To/ }).selectOption(receiver);
  await page.getByLabel("Amount", { exact: true }).fill("100");
  await page.getByRole("button", { name: "Send 1 carrier" }).click();
  await expect(toast(page, `Carriers set out for ${receiver}`)).toBeVisible();
  await expect(page.locator("tr").filter({ hasText: receiver })).toContainText("100 food");

  await passTurns(page, 2);
  await expect(page.locator("main")).toContainText("No carriers are out.");
  await goTo(other, "Chronicle");
  await expect(other.locator("main")).toContainText(/carriers delivered 100 food/);
  await other.close();
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
  await passTurns(squirePage, 5);
  await goTo(squirePage, "Train");
  await squirePage.getByLabel("Infantry to train", { exact: true }).fill("10");
  await squirePage.getByRole("button", { name: "Train infantry", exact: true }).click();
  await expect(toast(squirePage, "Trained 10 recruits")).toBeVisible();
  await goTo(squirePage, "Army");
  await squirePage.getByRole("combobox", { name: "Ally" }).selectOption(emperor.kingdomName);
  await squirePage.getByLabel(/^Infantry to send, \d+ at home/).fill("10");
  await squirePage.getByRole("button", { name: "Send to stand guard" }).click();
  await expect(toast(squirePage, `10 soldiers march to stand guard in ${emperor.kingdomName}`)).toBeVisible();
  await passTurns(squirePage, 2);
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
  const emperor = await foundKingdom(page, "Emperor");
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
  await passTurns(squirePage, 5);
  await goTo(squirePage, "Train");
  await squirePage.getByLabel("Infantry to train", { exact: true }).fill("10");
  await squirePage.getByRole("button", { name: "Train infantry", exact: true }).click();
  await expect(toast(squirePage, "Trained 10 recruits")).toBeVisible();
  await goTo(squirePage, "Army");
  await squirePage.getByRole("combobox", { name: /^Target/ }).selectOption(target);
  await squirePage.getByLabel("Infantry to send", { exact: true }).fill("10");
  await squirePage.getByRole("button", { name: "March" }).click();
  await expect(toast(squirePage, `The army marches on ${target}`)).toBeVisible();
  await squirePage.close();

  // the steward points an emperor with soldiers at home to the strike
  await goTo(emperorPage, "Overview");
  await passTurns(emperorPage, 5);
  await goTo(emperorPage, "Train");
  await emperorPage.getByLabel("Infantry to train", { exact: true }).fill("10");
  await emperorPage.getByRole("button", { name: "Train infantry", exact: true }).click();
  await expect(toast(emperorPage, "Trained 10 recruits")).toBeVisible();
  await goTo(emperorPage, "Overview");
  await expect(emperorPage.locator(".counsel")).toContainText(`${squire.kingdomName} marches on ${target}`);

  await goTo(emperorPage, "Alliance");
  await expect(emperorPage.getByRole("heading", { name: "War room" })).toBeVisible();
  await expect(emperorPage.locator("main")).toContainText(`${squire.kingdomName} → ${target}`);
  await goTo(emperorPage, "Army");
  const march = emperorPage.locator("li").filter({ hasText: `${squire.kingdomName} marches` });
  await expect(march).toContainText(target);
  await expect(march.getByRole("button", { name: "Join the strike" })).toBeVisible();

  // the steward's link sets the strike up on the Army page
  await goTo(emperorPage, "Overview");
  await emperorPage.locator(".counsel").getByRole("link", { name: "Join the strike" }).click();
  await expect(emperorPage).toHaveURL(/\/kingdom\/war$/);
  await expect(emperorPage.getByRole("combobox", { name: /^Target/ })).toHaveValue(target);
  await expect(emperorPage.getByRole("combobox", { name: /^Target/ }).locator("option", { hasText: squire.kingdomName })).toHaveCount(0);

  // the joined army waits at the border, however many turns the emperor passes, and fights right after the squire's lands
  const sendArmy = emperorPage.locator(".panel").filter({ has: emperorPage.getByRole("heading", { name: "Send an army" }) });
  await expect(sendArmy).toContainText(`Joining ${squire.kingdomName}'s strike on ${target}`);
  await emperorPage.getByLabel("Infantry to send", { exact: true }).fill("10");
  await sendArmy.getByRole("button", { name: "Join the strike" }).click();
  await expect(toast(emperorPage, `The army marches to join ${squire.kingdomName}'s strike on ${target}`)).toBeVisible();
  await expect(march).toContainText("Our army joins");
  await passTurns(emperorPage, 5);
  const ours = emperorPage.locator("tr").filter({ hasText: target });
  await expect(ours).toContainText("At the border");
  await expect(ours).toContainText(`joining ${squire.kingdomName}'s strike`);
  await goTo(emperorPage, "Overview");
  await expect(emperorPage.locator("main")).toContainText(`Army at the border of ${target}, waiting for ${squire.kingdomName}'s army`);

  const squireAgain = await browser.newPage();
  await signIn(squireAgain, squire.email, password);
  // the leader's steward says who waits on its army, and the leader hears how the ally fared
  await expect(squireAgain.locator(".counsel")).toContainText(`${emperor.kingdomName} waits at the border of ${target}`);
  await goTo(squireAgain, "Army");
  await expect(squireAgain.locator("tr").filter({ hasText: target })).toContainText(`joined by ${emperor.kingdomName}`);
  await passTurns(squireAgain, 4);
  await expect(squireAgain.locator(".battle").first()).toContainText(target);
  await goTo(squireAgain, "Chronicle");
  await expect(squireAgain.locator("main")).toContainText(`${emperor.kingdomName} struck ${target} right after our army`);
  await squireAgain.close();

  await goTo(emperorPage, "Army");
  await expect(emperorPage.getByRole("heading", { name: "Armies on the march" })).toHaveCount(0);
  await expect(emperorPage.locator(".battle").first()).toContainText(target);
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
