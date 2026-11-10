import type { Player } from "../api/client.ts";
import { militaryUnits, type BuildingName, type Counts, type Kingdom, type MarketResource, type MarketTrade, type UnitName } from "../api/types.ts";
import { developEconomy, fillBarracks, strikeLargest, walls, type Plan, type Strategy, type TurnContext } from "./strategies.ts";

/**
 * Rulers of coalitions that trade: each lives by one trade and buys the rest, from its allies first and from anyone
 * else on the market for what they cannot supply. Players in a coalition do this on purpose, so most of the coalition's
 * market should stay inside it; the merchant bots are a backstop. A coalition may also leave a craft out and rely on the
 * market for it, or corner a good to starve a rival that does.
 */
export type Good = MarketResource;
/**
 * a ruler's trade: gold producer, farmer, smith (tools and weapons), iron miner, warrior (an army fed by bought weapons),
 * or cornering a good, which means buying up what rivals offer and reselling it dear
 */
export type Craft = "gold" | "food" | "smith" | "iron" | "warrior" | `corner:${Good}`;

export const goods: Good[] = ["food", "tools", "weapons", "iron"];
const callingNames = ["gold", "food", "smith", "iron", "warrior"] as const;
type CallingName = (typeof callingNames)[number];

export const parseCraft = (text: string): Craft => {
  const craft = text.trim();
  const [kind, good] = craft.split(":");
  if (good === undefined && (callingNames as readonly string[]).includes(kind ?? "")) return craft as Craft;
  if (kind === "corner" && (goods as string[]).includes(good ?? "")) return craft as Craft;
  throw new Error(`Unknown craft "${craft}": use ${callingNames.join(", ")}, or corner: with one of ${goods.join(", ")}`);
};

type Calling = { plan: Plan; workers: UnitName[]; sells: Good[]; army?: Partial<Counts<UnitName>> };

const warriorBarracks = 0.06;
/**
 * the share of its soldiers a warrior leaves at home, half bowmen and half infantry; the rest are its marchers, two
 * infantry to a cavalryman. A warrior that marched with everyone was broken at home by every rival warrior, and with a
 * third at home a third of the strikes still fell on warriors, against a tenth with half
 */
const homeGuard = 0.5;

