import type { Player, World } from "./client.ts";
import { armyAtHome, armyPower, attackableKingdoms, estimatedDefense, raiseArmy, turtle, type Strategy } from "./strategies.ts";
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
};

export type Coalition = { name: string; rulers: Ruler[]; council: Council };

const strikeMargin = 1.3;

/**
 * a warlord who marches with the coalition: on an attack turn the first warlord to act scouts the largest enemy within
 * reach, and if the coalition's armies at home are together clearly stronger, warlords join the strike one after another
 * until enough strength marches; the rest stay home to defend
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
      const candidate = attackableKingdoms(context)[0];
      if (!candidate || kingdom.units.availableUnits.spy <= 0) return;
      const { report } = await player.sendSpies(candidate.kingdomName, kingdom.units.availableUnits.spy);
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

    if (!council.target || council.committed > council.defense * strikeMargin) return;
    if (!attackableKingdoms(context).some(rank => rank.kingdomName === council.target)) return;
    const army = armyAtHome(kingdom);
    const power = armyPower(army, config);
    if (power <= 0) return;
    const sent = await player.sendAttack(council.target, "land", army);
    if (sent.success) council.committed += power;
  },
});

/**
 * a turtle that guards the coalition: it builds walls and an army like a turtle, and every few turns sends half of the
 * soldiers it has at home to stand guard with the ally whose defense is weakest for its size
 */
export const guardian = (council: Council, guardEvery = 6): Strategy => ({
  name: "guardian",
  weaponsPercentage: turtle.weaponsPercentage,
  play: async context => {
    await turtle.play(context);
    const { player, kingdom, turn } = context;
    if (turn % guardEvery !== 0) return;
    const allies = await Promise.all(
      council.members
        .filter(member => member.name !== player.name)
        .map(async member => ({ member, defense: (await member.details()).defensePower, land: (await member.kingdom()).resources.land })),
    );
    const weakest = allies.sort((one, other) => one.defense / one.land - other.defense / other.land)[0];
    if (!weakest) return;
    const half = Object.fromEntries(militaryUnits.map(unit => [unit, Math.floor(kingdom.units.availableUnits[unit] / 2)])) as Partial<
      Counts<UnitName>
    >;
    if (militaryUnits.every(unit => (half[unit] ?? 0) === 0)) return;
    await player.sendReinforcements(weakest.member.name, half);
  },
});

/**
 * founds the rulers of one coalition: the first founds the alliance and invites the others, who accept; warlords made
 * with `coalitionWarlord(coalition.council)` march together
 */
export const foundCoalition = async (world: World, name: string, roles: (council: Council) => Strategy[]): Promise<Coalition> => {
  const council: Council = {
    members: [],
    warlords: [],
    turn: -1,
    target: null,
    defense: Infinity,
    committed: 0,
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
