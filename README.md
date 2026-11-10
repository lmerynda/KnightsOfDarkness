# Knights of Darkness

A browser strategy game of dark medieval kingdoms. Each player rules one kingdom: build on land, train workers and soldiers, trade on the market, send carriers to allies, spy on rivals and march armies to take land or plunder stores. Kingdoms swear to alliances, write letters, and compete for land. An alliance wins the era by completing its monument and holding it for three days against every army sent to knock it down; if none does within four months, the alliance holding the most land wins. Then every realm starts again.

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

A new turn is handed to every kingdom every hour, on the hour, and a kingdom stores up to 48, so a ruler can stay away two days without losing one. An era lasts at most 2,880 of those ticks, four months, and ends sooner when a monument wins it. When the clock runs out, the alliance holding the most land wins the era; the hall of fame records it with how the era ended and the final rankings, and every kingdom starts again while alliances stay. All three are set in `gameconfig.json` (`common.newTurnPeriodicity`, `common.maxTurns`, `era.maxLengthInTicks`). Turns come on the wall clock's whole periods, so a restart neither hands out an extra turn nor delays the next. Local play (`bootRun`) speeds the clock up to a turn every 10 seconds with `game.clock.turn-period`, so a local era lasts eight hours; the jar keeps the hourly pace.

An alliance's emperor may found one monument an era in a member's kingdom. Members pay gold, iron, tools and food into its store, and each of its 10 stages rises as soon as the store covers it (5 billion gold, 1.5 million iron and 200,000 tools a stage). Every stage eats gold and food each turn, and a store that cannot pay lets the monument crumble. Everyone sees how far each monument stands, and hears when it is founded, from the half-way mark on, and when it is complete (`monument` in `gameconfig.json`). Only soldiers the members send defend it: no towers, castles, guards or the seat's own army. Any kingdom outside the alliance may march on it, whatever its size and even in peace, and a monument has no protection after a defeat. A won assault takes no land or stores, and knocks down up to 25% of the stages standing, scaled like land capture (`monument.maxRazeRatio`). A monument that stands complete with its upkeep paid for 72 ticks, three days, wins the era for its alliance, whatever the land (`monument.holdTicks`). Everyone sees how many turns it has left, hears when it is complete and once more a day before it wins, and Your turn puts it first for every rival. A won assault in that time, or a turn its store cannot pay, knocks it below complete, and once rebuilt it must stand the whole hold again. [ERA.md](backend/ERA.md) has the design and what is left: simulated rulers that build and assault monuments, and the prices tuned on them.

Build can fill in a plan (Economy, War or Fortress, the land shares the simulated rulers grow on) one building at a time into the largest gap, and Train can staff every free workplace; both only fill in the order, which the player then raises or trains.

The pages are written for players who know the game: they show figures, and Your turn lists only what waits on the ruler: first what will not wait (a rival's complete monument, an army on the way, hunger, unpaid soldiers, a crumbling or unguarded monument, an ally's call for gold, a full turn store), then the turn's own work, with its order worked out so that one click carries it out (the building points that would otherwise be lost, by the plan last chosen on Build, and idle people for the free workplaces). The rules sit in tooltips, behind a `?` beside a title or a dotted word, opened by hovering, focusing or tapping.

## How a turn works

Passing a turn spends one stored turn: people who lack houses leave, farmers bring in the harvest, everyone eats and people who go hungry leave, the other workers produce (small kingdoms get a production bonus that fades by 1,000 land), soldiers are paid a hundredth of their training gold (`common.soldierUpkeepRatio`; those away on the march or standing guard with an ally too) and when the gold falls short soldiers at home desert in proportion to the unpaid share, a tenth of them when nothing is paid, new people arrive into empty houses, carriers deliver, and armies on the march move one step closer. Building points are only good for the current turn; what the land cannot take goes into great works, up to five, one of each kind except warehouses, which may be raised more than once. Each level of a gold shaft, iron shaft, granary or forge adds a fifth to its production. The overview page shows an exact forecast of the next turn, computed by the server. Now and then fortune strikes after a turn (`events` in `gameconfig.json`, one turn in ten): a bountiful harvest or a rich gold vein, rats in the granary or a fire in the workshops, or bandits on the roads, who leave a realm alone when it keeps one guard for every 20 acres. The forecast never includes fortune.

