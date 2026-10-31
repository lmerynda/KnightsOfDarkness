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

Passing a turn spends one stored turn: people who lack houses leave, farmers bring in the harvest, everyone eats and people who go hungry leave, the other workers produce (small kingdoms get a production bonus that fades by 1,000 land), new people arrive into empty houses, carriers deliver, and armies on the march move one step closer. Building points are only good for the current turn. The overview page shows an exact forecast of the next turn, computed by the server.

Armies arrive after 4 of the attacker's turns. Archers on both sides loose first, then units meet in melee; the outcome compares the surviving attack power with the defender's power (soldiers and guards at home, plus towers and castles), and the stronger the attack, the more guards fall. A won land attack takes up to 10% of the defender's land, razing buildings if no land is free; a won plunder attack takes up to 20% of stores, less for every warehouse level. Both sides get a battle report. Spies succeed when they outnumber the target's counter intelligence (its spies and guards) and bring back its stores, people and buildings.

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

`simulation/` drives the game through its HTTP edge only, so the server can be rebalanced or rewritten underneath it. It starts the shipped server jar (building it when missing) in the `simulation` profile, where the wall clock is off and time moves only through `POST /simulation/advance`. Kingdoms are founded through the public sign-up and played by scripted rulers: `economist`, `warlord`, `turtle` and `idle`.

```bash
cd simulation && npm install && npm test
```

The scenarios check rule invariants for every kingdom on every turn (no negative stocks, no buildings beyond land, marching armies match soldiers away), economic growth, starvation, armies, battles, bot limits and rankings, and that a balance change does what it claims. A run takes about two minutes.

To see what a balance change does, compare the base `gameconfig.json` with overrides:

```bash
cd simulation && npm run sim -- --players economist,warlord,turtle --turns 30 --set attack.maxLandCaptureRatio=0.2 --compare
```

It prints land, gold, food, buildings, people, army, defense and battles for every ruler, with the change against the base run in brackets; `--out run.json` keeps the full per-turn timelines.
