import assert from "node:assert/strict";
import { gone, goTo, pageCheck, pagesOn, shows, toast } from "../src/pages.ts";
import { noPeace } from "../src/server.ts";

// What waits on the ruler each turn, on a monument cheap enough for a new kingdom to raise.
pagesOn({
  ...noPeace,
  "monument.stages": 4,
  "monument.stageCost.gold": 1000,
  "monument.stageCost.iron": 10,
  "monument.stageCost.tools": 10,
  "monument.upkeepPerStage.gold": 0,
  "monument.upkeepPerStage.food": 0,
  // no monument wins the era here, so that the world stays as the checks left it
  "monument.holdTicks": 1_000_000,
});

const line = (page: Parameters<typeof toast>[0], text: string | RegExp) => page.locator(".counsel li").filter({ hasText: text });

pageCheck("one click spends what would be lost and staffs the free places", async ({ game, open }) => {
  const ruler = await game.foundsAKingdom();
  const page = await open(ruler);

  // a new kingdom has free land, and points that are gone when the turn passes
  const empty = line(page, /acres stand empty/);
  await shows(empty, /building points are lost if the turn passes first\. The Economy plan raises \d+/);
  await empty.getByRole("button", { name: /^Raise \d+ buildings?$/ }).click();
  await shows(toast(page, /Raised \d+ buildings?/), "Raised");

  const idle = line(page, /people stand idle/);
  await shows(idle, /There is work and gold for \d+ farmers/);
  // each line also leads to the page, for a ruler who wants it otherwise
  assert.equal(await idle.getByRole("link", { name: "Choose" }).getAttribute("href"), "/kingdom/train");
  await idle.getByRole("button", { name: /^Train \d+ workers?$/ }).click();
  await shows(toast(page, /Trained \d+ recruits?/), "Trained");
  await gone(idle);
});

pageCheck("the plan last chosen on the build page is the one a click raises", async ({ game, open }) => {
  const ruler = await game.foundsAKingdom();
  const page = await open(ruler);
  await goTo(page, "Build");
  await page.getByRole("group", { name: "Plan" }).getByRole("button", { name: "Fortress" }).click();
  await goTo(page, "Your turn");
  await shows(line(page, /acres stand empty/), "The Fortress plan raises");
});

pageCheck("points with no land to stand on are named, and lead to the build page", async ({ game, open }) => {
  const ruler = await game.foundsAKingdom();
  await ruler.raises({ house: 41 });
  const page = await open(ruler);
  const stranded = line(page, /building points have no land to stand on/);
  await shows(stranded, "They are lost when the turn passes");
  await stranded.getByRole("link", { name: "Spend them" }).click();
  await page.waitForURL(/\/kingdom\/build$/);
});

pageCheck("an unguarded monument waits on its members until soldiers stand there", async ({ game, open }) => {
  const emperor = await game.foundsAKingdom("Emperor");
  await emperor.foundsAlliance(`Wardens ${Math.random().toString(36).slice(2, 7)}`);
  await emperor.foundsAMonumentAt(emperor);

  // with nothing built there is nothing to knock down
  const page = await open(emperor);
  await shows(page.locator(".counsel"), "stand empty");
  assert.equal(await line(page, "Nobody guards our monument").count(), 0);

  await emperor.paysIntoTheMonument({ gold: 1000, iron: 10, tools: 10 });
  await page.reload();
  const unguarded = line(page, "Nobody guards our monument");
  await shows(unguarded, "Stage 1 of 4. Any army that marches there wins and knocks down up to 25% of it");
  await unguarded.getByRole("link", { name: "Send soldiers" }).click();
  await page.waitForURL(/\/kingdom\/alliance$/);

  await emperor.passesTurns(5);
  await emperor.trains({ infantry: 5 });
  await emperor.sendsToTheMonument({ infantry: 5 });
  await page.goto(page.url().replace("alliance", "overview"));
  // still on the road they guard nothing
  await shows(line(page, "Nobody guards our monument"), "Stage 1 of 4");
  await emperor.passesTurns(2);
  await page.reload();
  await shows(page.locator(".counsel"), "stand empty");
  await gone(line(page, "Nobody guards our monument"));
});

pageCheck("an army marching on the monument warns every member", async ({ game, open }) => {
  const squire = await game.foundsAKingdom("Squire");
  const emperor = await game.foundsAKingdom("Emperor");
  const alliance = `Besieged ${Math.random().toString(36).slice(2, 7)}`;
  await emperor.foundsAlliance(alliance);
  await emperor.invites(squire);
  await squire.joins(alliance);
  await emperor.foundsAMonumentAt(emperor);
  const raider = await game.foundsAKingdom("Raider");
  await raider.passesTurns(5);
  await raider.trains({ infantry: 10 });
  await raider.marchesOnTheMonumentAt(emperor, { infantry: 10 });

  const seat = await open(emperor);
  await shows(seat.locator(".counsel li.urgent").filter({ hasText: "marches on our monument" }), `${raider} marches on our monument`);
  await squire.passesTurns(5);
  await squire.trains({ infantry: 1 });
  const member = await open(squire);
  await shows(member.locator(".counsel"), `${raider} marches on our monument at ${emperor}`);
});

// last: a complete monument stands in this world from here on, and every kingdom founded after it hears of it
pageCheck("a complete monument tells its members to hold it, and every rival to march on it", async ({ game, open }) => {
  const emperor = await game.foundsAKingdom("Emperor");
  const name = `Finishers ${Math.random().toString(36).slice(2, 7)}`;
  await emperor.foundsAlliance(name);
  await emperor.foundsAMonumentAt(emperor);
  await emperor.paysIntoTheMonument({ gold: 4000, iron: 40, tools: 40 });
  const rival = await game.foundsAKingdom("Rival");

  const theirs = await open(rival);
  const warning = theirs.locator(".counsel li.urgent").filter({ hasText: `The monument of ${name} wins the era in` });
  await shows(warning, "Only a won assault knocks it down");
  assert.equal(await warning.getByRole("link", { name: "March on it" }).getAttribute("href"), `/kingdom/war?assault=${encodeURIComponent(emperor.name)}`);

  const ours = await open(emperor);
  await shows(line(ours, "Our monument wins the era in"), "If its store pays the upkeep every turn");
  await shows(line(ours, "Nobody guards our monument"), "It stands complete. Any army that marches there wins, knocks down up to 25% of it, and makes it stand the whole hold again");
  assert.equal(await ours.locator(".counsel li").filter({ hasText: "The monument of" }).count(), 0);

  // the rankings say what will end the era first
  await goTo(theirs, "Rankings");
  await shows(theirs.locator("main"), new RegExp(`The monument of ${name} wins it in \\d+ turns unless an army knocks it down; otherwise the clock ends it in about`));
});