Armies arrive after 4 of the attacker's turns. Archers on both sides loose first, then infantry and cavalry meet in melee, their blows spread over the whole enemy army, bowmen included, though bowmen strike nobody there; the outcome compares the surviving attack power with the defender's power (soldiers and guards at home and allied garrisons), and the stronger the attack, the more guards fall. Towers and castles strengthen only the defenders they have places for, and soldiers still live in barracks. Each tower has places for 50 bowmen (`buildingCapacity.tower`), who loose twice the arrows (`attack.towerFireBonus`), so more of the attackers fall before anyone strikes. Each castle shelters 500 infantry or cavalry (`buildingCapacity.castle`), infantry first, who defend twice as strong and take twice the blows to fall (`attack.castleSoldierBonus`), so a kingdom beaten once still has an army for the next strike. A won land attack takes up to 10% of the defender's land, razing buildings if no land is free, scaled down by the defender's size against the attacker's (a target with half your land yields half the share; `attack.smallerTargetCaptureExponent`), so large kingdoms gain little from beating small ones; a won plunder attack takes up to 20% of stores, less for every warehouse level. Both sides get a battle report. Every kingdom starts at peace, so that everyone can raise an economy first: it cannot be attacked for its first 336 turns, two weeks of the clock (`attack.protectionTicksAtStart`), whether it starts with the era or is founded later. A kingdom whose defense was broken cannot be attacked for the next 24 turns (`attack.protectionTicksAfterDefeat`). Either protection ends when the kingdom sends an army of its own; armies already on the road still arrive. Spies succeed when they outnumber the target's counter intelligence (its spies and guards) and bring back its stores, people and buildings.

## Coalitions

The game centres on alliances of up to ten kingdoms competing for the alliance ranking. Allies cannot attack each other and write to the whole alliance at once. Every alliance has a war room on its Alliance page: the members' armies on the march, the soldiers they keep standing guard with each other, the freshest intel any member's spies brought back on each enemy, and the members' recent battles. A beaten kingdom is protected only from armies sent after its defeat, so a coalition that times its strikes can hit one enemy several times. The Army page lists allies on the march with a button to join their strike (`joins` on `POST /kingdom/send-attack`): the joining army marches its four turns as usual, then waits at the border and fights right after the ally's army lands, each army in its own battle, however fast either ruler passes turns. One that arrives late fights when it arrives, one whose lead turned back fights on its owner's next turn, and one that has waited four of its owner's turns at the border (`attack.turnsToWaitAtBorder`) fights alone, so an ally who stops playing cannot hold an army there. The leader hears who joined and how each joined battle went, and its steward keeps saying who waits on its army until it lands.

A kingdom's scouts see every army marching on it, with its sender, its size to two figures and how many of the sender's turns it needs to land; the chronicle warns when one sets out, and the war room lists every enemy army marching on a member. An attack takes four turns and help two, so allies who watch can answer in time.

Allies also defend each other. A member sends infantry, bowmen or cavalry to stand guard in an ally's kingdom; they arrive after two of the sender's turns (`common.turnsToReinforce`), count in the host's defense and fight beside its soldiers, sharing the losses by unit type. They stay the sender's: away from home, fed by the sender, called home at once, and sent home when either kingdom leaves the alliance. Battle reports name the allied losses, and the sender hears how every battle its soldiers stood in went.

