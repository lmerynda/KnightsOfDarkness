import type { BuildingName, MarketResource, SpecialBuildingType, UnitName } from "../api/types.ts";
import type { Buildings, Credentials, Outcome, RulerDriver, Store, Units } from "./driver.ts";

/** checks again until it holds or time is up, since a page may still be catching up with the game */
export const eventually = async (what: string, check: () => Promise<boolean>, timeout = 10_000): Promise<void> => {
  const deadline = Date.now() + timeout;
  for (;;) {
    if (await check()) return;
    if (Date.now() > deadline) throw new Error(`Never came true: ${what}`);
    await new Promise(wait => setTimeout(wait, 200));
  }
};

const done = (what: string, outcome: Outcome): Outcome => {
  if (!outcome.done) throw new Error(`${what} was refused: ${outcome.message}`);
  return outcome;
};

const refused = (what: string, outcome: Outcome, reason: string | RegExp): void => {
  if (outcome.done) throw new Error(`${what} went through, but should have been refused`);
  const matches = typeof reason === "string" ? outcome.message.includes(reason) : reason.test(outcome.message);
  if (!matches) throw new Error(`${what} was refused with "${outcome.message}", not ${reason}`);
};

const soldiers = (units: Units): number => Object.values(units).reduce((sum, count) => sum + (count ?? 0), 0);

/**
 * a ruler in the game's own words: what they do, and what they expect to see; every action must go through, every
 * expectation is waited for, and a refusal or an unmet expectation fails the scenario with the game's own message
 */
export class Ruler {
  public readonly name: string;
  public credentials: Credentials;
  private readonly signIn: (credentials: Credentials) => Promise<RulerDriver>;
  private session: RulerDriver;

  public constructor(name: string, credentials: Credentials, driver: RulerDriver, signIn: (credentials: Credentials) => Promise<RulerDriver>) {
    this.name = name;
    this.credentials = credentials;
    this.session = driver;
    this.signIn = signIn;
  }

  private get act(): RulerDriver {
    return this.session;
  }

  public toString(): string {
    return this.name;
  }

  // the realm

  public async passesTurns(count: number): Promise<void> {
    for (let turn = 0; turn < count; turn++) done(`${this.name} passing a turn`, await this.act.passTurn());
  }

  public async spendsTheTreasuryOnLand(): Promise<void> {
    done(`${this.name} spending the treasury on land`, await this.act.buyAllTheLandWeCanAfford());
  }

  public async mayNotPassATurn(reason: string | RegExp): Promise<void> {
    refused(`${this.name} passing a turn`, await this.act.passTurn(), reason);
  }

  public async hasTurnsStored(count: number): Promise<void> {
    await eventually(`${this.name} has ${count} turns stored`, async () => (await this.act.turnsStored()) === count);
  }

  public async raises(buildings: Buildings): Promise<void> {
    done(`${this.name} raising ${JSON.stringify(buildings)}`, await this.act.build(buildings));
  }

  public async trains(units: Units): Promise<void> {
    done(`${this.name} training ${JSON.stringify(units)}`, await this.act.train(units));
  }

  public async tearsDown(buildings: Buildings): Promise<void> {
    done(`${this.name} tearing down ${JSON.stringify(buildings)}`, await this.act.demolish(buildings));
  }

  public async dismisses(units: Units): Promise<void> {
    done(`${this.name} dismissing ${JSON.stringify(units)}`, await this.act.dismiss(units));
  }

  public unitsAtHome(unit: UnitName): Promise<number> {
    return this.act.unitsAtHome(unit);
  }

  public async hasAtHome(count: number, unit: UnitName): Promise<void> {
    await eventually(`${this.name} has ${count} ${unit} at home`, async () => (await this.act.unitsAtHome(unit)) === count);
  }

  public async beginsAGreatWork(type: SpecialBuildingType): Promise<void> {
    done(`${this.name} beginning a ${type}`, await this.act.beginGreatWork(type));
  }

  public async cannotBeginAGreatWork(type: SpecialBuildingType, reason: string | RegExp): Promise<void> {
    refused(`${this.name} beginning a ${type}`, await this.act.beginGreatWork(type), reason);
  }

