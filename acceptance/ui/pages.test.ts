import assert from "node:assert/strict";
import { goTo, gone, holds, pageCheck, shows, toast } from "../src/pages.ts";

pageCheck("the landing page shows the era", async ({ open, ui }) => {
  const page = await open();
  await page.goto(`${ui}/`);
  await shows(page.getByRole("heading", { name: "Knights of Darkness" }), "Knights of Darkness");
  await shows(page.getByRole("heading", { name: /Era \d+/ }), /Era \d+/);
});

pageCheck("the sign-up page says when the two passwords differ", async ({ open, ui }) => {
  const page = await open();
  await page.goto(`${ui}/register`);
  await page.getByLabel("Kingdom name").fill("Twice Typed");
  await page.getByLabel("Email").fill("twice@acceptance.test");
  await page.getByLabel("Password, at least 8 characters").fill("first-password");
  await page.getByLabel("Password again").fill("other-password");
  await page.getByRole("button", { name: "Found the kingdom" }).click();
  await shows(page.locator("p.warning"), "The two passwords are different");
});

pageCheck("a passed turn is summed up in one message, however many pass", async ({ game, open }) => {
  const ruler = await game.foundsAKingdom();
  const page = await open(ruler);
  await page.getByRole("button", { name: /^Pass turn/ }).click();
  await shows(toast(page, /A turn has passed: \+[\d,]+ gold/), "A turn has passed");
  for (let turn = 0; turn < 4; turn++) {
    const passed = page.waitForResponse(response => response.url().endsWith("/kingdom/pass-turn"));
    await page.getByRole("button", { name: /^Pass turn/ }).click();
    await passed;
  }
  await page.waitForLoadState("networkidle");
  assert.equal(await toast(page, /A turn has passed/).count(), 1);
});

pageCheck("the rules wait behind a mark until hovered or tapped", async ({ game, open }) => {
  const ruler = await game.foundsAKingdom();
  const page = await open(ruler);
  await goTo(page, "Build");
  const rule = page.getByRole("tooltip");
  assert.ok(!(await page.locator("main").innerText()).includes("Every building stands on an acre"));
  const mark = page.getByRole("button", { name: "About Land" });
  await mark.hover();
  await shows(rule, "Every building stands on an acre");
  await page.mouse.move(0, 0);
  await gone(rule);
  // a tap keeps it open until the next tap
  await mark.click();
  await page.mouse.move(0, 0);
  await shows(rule, "Every building stands on an acre");
  await mark.click();
  await gone(rule);

  // words and the figures in the top bar explain themselves the same way
  await goTo(page, "Overview");
  await shows(page.locator(".realm-meters"), /Idle people[\s\S]*at work/);
  await page.locator(".term", { hasText: "counter intelligence" }).hover();
  await shows(rule, "for each spy");
  await page.getByRole("button", { name: /^Gold [\d,]+$/ }).hover();
  await shows(rule, "from the mines next turn");
  await page.getByRole("button", { name: /^Idle people [\d,]+$/ }).hover();
  await shows(rule, /Ready to train; [\d,]+ others at work/);
  await page.locator(".term", { hasText: "turns stored" }).hover();
  await shows(rule, /A new turn every [\w ]+, the next at/);
});

pageCheck("a plan fills the build order and the free places fill the training order", async ({ game, open }) => {
  const ruler = await game.foundsAKingdom();
  const page = await open(ruler);
  await goTo(page, "Build");
  await page.getByRole("group", { name: "Plan" }).getByRole("button", { name: "War" }).click();
  await page.getByRole("button", { name: "Fill in the plan" }).click();
  await holds(page.getByLabel("Barracks to raise", { exact: true }), /^[1-9]/);
  await holds(page.getByLabel("Farms to raise", { exact: true }), /^[1-9]/);
  await shows(page.locator(".order-bar"), /Raise \d+ buildings/);
  await page.getByRole("button", { name: "Raise all" }).click();
  await shows(toast(page, /Raised \d+ buildings/), "Raised");

  await goTo(page, "Train");
  // a new kingdom has fewer idle people than free farm places, so every one of them goes to the farms
  await page.getByRole("button", { name: "Staff the free places" }).click();
  assert.equal(await page.locator(".plate.chosen").count(), 1);
  await holds(page.getByLabel("Farmers to train", { exact: true }), "10");
});

