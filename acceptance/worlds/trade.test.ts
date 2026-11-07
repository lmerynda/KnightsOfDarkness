import assert from "node:assert/strict";
import { test } from "node:test";
import { World } from "../src/api/client.ts";
import { startServer } from "../src/server.ts";
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
