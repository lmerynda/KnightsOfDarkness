import assert from "node:assert/strict";
import { test } from "node:test";
import { World } from "../src/api/client.ts";
import { noPeace, startServer } from "../src/server.ts";
import { foundCoalition } from "../src/simulation/coalition.ts";
import { coalitionOf, selfSufficient, tradeShares } from "../src/simulation/trade.ts";
import { runWorld } from "../src/simulation/world.ts";

test("a coalition of specialists buys most of what it needs from its own members", async () => {
  const server = await startServer();
  try {
    const world = new World(server.url);
    const guild = await foundCoalition(world, "Guild", () => coalitionOf(selfSufficient));
    const run = await runWorld(world, guild.rulers, 30);
    assert.deepEqual(run.violations, []);

    const bots = (await world.rankings()).kingdoms.filter(rank => rank.bot).map(rank => rank.kingdomName);
    const shares = tradeShares(await world.trades(), guild.rulers.map(ruler => ruler.player.name), bots);
    assert.ok(shares.bought.food.allies > 0, "no member bought food from the guild's farmer");
    assert.ok(shares.bought.iron.allies > 0, "no smith bought iron from the guild's miner");
    assert.ok(shares.bought.weapons.allies > 0, "the warrior bought no weapons from the guild's smiths");
    assert.ok(shares.fromAllies > 0.5, `only ${Math.round(shares.fromAllies * 100)}% of the pieces bought came from members: ${JSON.stringify(shares.bought)}`);
    const warrior = guild.rulers.find(ruler => ruler.strategy.name === "warrior");
    assert.ok(warrior, "the guild has no warrior");
    const last = run.timeline.filter(entry => entry.kingdom === warrior.player.name).at(-1);
    assert.ok((last?.army ?? 0) > 0, "the warrior raised no army from bought weapons");
    // a warrior that bought weapons for every free barracks place, more than its pay could ever arm, ended a run holding
    // 150,000 of them
    const config = await world.config();
    const mostWeaponsASoldierTakes = Math.max(...(["infantry", "cavalry"] as const).map(unit => config.trainingCost[unit].weapons));
    const hoard = (await warrior.player.kingdom()).resources.weapons;
    assert.ok(hoard <= (last?.army ?? 0) * mostWeaponsASoldierTakes, `the warrior holds ${hoard} weapons for an army of ${last?.army}`);
    // craftsmen without a garrison held 2 to 6 defense, so every strike on them won and warriors took everyone's land
    const undefended = guild.rulers
      .filter(ruler => ruler !== warrior)
      .map(ruler => run.timeline.filter(entry => entry.kingdom === ruler.player.name).at(-1))
      .filter(entry => (entry?.defense ?? 0) < 100);
    assert.deepEqual(undefended.map(entry => `${entry?.kingdom}: ${entry?.defense}`), [], "craftsmen kept no garrison");
    // a ruler may be caught short for a turn when the market runs dry, but grows its own food before it starves again;
    // rulers who judged hunger by what the stores could cover starved turn after turn until their production collapsed
    const starving = new Set(run.timeline.filter(entry => entry.starved > 0).map(entry => `${entry.kingdom}@${entry.turn}`));
    assert.deepEqual(
      run.timeline.filter(entry => entry.starved > 0 && starving.has(`${entry.kingdom}@${entry.turn - 1}`)),
      [],
      "a kingdom starved two turns in a row",
    );
  } finally {
    await server.stop();
  }
});