pageCheck("a trial kingdom lives in the browser until it is kept", async ({ open, ui }) => {
  const page = await open();
  const kingdomName = `Trial ${Date.now() % 1000000}`;
  await page.goto(`${ui}/`);
  await page.getByLabel("Play now with only a kingdom name").fill(kingdomName);
  await page.getByRole("button", { name: "Play now" }).click();
  await page.waitForURL(/\/kingdom\/overview$/);
  await shows(page.locator(".trial-notice"), "This kingdom lives only in this browser");

  // the session runs out, and the browser signs back in with the account it keeps
  await page.evaluate(() => localStorage.setItem("authToken", "expired"));
  await page.reload();
  await shows(page.locator("main h1"), kingdomName);

  // signing out of a kingdom not yet kept asks first
  page.once("dialog", dialog => void dialog.dismiss());
  await page.getByRole("button", { name: "Sign out" }).click();
  await shows(page.locator("main h1"), kingdomName);
});

pageCheck("a reply is addressed to the sender", async ({ game, open }) => {
  const writer = await game.foundsAKingdom("Writer");
  const reader = await game.foundsAKingdom("Reader");
  await writer.writesTo(reader, "Will you answer?");
  const page = await open(reader);
  await shows(page.getByRole("link", { name: /Letters/ }).locator(".badge"), /\d/);
  await goTo(page, "Letters");
  await page.getByRole("button", { name: new RegExp(writer.name) }).first().click();
  await shows(page.locator(".letter-body"), "Will you answer?");
  await page.getByRole("button", { name: `Reply to ${writer.name}` }).click();
  await holds(page.getByRole("combobox", { name: /^To/ }), writer.name);
});

pageCheck("the steward warns of an army on the way and points to the army", async ({ game, open }) => {
  const target = await game.foundsAKingdom("Target");
  const attacker = await game.foundsAKingdom("Attacker");
  await attacker.passesTurns(5);
  await attacker.trains({ infantry: 10 });

  const attackerPage = await open(attacker);
  await goTo(attackerPage, "Army");
  await attackerPage.getByRole("combobox", { name: /^Target/ }).selectOption(target.name);
  // both hold 100 acres, so the share a won strike takes is the whole of what is at stake
  const atStake = Math.floor(100 * (await game.rules()).attack.maxLandCaptureRatio);
  await shows(attackerPage.locator("main"), `At stake: up to ${atStake} acres`);

  await attacker.marchesOn(target, { infantry: 10 });
  const targetPage = await open(target);
  const warning = targetPage.locator(".counsel li.urgent").filter({ hasText: `${attacker} marches on us` });
  await warning.getByRole("link", { name: "Prepare" }).click();
  await targetPage.waitForURL(/\/kingdom\/war$/);
  await shows(targetPage.locator("main"), `${attacker} with about 10 soldiers`);
});

pageCheck("the steward sets up a strike to join, and the army waits at the border", async ({ game, open }) => {
  const target = await game.foundsAKingdom("Target");
  const squire = await game.foundsAKingdom("Squire");
  const emperor = await game.foundsAKingdom("Emperor");
  const alliance = `Pact ${Math.random().toString(36).slice(2, 7)}`;
  await emperor.foundsAlliance(alliance);
  await emperor.invites(squire);
  await squire.joins(alliance);
  await squire.passesTurns(5);
  await squire.trains({ infantry: 10 });
  await squire.marchesOn(target, { infantry: 10 });
  await emperor.passesTurns(5);
  await emperor.trains({ infantry: 10 });

  const page = await open(emperor);
  await shows(page.locator(".counsel"), `${squire} marches on ${target}`);
  await page.locator(".counsel").getByRole("link", { name: "Join the strike" }).click();
  await page.waitForURL(/\/kingdom\/war$/);
  await holds(page.getByRole("combobox", { name: /^Target/ }), target.name);
  assert.equal(await page.getByRole("combobox", { name: /^Target/ }).locator("option", { hasText: squire.name }).count(), 0);
  const sendArmy = page.locator(".panel").filter({ has: page.getByRole("heading", { name: "Send an army" }) });
  await shows(sendArmy, `Joining ${squire}'s strike on ${target}`);
  await page.getByLabel("Infantry to send", { exact: true }).fill("10");
  await sendArmy.getByRole("button", { name: "Join the strike" }).click();
  await shows(toast(page, `The army marches to join ${squire}'s strike on ${target}`), "marches to join");

  await emperor.passesTurns(5);
  await page.reload();
  const ours = page.locator("tr").filter({ hasText: target.name });
  await shows(ours, "At the border");
  await shows(ours, "or alone in 3 turns");
  await goTo(page, "Overview");
  await shows(page.locator("main"), `Army at the border of ${target}, waiting for ${squire}'s army`);

  const squirePage = await open(squire);
  await shows(squirePage.locator(".counsel"), `${emperor} waits at the border of ${target}`);
  await goTo(squirePage, "Army");
  await shows(squirePage.locator("tr").filter({ hasText: target.name }), `joined by ${emperor}`);
});