  public async investsIn(type: SpecialBuildingType, buildingPoints: number): Promise<void> {
    done(`${this.name} investing ${buildingPoints} points in the ${type}`, await this.act.investInGreatWork(type, buildingPoints));
  }

  public async tearsDownTheGreatWork(type: SpecialBuildingType): Promise<void> {
    done(`${this.name} tearing down the ${type}`, await this.act.tearDownGreatWork(type));
  }

  public async hasAGreatWork(type: SpecialBuildingType, level: number): Promise<void> {
    await eventually(`${this.name} has a ${type} at level ${level}`, async () =>
      (await this.act.greatWorks()).some(work => work.type === type && work.level === level),
    );
  }

  public async hasNoGreatWorks(): Promise<void> {
    await eventually(`${this.name} has no great works`, async () => (await this.act.greatWorks()).length === 0);
  }

  public buildingsOf(building: BuildingName): Promise<number> {
    return this.act.buildingCount(building);
  }

  public async owns(count: number, building: BuildingName): Promise<void> {
    await eventually(`${this.name} owns ${count} ${building}`, async () => (await this.act.buildingCount(building)) === count);
  }

  public priceOfLand(acres: number): Promise<number> {
    return this.act.landPrice(acres);
  }

  public async buysLand(acres: number): Promise<void> {
    done(`${this.name} buying ${acres} acres`, await this.act.buyLand(acres));
  }

  public async paysSoldiers(gold: number): Promise<void> {
    await eventually(`${this.name} pays soldiers ${gold} gold a turn`, async () => (await this.act.soldiersPay()) === gold);
  }

  public holds(store: Store): Promise<number> {
    return this.act.stores(store);
  }

  /** what the kingdom's people will bring in of a good next turn */
  public expectsToBringIn(store: Store): Promise<number> {
    return this.act.nextYield(store);
  }

  // war

  public async marchesOn(target: Ruler, units: Units): Promise<void> {
    done(`${this.name} marching on ${target.name}`, await this.act.march(target.name, units, "land"));
  }

  public async marchesToPlunder(target: Ruler, units: Units): Promise<void> {
    done(`${this.name} marching to plunder ${target.name}`, await this.act.march(target.name, units, "plunder"));
  }

  public async mayNotMarchOn(target: Ruler, units: Units, reason: string | RegExp): Promise<void> {
    refused(`${this.name} marching on ${target.name}`, await this.act.march(target.name, units, "land"), reason);
  }

  public async callsBackTheArmyFrom(target: Ruler): Promise<void> {
    done(`${this.name} calling back the army from ${target.name}`, await this.act.recallArmy(target.name));
  }

  public async joinsTheStrike(leader: Ruler, target: Ruler, units: Units): Promise<void> {
    done(`${this.name} joining ${leader.name}'s strike on ${target.name}`, await this.act.joinStrike(leader.name, target.name, units));
  }

  public async seesAnArmyComing(from: Ruler, soldiers: number): Promise<void> {
    await eventually(`${this.name} sees ${from.name} coming with ${soldiers} soldiers`, async () =>
      (await this.act.armiesComing()).some(army => army.from === from.name && army.soldiers === soldiers),
    );
  }

  public async waitsAtTheBorderOf(target: Ruler, leader: Ruler): Promise<void> {
    await eventually(`${this.name} waits at the border of ${target.name} for ${leader.name}`, async () =>
      (await this.act.armiesOut()).some(army => army.to === target.name && army.joining === leader.name && army.atTheBorder),
    );
  }

  public async hasNoArmyOut(): Promise<void> {
    await eventually(`${this.name} has no army out`, async () => (await this.act.armiesOut()).length === 0);
  }

  public async hasDefeated(target: Ruler): Promise<void> {
    await eventually(`${this.name} has defeated ${target.name}`, async () =>
      (await this.act.battles()).some(battle => battle.attacker === this.name && battle.defender === target.name && battle.attackerWon),
    );
  }

  /** the army lost the battle at the target and came home with nothing */
  public async hasBeenBeatenBackBy(target: Ruler): Promise<void> {
    await eventually(`${this.name} has been beaten back by ${target.name}`, async () =>
      (await this.act.battles()).some(
        battle => battle.attacker === this.name && battle.defender === target.name && !battle.attackerWon && battle.landTaken === 0 && battle.goldPlundered === 0,
      ),
    );
  }

