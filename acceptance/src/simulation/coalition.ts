import type { Player, World } from "./client.ts";
import {
  armyAtHome,
  armyPower,
  attackableKingdoms,
  economist,
  estimatedDefense,
  raiseArmy,
  turtle,
  type Strategy,
  type TurnContext,
} from "./strategies.ts";
import { militaryUnits, type Counts, type UnitName } from "./types.ts";
import { foundRulers, type Ruler } from "./world.ts";

/**
 * what a coalition's warlords agree on for one attack turn, standing in for players who talk to each other: one enemy,
 * its defense as the spies saw it, and how much strength has already marched on it
 */
export type Council = {
  /** every member, so allies can look out for each other */
  members: Player[];
  warlords: Player[];
  turn: number;
  target: string | null;
  defense: number;
  committed: number;
  /** kingdoms already named this attack turn, so each strike goes to a new enemy */
  struck: string[];
  /** the first army sent on the current target; the warlords after it join its strike */
  lead: string | null;
  /** whether warlords join the first army's strike or each march alone, to measure what joining is worth */
  joins: boolean;
};

export type Coalition = { name: string; rulers: Ruler[]; council: Council };

const strikeMargin = 1.3;
/** a guardian shares soldiers only while its own defense per acre is this many times the weakest ally's */
const shareAbove = 1.5;

/**
 * a warlord who marches with the coalition: on an attack turn the first warlord to act scouts the largest enemy within
 * reach, and if the coalition's armies at home are together clearly stronger, warlords join the strike one after another
 * until enough strength marches, each joining the first army's strike so they land together; the next warlord then names
 * the next enemy, so every army can strike in the same turn
 */
export const coalitionWarlord = (council: Council, attackEvery = 6): Strategy => ({
  name: "allied warlord",
  weaponsPercentage: 70,
  play: async context => {
    const { player, kingdom, config, turn } = context;
    await raiseArmy(context);
    if (turn % attackEvery !== 0 || kingdom.ongoingAttacks.length > 0) return;

    if (council.turn !== turn) {
      council.turn = turn;
      council.target = null;
      council.committed = 0;
      council.struck = [];
      council.lead = null;
    }
    // once a strike has enough strength, the next warlord to act names a new enemy
    if (council.target === null || council.committed > council.defense * strikeMargin) {
      council.target = null;
      council.committed = 0;
      council.lead = null;
      const candidate = attackableKingdoms(context).find(rank => !council.struck.includes(rank.kingdomName));
      if (!candidate || kingdom.units.availableUnits.spy <= 0) return;
      council.struck.push(candidate.kingdomName);
      const { data: report } = await player.sendSpies(candidate.kingdomName, kingdom.units.availableUnits.spy);
      if (!report?.success) return;
      const defense = estimatedDefense(report, config);
      const homes = await Promise.all(council.warlords.map(warlord => warlord.kingdom()));
      const ready = homes
        .filter(home => home.ongoingAttacks.length === 0)
        .reduce((sum, home) => sum + armyPower(armyAtHome(home), config), 0);
      if (ready <= defense * strikeMargin) return;
      council.target = candidate.kingdomName;
      council.defense = defense;
    }

    if (!council.target) return;
    if (!attackableKingdoms(context).some(rank => rank.kingdomName === council.target)) return;
    const army = armyAtHome(kingdom);
    const power = armyPower(army, config);
    if (power <= 0) return;
    const sent = await player.sendAttack(council.target, "land", army, council.joins ? (council.lead ?? undefined) : undefined);
    if (!sent.success) return;
    council.committed += power;
    if (council.lead === null) {
      const target = council.target;
      council.lead = (await player.kingdom()).ongoingAttacks.find(attack => attack.to === target)?.id ?? null;
    }
  },
});

/**
 * a turtle that guards the coalition: it builds walls and an army like a turtle. When an enemy army marches on an ally and
 * help can still arrive in time, it sends a third of the soldiers it has at home there; otherwise every few turns, while
 * its own defense per acre is clearly above the weakest ally's, it sends a third to stand guard with that ally
 */