Allies pass gold through the market, since carriers cannot carry it. A member asks by offering a few pieces of goods at the price it needs (the Alliance page's "Gold between allies" offers one piece for the sum asked), and an ally sends the gold by buying them. The market lists only the cheapest offers of each good, but every member sees all of its allies' offers, however dear, on the Market and Alliance pages (`GET /market/allies`).

## The API

The browser and the acceptance tests drive the same HTTP API, and so can any other client. Every endpoint asks for a signed-in ruler (`Authorization: Bearer <token>` from `POST /auth/authenticate`) except what a visitor needs: `GET /health`, signing in, registering or starting a trial (`POST /auth/authenticate`, `/auth/register`, `/auth/trial`), and the landing page's `GET /game/era`, `GET /game/monuments` and `GET /game/rankings`; a test calls every endpoint without a token, so a new one is closed unless it is added to that list on purpose. The simulation profile opens `/simulation/**` for the acceptance tests. The server describes the API as OpenAPI at `GET /v3/api-docs` (signed in), and the description is kept in `api/openapi.json`; the browser and the acceptance tests generate their types from that file (`npm run api:types` in each), so a field the server renames or drops breaks their build instead of the game. When the API changes, the server tests rewrite `api/openapi.json` and fail once; regenerate both clients' types and commit the three together. CI checks all three agree. Every field is always present; a field is null only when it is an `Optional` or marked `@Nullable`, and a count map carries every key only when marked `@EveryKey`. Every action answers in one shape, `{ message, success, data }`: `success` says whether it went through, `message` is ready to show a player, and `data` is what it produced (the buildings raised, the turn report, the spy report, a purchase), or null. An action that cannot go through still answers 200 with `success: false`; 401 means no valid token, and 404 that the signed-in kingdom no longer exists.

## The world's database

The world lives in PostgreSQL and outlives every restart and deploy. Locally, `bootRun` starts PostgreSQL from `backend/compose.yaml` and keeps the world in a Docker volume; `docker compose -f backend/compose.yaml down -v` throws it away for a fresh one. In production the `prod` profile connects to the database that `DB_HOST`, `DB_NAME`, `DB_USER` and `DB_PASSWORD` name, and its first start needs an empty database.

Flyway builds the schema from the migrations in `backend/src/main/resources/db/migration`, and Hibernate only checks that the entities match it. A change to an entity needs a new migration beside the others (`V2__what_changed.sql`, never an edit to one already shipped); without it every server test fails on the missing or extra column. Enum columns are plain text, so a new enum value needs no migration. The server tests run on the same PostgreSQL image in a container, each test context in a fresh database migrated as production's would be, and one starts the server twice on one database to check that a restart finds everything as it was. The acceptance tests' worlds alone run in memory, built from the entities, since each is short and thrown away.

The game clock counts ticks, not hours: while the server is down no turns come and the era does not move, for every kingdom alike, so a restart costs nobody anything.

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

- `scenarios/`: what a ruler does and expects, one file per part of the game (gate, realm, war, trade, council, clock, era, monument, assault, winning).
- `src/dsl/`: the language. `Game` founds kingdoms and lets time pass; `Ruler` holds every verb (`raises`, `marchesOn`, `joinsTheStrike`, `asksAlliesFor`) and every expectation (`hasDefeated`, `isGuardedBy`, `readsInTheChronicle`). An action must go through in full (an order the game fills only in part fails), and an expectation is waited for, so a page catching up is never a failure.
- `src/dsl/driver.ts`: the seam. A driver reaches the game one way and answers in the game's terms.
- `src/drivers/api.ts` plays through the HTTP API with exactly the calls the browser makes; `src/drivers/browser.ts` plays through Chrome on the built pages, filling in and clicking what a player would and reading what they see.

`npm test` runs every scenario through the API, the worlds and the robustness checks below; `npm run test:browser` runs the same scenarios through Chrome, plus `ui/`, the checks only a browser can make (help marks, phone layouts, the steward's links, forms filled in for the player). The browser suite builds the pages and serves them as production does, with `/api` passed to the test's world, and drives the installed Chrome; screenshots of a failed scenario land in `acceptance/test-results`. To play some scenarios only, name them: `npm test -- --test-name-pattern="granary|spy"`; a file whose scenarios are all skipped starts no world. A new rule of the game gets a scenario; a new verb goes into `Ruler` and both drivers.

`worlds/` plays whole worlds with scripted rulers (`economist`, `warlord`, `turtle`, `raider`, `idle`, a `hoarder` who stays small for half the era and then buys land with everything, newcomers who found their kingdom mid-era, coalitions with a war council, and coalitions of specialists who trade with each other) and checks rule invariants for every kingdom on every turn (no negative stocks, no buildings beyond land, marching armies match soldiers away), growth, starvation, battles, bot limits, rankings, and that a balance change does what it claims.

`robustness/` checks that the server stands up to players. `races.test.ts` sends requests at the same moment, as from two tabs or two players reaching for one thing: buyers racing for one offer, a buyer racing the seller's withdrawal, turns passed and gold spent from ten tabs, one kingdom or alliance name claimed twice, the last places in an alliance. Nothing may be spent twice, made out of nothing or lost, and the loser of a race is told so (a refusal, or 409), never a server error. `hostile.test.ts` sends some 900 seeded requests from three rulers, the signed-out and a forger to every endpoint, with numbers below zero and past what an int holds, text where numbers go, other players' names and ids, and bodies that are not JSON; once with no time passing, where the world's gold may only change hands or be spent, and once with turns passed at any weapons share in between. No answer may be a server error and every rule must hold after every batch. A failure names its seed; `KOD_FUZZ_SEED=7 npm test -- --test-name-pattern=hostile` plays that run again, or another one.

`npm run test:long` plays months of the game as configured: two eras, hour by hour, by two rival coalitions and lone rulers who come back at their own pace while the merchants trade. In the first, one coalition founds a monument a month in and pays for it, priced in gold alone so that one economist can raise it, and wins the era by it before the clock runs out; in the second nobody builds one and the clock ends it after four months. Every rule holds for every kingdom after every tick, and every era ends as promised: by the monument or else the alliance with the most land winning, the era written into the hall of fame with its winner, its ending and its final rankings, every kingdom starting afresh with nothing left over from the last era, and alliances kept. It takes about 14 minutes and runs on demand, not in CI.

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

`--guard` turns the coordinated coalition's turtles into guardians who send soldiers to allies under threat; `--fund` has its warlords ask for gold on the market and its economists pay half of their spare gold into those asks. `--mirror` makes Horde play exactly as the coordinated coalition does, to see whether one of two equal coalitions runs away with the era. `--uneven` has rulers play in bursts of one to three turns at different ticks, as players who come back at different times; the war council's warlords join the first army's strike so their armies land together, and `--solo-strikes` has them march alone instead, to see what joining is worth.

Real coalitions try to feed, tool and arm themselves, so most of their market should stay inside them; others bet on buying a good cheaply instead of making it, and a rival can answer by buying that good off the market. To see how these play out, play coalitions of specialists, each buying from allies first and from anyone else for the rest:

- gold producers buy what they eat and train with;
- farmers (`food`) sell food;
- smiths make tools or weapons, whichever sells, from bought iron;
- iron miners sell iron;
- warriors arm an army with bought weapons, keep half of it at home as a guard of bowmen and infantry, and strike with the rest on the largest kingdom within reach whose defense it clearly beats.

A run shorter than the peace sees no war, so a short run about war adds `--set attack.protectionTicksAtStart=0`. Every member but the warrior keeps a garrison of bowmen and infantry in barracks on 1% of its land, with towers and castles that have a place for all of them, and builds them before the rest of its plan; each strike it lost in the last 24 turns adds another 1%, up to 4%.

Every `--coalition` gives one coalition's make-up. `corner:iron` is a member who buys up every piece of iron rivals and merchants offer and asks twice the merchants' price for it; the coalition then passes its iron to its own members by carriers, billed at the merchants' price, so only rivals pay the doubled price:

```bash
cd acceptance && npm run economy -- --turns 120 --coalition Guild=gold,gold,gold,corner:iron,food,smith,smith,iron,warrior --coalition Hanse=gold,gold,gold,gold,gold,food,smith,smith,warrior --set bots.prices.weapons=250 --compare
```

It prints every kingdom's land, gold and army, each coalition's land with its craftsmen's land against its gold producers', and the pieces of every good each coalition bought from allies, from the merchant bots and from other kingdoms. The bots' asking prices (`bots.prices` in `gameconfig.json`) anchor what goods are worth to everyone else. `--progress days.jsonl` appends one line per kingdom every 24 turns while the run plays: its coalition, land, gold, army, defense, soldiers' pay and next turn's production. A run as long as an era (2,880 turns, about an hour for four coalitions of nine) can then be read as it goes and survives a crash.

`--monuments` plays the era's endgame and stops when the era ends: `--found Name=0.2` lets a coalition found its monument once its land leads the other alliances' average by 20% (from day 30, `--found-from`), its members pay in and stand guard, and the warriors of every other coalition (or those named by `--assault`) march on it from half its stages, learning from their battle reports how strong they must be; `--band` makes rivals march together on the same turns. The report tells how the era ended, who won, and every assault with its attack and defense; `--progress` adds each alliance's lead and each monument's stages and garrison every day.

To see where a world spends its time, set `KOD_PROFILE=calls.json` for every endpoint's calls, wall time and bytes; `KOD_JAVA_OPTS="-XX:StartFlightRecording=duration=300s,filename=server.jfr,settings=profile"` for a flight recording of the server (read it with `jfr view hot-methods`); `KOD_SERVER_LOG=server.log` for the whole server log; and `node --cpu-prof` for the harness itself. Measured on 2026-11-09 over 240 turns of four coalitions of nine: a world is its requests, about 1,850 a turn (51 a ruler, 15 of them rereading its own kingdom), at about 0.9 ms each on an idle machine. The server's share of that is mostly Hibernate and H2 (43%), Spring MVC, Jackson and Tomcat (34%) and security (9%), with the game's own rules at 5%; Node's fetch takes about as long again. Worlds started side by side share the machine: nine at once make every request three times slower. `runWorld(..., { atOnce: true })` plays every ruler's turn at the same moment, as players do, and a write that loses a race is sent again as the server's 409 asks: one world plays 3.5 times faster, and six side by side about twice as fast, though the market, met by everyone at once, leaves every coalition 3 to 7% less land.

To see how the game looks as a world unfolds, play one and photograph the built pages along the way (needs `npm install` in `browser/` and an installed Chrome):

```bash
cd acceptance && npm run stages -- --turns 12,30,60
```

The world holds two rival coalitions of four, Order (warlords who strike together, an economist and a guardian) and Horde (a warlord, an economist, a turtle and a raider). Screenshots land in `acceptance/stages`: the landing, sign-in and register pages, a newcomer's first look, an Order warlord at every listed turn, and at the last turn Order's economist with its war room, its guardian and a phone-sized view.
