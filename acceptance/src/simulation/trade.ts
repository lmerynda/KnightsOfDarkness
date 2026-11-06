import type { Player } from "../api/client.ts";
import type { BuildingName, MarketResource, MarketTrade, UnitName } from "../api/types.ts";
import { developEconomy, type Plan, type Strategy, type TurnContext } from "./strategies.ts";

/**
 * Rulers of a self-sufficient coalition: each lives by one trade and buys the rest, from its allies first and from anyone
 * else on the market for what they cannot supply. Players in a coalition do this on purpose, so most of the coalition's
 * market should stay inside it; the merchant bots are a backstop.
 */
export type Craft = "gold" | "food" | "tools" | "iron";

type Calling = { plan: Plan; workers: UnitName[]; sells?: MarketResource };

/** every calling keeps houses for its workers and a few gold mines, since land is bought with gold */
const callings: Record<Craft, Calling> = {
  gold: { plan: { house: 0.4, goldMine: 0.5 }, workers: ["goldMiner"] },
  food: { plan: { house: 0.4, farm: 0.4, goldMine: 0.1 }, workers: ["farmer", "goldMiner"], sells: "food" },
  tools: { plan: { house: 0.4, workshop: 0.4, goldMine: 0.1 }, workers: ["blacksmith", "goldMiner"], sells: "tools" },
  iron: { plan: { house: 0.4, ironMine: 0.4, goldMine: 0.1 }, workers: ["ironMiner", "goldMiner"], sells: "iron" },
};

const workplace: Partial<Record<UnitName, BuildingName>> = { goldMiner: "goldMine", farmer: "farm", blacksmith: "workshop", ironMiner: "ironMine" };

/** turns of food a ruler keeps in store */
const foodTurns = 3;
/** a share of the treasury a ruler spends on one good in one turn, so land still gets bought */
const spendShare = 0.5;

/** how much of each good the ruler wants in store before it develops: a few turns of food, tools for the free workplaces, iron for the forges */
const wants = (context: TurnContext, calling: Calling): Partial<Record<MarketResource, number>> => {
  const { details, config, kingdom } = context;
  const hungry = Math.max(0, details.forecast.foodConsumed - (details.forecast.resourcesProduced.food ?? 0));
  const freePlaces = (unit: UnitName): number => {
    const home = workplace[unit];
    return home ? Math.max(0, (details.buildingCapacity[home] ?? 0) - (details.buildingOccupancy[home] ?? 0)) : 0;
  };
  const builders = Math.max(0, Math.floor(kingdom.resources.land / 10) - kingdom.units.availableUnits.builder);
  const tools = calling.workers.reduce((sum, unit) => sum + freePlaces(unit) * config.trainingCost[unit].tools, 0) + builders * config.trainingCost.builder.tools;
  const iron = kingdom.units.availableUnits.blacksmith * config.production.blacksmith.rate * config.common.ironConsumptionPerProductionUnit;
  return { food: foodTurns * hungry, tools, iron: 2 * iron };
};

const held = (context: TurnContext, resource: MarketResource): number => context.kingdom.resources[resource];

/** buys up to the count, cheapest allies' offers first, then anyone's; spends at most the budget */
const buy = async (player: Player, resource: MarketResource, count: number, budget: number): Promise<void> => {
  const allies = (await player.alliesOffers()).filter(offer => offer.resource === resource).sort((one, other) => one.price - other.price);
  const allyIds = new Set(allies.map(offer => offer.id));
  const others = (await player.offers(resource)).filter(offer => !allyIds.has(offer.id) && offer.sellerName !== player.name).sort((one, other) => one.price - other.price);
  for (const offer of [...allies, ...others]) {
    const pieces = Math.min(count, offer.count, Math.floor(budget / offer.price));
    if (pieces <= 0) return;
    const bought = await player.buyOffer(offer.id, pieces);
    if (!bought.success || !bought.data) continue;
    count -= bought.data.count;
    budget -= bought.data.totalCost;
  }
};

/** the price outsiders ask, or the merchants' own when nobody outside the alliance sells the good */
const marketPrice = async ({ player, config }: TurnContext, resource: MarketResource): Promise<number> => {
  const allies = new Set((await player.alliesOffers()).map(offer => offer.id));
  const outside = (await player.offers(resource)).filter(offer => !allies.has(offer.id) && offer.sellerName !== player.name);
  if (outside.length) return Math.min(...outside.map(offer => offer.price));
  const merchants = config.bots.prices[resource];
  if (merchants === undefined) throw new Error(`The game config sets no merchants' price for ${resource}`);
  return merchants;
};

/** takes its old offer back and puts up everything beyond its own wants at the market's price */
const sell = async (context: TurnContext, resource: MarketResource, keep: number): Promise<void> => {
  const { player } = context;
  for (const offer of (await player.kingdom()).marketOffers.filter(offer => offer.resource === resource)) {
    await player.withdrawOffer(offer.id);
  }
  const surplus = (await player.kingdom()).resources[resource] - keep;
  if (surplus <= 0) return;
  await player.postOffer(resource, surplus, await marketPrice(context, resource));
};

export const specialist = (craft: Craft): Strategy => {
  const calling = callings[craft];
  return {
    name: `${craft} specialist`,
    weaponsPercentage: 0,
    play: async context => {
      const want = wants(context, calling);
      for (const resource of ["food", "tools", "iron"] as const) {
        if (resource === calling.sells) continue;
        const missing = (want[resource] ?? 0) - held(context, resource);
        if (missing > 0) await buy(context.player, resource, missing, (await context.player.kingdom()).resources.gold * spendShare);
      }
      await developEconomy({ ...context, kingdom: await context.player.kingdom(), details: await context.player.details() }, calling.plan, calling.workers);
      if (calling.sells) await sell(context, calling.sells, want[calling.sells] ?? 0);
    },
  };
};

/** a coalition that feeds, arms and tools itself: a few gold producers, a farmer, blacksmiths and an iron miner */
export const selfSufficient: Craft[] = ["gold", "gold", "gold", "gold", "food", "tools", "tools", "iron"];

export type TradeShares = {
  /** pieces of each good the group's members bought, by where they came from */
  bought: Record<MarketResource, { allies: number; bots: number; others: number }>;
  /** the share of all pieces the group bought that came from its own members */
  fromAllies: number;
};

/** where a group's purchases came from, by good: its own members, the merchant bots, or anyone else */
export const tradeShares = (trades: MarketTrade[], members: string[], bots: string[]): TradeShares => {
  const inside = new Set(members);
  const merchants = new Set(bots);
  const bought = Object.fromEntries((["food", "iron", "tools", "weapons"] as const).map(resource => [resource, { allies: 0, bots: 0, others: 0 }])) as TradeShares["bought"];
  for (const trade of trades.filter(candidate => inside.has(candidate.buyer))) {
    const from = inside.has(trade.seller) ? "allies" : merchants.has(trade.seller) ? "bots" : "others";
    bought[trade.resource][from] += trade.count;
  }
  const totals = Object.values(bought).reduce((sum, row) => ({ allies: sum.allies + row.allies, all: sum.all + row.allies + row.bots + row.others }), { allies: 0, all: 0 });
  return { bought, fromAllies: totals.all ? totals.allies / totals.all : 0 };
};

