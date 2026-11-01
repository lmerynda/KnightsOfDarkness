# Knights of Darkness

A browser strategy game of dark medieval kingdoms. Each player rules one kingdom: build on land, train workers and soldiers, trade on the market, send carriers to allies, spy on rivals and march armies to take land or plunder stores. Kingdoms swear to alliances, write letters, and compete for land until the round ends; then the largest kingdom and alliance are remembered and every realm starts again.

- `backend/`: Spring Boot game server (Java 21, Gradle). Game rules live in `kingdom/model`, numbers in `src/main/resources/gameconfig.json`.
- `browser/`: React and Vite client. Art and its prompts are in `browser/art` and `browser/public/art`.
- `simulation/`: a harness that plays whole worlds against the server over HTTP, for invariants, balance checks and A/B balance reports.

## Run it locally

Two terminals:

```bash
cd backend && ./gradlew bootRun
```

```bash
cd browser && npm install && npm run dev
```

Open http://localhost:3000. Locally the server uses an in-memory H2 database, so every restart begins a fresh world. Local development, and only local development (`game.seed-dev-accounts`), seeds two player kingdoms in the alliance Legion, both with a small garrison and spies:

| Sign in as | Password |
|---|---|
| `uprzejmy` | `uprzejmy_password` |
| `umbar` | `umbar_password` |

You can also found a new kingdom from the landing page. Four merchant bots (FarmerBot, GoldMinerBot, IronMinerBot, BlacksmithBot) trade on the market, can be attacked and spied on, and are not ranked. They stop growing at 400 acres and keep their stores and offers under the caps in `gameconfig.json` (`bots`).

A new turn is handed to every kingdom every 10 seconds (up to 36 stored) and a round lasts 4,320 of those ticks, about 12 hours; both are set in `gameconfig.json` (`common.newTurnPeriodicity`, `round.lengthInTicks`).

## How a turn works

Passing a turn spends one stored turn: people who lack houses leave, farmers bring in the harvest, everyone eats and people who go hungry leave, the other workers produce (small kingdoms get a production bonus that fades by 1,000 land), new people arrive into empty houses, carriers deliver, and armies on the march move one step closer. Building points are only good for the current turn. The overview page shows an exact forecast of the next turn, computed by the server. Now and then fortune strikes after a turn (`events` in `gameconfig.json`, one turn in ten): a bountiful harvest or a rich gold vein, rats in the granary or a fire in the workshops, or bandits on the roads, who leave a realm alone when it keeps one guard for every 20 acres. The forecast never includes fortune.

Armies arrive after 4 of the attacker's turns. Archers on both sides loose first, then infantry and cavalry meet in melee, their blows spread over the whole enemy army, bowmen included, though bowmen strike nobody there; the outcome compares the surviving attack power with the defender's power (soldiers and guards at home and allied garrisons, plus towers and castles), and the stronger the attack, the more guards fall. A won land attack takes up to 15% of the defender's land, razing buildings if no land is free; a won plunder attack takes up to 20% of stores, less for every warehouse level. Both sides get a battle report. A kingdom whose defense was broken cannot be attacked for the next 24 turns (`attack.protectionTicksAfterDefeat`), unless it sends an army of its own; armies already on the road still arrive. Spies succeed when they outnumber the target's counter intelligence (its spies and guards) and bring back its stores, people and buildings.

## Coalitions

The game centres on alliances of up to ten kingdoms competing for the alliance ranking. Allies cannot attack each other and write to the whole alliance at once. Every alliance has a war room on its Alliance page: the members' armies on the march, the soldiers they keep standing guard with each other, the freshest intel any member's spies brought back on each enemy, and the members' recent battles. Armies that land on the same turn strike together, and a beaten kingdom is protected only from armies sent after its defeat, so a coalition that times its strikes can hit one enemy several times; the Army page lists allies on the march with a button to join their strike.

A kingdom's scouts see every army marching on it, with its sender, its size to two figures and how many of the sender's turns it needs to land; the chronicle warns when one sets out, and the war room lists every enemy army marching on a member. An attack takes four turns and help two, so allies who watch can answer in time.

Allies also defend each other. A member sends infantry, bowmen or cavalry to stand guard in an ally's kingdom; they arrive after two of the sender's turns (`common.turnsToReinforce`), count in the host's defense and fight beside its soldiers, sharing the losses by unit type. They stay the sender's: away from home, fed by the sender, called home at once, and sent home when either kingdom leaves the alliance. Battle reports name the allied losses, and the sender hears how every battle its soldiers stood in went.

Allies pass gold through the market, since carriers cannot carry it. A member asks by offering a few pieces of goods at the price it needs (the Alliance page's "Gold between allies" offers one piece for the sum asked), and an ally sends the gold by buying them. The market lists only the cheapest offers of each good, but every member sees all of its allies' offers, however dear, on the Market and Alliance pages (`GET /market/allies`).

## Test

```bash
cd backend && ./gradlew test
```

```bash
cd browser && npm run build && npm run ci:lint
```

Black-box scenarios drive the installed Chrome against the real server; they start the backend and the dev server when they are not already running:

```bash
cd browser && npm run test:e2e
```

## Simulate worlds

`simulation/` drives the game through its HTTP edge only, so the server can be rebalanced or rewritten underneath it. It brings the shipped server jar up to date with the backend sources and starts it in the `simulation` profile, where the wall clock is off and time moves only through `POST /simulation/advance`. Kingdoms are founded through the public sign-up and played by scripted rulers: `economist`, `warlord`, `turtle` and `idle`.

```bash
cd simulation && npm install && npm test
```

The scenarios check rule invariants for every kingdom on every turn (no negative stocks, no buildings beyond land, marching armies match soldiers away), economic growth, starvation, armies, battles, bot limits and rankings, and that a balance change does what it claims. A run takes about two minutes.

To see what a balance change does, compare the base `gameconfig.json` with overrides:

```bash
cd simulation && npm run sim -- --players economist,warlord,turtle --turns 30 --set attack.maxLandCaptureRatio=0.25 --compare
```

It prints land, gold, food, buildings, people, army, defense and battles for every ruler, with the change against the base run in brackets; `--out run.json` keeps the full per-turn timelines.

The game centres on coalitions of up to ten kingdoms competing with each other. To see how coalitions fare, play three groups of ten with the same make-up: a coalition whose warlords strike together through a war council, a coalition whose warlords each pick their own target, and ten kingdoms in no alliance (`--set` and `--compare` work as above):

```bash
cd simulation && npm run coalitions -- --turns 120
```

`--guard` turns the coordinated coalition's turtles into guardians who send soldiers to allies under threat; `--fund` has its warlords ask for gold on the market and its economists pay half of their spare gold into those asks.

To see how the game looks as a world unfolds, play one and photograph the real browser UI along the way (needs `npm install` in `browser/` and an installed Chrome; the UI runs on port 3001):

```bash
cd simulation && npm run stages -- --turns 12,30,60
```

The world holds two rival coalitions of four, Order (warlords who strike together, an economist and a guardian) and Horde (a warlord, an economist, a turtle and a raider). Screenshots land in `simulation/stages`: the landing, sign-in and register pages, a newcomer's first look, an Order warlord at every listed turn, and at the last turn Order's economist with its war room, its guardian and a phone-sized view.