pageCheck("an ally's ask for gold is shown in the counsel and on the market", async ({ game, open }) => {
  const squire = await game.foundsAKingdom("Squire");
  const emperor = await game.foundsAKingdom("Emperor");
  const alliance = `Purse ${Math.random().toString(36).slice(2, 7)}`;
  await emperor.foundsAlliance(alliance);
  await emperor.invites(squire);
  await squire.joins(alliance);
  await squire.asksAlliesFor(50000);

  const page = await open(emperor);
  await shows(page.locator(".counsel"), `${squire} asks the alliance for 50,000 gold`);
  await goTo(page, "Market");
  await shows(page.locator("main"), `${squire} (ally)`);
});

pageCheck("each good on the market lists only its own offers", async ({ game, open }) => {
  const buyer = await game.foundsAKingdom("Buyer");
  await game.timePasses(1);
  const page = await open(buyer);
  await goTo(page, "Market");
  const foodField = page.getByRole("spinbutton", { name: /^Amount of food to buy/ });
  await foodField.first().waitFor();
  await page.getByRole("tab", { name: /Iron/ }).click();
  await gone(foodField);
  await page.getByRole("tab", { name: /Food/ }).click();
  await foodField.first().waitFor();
});

pageCheck("on a phone the market's buy field stays on screen", async ({ game, open, ui }) => {
  const buyer = await game.foundsAKingdom("Buyer");
  await game.timePasses(1);
  const page = await open(buyer, { width: 390, height: 844 });
  await page.goto(`${ui}/kingdom/market`);
  const buy = page.getByRole("button", { name: /^Buy/ }).first();
  await buy.waitFor();
  const box = await buy.boundingBox();
  assert.ok((box?.x ?? Infinity) + (box?.width ?? 0) <= 390, `the buy button ends at ${(box?.x ?? 0) + (box?.width ?? 0)}`);
  await page.getByRole("spinbutton", { name: /^Amount of food to buy/ }).first().fill("10");
  await buy.click();
  await shows(toast(page, /Bought 10 food for [\d,]+ gold/), "Bought 10 food");
});

pageCheck("looks at one kingdom make one row that says what changed", async ({ game, open }) => {
  const spy = await game.foundsAKingdom("Spy");
  const target = await game.foundsAKingdom("Target");
  const other = await game.foundsAKingdom("Other");
  await spy.raises({ spyGuild: 1 });
  // a new kingdom has weapons for two spies, more than the one a target's five guards can catch
  await spy.trains({ spy: 2 });
  await spy.sendsSpies(target, 2);
  await target.passesTurns(2);
  await spy.sendsSpies(target, 2);
  await spy.sendsSpies(other, 2);

  const page = await open(spy);
  await goTo(page, "Spies");
  const rows = page.locator("tr.dossier");
  await shows(rows.first(), other.name);
  assert.equal(await rows.count(), 2);

  // the kingdom looked at last stands open; the other row counts its looks and shows how its gold moved
  await shows(page.locator(".dossier-detail"), "People at home");
  const looked = rows.filter({ hasText: target.name });
  await shows(looked.locator('td[data-label="Looks"]'), "2");
  await shows(looked.locator('td[data-label="Gold"] .dossier-change'), /^\+[\d,]+$/);
  await looked.getByRole("button", { name: "More" }).click();
  assert.equal(await page.locator(".dossier-detail").count(), 1);
  await looked.getByRole("button", { name: "Less" }).click();
  await gone(page.locator(".dossier-detail"));

  await looked.getByRole("link", { name: "Target" }).click();
  await page.waitForURL(/\/kingdom\/war$/);
  await holds(page.getByRole("combobox", { name: /^Target/ }), target.name);
});

pageCheck("spies and the army both warn of allies standing guard unseen", async ({ game, open }) => {
  const raider = await game.foundsAKingdom("Raider");
  const target = await game.foundsAKingdom("Target");
  const guardian = await game.foundsAKingdom("Guardian");
  const pact = `Pact ${Math.random().toString(36).slice(2, 7)}`;
  await target.foundsAlliance(pact);
  await target.invites(guardian);
  await guardian.joins(pact);
  await guardian.passesTurns(5);
  await guardian.trains({ infantry: 20 });
  await guardian.sendsToStandGuard(target, { infantry: 20 });
  await guardian.passesTurns(2);
  await raider.raises({ spyGuild: 1 });
  await raider.trains({ spy: 2 });
  await raider.passesTurns(5);
  await raider.trains({ infantry: 10 });
  await raider.marchesOn(target, { infantry: 10 });
  await raider.passesTurns(4);
  await raider.hasFoughtAt(target);
  await raider.sendsSpies(target, 2);

  const page = await open(raider);
  await goTo(page, "Spies");
  await shows(page.locator(".dossier-note"), /An army here last met [\d,]+ defense: allies stand guard/);
  await goTo(page, "Army");
  await page.getByRole("combobox", { name: /^Target/ }).selectOption(target.name);
  await shows(page.locator(".panel").filter({ has: page.getByRole("heading", { name: "Send an army" }) }), "allies stand guard");
});