test("a coalition without an iron miner buys its iron outside, and a rival that corners iron sells it only dear and carries its own", async () => {
  const server = await startServer();
  try {
    const world = new World(server.url);
    const guild = await foundCoalition(world, "Guild", () => coalitionOf(["gold", "gold", "corner:iron", "food", "smith", "iron", "warrior"]));
    const hanse = await foundCoalition(world, "Hanse", () => coalitionOf(["gold", "gold", "gold", "food", "smith", "warrior"]));
    const smith = guild.rulers.find(ruler => ruler.strategy.name === "smith")?.player.name;
    const carriedToSmith: number[] = [];
    const run = await runWorld(world, [...guild.rulers, ...hanse.rulers], 30, {
      afterTurn: async () => {
        for (const ruler of guild.rulers) {
          const loads = (await ruler.player.kingdom()).carriersOnTheMove.filter(load => load.to === smith && load.resource === "iron" && load.from === ruler.player.name);
          carriedToSmith.push(...loads.map(load => load.count));
        }
      },
    });
    assert.deepEqual(run.violations, []);
    assert.ok(carriedToSmith.length > 0, "nobody in Guild carried iron to its smith");
    // carrying the coalition's iron must not take the miner's people: shippers who hired carriers for the allies' whole
    // need, ten iron to a carrier, ended with a hundred miners and fifteen hundred carriers
    const miner = (await guild.rulers.find(ruler => ruler.strategy.name === "iron")?.player.kingdom())?.units;
    assert.ok(miner, "Guild has no iron miner");
    const carriers = miner.availableUnits.carrier + miner.mobileUnits.carrier;
    assert.ok(miner.availableUnits.ironMiner > 2 * carriers, `Guild's iron miner keeps ${miner.availableUnits.ironMiner} miners and ${carriers} carriers`);

    const config = await world.config();
    const trades = await world.trades();
    const guildNames = new Set(guild.rulers.map(ruler => ruler.player.name));
    const hanseNames = new Set(hanse.rulers.map(ruler => ruler.player.name));
    const ironToHanse = trades.filter(trade => trade.resource === "iron" && hanseNames.has(trade.buyer));
    assert.ok(ironToHanse.length > 0, "Hanse's blacksmith bought no iron at all");
    const fromGuild = ironToHanse.filter(trade => guildNames.has(trade.seller));
    assert.ok(fromGuild.length > 0, "Guild sold Hanse no iron");
    assert.deepEqual([...new Set(fromGuild.map(trade => trade.price))], [2 * (config.bots.prices.iron ?? 0)]);
    const cornered = trades.filter(trade => trade.resource === "iron" && trade.buyer === guild.rulers[2]?.player.name && !guildNames.has(trade.seller));
    assert.ok(cornered.length > 0, "the cornerer bought no iron outside its coalition");
    // inside Guild iron changes hands only as one-piece bills, through which the smith pays for what it was carried
    const ironWithinGuild = trades.filter(trade => trade.resource === "iron" && guildNames.has(trade.buyer) && guildNames.has(trade.seller));
    assert.deepEqual(ironWithinGuild.filter(trade => trade.count !== 1), [], "Guild's members bought cornered iron from each other at the dear price");
    assert.ok(ironWithinGuild.some(trade => trade.buyer === smith && trade.price > 2 * (config.bots.prices.iron ?? 0)), "Guild's smith never paid for the iron it was carried");
    // with iron short everything the smiths make sells; a smith that kept its last split of tools and weapons once both
    // sold out made tools alone from then on, and the warriors it armed stopped growing for the rest of the run
    for (const coalition of [guild, hanse]) {
      const warrior = coalition.rulers.find(ruler => ruler.strategy.name === "warrior")?.player.name;
      const army = (turn: number): number => run.timeline.find(entry => entry.kingdom === warrior && entry.turn === turn)?.army ?? 0;
      assert.ok(army(30) > army(20), `${coalition.name}'s warrior raised no soldiers after turn 20: ${army(20)} then ${army(30)}`);
    }
  } finally {
    await server.stop();
  }
});

// A smith whose income collapsed when its iron was cornered kept paying its garrison, could buy neither food nor farmers,
// and starved for forty turns; soldiers' pay here is 60% of their training cost, so ten infantry outgrow a gold producer's
// income
test("a craftsman whose soldiers' pay outgrows its income dismisses soldiers", async () => {
  const server = await startServer({ "common.soldierUpkeepRatio": 0.6, "kingdomStartConfiguration.resources.weapons": 1_000 });
  try {
    const world = new World(server.url);
    const solo = await foundCoalition(world, "Solo", () => coalitionOf(["gold"]));
    const ruler = solo.rulers[0];
    assert.ok(ruler, "the coalition has no ruler");
    assert.ok((await ruler.player.train({ infantry: 10 })).success, "the ruler could not train infantry");
    const run = await runWorld(world, solo.rulers, 1, { bots: false });
    assert.deepEqual(run.violations, []);
    assert.equal(run.timeline.at(-1)?.deserted, 0, "the soldiers deserted before the ruler could dismiss them");
    const infantry = (await ruler.player.kingdom()).units.availableUnits.infantry;
    assert.ok(infantry < 10, `the ruler kept all ${infantry} infantry it could not pay`);
  } finally {
    await server.stop();
  }
});