  /** the kingdom stood against an attack and lost nothing */
  public async hasRepelled(attacker: Ruler): Promise<void> {
    await eventually(`${this.name} has repelled ${attacker.name}`, async () =>
      (await this.act.battles()).some(
        battle => battle.attacker === attacker.name && battle.defender === this.name && !battle.attackerWon && battle.landTaken === 0 && battle.goldPlundered === 0,
      ),
    );
  }

  public async hasTakenLandFrom(target: Ruler): Promise<void> {
    await eventually(`${this.name} has taken land from ${target.name}`, async () =>
      (await this.act.battles()).some(battle => battle.attacker === this.name && battle.defender === target.name && battle.landTaken > 0),
    );
  }

  public async hasPlundered(target: Ruler): Promise<void> {
    await eventually(`${this.name} has plundered ${target.name}`, async () =>
      (await this.act.battles()).some(
        battle => battle.attacker === this.name && battle.defender === target.name && battle.goldPlundered > 0 && battle.landTaken === 0,
      ),
    );
  }

  /** the gold carried off from the target in a won raid */
  public async goldPlunderedFrom(target: Ruler): Promise<number> {
    await this.hasPlundered(target);
    const raid = (await this.act.battles()).find(battle => battle.attacker === this.name && battle.defender === target.name && battle.attackerWon);
    return raid?.goldPlundered ?? 0;
  }

  public async hasFoughtAt(target: Ruler): Promise<void> {
    await eventually(`${this.name} has fought at ${target.name}`, async () =>
      (await this.act.battles()).some(battle => battle.attacker === this.name && battle.defender === target.name),
    );
  }

  public async sendsSpies(target: Ruler, spies: number): Promise<void> {
    done(`${this.name} sending spies to ${target.name}`, await this.act.sendSpies(target.name, spies));
  }

  public async hasIntelOnTheStoresOf(target: Ruler): Promise<void> {
    await eventually(`${this.name} knows the stores of ${target.name}`, async () => {
      const intel = await this.act.latestIntel();
      return intel?.about === target.name && intel.stores;
    });
  }

  public async hasLostSpiesIn(target: Ruler, spies: number): Promise<void> {
    await eventually(`${this.name} lost ${spies} spies in ${target.name}`, async () => {
      const intel = await this.act.latestIntel();
      return intel?.about === target.name && !intel.stores && intel.spiesLost === spies;
    });
  }

  public async sendsToStandGuard(ally: Ruler, units: Units): Promise<void> {
    done(`${this.name} sending guards to ${ally.name}`, await this.act.sendGuards(ally.name, units));
  }

  public async hasStandingGuardWith(ally: Ruler, units: Units): Promise<void> {
    await eventually(`${this.name} has ${soldiers(units)} soldiers standing guard with ${ally.name}`, async () =>
      (await this.act.guardsAway()).some(guard => guard.with === ally.name && guard.soldiers === soldiers(units)),
    );
  }

  public async isGuardedBy(ally: Ruler, units: Units): Promise<void> {
    await eventually(`${this.name} is guarded by ${soldiers(units)} soldiers of ${ally.name}`, async () =>
      (await this.act.guardsHosted()).some(guard => guard.from === ally.name && guard.soldiers === soldiers(units)),
    );
  }

  public async callsTheGuardsHome(): Promise<void> {
    done(`${this.name} calling the guards home`, await this.act.callGuardsHome());
  }

  public async hasNoGuardsAway(): Promise<void> {
    await eventually(`${this.name} has no guards away`, async () => (await this.act.guardsAway()).length === 0);
  }

  // trade

  public async buys(count: number, resource: MarketResource): Promise<void> {
    done(`${this.name} buying ${count} ${resource}`, await this.act.buy(resource, count));
  }

  public async buysFrom(seller: Ruler, count: number, resource: MarketResource): Promise<void> {
    done(`${this.name} buying ${count} ${resource} from ${seller.name}`, await this.act.buyFrom(seller.name, resource, count));
  }

