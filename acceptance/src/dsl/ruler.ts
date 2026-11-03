import type { BuildingName, MarketResource } from "../api/types.ts";
import type { Buildings, Credentials, Outcome, RulerDriver, Units } from "./driver.ts";

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

  public async raises(buildings: Buildings): Promise<void> {
    done(`${this.name} raising ${JSON.stringify(buildings)}`, await this.act.build(buildings));
  }

  public async trains(units: Units): Promise<void> {
    done(`${this.name} training ${JSON.stringify(units)}`, await this.act.train(units));
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

  // war

  public async marchesOn(target: Ruler, units: Units): Promise<void> {
    done(`${this.name} marching on ${target.name}`, await this.act.march(target.name, units));
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

  public async hasNoCarriersOut(): Promise<void> {
    await eventually(`${this.name} has no carriers out`, async () => (await this.act.carriersOut()).length === 0);
  }

  // letters and alliances

  public async writesTo(to: Ruler, body: string): Promise<void> {
    done(`${this.name} writing to ${to.name}`, await this.act.writeLetter(to.name, body));
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