export const guardian = (council: Council, guardEvery = 6): Strategy => ({
  name: "guardian",
  weaponsPercentage: turtle.weaponsPercentage,
  play: async context => {
    await turtle.play(context);
    const { player, kingdom, turn, config } = context;
    const third = Object.fromEntries(militaryUnits.map(unit => [unit, Math.floor(kingdom.units.availableUnits[unit] / 3)])) as Partial<
      Counts<UnitName>
    >;
    if (militaryUnits.every(unit => (third[unit] ?? 0) === 0)) return;
    const allies = await Promise.all(
      council.members
        .filter(member => member.name !== player.name)
        .map(async member => ({ member, defense: (await member.details()).defensePower, home: await member.kingdom() })),
    );
    const guarded = new Set(kingdom.reinforcements.map(sent => sent.to));
    const threatened = allies
      .filter(ally => !guarded.has(ally.member.name))
      .map(ally => ({ ...ally, threat: ally.home.incoming.filter(army => army.turnsLeft >= config.common.turnsToReinforce) }))
      .filter(ally => ally.threat.length > 0)
      .sort(
        (one, other) =>
          other.threat.reduce((sum, army) => sum + army.soldiers, 0) - one.threat.reduce((sum, army) => sum + army.soldiers, 0),
      )[0];
    if (threatened) {
      await player.sendReinforcements(threatened.member.name, third);
      return;
    }
    if (turn % guardEvery !== 0) return;
    const cover = (defense: number, land: number): number => defense / Math.max(1, land);
    const weakest = allies.sort(
      (one, other) => cover(one.defense, one.home.resources.land) - cover(other.defense, other.home.resources.land),
    )[0];
    const ownCover = cover((await player.details()).defensePower, kingdom.resources.land);
    if (!weakest || cover(weakest.defense, weakest.home.resources.land) * shareAbove >= ownCover) return;
    await player.sendReinforcements(weakest.member.name, third);
  },
});

/** the price of one piece of food a warlord offers when it asks for gold; no merchant pays it, so it tells allies an ask */
export const askPrice = 5000;

/**
 * a warlord's ask for gold: when the gold at hand cannot fill its free barracks places, it offers as many pieces of food
 * at the asking price as the shortfall is worth, so allies can send part of it by buying only some pieces
 */
const askForGold = async ({ player, config }: TurnContext): Promise<void> => {
  const kingdom = await player.kingdom();
  if (kingdom.marketOffers.some(offer => offer.price === askPrice)) return;
  const details = await player.details();
  const free = (details.buildingCapacity.barracks ?? 0) - (details.buildingOccupancy.barracks ?? 0);
  const perSoldier = (2 * config.trainingCost.infantry.gold + config.trainingCost.cavalry.gold) / 3;
  const pieces = Math.min(kingdom.resources.food, Math.floor((free * perSoldier - kingdom.resources.gold) / askPrice));
  if (pieces <= 0) return;
  await player.postOffer("food", pieces, askPrice);
};

/** a coalition warlord that also asks its allies for the gold its barracks could use */
export const askingWarlord = (council: Council, attackEvery = 6): Strategy => {
  const strategy = coalitionWarlord(council, attackEvery);
  return {
    ...strategy,
    play: async context => {
      await strategy.play(context);
      await askForGold(context);
    },
  };
};

/**
 * an economist that funds the coalition's warlords: after its own turn it spends up to the given share of the gold left
 * on its allies' asks for gold, the largest first
 */
export const treasurer = (share = 0.5): Strategy => ({
  name: "treasurer",
  weaponsPercentage: economist.weaponsPercentage,
  play: async context => {
    await economist.play(context);
    let budget = (await context.player.kingdom()).resources.gold * share;
    const asks = (await context.player.alliesOffers())
      .filter(offer => offer.price === askPrice)
      .sort((one, other) => other.count - one.count);
    for (const ask of asks) {
      const pieces = Math.min(ask.count, Math.floor(budget / ask.price));
      if (pieces <= 0) return;
      const bought = await context.player.buyOffer(ask.id, pieces);
      budget -= bought.data?.totalCost ?? 0;
    }
  },
});

/**
 * founds the rulers of one coalition: the first founds the alliance and invites the others, who accept; warlords made
 * with `coalitionWarlord(coalition.council)` march together
 */
export const foundCoalition = async (
  world: World,
  name: string,
  roles: (council: Council) => Strategy[],
  options: { joins?: boolean } = {},
): Promise<Coalition> => {
  const council: Council = {
    members: [],
    warlords: [],
    turn: -1,
    target: null,
    defense: Infinity,
    committed: 0,
    struck: [],
    lead: null,
    joins: options.joins ?? true,
  };
  const strategies = roles(council);
  const rulers = await foundRulers(world, strategies, name);
  council.members = rulers.map(ruler => ruler.player);
  council.warlords = rulers.filter(ruler => ruler.strategy.name === "allied warlord").map(ruler => ruler.player);
  const [leader, ...members] = rulers;
  const created = await leader.player.createAlliance(name);
  if (!created.success) throw new Error(`Could not found ${name}: ${created.message}`);
  for (const member of members) {
    await leader.player.invite(member.player.name, name);
    const invitation = (await member.player.invitations()).find(candidate => candidate.allianceName === name);
    if (!invitation) throw new Error(`${member.player.name} got no invitation to ${name}`);
    await member.player.acceptInvitation(invitation.id);
  }
  return { name, rulers, council };
};