  public async offersForSale(count: number, resource: MarketResource, price: number): Promise<void> {
    done(`${this.name} offering ${count} ${resource}`, await this.act.offer(resource, count, price));
  }

  public async hasOnTheMarket(count: number, resource: MarketResource): Promise<void> {
    await eventually(`${this.name} has ${count} ${resource} on the market`, async () =>
      (await this.act.ownOffers()).some(offer => offer.resource === resource && offer.count === count),
    );
  }

  public async hasNothingOnTheMarket(): Promise<void> {
    await eventually(`${this.name} has nothing on the market`, async () => (await this.act.ownOffers()).length === 0);
  }

  public async withdrawsTheOfferOf(resource: MarketResource): Promise<void> {
    done(`${this.name} withdrawing the ${resource}`, await this.act.withdrawOffer(resource));
  }

  public async asksAlliesFor(gold: number): Promise<void> {
    done(`${this.name} asking allies for ${gold} gold`, await this.act.askAlliesForGold(gold));
  }

  public async seesTheAsk(ally: Ruler, gold: number): Promise<void> {
    await eventually(`${this.name} sees ${ally.name} ask for ${gold} gold`, async () =>
      (await this.act.alliesAsks()).some(ask => ask.from === ally.name && ask.gold === gold),
    );
  }

  public async paysTheAskOf(ally: Ruler): Promise<void> {
    done(`${this.name} paying ${ally.name}'s ask`, await this.act.payAsk(ally.name));
  }

  public async seesNoAsks(): Promise<void> {
    await eventually(`${this.name} sees no asks`, async () => (await this.act.alliesAsks()).length === 0);
  }

  public async sendsCarriers(to: Ruler, count: number, resource: MarketResource): Promise<void> {
    done(`${this.name} sending ${count} ${resource} to ${to.name}`, await this.act.sendCarriers(to.name, resource, count));
  }

  public async hasCarriersOnTheWayTo(to: Ruler, count: number, resource: MarketResource): Promise<void> {
    await eventually(`${this.name} has ${count} ${resource} on the way to ${to.name}`, async () =>
      (await this.act.carriersOut()).some(load => load.to === to.name && load.count === count && load.resource === resource),
    );
  }

  public async callsBackTheCarriersFrom(to: Ruler): Promise<void> {
    done(`${this.name} calling back the carriers from ${to.name}`, await this.act.recallCarriers(to.name));
  }

  public async hasNoCarriersOut(): Promise<void> {
    await eventually(`${this.name} has no carriers out`, async () => (await this.act.carriersOut()).length === 0);
  }

  // letters and alliances

  public async writesTo(to: Ruler, body: string): Promise<void> {
    done(`${this.name} writing to ${to.name}`, await this.act.writeLetter(to.name, body));
  }

  public async writesToTheAlliance(body: string): Promise<void> {
    done(`${this.name} writing to the alliance`, await this.act.writeToAlliance(body));
  }

  public async hasUnreadLetters(): Promise<void> {
    await eventually(`${this.name} has unread letters`, async () => (await this.act.unreadLetters()) > 0);
  }

  public async hasALetterFrom(sender: Ruler, body: string): Promise<void> {
    await eventually(`${this.name} has a letter from ${sender.name}`, async () =>
      (await this.act.letters()).some(letter => letter.from === sender.name && letter.body === body),
    );
  }

  public async hasSentALetterTo(recipient: Ruler, body: string): Promise<void> {
    await eventually(`${this.name} has sent a letter to ${recipient.name}`, async () =>
      (await this.act.sentLetters()).some(letter => letter.to === recipient.name && letter.body === body),
    );
  }

  public async hasNoLetters(): Promise<void> {
    await eventually(`${this.name} has no letters`, async () => (await this.act.letters()).length === 0);
  }

  public async foundsAlliance(name: string): Promise<void> {
    done(`${this.name} founding ${name}`, await this.act.foundAlliance(name));
  }

  public async invites(kingdom: Ruler): Promise<void> {
    done(`${this.name} inviting ${kingdom.name}`, await this.act.invite(kingdom.name));
  }

  public async joins(allianceName: string): Promise<void> {
    done(`${this.name} joining ${allianceName}`, await this.act.join(allianceName));
  }

