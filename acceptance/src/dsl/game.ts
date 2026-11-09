import type { World } from "../api/client.ts";
import type { GameConfig } from "../api/types.ts";
import type { Credentials, GateDriver, Outcome, RulerDriver } from "./driver.ts";
import { Ruler } from "./ruler.ts";

/** where visitors come from: each one a fresh browser, or a fresh API client, that the scenario closes at its end */
export type Visitors = {
  arrive: () => Promise<GateDriver>;
  /** keeps what a failed scenario looked like, where a driver can */
  keepEvidence: (scenario: string) => Promise<void>;
  close: () => Promise<void>;
};

const password = "acceptance-password";
let founded = 0;

/** a kingdom name of its own, since the scenarios of one file share a world */
const nameFor = (prefix: string): string => `${prefix} ${++founded}${Math.random().toString(36).slice(2, 5)}`;

const credentialsFor = (name: string): Credentials => ({ email: `${name.toLowerCase().replace(/[^a-z0-9]+/g, "-")}@acceptance.test`, password });

const expectRefusal = (what: string, outcome: Outcome, reason: string | RegExp): void => {
  if (outcome.done) throw new Error(`${what} went through, but should have been refused`);
  const matches = typeof reason === "string" ? outcome.message.includes(reason) : reason.test(outcome.message);
  if (!matches) throw new Error(`${what} was refused with "${outcome.message}", not ${reason}`);
};

/** the game as a scenario tells it: rulers come and go, and time passes */
export class Game {
  private readonly visitors: Visitors;
  private readonly world: World;

  public constructor(visitors: Visitors, world: World) {
    this.visitors = visitors;
    this.world = world;
  }

  private readonly signIn = async (credentials: Credentials): Promise<RulerDriver> => {
    const gate = await this.visitors.arrive();
    const outcome = await gate.signIn(credentials);
    if (!outcome.done) throw new Error(`Signing in as ${credentials.email} was refused: ${outcome.message}`);
    return gate.ruler();
  };

  /** a newcomer founds a kingdom through the sign-up and rules it */
  public async foundsAKingdom(prefix = "Realm"): Promise<Ruler> {
    const name = nameFor(prefix);
    const credentials = credentialsFor(name);
    const gate = await this.visitors.arrive();
    const outcome = await gate.foundKingdom(name, credentials);
    if (!outcome.done) throw new Error(`Founding ${name} was refused: ${outcome.message}`);
    return new Ruler(name, credentials, gate.ruler(), this.signIn);
  }

  /** a newcomer starts playing with only a kingdom name */
  public async playsWithOnlyAName(prefix = "Trial"): Promise<Ruler> {
    const name = nameFor(prefix);
    const gate = await this.visitors.arrive();
    const outcome = await gate.playWithOnlyAName(name);
    if (!outcome.done) throw new Error(`Playing as ${name} was refused: ${outcome.message}`);
    return new Ruler(name, credentialsFor(name), gate.ruler(), this.signIn);
  }

  public async refusesToFound(kingdomName: string, reason: string | RegExp): Promise<void> {
    const gate = await this.visitors.arrive();
    expectRefusal(`Founding ${kingdomName}`, await gate.foundKingdom(kingdomName, credentialsFor(nameFor("Other"))), reason);
  }

  public async refusesToSignIn(credentials: Credentials, reason: string | RegExp): Promise<void> {
    const gate = await this.visitors.arrive();
    expectRefusal(`Signing in as ${credentials.email}`, await gate.signIn(credentials), reason);
  }

  /** the clock ticks: every kingdom gets a turn, the era moves on and the merchants trade */
  /** the rules the world plays by, for checks whose numbers follow from them */
  public rules(): Promise<GameConfig> {
    return this.world.config();
  }

  public async timePasses(ticks = 1): Promise<void> {
    await this.world.advance(ticks);
  }

  /** the number of the era being played */
  public async currentEra(): Promise<number> {
    return (await this.world.era()).number;
  }

  /** the clock runs on to the era's last tick, which ends it; says which era that was */
  public async theEraRunsOut(): Promise<number> {
    const era = await this.world.era();
    const next = await this.world.advance(era.maxLengthInTicks - era.ticksElapsed);
    if (next.number !== era.number + 1) throw new Error(`Era ${era.number} ran its ${era.maxLengthInTicks} ticks, and era ${next.number} followed`);
    return era.number;
  }
}