// Warriors who marched with everyone were broken at home by rival warriors, a player leaves a guard behind
test("a warrior leaves a guard of bowmen and infantry at home when it marches", async () => {
  const server = await startServer(noPeace);
  try {
    const world = new World(server.url);
    const guild = await foundCoalition(world, "Guild", () => coalitionOf(selfSufficient));
    const horde = await foundCoalition(world, "Horde", () => coalitionOf(selfSufficient));
    const run = await runWorld(world, [...guild.rulers, ...horde.rulers], 60, { shuffle: true });
    assert.deepEqual(run.violations, []);

    const warriors = [...guild.rulers, ...horde.rulers].filter(ruler => ruler.strategy.name === "warrior").map(ruler => ruler.player.name);
    const strikes = run.battles.filter(battle => warriors.includes(battle.attacker));
    assert.ok(strikes.length > 0, "the warriors never marched");
    for (const strike of strikes) assert.equal(strike.unitsSent.bowman ?? 0, 0, `${strike.attacker} marched with its bowmen`);

    const kingdoms = await world.kingdoms();
    for (const name of warriors) {
      const kingdom = kingdoms.find(candidate => candidate.name === name);
      assert.ok(kingdom, `${name} is gone`);
      const { bowman, infantry } = kingdom.units.availableUnits;
      assert.ok(bowman + infantry > 0, `${name} has nobody at home`);
      assert.ok(kingdom.buildings.tower > 4, `${name} raised no towers for its guard`);
    }
  } finally {
    await server.stop();
  }
});

// Three warriors took a third more land than the expected make-up while every garrison stayed at 1% of land; a player who
// loses land walls up, and so do these rulers
test("a craftsman struck by warriors keeps more barracks", async () => {
  // strong riders, so that warriors who keep half their soldiers at home still strike within this short run
  const server = await startServer({ ...noPeace, "attack.unitAttackPower.cavalry": 30 });
  try {
    const world = new World(server.url);
    const guild = await foundCoalition(world, "Guild", () => coalitionOf(selfSufficient));
    const horde = await foundCoalition(world, "Horde", () => coalitionOf(["gold", "gold", "food", "smith", "smith", "iron", "warrior", "warrior", "warrior"]));
    const turns = 60;
    const craftsmen = guild.rulers.filter(ruler => ruler.strategy.name !== "warrior");
    // the turn each won strike on a craftsman was reported by; a strike lands during the warrior's turn, and a craftsman
    // who plays after it in that round buys back more land than most strikes take, so its land alone hid them
    const struckOn = new Map<string, { defender: string; turn: number }>();
    /** each craftsman's barracks share of its land at the end of each turn */
    const shareOn = new Map<string, number>();
    const run = await runWorld(world, [...guild.rulers, ...horde.rulers], turns, {
      shuffle: true,
      afterTurn: async turn => {
        for (const ruler of craftsmen) {
          const kingdom = await ruler.player.kingdom();
          shareOn.set(`${kingdom.name}@${turn}`, kingdom.buildings.barracks / kingdom.resources.land);
          for (const report of await ruler.player.battleReports()) {
            if (report.defender === kingdom.name && report.attackerWon && !struckOn.has(report.id)) struckOn.set(report.id, { defender: kingdom.name, turn });
          }
        }
      },
    });
    assert.deepEqual(run.violations, []);

    // a struck craftsman is judged by the share it reached in the turns after its first strike, so a strike early in the
    // run counts as much as a late one; once every craftsman held its garrison, a run went twenty turns without a won strike
    const firstStrike = new Map<string, number>();
    for (const { defender, turn } of struckOn.values()) if (turn < turns) firstStrike.set(defender, Math.min(turn, firstStrike.get(defender) ?? turn));
    assert.ok(firstStrike.size > 0, "the warriors struck no craftsman");
    const highest = (name: string, after: number): number => Math.max(...Array.from({ length: turns - after }, (_, step) => shareOn.get(`${name}@${after + 1 + step}`) ?? 0));
    // a craftsman struck on fully built land buys the acres for its walls rather than tear down its workplaces; without
    // walling up no craftsman keeps barracks on more than 1% of its land
    const shares = [...firstStrike].map(([name, turn]) => `${name} struck on turn ${turn}: ${highest(name, turn).toFixed(3)}`);
    assert.ok([...firstStrike].some(([name, turn]) => highest(name, turn) > 0.015), `no struck craftsman walled up: ${shares.join(", ")}`);
  } finally {
    await server.stop();
  }
});
