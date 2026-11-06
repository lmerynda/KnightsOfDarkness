import type { Player } from "../api/client.ts";
import type { BuildingName, MarketResource, MarketTrade, UnitName } from "../api/types.ts";
import { developEconomy, type Plan, type Strategy, type TurnContext } from "./strategies.ts";

/**
 * Rulers of coalitions that trade: each lives by one trade and buys the rest, from its allies first and from anyone
 * else on the market for what they cannot supply. Players in a coalition do this on purpose, so most of the coalition's
 * market should stay inside it; the merchant bots are a backstop. A coalition may also leave a craft out and rely on the
 * market for it, or corner a good to starve a rival that does.
 */
export type Good = "food" | "tools" | "iron";
/** a ruler's trade: gold, one of the goods, or cornering a good, which means buying up what rivals offer and reselling it dear */
export type Craft = "gold" | Good | `corner:${Good}`;

export const goods: Good[] = ["food", "tools", "iron"];

export const parseCraft = (text: string): Craft => {
  const craft = text.trim();
  const [kind, good] = craft.split(":");
  if (good === undefined && (kind === "gold" || (goods as string[]).includes(kind ?? ""))) return craft as Craft;
  if (kind === "corner" && (goods as string[]).includes(good ?? "")) return craft as Craft;
  throw new Error(`Unknown craft "${craft}": use gold, food, tools, iron, corner:food, corner:tools or corner:iron`);
};

type Calling = { plan: Plan; workers: UnitName[]; sells?: Good };

