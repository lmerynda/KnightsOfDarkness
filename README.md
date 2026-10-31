# Knights of Darkness

A browser strategy game of dark medieval kingdoms. Each player rules one kingdom: build on land, train workers and soldiers, trade on the market, send carriers to allies, spy on rivals and march armies to take land or plunder stores. Kingdoms swear to alliances, write letters, and compete for land until the round ends; then the largest kingdom and alliance are remembered and every realm starts again.

- `backend/`: Spring Boot game server (Java 21, Gradle). Game rules live in `kingdom/model`, numbers in `src/main/resources/gameconfig.json`.
- `browser/`: React and Vite client. Art and its prompts are in `browser/art` and `browser/public/art`.

## Run it locally

Two terminals:

```bash
cd backend && ./gradlew bootRun
```

```bash
cd browser && npm install && npm run dev
```

Open http://localhost:3000. Locally the server uses an in-memory H2 database, so every restart begins a fresh world. It seeds two player kingdoms in the alliance Legion, both with a small garrison and spies:

| Sign in as | Password |
|---|---|
| `uprzejmy` | `uprzejmy_password` |
| `umbar` | `umbar_password` |

You can also found a new kingdom from the landing page. Four merchant bots (FarmerBot, GoldMinerBot, IronMinerBot, BlacksmithBot) trade on the market, can be attacked and spied on, and are not ranked.

A new turn is handed to every kingdom every 10 seconds (up to 36 stored) and a round lasts 4,320 of those ticks, about 12 hours; both are set in `gameconfig.json` (`common.newTurnPeriodicity`, `round.lengthInTicks`).

## How a turn works

Passing a turn spends one stored turn: people who lack houses or food leave, workers produce (small kingdoms get a production bonus that fades by 1,000 land), new people arrive into empty houses, carriers deliver, and armies on the march move one step closer. Building points are only good for the current turn. The overview page shows an exact forecast of the next turn, computed by the server.

Armies arrive after 4 of the attacker's turns. Archers on both sides loose first, then units meet in melee; the outcome compares the surviving attack power with the defender's power (soldiers and guards at home, plus towers and castles). A won land attack takes up to 10% of the defender's land, razing buildings if no land is free; a won plunder attack takes up to 20% of stores, less for every warehouse level. Both sides get a battle report. Spies succeed when they outnumber the target's counter intelligence (its spies and guards) and bring back its stores, people and buildings.

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
