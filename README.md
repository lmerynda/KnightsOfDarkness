# Knights of Darkness

A browser strategy game of dark medieval kingdoms. Each player rules one kingdom: build on land, train workers and soldiers, trade on the market, send carriers to allies, spy on rivals and march armies to take land or plunder stores. Kingdoms swear to alliances, write letters, and compete for land until the round ends; then the largest kingdom and alliance are remembered and every realm starts again.

- `backend/`: Spring Boot game server (Java 25, Gradle). Game rules live in `kingdom/model`, numbers in `src/main/resources/gameconfig.json`.
- `browser/`: React and Vite client. Art and its prompts are in `browser/art` and `browser/public/art`.
- `acceptance/`: tests of the whole game from the outside, in its own words, through the HTTP API and through Chrome, plus the tools that simulate whole worlds for balance.

## Run it locally

You need JDK 25, Node.js 26 and Docker; the Gradle wrapper fetches Gradle itself and finds JDK 25 on its own, while the acceptance tests run the server with the Java that `JAVA_HOME` names, or else the one on the path. Two terminals:

```bash
cd backend && ./gradlew bootRun
```

```bash
cd browser && npm install && npm run dev
```

Open http://localhost:3000. The page asks its own origin for `/api`, as in production, and the dev server forwards that to the game server on 8080 (`GAME_SERVER` to point elsewhere), so another device on your network can play at `http://<this machine>:3000`. The world lasts across restarts, locally as in production (see [The world's database](#the-worlds-database)). Local development, and only local development (`game.seed-dev-accounts`), seeds two player kingdoms in the alliance Legion, both with a small garrison and spies:

| Sign in as | Password |
|---|---|
| `uprzejmy` | `uprzejmy_password` |
| `umbar` | `umbar_password` |

You can also found a new kingdom from the landing page, or play at once with only a kingdom name (Play now, `POST /auth/trial`): the browser keeps that trial account, signing back in by itself when a session runs out, until the ruler keeps the kingdom with an address and password of their own (`POST /auth/keep`). Signing out of a trial kingdom before keeping it loses it, and the game asks first. Four merchant bots (FarmerBot, GoldMinerBot, IronMinerBot, BlacksmithBot) trade on the market, can be attacked and spied on, and are not ranked. They stop growing at 400 acres and keep their stores and offers under the caps in `gameconfig.json` (`bots`).

A new turn is handed to every kingdom every hour, on the hour, and a kingdom stores up to 48, so a ruler can stay away two days without losing one. A round lasts 672 of those ticks, four weeks. All three are set in `gameconfig.json` (`common.newTurnPeriodicity`, `common.maxTurns`, `round.lengthInTicks`). Turns come on the wall clock's whole periods, so a restart neither hands out an extra turn nor delays the next. Local play (`bootRun`) speeds the clock up to a turn every 10 seconds with `game.clock.turn-period`, so a local round lasts under two hours; the jar keeps the hourly pace.

Build can fill in a plan (Economy, War or Fortress, the land shares the simulated rulers grow on) one building at a time into the largest gap, and Train can staff every free workplace; both only fill in the order, which the player then raises or trains.