  public async declinesTheInvitationTo(allianceName: string): Promise<void> {
    done(`${this.name} declining ${allianceName}`, await this.act.decline(allianceName));
  }

  public async leavesTheAlliance(): Promise<void> {
    done(`${this.name} leaving the alliance`, await this.act.leaveAlliance());
  }

  public async removesFromTheAlliance(member: Ruler): Promise<void> {
    done(`${this.name} removing ${member.name}`, await this.act.removeMember(member.name));
  }

  public async raisesAVassal(name: string): Promise<void> {
    done(`${this.name} raising the vassal ${name}`, await this.act.raiseVassal(name));
  }

  public async isInNoAlliance(): Promise<void> {
    await eventually(`${this.name} is in no alliance`, async () => (await this.act.alliance()) === null);
  }

  /** the alliance has exactly these members by name, vassals the game runs among them */
  public async leadsAllianceOf(allianceName: string, ...members: string[]): Promise<void> {
    const expected = [this.name, ...members].sort();
    await eventually(`${this.name} leads ${allianceName} of ${expected.join(", ")}`, async () => {
      const alliance = await this.act.alliance();
      return alliance?.name === allianceName && JSON.stringify([...alliance.members].sort()) === JSON.stringify(expected);
    });
  }

  public async isInAllianceWith(allianceName: string, ...members: Ruler[]): Promise<void> {
    const expected = [this, ...members].map(member => member.name).sort();
    await eventually(`${this.name} is in ${allianceName} with ${members.join(", ")}`, async () => {
      const alliance = await this.act.alliance();
      return alliance?.name === allianceName && expected.every(member => alliance.members.includes(member));
    });
  }

  public async seesInTheWarRoom(from: Ruler, to: Ruler): Promise<void> {
    await eventually(`${this.name} sees ${from.name} march on ${to.name} in the war room`, async () =>
      (await this.act.warRoomMarches()).some(march => march.from === from.name && march.to === to.name),
    );
  }

  public async readsInTheChronicle(pattern: RegExp): Promise<void> {
    await eventually(`${this.name}'s chronicle tells ${pattern}`, async () => (await this.act.chronicle()).some(entry => pattern.test(entry)));
  }

  // the round

  public async seesInTheHallOfFame(round: number, winner: Ruler): Promise<void> {
    await eventually(`${this.name} sees ${winner.name} remembered for round ${round}`, async () =>
      (await this.act.pastRounds()).some(past => past.round === round && past.winner === winner.name),
    );
  }

  /** what a kingdom holds when it starts: land, gold, turns, houses, builders and soldiers */
  private async standing(): Promise<number[]> {
    return [
      await this.act.land(),
      await this.act.stores("gold"),
      await this.act.turnsStored(),
      await this.act.buildingCount("house"),
      await this.act.unitsAtHome("builder"),
      await this.act.unitsAtHome("infantry"),
    ];
  }

  /** holds exactly what the other kingdom holds, as two kingdoms just begun do */
  public async standsLevelWith(other: Ruler): Promise<void> {
    const [mine, theirs] = [await this.standing(), await other.standing()];
    if (JSON.stringify(mine) !== JSON.stringify(theirs)) {
      throw new Error(`${this.name} holds ${mine.join(", ")} in land, gold, turns, houses, builders and infantry, ${other.name} ${theirs.join(", ")}`);
    }
  }

  // the account

  public async keepsTheKingdom(credentials: Credentials): Promise<void> {
    done(`${this.name} keeping the kingdom`, await this.act.keepKingdom(credentials));
    this.credentials = credentials;
  }

  public async playsOnATrial(): Promise<void> {
    await eventually(`${this.name} plays on a trial`, () => this.act.isTrial());
  }

  public async ownsTheKingdom(): Promise<void> {
    await eventually(`${this.name} owns the kingdom`, async () => !(await this.act.isTrial()));
  }

  /** signs out and back in with the ruler's own address and password */
  public async comesBack(): Promise<void> {
    await this.act.signOut();
    this.session = await this.signIn(this.credentials);
    const name = await this.session.kingdomName();
    if (name !== this.name) throw new Error(`${this.name} came back to ${name}`);
  }
}