/** every calling keeps houses for its workers and a few gold mines, since land is bought with gold */
const callings: Record<"gold" | Good, Calling> = {
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
/** turns of its own output a craftsman lets pile up unsold before it grows gold mines instead of workplaces */
const unsoldTurns = 5;
/** how many times the merchants' price a cornerer pays at most, and asks for what it bought */
const cornerMarkup = 2;

/** how much of each good the ruler wants in store before it develops: a few turns of food, tools for the free workplaces, iron for the forges */
const wants = (context: TurnContext, calling: Calling): Partial<Record<MarketResource, number>> => {
  const { details, config, kingdom } = context;
  const hungry = Math.max(0, details.foodUpkeep - (details.forecast.resourcesProduced.food ?? 0));
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

/** buys up to the count, cheapest allies' offers first (unless told to leave them), then anyone's; spends at most the budget */
const buy = async (player: Player, resource: MarketResource, count: number, budget: number, { fromAllies = true, maxPrice = Infinity } = {}): Promise<void> => {
  const allies = (await player.alliesOffers()).filter(offer => offer.resource === resource).sort((one, other) => one.price - other.price);
  const allyIds = new Set(allies.map(offer => offer.id));
  const others = (await player.offers(resource)).filter(offer => !allyIds.has(offer.id) && offer.sellerName !== player.name).sort((one, other) => one.price - other.price);
  for (const offer of [...(fromAllies ? allies : []), ...others]) {
    if (offer.price > maxPrice) return;
    const pieces = Math.min(count, offer.count, Math.floor(budget / offer.price));
    if (pieces <= 0) return;
    const bought = await player.buyOffer(offer.id, pieces);
    if (!bought.success || !bought.data) continue;
    count -= bought.data.count;
    budget -= bought.data.totalCost;
  }
};

const merchantsPrice = (config: TurnContext["config"], resource: MarketResource): number => {
  const price = config.bots.prices[resource];
  if (price === undefined) throw new Error(`The game config sets no merchants' price for ${resource}`);
  return price;
};

/** the price outsiders ask, or the merchants' own when nobody outside the alliance sells the good */
const marketPrice = async ({ player, config }: TurnContext, resource: MarketResource): Promise<number> => {
  const allies = new Set((await player.alliesOffers()).map(offer => offer.id));
  const outside = (await player.offers(resource)).filter(offer => !allies.has(offer.id) && offer.sellerName !== player.name);
  if (outside.length) return Math.min(...outside.map(offer => offer.price));
  return merchantsPrice(config, resource);
};

/** takes its old offer back and puts up everything beyond its own wants at the market's price, or at the price given */
const sell = async (context: TurnContext, resource: MarketResource, keep: number, price?: number): Promise<void> => {
  const { player } = context;
  for (const offer of (await player.kingdom()).marketOffers.filter(offer => offer.resource === resource)) {
    await player.withdrawOffer(offer.id);
  }
  const surplus = (await player.kingdom()).resources[resource] - keep;
  if (surplus <= 0) return;
  await player.postOffer(resource, surplus, price ?? (await marketPrice(context, resource)));
};

/** buys what the ruler lacks of every good it does not make itself */
const buyWants = async (context: TurnContext, want: Partial<Record<MarketResource, number>>, makes?: Good): Promise<void> => {
  for (const resource of goods) {
    if (resource === makes) continue;
    const missing = (want[resource] ?? 0) - held(context, resource);
    if (missing > 0) await buy(context.player, resource, missing, (await context.player.kingdom()).resources.gold * spendShare);
  }
};

const refreshed = async (context: TurnContext): Promise<TurnContext> => ({ ...context, kingdom: await context.player.kingdom(), details: await context.player.details() });

/** the share of its land a ruler turns into farms when the market leaves it short of food */
const selfFeedShare = 0.05;
/**
 * turns of food below which a ruler grows its own: starving cuts production so hard that a kingdom which waits until
 * its stores are empty can no longer afford the farms
 */
const selfFeedBelowTurns = 2;

/**
 * what the ruler builds and staffs this turn: its calling, gold mines instead while its good piles up unsold, and farms
 * of its own when it could not buy enough food, as a player does who will not starve waiting for the coalition's farmer
 */
const thisTurn = async (context: TurnContext, calling: Calling, unsold: boolean): Promise<{ plan: Plan; workers: UnitName[] }> => {
  const { plan, workers } = unsold ? callings.gold : calling;
  const hungry = context.details.foodUpkeep - (context.details.forecast.resourcesProduced.food ?? 0);
  if ((await context.player.kingdom()).resources.food >= selfFeedBelowTurns * hungry) return { plan, workers };
  return { plan: { ...plan, farm: (plan.farm ?? 0) + selfFeedShare }, workers: [...workers, "farmer"] };
};

/**
 * the rulers of one coalition, one per craft; when the coalition corners a good its own craftsmen of that good ask the
 * cornerer's dear price too, so rivals cannot buy it cheaply from them, while allies' payments stay in the coalition
 */
export const coalitionOf = (crafts: Craft[]): Strategy[] => {
  const cornered = crafts.filter(craft => craft.startsWith("corner:")).map(craft => craft.slice("corner:".length) as Good);
  return crafts.map(craft => (craft.startsWith("corner:") ? cornerer(craft.slice("corner:".length) as Good) : craftsman(craft as "gold" | Good, cornered)));
};

/**
 * lives by one craft and buys the rest; when its good piles up unsold it grows gold mines instead of workplaces until
 * the market takes the stock, as a player would rather than make what nobody buys
 */
const craftsman = (craft: "gold" | Good, cornered: Good[]): Strategy => {
  const calling = callings[craft];
  return {
    name: `${craft} specialist`,
    weaponsPercentage: 0,
    play: async context => {
      const want = wants(context, calling);
      await buyWants(context, want, calling.sells);
      const output = calling.sells ? (context.details.forecast.resourcesProduced[calling.sells] ?? 0) : 0;
      const unsold = calling.sells !== undefined && held(context, calling.sells) - (want[calling.sells] ?? 0) > unsoldTurns * output;
      const { plan, workers } = await thisTurn(context, calling, unsold);
      await developEconomy(await refreshed(context), plan, workers);
      if (calling.sells) {
        const dear = cornered.includes(calling.sells) ? cornerMarkup * merchantsPrice(context.config, calling.sells) : undefined;
        await sell(context, calling.sells, want[calling.sells] ?? 0, dear);
      }
    },
  };
};

/**
 * a gold producer that corners one good: each turn it buys every piece rivals and merchants offer up to twice the
 * merchants' price, so a rival coalition that relies on the market for that good finds little, and offers what it holds
 * at that dear price; its allies buy from their own craftsman first, and what they pay the cornerer stays in the coalition
 */
const cornerer = (good: Good): Strategy => {
  const calling = callings.gold;
  return {
    name: `${good} cornerer`,
    weaponsPercentage: 0,
    play: async context => {
      const want = wants(context, calling);
      await buyWants(context, want);
      const ceiling = cornerMarkup * merchantsPrice(context.config, good);
      await buy(context.player, good, Number.MAX_SAFE_INTEGER, (await context.player.kingdom()).resources.gold * spendShare, { fromAllies: false, maxPrice: ceiling });
      const { plan, workers } = await thisTurn(context, calling, false);
      await developEconomy(await refreshed(context), plan, workers);
      await sell(context, good, want[good] ?? 0, ceiling);
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