The pages are written for players who know the game: they show figures, and the steward on the overview warns only of what will not wait (an army on the way, hunger, unpaid soldiers, an ally's call for gold, a full turn store). The rules sit in tooltips, behind a `?` beside a title or a dotted word, opened by hovering, focusing or tapping.

## How a turn works

Passing a turn spends one stored turn: people who lack houses leave, farmers bring in the harvest, everyone eats and people who go hungry leave, the other workers produce (small kingdoms get a production bonus that fades by 1,000 land), soldiers are paid a hundredth of their training gold (`common.soldierUpkeepRatio`; those away on the march or standing guard with an ally too) and when the gold falls short soldiers at home desert in proportion to the unpaid share, a tenth of them when nothing is paid, new people arrive into empty houses, carriers deliver, and armies on the march move one step closer. Building points are only good for the current turn; what the land cannot take goes into great works, up to five, one of each kind except warehouses, which may be raised more than once. Each level of a gold shaft, iron shaft, granary or forge adds a fifth to its production. The overview page shows an exact forecast of the next turn, computed by the server. Now and then fortune strikes after a turn (`events` in `gameconfig.json`, one turn in ten): a bountiful harvest or a rich gold vein, rats in the granary or a fire in the workshops, or bandits on the roads, who leave a realm alone when it keeps one guard for every 20 acres. The forecast never includes fortune.

Armies arrive after 4 of the attacker's turns. Archers on both sides loose first, then infantry and cavalry meet in melee, their blows spread over the whole enemy army, bowmen included, though bowmen strike nobody there; the outcome compares the surviving attack power with the defender's power (soldiers and guards at home and allied garrisons, plus towers and castles), and the stronger the attack, the more guards fall. A won land attack takes up to 15% of the defender's land, razing buildings if no land is free, scaled down by the defender's size against the attacker's (a target with half your land yields half the share; `attack.smallerTargetCaptureExponent`), so large kingdoms gain little from beating small ones; a won plunder attack takes up to 20% of stores, less for every warehouse level. Both sides get a battle report. A kingdom whose defense was broken cannot be attacked for the next 24 turns (`attack.protectionTicksAfterDefeat`), unless it sends an army of its own; armies already on the road still arrive. Spies succeed when they outnumber the target's counter intelligence (its spies and guards) and bring back its stores, people and buildings.

## Coalitions

The game centres on alliances of up to ten kingdoms competing for the alliance ranking. Allies cannot attack each other and write to the whole alliance at once. Every alliance has a war room on its Alliance page: the members' armies on the march, the soldiers they keep standing guard with each other, the freshest intel any member's spies brought back on each enemy, and the members' recent battles. A beaten kingdom is protected only from armies sent after its defeat, so a coalition that times its strikes can hit one enemy several times. The Army page lists allies on the march with a button to join their strike (`joins` on `POST /kingdom/send-attack`): the joining army marches its four turns as usual, then waits at the border and fights right after the ally's army lands, each army in its own battle, however fast either ruler passes turns. One that arrives late fights when it arrives, one whose lead turned back fights on its owner's next turn, and one that has waited four of its owner's turns at the border (`attack.turnsToWaitAtBorder`) fights alone, so an ally who stops playing cannot hold an army there. The leader hears who joined and how each joined battle went, and its steward keeps saying who waits on its army until it lands.

A kingdom's scouts see every army marching on it, with its sender, its size to two figures and how many of the sender's turns it needs to land; the chronicle warns when one sets out, and the war room lists every enemy army marching on a member. An attack takes four turns and help two, so allies who watch can answer in time.

Allies also defend each other. A member sends infantry, bowmen or cavalry to stand guard in an ally's kingdom; they arrive after two of the sender's turns (`common.turnsToReinforce`), count in the host's defense and fight beside its soldiers, sharing the losses by unit type. They stay the sender's: away from home, fed by the sender, called home at once, and sent home when either kingdom leaves the alliance. Battle reports name the allied losses, and the sender hears how every battle its soldiers stood in went.

Allies pass gold through the market, since carriers cannot carry it. A member asks by offering a few pieces of goods at the price it needs (the Alliance page's "Gold between allies" offers one piece for the sum asked), and an ally sends the gold by buying them. The market lists only the cheapest offers of each good, but every member sees all of its allies' offers, however dear, on the Market and Alliance pages (`GET /market/allies`).

## The API

The browser and the acceptance tests drive the same HTTP API, and so can any other client. Every endpoint asks for a signed-in ruler (`Authorization: Bearer <token>` from `POST /auth/authenticate`) except what a visitor needs: `GET /health`, signing in, registering or starting a trial (`POST /auth/authenticate`, `/auth/register`, `/auth/trial`), and the landing page's `GET /game/round` and `GET /game/rankings`; a test calls every endpoint without a token, so a new one is closed unless it is added to that list on purpose. The simulation profile opens `/simulation/**` for the acceptance tests. The server describes the API as OpenAPI at `GET /v3/api-docs` (signed in), and the description is kept in `api/openapi.json`; the browser and the acceptance tests generate their types from that file (`npm run api:types` in each), so a field the server renames or drops breaks their build instead of the game. When the API changes, the server tests rewrite `api/openapi.json` and fail once; regenerate both clients' types and commit the three together. CI checks all three agree. Every field is always present; a field is null only when it is an `Optional` or marked `@Nullable`, and a count map carries every key only when marked `@EveryKey`. Every action answers in one shape, `{ message, success, data }`: `success` says whether it went through, `message` is ready to show a player, and `data` is what it produced (the buildings raised, the turn report, the spy report, a purchase), or null. An action that cannot go through still answers 200 with `success: false`; 401 means no valid token, and 404 that the signed-in kingdom no longer exists.

## The world's database

The world lives in PostgreSQL and outlives every restart and deploy. Locally, `bootRun` starts PostgreSQL from `backend/compose.yaml` and keeps the world in a Docker volume; `docker compose -f backend/compose.yaml down -v` throws it away for a fresh one. In production the `prod` profile connects to the database that `DB_HOST`, `DB_NAME`, `DB_USER` and `DB_PASSWORD` name, and its first start needs an empty database.

Flyway builds the schema from the migrations in `backend/src/main/resources/db/migration`, and Hibernate only checks that the entities match it. A change to an entity needs a new migration beside the others (`V2__what_changed.sql`, never an edit to one already shipped); without it every server test fails on the missing or extra column. Enum columns are plain text, so a new enum value needs no migration. The server tests run on the same PostgreSQL image in a container, each test context in a fresh database migrated as production's would be, and one starts the server twice on one database to check that a restart finds everything as it was. The acceptance tests' worlds alone run in memory, built from the entities, since each is short and thrown away.

The game clock counts ticks, not hours: while the server is down no turns come and the round does not move, for every kingdom alike, so a restart costs nobody anything.

## Test

```bash
cd backend && ./gradlew test
```

```bash
cd browser && npm run build && npm run ci:lint
```

```bash
cd acceptance && npm install && npm test && npm run test:browser
```

CI (`.github/workflows/ci.yml`) runs the server tests, the browser build and lint, and both acceptance suites on every push to master and every pull request, on the same Java and Node versions. Dependabot opens one grouped pull request a week per ecosystem (Actions, Gradle, both npm projects, Docker images, the local PostgreSQL, the dev container) to keep them current. It holds back three major versions on purpose: TypeScript 7, the native compiler, lacks the JavaScript API that openapi-typescript and typescript-eslint need, so both npm projects stay on TypeScript 6; and the server image stays on Java 25, the long-term release, until a move to a newer Java changes the Gradle toolchain and the dev container with it; and PostgreSQL stays on 18, since a new major cannot open an older one's data without an upgrade that has to be planned for production.

## Acceptance

`acceptance/` tests the whole game from the outside, in its own words. It brings the server jar up to date with the backend sources and starts a world of its own for every test file: the jar in the `simulation` profile, with an in-memory database and the wall clock off, so time moves only when a test says so (`POST /simulation/advance`). Nothing it does touches the world you play locally.

Scenarios read like the game, and say nothing about pages or endpoints:

```ts
scenario("an army marches, is seen coming, and wins", async game => {
  const target = await game.foundsAKingdom("Target");
  const attacker = await game.foundsAKingdom("Attacker");
  await attacker.passesTurns(5);
  await attacker.trains({ infantry: 10 });
  await attacker.marchesOn(target, { infantry: 10 });
  await target.seesAnArmyComing(attacker, 10);
  await attacker.passesTurns(4);
  await attacker.hasDefeated(target);
});
```

The layers, top to bottom:

- `scenarios/`: what a ruler does and expects, one file per part of the game (gate, realm, war, trade, council, clock, round).
- `src/dsl/`: the language. `Game` founds kingdoms and lets time pass; `Ruler` holds every verb (`raises`, `marchesOn`, `joinsTheStrike`, `asksAlliesFor`) and every expectation (`hasDefeated`, `isGuardedBy`, `readsInTheChronicle`). An action must go through in full (an order the game fills only in part fails), and an expectation is waited for, so a page catching up is never a failure.
- `src/dsl/driver.ts`: the seam. A driver reaches the game one way and answers in the game's terms.
- `src/drivers/api.ts` plays through the HTTP API with exactly the calls the browser makes; `src/drivers/browser.ts` plays through Chrome on the built pages, filling in and clicking what a player would and reading what they see.

`npm test` runs every scenario through the API, the worlds and the robustness checks below; `npm run test:browser` runs the same scenarios through Chrome, plus `ui/`, the checks only a browser can make (help marks, phone layouts, the steward's links, forms filled in for the player). The browser suite builds the pages and serves them as production does, with `/api` passed to the test's world, and drives the installed Chrome; screenshots of a failed scenario land in `acceptance/test-results`. To play some scenarios only, name them: `npm test -- --test-name-pattern="granary|spy"`; a file whose scenarios are all skipped starts no world. A new rule of the game gets a scenario; a new verb goes into `Ruler` and both drivers.

`worlds/` plays whole worlds with scripted rulers (`economist`, `warlord`, `turtle`, `raider`, `idle`, a `hoarder` who stays small for half the round and then buys land with everything, newcomers who found their kingdom mid-round, and coalitions with a war council) and checks rule invariants for every kingdom on every turn (no negative stocks, no buildings beyond land, marching armies match soldiers away), growth, starvation, battles, bot limits, rankings, and that a balance change does what it claims.

`robustness/` checks that the server stands up to players. `races.test.ts` sends requests at the same moment, as from two tabs or two players reaching for one thing: buyers racing for one offer, a buyer racing the seller's withdrawal, turns passed and gold spent from ten tabs, one kingdom or alliance name claimed twice, the last places in an alliance. Nothing may be spent twice, made out of nothing or lost, and the loser of a race is told so (a refusal, or 409), never a server error. `hostile.test.ts` sends some 900 seeded requests from three rulers, the signed-out and a forger to every endpoint, with numbers below zero and past what an int holds, text where numbers go, other players' names and ids, and bodies that are not JSON; once with no time passing, where the world's gold may only change hands or be spent, and once with turns passed at any weapons share in between. No answer may be a server error and every rule must hold after every batch. A failure names its seed; `KOD_FUZZ_SEED=7 npm test -- --test-name-pattern=hostile` plays that run again, or another one.

`npm run test:long` plays months of the game as configured: three rounds of four weeks, hour by hour, by two rival coalitions and lone rulers who come back at their own pace while the merchants trade. Every rule holds for every kingdom after every tick, and every round ends as promised: the largest kingdom crowned, the round written into the hall of fame with its final rankings, every kingdom starting afresh with nothing left over from the last round, and alliances kept. It takes about five minutes and runs on demand, not in CI.

## Simulate worlds

What the simulations have measured so far, and the balance decisions taken from it, is recorded in [backend/BALANCE.md](backend/BALANCE.md). The tools live in `acceptance/src/simulation` and play through the HTTP edge only, so the server can be rebalanced or rewritten underneath them.

To see what a balance change does, compare the base `gameconfig.json` with overrides:

```bash
cd acceptance && npm run sim -- --players economist,warlord,turtle --turns 30 --set attack.maxLandCaptureRatio=0.25 --compare
```

It prints land, gold, food, buildings, people, army, defense and battles for every ruler, with the change against the base run in brackets; `--out run.json` keeps the full per-turn timelines. A player written `economist@300` founds their kingdom on turn 300, to see how a newcomer catches up.

The game centres on coalitions of up to ten kingdoms competing with each other. To see how coalitions fare, play three groups of ten with the same make-up: a coalition whose warlords strike together through a war council, a coalition whose warlords each pick their own target, and ten kingdoms in no alliance (`--set` and `--compare` work as above):

```bash
cd acceptance && npm run coalitions -- --turns 120
```

`--guard` turns the coordinated coalition's turtles into guardians who send soldiers to allies under threat; `--fund` has its warlords ask for gold on the market and its economists pay half of their spare gold into those asks. `--mirror` makes Horde play exactly as the coordinated coalition does, to see whether one of two equal coalitions runs away with the round. `--uneven` has rulers play in bursts of one to three turns at different ticks, as players who come back at different times; the war council's warlords join the first army's strike so their armies land together, and `--solo-strikes` has them march alone instead, to see what joining is worth.

To see how the game looks as a world unfolds, play one and photograph the built pages along the way (needs `npm install` in `browser/` and an installed Chrome):

```bash
cd acceptance && npm run stages -- --turns 12,30,60
```

The world holds two rival coalitions of four, Order (warlords who strike together, an economist and a guardian) and Horde (a warlord, an economist, a turtle and a raider). Screenshots land in `acceptance/stages`: the landing, sign-in and register pages, a newcomer's first look, an Order warlord at every listed turn, and at the last turn Order's economist with its war room, its guardian and a phone-sized view.
