import assert from "node:assert/strict";
import { test } from "node:test";
import { Player, simulationCredentials, World } from "../src/api/client.ts";
import { startServer } from "../src/server.ts";

// a simulation of an era plays for longer than a token lasts; its rulers stopped at the hour with "GET /kingdom answered
// 401" and the run was lost
test("a scripted ruler whose session ran out signs in again and plays on", async () => {
  const server = await startServer();
  try {
    const world = new World(server.url);
    await world.foundKingdom("Sleeper");
    const sleeper = new Player(server.url, "Sleeper", simulationCredentials("Sleeper"), "a token that ran out");
    assert.equal((await sleeper.kingdom()).name, "Sleeper");
    assert.ok((await sleeper.passTurn()).success, "the ruler could not pass a turn after signing in again");
  } finally {
    await server.stop();
  }
});