/** every calling keeps houses for its workers and a few gold mines, since land is bought with gold */
const callings: Record<CallingName, Calling> = {
  gold: { plan: { house: 0.4, goldMine: 0.5 }, workers: ["goldMiner"], sells: [] },
  food: { plan: { house: 0.4, farm: 0.4, goldMine: 0.1 }, workers: ["farmer", "goldMiner"], sells: ["food"] },
  smith: { plan: { house: 0.4, workshop: 0.4, goldMine: 0.1 }, workers: ["blacksmith", "goldMiner"], sells: ["tools", "weapons"] },
  iron: { plan: { house: 0.4, ironMine: 0.4, goldMine: 0.1 }, workers: ["ironMiner", "goldMiner"], sells: ["iron"] },
  // a gold economy with barracks for the army its pay share keeps; it makes no weapons, every soldier is armed from the
  // market. With the warlords' 12% of land in barracks most of them stood empty and the warrior paid less than gold
  warrior: {
    plan: { house: 0.3, goldMine: 0.51, barracks: warriorBarracks, spyGuild: 0.01 },
    workers: ["goldMiner"],
    sells: [],
    army: { bowman: homeGuard / 2, infantry: homeGuard / 2 + ((1 - homeGuard) * 2) / 3, cavalry: (1 - homeGuard) / 3 },
  },
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
/** spies a warrior keeps, so it can see its target's defense */
const warriorSpies = 12;
/**
 * the share of its gold income a warrior spends on soldiers' pay; a warrior that pays out everything stops buying land,
 * conquers acres it cannot build on or feed, and starves
 */
const armyPayShare = 0.3;

/** the soldiers a craftsman keeps at home against warriors: bowmen shoot first, infantry holds the walls */
const garrison: Partial<Counts<UnitName>> = { bowman: 1, infantry: 1 };
/**
 * the share of its land every craftsman keeps in barracks for its garrison, the smallest share measured that holds a
 * warrior to about a gold producer's land; every larger one cost more land than it saved
 */
const garrisonShare = 0.01;

/**
 * fills the garrison's barracks with what the treasury can train and pay. Sized by its gold income instead, a smith or an
 * iron miner, who earn their gold on the market, kept a token garrison and became every warrior's easiest target
 */
const raiseGarrison = async (context: TurnContext): Promise<void> => {
  const kingdom = await context.player.kingdom();
  await fillBarracks(context, garrison, kingdom.resources.gold * spendShare);
};

/**
 * how many soldiers of the mix the ruler can recruit this turn: as many as half its treasury trains and arms at the
 * merchants' price for weapons, and for a warrior as many as its share of its income pays. A warrior that bought weapons
 * for every free barracks place ended a run with 150,000 weapons it could never arm anyone with
 */
const recruitable = ({ details, config, kingdom }: TurnContext, mix: Partial<Counts<UnitName>>, paid: boolean): number => {
  const shares = Object.entries(mix) as [UnitName, number][];
  const total = shares.reduce((sum, [, share]) => sum + share, 0);
  const average = (cost: (unit: UnitName) => number): number => shares.reduce((sum, [unit, share]) => sum + (cost(unit) * share) / total, 0);
  const gold = average(unit => config.trainingCost[unit].gold);
  const arms = average(unit => config.trainingCost[unit].weapons) * merchantsPrice(config, "weapons");
  const trained = Math.floor((kingdom.resources.gold * spendShare) / (gold + arms));
  if (!paid) return trained;
  const payRoom = armyPayShare * (details.forecast.resourcesProduced.gold ?? 0) - details.forecast.goldUpkeep;
  return Math.max(0, Math.min(trained, Math.floor(payRoom / (gold * config.common.soldierUpkeepRatio))));
};

/** turns of soldiers' pay below which a ruler whose pay outgrows its income dismisses soldiers */
const payReserveTurns = 10;

/**
 * dismisses soldiers at home until their pay fits within half the gold income, once the treasury holds less than a few
 * turns of it; a smith whose income collapsed when its iron was cornered kept paying its garrison, could buy neither
 * food nor farmers, and starved for forty turns
 */
const disbandUnpaid = async ({ player, details, config, kingdom }: TurnContext): Promise<void> => {
  const pay = details.forecast.goldUpkeep;
  const excess = pay - (details.forecast.resourcesProduced.gold ?? 0) / 2;
  if (excess <= 0 || kingdom.resources.gold >= payReserveTurns * pay) return;
  const home = militaryUnits.filter(unit => kingdom.units.availableUnits[unit] > 0);
  const homePay = home.reduce((sum, unit) => sum + kingdom.units.availableUnits[unit] * config.trainingCost[unit].gold * config.common.soldierUpkeepRatio, 0);
  if (homePay <= 0) return;
  const share = Math.min(1, excess / homePay);
  await player.dismiss(Object.fromEntries(home.map(unit => [unit, Math.ceil(kingdom.units.availableUnits[unit] * share)])));
};

/**
 * whether the ruler's food store holds the turns of food it keeps; a ruler that recruits while the market leaves it short
 * only adds mouths, and a warrior that did starved two turns running
 */
const fed = async ({ player, details }: TurnContext): Promise<boolean> => {
  const hungry = Math.max(0, details.foodUpkeep - (details.forecast.resourcesProduced.food ?? 0));
  return (await player.kingdom()).resources.food >= selfFeedBelowTurns * hungry;
};

/** trains soldiers while their pay stays within the warrior's share of its income */
const raiseSoldiers = async (context: TurnContext, army: Partial<Counts<UnitName>>): Promise<void> => {
  const { details, config, kingdom } = context;
  const payRoom = armyPayShare * (details.forecast.resourcesProduced.gold ?? 0) - details.forecast.goldUpkeep;
  if (payRoom <= 0) return;
  await fillBarracks(context, army, Math.min(kingdom.resources.gold / 2, payRoom / config.common.soldierUpkeepRatio));
};

/**
 * how much of each good the ruler wants in store before it develops: a few turns of food, tools for the free workplaces,
 * iron for the forges, weapons for the free barracks places and missing spies
 */
const wants = (context: TurnContext, calling: Calling): Partial<Record<MarketResource, number>> => {
  const { details, config, kingdom } = context;
  const hungry = Math.max(0, details.foodUpkeep - (details.forecast.resourcesProduced.food ?? 0));
  const free = (building: BuildingName): number => Math.max(0, (details.buildingCapacity[building] ?? 0) - (details.buildingOccupancy[building] ?? 0));
  const freePlaces = (unit: UnitName): number => {
    const home = workplace[unit];
    return home ? free(home) : 0;
  };
  const builders = Math.max(0, Math.floor(kingdom.resources.land / 10) - kingdom.units.availableUnits.builder);
  const tools = calling.workers.reduce((sum, unit) => sum + freePlaces(unit) * config.trainingCost[unit].tools, 0) + builders * config.trainingCost.builder.tools;
  const iron = kingdom.units.availableUnits.blacksmith * config.production.blacksmith.rate * config.common.ironConsumptionPerProductionUnit;
  const shares = Object.entries(calling.army ?? garrison) as [UnitName, number][];
  const total = shares.reduce((sum, [, share]) => sum + share, 0);
  const perSoldier = shares.reduce((sum, [unit, share]) => sum + (config.trainingCost[unit].weapons * share) / total, 0);
  const spies = calling.army ? Math.max(0, warriorSpies - kingdom.units.availableUnits.spy) : 0;
  const weapons = Math.ceil(Math.min(free("barracks"), recruitable(context, calling.army ?? garrison, calling.army !== undefined)) * perSoldier) + spies * config.trainingCost.spy.weapons;
  return { food: foodTurns * hungry, tools, iron: 2 * iron, weapons };
};

const held = (context: TurnContext, resource: MarketResource): number => context.kingdom.resources[resource];

/** what the ruler has of a good, in store and still on offer from earlier turns */
const stock = (context: TurnContext, resource: MarketResource): number =>
  held(context, resource) + context.kingdom.marketOffers.filter(offer => offer.resource === resource).reduce((sum, offer) => sum + offer.count, 0);

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

/** goods carriers are bringing to the ruler */
const incoming = (context: TurnContext, resource: MarketResource): number =>
  context.kingdom.carriersOnTheMove.filter(load => load.to === context.kingdom.name && load.resource === resource).reduce((sum, load) => sum + load.count, 0);

/**
 * what a coalition that corners a good shares among its members: each member writes down how much of the cornered goods
 * it lacks, and whoever holds them ships it by carriers, so the coalition's own goods never pass the market where the
 * cornering price would charge its members too; a stand-in for players telling each other what they need
 */
type Ledger = {
  cornered: Good[];
  needs: Map<string, Partial<Record<Good, number>>>;
  /** gold each member owes each shipper for the loads it received, at the merchants' price: debtor, then shipper */
  owed: Map<string, Map<string, number>>;
  /** the one-piece offers shippers post to be paid through: offer id, then debtor */
  bills: Map<string, string>;
  /** the gold each member says it can spend on a bill, so a large debt is paid in instalments */
  budgets: Map<string, number>;
};

/**
 * the share of its land a shipper turns into markets, which house the carriers; a carrier holds what twenty workers make
 * in a turn, so a few carriers keep a craft's goods moving
 */
const shipperMarketShare = 0.01;

/**
 * buys what the ruler lacks of every good it does not make itself, counting what carriers are bringing, and never above
 * the cornering price; goods its coalition corners come by carriers only
 */
const buyWants = async (context: TurnContext, want: Partial<Record<MarketResource, number>>, makes: Good[] = [], ledger?: Ledger): Promise<void> => {
  for (const resource of goods) {
    if (makes.includes(resource)) continue;
    const missing = (want[resource] ?? 0) - held(context, resource) - incoming(context, resource);
    if (ledger?.cornered.includes(resource)) {
      ledger.needs.set(context.kingdom.name, { ...ledger.needs.get(context.kingdom.name), [resource]: Math.max(0, missing) });
      continue;
    }
    if (missing > 0) await buy(context.player, resource, missing, (await context.player.kingdom()).resources.gold * spendShare, { maxPrice: cornerMarkup * merchantsPrice(context.config, resource) });
  }
};

const refreshed = async (context: TurnContext): Promise<TurnContext> => ({ ...context, kingdom: await context.player.kingdom(), details: await context.player.details() });

/** buys food again once the turn's recruits are trained, since every worker and soldier raises the upkeep */
const topUpFood = async (context: TurnContext, calling: Calling): Promise<void> => {
  if (calling.sells.includes("food")) return;
  const now = await refreshed(context);
  const missing = (wants(now, calling).food ?? 0) - held(now, "food");
  if (missing > 0) await buy(now.player, "food", missing, now.kingdom.resources.gold * spendShare);
};

/** the share of its land a ruler turns into farms when the market leaves it short of food */
const selfFeedShare = 0.05;
/**
 * turns of food below which a ruler grows its own, which is whenever the market could not fill its store: starving cuts
 * production so hard that a kingdom which waits until its stores are empty can no longer afford the farms
 */
const selfFeedBelowTurns = foodTurns;

/**
 * what the ruler builds and staffs this turn: its calling, gold mines instead while its goods pile up unsold, and farms
 * of its own when it could not buy enough food, as a player does who will not starve waiting for the coalition's farmer
 */
const thisTurn = async (context: TurnContext, calling: Calling, unsold: boolean): Promise<{ plan: Plan; workers: UnitName[] }> => {
  const { plan: craft, workers } = unsold ? callings.gold : calling;
  const plan = calling.army ? { ...craft, ...walls(warriorBarracks * homeGuard, context.config) } : defended(context, craft);
  const hungry = context.details.foodUpkeep - (context.details.forecast.resourcesProduced.food ?? 0);
  if ((await context.player.kingdom()).resources.food >= selfFeedBelowTurns * hungry) return { plan, workers };
  return { plan: { ...plan, farm: (plan.farm ?? 0) + selfFeedShare }, workers: [...workers, "farmer"] };
};

/** turns a ruler remembers a strike on it by, and the most of its land it turns into barracks against strikes */
const strikeMemoryTurns = 24;
const mostGarrisonShare = 0.04;

/**
 * a craftsman's plan with barracks for its garrison, and towers and castles with a place for all of it; without them
 * every strike on a craftsman won, and whichever coalition's warrior struck took its rivals' land. Each strike it
 * remembers adds its garrison share again, as a player who loses land walls up: a coalition of three warriors took a
 * third more land than the expected make-up while every garrison stayed at its first size
 */
const defended = ({ config, memory, turn }: TurnContext, plan: Plan): Plan => {
  const struck = ((memory.struck as number[] | undefined) ?? []).filter(when => when > turn - strikeMemoryTurns);
  const share = Math.min(mostGarrisonShare, garrisonShare * (1 + struck.length));
  return { ...plan, barracks: (plan.barracks ?? 0) + share, ...walls(share, config) };
};

/**
 * builds the barracks, towers and castles its plan calls for before anything else, on land bought for them when its land
 * is all built on, as far as half the treasury pays; built in turn with the rest of the plan, the garrison's small share
 * was always the smallest shortfall and came last, so a smith ended with barracks on 0.3% of its land instead of 1%. A
 * farmer, a smith or an iron miner builds its land full every turn, and one that waited for free land kept the barracks
 * of turn 25 to the end of the run, on 0.5% of its land however often it was struck
 */
const wallUp = async (context: TurnContext, plan: Plan): Promise<void> => {
  const { player, kingdom } = await refreshed(context);
  const missing = (building: "barracks" | "tower" | "castle"): number => Math.max(0, Math.floor((plan[building] ?? 0) * kingdom.resources.land) - kingdom.buildings[building]);
  const walls = missing("barracks") + missing("tower") + missing("castle");
  if (walls <= 0) return;
  const built = Object.values(kingdom.buildings).reduce((sum, count) => sum + count, 0);
  for (let acres = walls - Math.max(0, kingdom.resources.land - built); acres > 0; acres = Math.floor(acres / 2)) {
    const quote = await player.landQuote(acres);
    if (quote.count === acres && quote.cost <= kingdom.resources.gold * spendShare) {
      await player.buyLand(acres);
      break;
    }
  }
  await player.build({ barracks: missing("barracks"), tower: missing("tower"), castle: missing("castle") });
};

/**
 * notes the turn on which the ruler reads each new report of a strike on it that won; judged by its land shrinking since
 * its last turn, it missed every strike that took fewer acres than it had bought that turn, and walled up for none of them
 */
const noteStrikes = async ({ player, kingdom, memory, turn }: TurnContext): Promise<void> => {
  const read = new Set((memory.reports as string[] | undefined) ?? []);
  const won = (await player.battleReports()).filter(report => report.defender === kingdom.name && report.attackerWon && !read.has(report.id));
  if (won.length === 0) return;
  memory.struck = [...((memory.struck as number[] | undefined) ?? []), ...won.map(() => turn)];
  memory.reports = [...read, ...won.map(report => report.id)];
};

/** ships the good to the members who wrote down they lack it, most lacking first, keeping what the shipper needs itself */
const ship = async (context: TurnContext, good: Good, keep: number, ledger: Ledger): Promise<void> => {
  let spare = (await context.player.kingdom()).resources[good] - keep;
  const lacking = [...ledger.needs.entries()].filter(([name, needs]) => name !== context.kingdom.name && (needs[good] ?? 0) > 0).sort(([, one], [, other]) => (other[good] ?? 0) - (one[good] ?? 0));
  for (const [name, needs] of lacking) {
    if (spare <= 0) return;
    const sent = await context.player.sendCarriers(name, good, Math.min(spare, needs[good] ?? 0));
    if (!sent.success || !sent.data) return;
    spare -= sent.data.count;
    ledger.needs.set(name, { ...needs, [good]: (needs[good] ?? 0) - sent.data.count });
    const debts = ledger.owed.get(name) ?? new Map<string, number>();
    debts.set(context.kingdom.name, (debts.get(context.kingdom.name) ?? 0) + sent.data.count * merchantsPrice(context.config, good));
    ledger.owed.set(name, debts);
  }
};

/** the allies who owe the shipper enough to bill, with this turn's instalment: the debt, or what the ally can spend */
const debtors = (context: TurnContext, ledger: Ledger): [string, number][] =>
  [...ledger.owed.entries()]
    .map(([debtor, debts]): [string, number] => [debtor, Math.floor(Math.min(debts.get(context.kingdom.name) ?? 0, ledger.budgets.get(debtor) ?? 0))])
    .filter(([, due]) => due >= context.config.market.minPrice);

/**
 * posts one piece of the good for each ally that owes the shipper, priced at the instalment, so the ally pays by buying it, the
 * way allies pass gold through the market; old bills went with the shipper's other offers of the good when it sold, which
 * kept a piece back for each
 */
const bill = async (context: TurnContext, good: Good, ledger: Ledger): Promise<void> => {
  for (const [debtor, due] of debtors(context, ledger)) {
    const posted = await context.player.postOffer(good, 1, due);
    const offer = posted.data as { id?: string } | null;
    if (posted.success && offer?.id) ledger.bills.set(offer.id, debtor);
  }
};

/** pays the bills its shippers posted for it, as far as half the treasury goes, and says what it can spend next time */
const payBills = async (context: TurnContext, ledger: Ledger): Promise<void> => {
  const name = context.kingdom.name;
  ledger.budgets.set(name, context.kingdom.resources.gold * spendShare);
  for (const offer of await context.player.alliesOffers()) {
    if (ledger.bills.get(offer.id) !== name) continue;
    if (offer.price > (await context.player.kingdom()).resources.gold * spendShare) continue;
    const paid = await context.player.buyOffer(offer.id, 1);
    if (!paid.success) continue;
    ledger.bills.delete(offer.id);
    const debts = ledger.owed.get(name);
    debts?.set(offer.sellerName, Math.max(0, (debts.get(offer.sellerName) ?? 0) - offer.price));
  }
};

/** a shipper keeps markets for its carriers on top of its calling */
const withCarriers = (calling: Calling): Calling => ({ ...calling, plan: { ...calling.plan, market: shipperMarketShare } });

/**
 * trains the carriers for what the shipper can send of what its allies wrote they lack (its stock and a turn's output),
 * before the turn's workers take the idle people; carriers are away for a few turns, so a shipper keeps that many loads'
 * worth. Shippers who hired for the allies' whole need put their people into carriers with nothing to carry, and the
 * coalition's iron miner ended with a hundred miners and fifteen hundred carriers
 */
const hireCarriers = async (context: TurnContext, good: Good, ledger: Ledger): Promise<void> => {
  const { config, details, kingdom } = context;
  const need = [...ledger.needs.entries()].filter(([name]) => name !== kingdom.name).reduce((sum, [, needs]) => sum + (needs[good] ?? 0), 0);
  const supply = kingdom.resources[good] + (details.forecast.resourcesProduced[good] ?? 0);
  const capacity = config.carrierCapacity[good] ?? 1;
  const wanted = Math.ceil(Math.min(need, supply) / capacity) * config.common.turnsToDeliverResources - kingdom.units.availableUnits.carrier - kingdom.units.mobileUnits.carrier;
  const room = (details.buildingCapacity.market ?? 0) - (details.buildingOccupancy.market ?? 0);
  if (Math.min(wanted, room) > 0) await context.player.train({ carrier: Math.min(wanted, room) });
};

/**
 * the rulers of one coalition, one per craft. When the coalition corners a good, its members get it by carriers from the
 * cornerer and from their own craftsmen of that good, and those ask the cornerer's dear price on the market, so rivals
 * cannot buy it cheaply from them
 */
export const coalitionOf = (crafts: Craft[]): Strategy[] => {
  const ledger: Ledger = { cornered: crafts.filter(craft => craft.startsWith("corner:")).map(craft => craft.slice("corner:".length) as Good), needs: new Map(), owed: new Map(), bills: new Map(), budgets: new Map() };
  return crafts.map(craft => (craft.startsWith("corner:") ? cornerer(craft.slice("corner:".length) as Good, ledger) : craftsman(craft as CallingName, ledger)));
};

/** a warrior's army on the march: its cavalry, and the infantry the home guard can spare beside the bowmen at home */
const marchers = (kingdom: Kingdom): Partial<Counts<UnitName>> => {
  const { bowman, infantry, cavalry } = kingdom.units.availableUnits;
  const guard = Math.ceil((bowman + infantry + cavalry) * homeGuard);
  return { infantry: Math.max(0, infantry - Math.max(0, guard - bowman)), cavalry };
};

/**
 * lives by one craft and buys the rest; when its goods pile up unsold it grows gold mines instead of workplaces until
 * the market takes the stock, as a player would rather than make what nobody buys. A smith turns its forges to whichever
 * of tools and weapons sold out, in proportion to what is left unsold of the other, and evenly when both sold out; a
 * warrior arms soldiers with bought weapons and strikes the largest kingdom in reach it clearly beats every few turns
 */
const craftsman = (craft: CallingName, ledger: Ledger): Strategy => {
  const ships = callings[craft].sells.filter(good => ledger.cornered.includes(good));
  const calling = ships.length ? withCarriers(callings[craft]) : callings[craft];
  const strategy: Strategy = {
    name: craft,
    weaponsPercentage: calling.sells.includes("weapons") ? 50 : 0,
    play: async context => {
      await noteStrikes(context);
      await disbandUnpaid(context);
      const want = wants(context, calling);
      await payBills(context, ledger);
      await buyWants(context, want, calling.sells, ledger);
      for (const good of ships) await hireCarriers(context, good, ledger);
      const output = calling.sells.reduce((sum, good) => sum + (context.details.forecast.resourcesProduced[good] ?? 0), 0);
      const surplus = calling.sells.reduce((sum, good) => sum + stock(context, good) - (want[good] ?? 0), 0);
      const unsold = calling.sells.length > 0 && surplus > unsoldTurns * output;
      if (calling.sells.includes("weapons")) {
        const left = (good: Good): number => context.kingdom.marketOffers.filter(offer => offer.resource === good).reduce((sum, offer) => sum + offer.count, 0);
        const [tools, weapons] = [left("tools"), left("weapons")];
        // with both sold out there is nothing to weigh, so the forges split evenly again; a smith that kept the last split
        // made tools alone for the rest of a run in which iron ran short and everything it made sold
        strategy.weaponsPercentage = tools + weapons > 0 ? Math.round((100 * tools) / (tools + weapons)) : 50;
      }
      if (calling.army) {
        const spies = warriorSpies - context.kingdom.units.availableUnits.spy;
        if (spies > 0) await context.player.train({ spy: spies });
        if (await fed(context)) await raiseSoldiers(await refreshed(context), calling.army);
      } else if (await fed(context)) {
        await raiseGarrison(context);
      }
      const { plan, workers } = await thisTurn(context, calling, unsold);
      await wallUp(context, plan);
      await developEconomy(await refreshed(context), plan, workers);
      for (const good of calling.sells) {
        if (ships.includes(good)) await ship(context, good, want[good] ?? 0, ledger);
        const dear = ships.includes(good) ? cornerMarkup * merchantsPrice(context.config, good) : undefined;
        await sell(context, good, (want[good] ?? 0) + (ships.includes(good) ? debtors(context, ledger).length : 0), dear);
        if (ships.includes(good)) await bill(context, good, ledger);
      }
      await topUpFood(context, calling);
      if (calling.army) await strikeLargest(await refreshed(context), { marchers });
    },
  };
  return strategy;
};

/**
 * a gold producer that corners one good: each turn it buys every piece rivals and merchants offer up to twice the
 * merchants' price, so a rival coalition that relies on the market for that good finds little, ships what its allies
 * lack by carriers, and offers the rest at that dear price
 */
const cornerer = (good: Good, ledger: Ledger): Strategy => {
  const calling = withCarriers(callings.gold);
  return {
    name: `corner ${good}`,
    weaponsPercentage: 0,
    play: async context => {
      await noteStrikes(context);
      await disbandUnpaid(context);
      const want = wants(context, calling);
      await payBills(context, ledger);
      await buyWants(context, want, [], ledger);
      const ceiling = cornerMarkup * merchantsPrice(context.config, good);
      await buy(context.player, good, Number.MAX_SAFE_INTEGER, (await context.player.kingdom()).resources.gold * spendShare, { fromAllies: false, maxPrice: ceiling });
      await hireCarriers(await refreshed(context), good, ledger);
      if (await fed(context)) await raiseGarrison(context);
      const { plan, workers } = await thisTurn(context, calling, false);
      await wallUp(context, plan);
      await developEconomy(await refreshed(context), plan, workers);
      await ship(context, good, want[good] ?? 0, ledger);
      await sell(context, good, (want[good] ?? 0) + debtors(context, ledger).length, ceiling);
      await bill(context, good, ledger);
      await topUpFood(context, calling);
    },
  };
};

/** a coalition that feeds, arms and tools itself: a few gold producers, a farmer, smiths, an iron miner and a warrior */
export const selfSufficient: Craft[] = ["gold", "gold", "gold", "gold", "food", "smith", "smith", "iron", "warrior"];

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