pageCheck("battles in a row against one enemy add up to one line that opens", async ({ game, open }) => {
  const raider = await game.foundsAKingdom("Raider");
  const target = await game.foundsAKingdom("Target");
  await target.passesTurns(5);
  await target.trains({ infantry: 30 });
  await raider.passesTurns(5);
  await raider.trains({ infantry: 4 });
  for (let strike = 0; strike < 2; strike++) {
    await raider.marchesOn(target, { infantry: 2 });
    await raider.passesTurns(4);
  }

  const page = await open(raider);
  await goTo(page, "Army");
  const run = page.locator("details.battle-run");
  await shows(run.first().locator("summary"), `2 defeats at ${target}`);
  assert.equal(await run.count(), 1);
  await shows(run.first().locator("summary"), /4 sent, [\d,]+ of ours fell/);
  assert.equal(await page.locator(".battle").first().isVisible(), false);
  await run.first().locator("summary").click();
  assert.equal(await page.locator(".battle").count(), 2);
  assert.ok(await page.locator(".battle").first().isVisible());
});

pageCheck("every store in the top bar stands under its name", async ({ game, open }) => {
  const ruler = await game.foundsAKingdom();
  const page = await open(ruler);
  await shows(page.locator(".ledger-bar"), "Weapons");
  const names = await page.locator(".ledger-bar .ledger-label").allInnerTexts();
  assert.deepEqual(names, ["Gold", "Food", "Land", "Points", "Idle", "Iron", "Tools", "Weapons"]);
  // and on a phone too, with the kingdom's name above them
  const phone = await open(ruler, { width: 390, height: 844 });
  await shows(phone.locator(".ledger-bar .ledger-who"), ruler.name);
  assert.deepEqual(await phone.locator(".ledger-bar .ledger-label").allInnerTexts(), names);
});

pageCheck("on a phone the thumb bar leads around the kingdom and passes the turn", async ({ game, open, ui }) => {
  const ruler = await game.foundsAKingdom();
  const page = await open(ruler, { width: 390, height: 844 });
  await page.goto(`${ui}/kingdom/overview`);
  const bar = page.getByRole("navigation", { name: "Parts of the kingdom" });

  await bar.getByRole("button", { name: "War" }).click();
  await page.locator(".thumb-sheet").getByRole("link", { name: "Spies" }).click();
  await page.waitForURL(/\/kingdom\/spies$/);
  await gone(page.locator(".thumb-sheet"));
  await shows(page.locator("main h1"), "Spies");

  await bar.getByRole("button", { name: "Council" }).click();
  await shows(page.locator(".thumb-sheet"), "Sign out");
  await page.getByRole("button", { name: "Close the menu" }).click({ position: { x: 10, y: 10 } });
  await gone(page.locator(".thumb-sheet"));

  await bar.getByRole("button", { name: /^Pass turn/ }).click();
  await shows(toast(page, /A turn has passed/), "A turn has passed");

  // the bar sits on the screen, and nothing under it is out of reach
  const box = await bar.boundingBox();
  assert.ok(box && box.y + box.height <= 844 && box.height >= 48, `the bar stands at ${JSON.stringify(box)}`);
  const padding = await page.evaluate(() => Number.parseFloat(getComputedStyle(document.querySelector("main")!).paddingBottom));
  assert.ok(padding >= (box?.height ?? 0), `the page leaves ${padding} px under its content for a bar of ${box?.height}`);
});

pageCheck("every page fits a phone screen", async ({ game, open, ui }) => {
  const ruler = await game.foundsAKingdom();
  const page = await open(ruler, { width: 390, height: 844 });
  for (const path of ["overview", "build", "train", "war", "spies", "market", "carriers", "alliance", "letters", "chronicle", "rankings"]) {
    await page.goto(`${ui}/kingdom/${path}`);
    await page.locator("main h1").waitFor();
    // nothing runs past the right edge, and no column is squeezed into a sliver beside another
    const overflow = await page.evaluate(() => {
      const width = document.documentElement.clientWidth;
      const columns = [...document.querySelectorAll("main .two-columns > *")].map(element => element.getBoundingClientRect().width);
      return { scroll: document.documentElement.scrollWidth - width, squeezed: columns.filter(column => column < 240).length };
    });
    assert.deepEqual(overflow, { scroll: 0, squeezed: 0 }, path);
  }
});
