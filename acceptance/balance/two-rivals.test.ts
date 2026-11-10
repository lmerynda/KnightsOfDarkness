import assert from "node:assert/strict";
import { test } from "node:test";
import { playEra, story, weakerRivals, wonByMonument } from "./eras.ts";

// Target 3: two rival coalitions that band together stop a coalition about 20% ahead from winning by its monument.
test("two rivals banded together stop a 20% lead's monument", { timeout: 3 * 60 * 60_000 }, async () => {
  const era = await playEra(weakerRivals, 0.18);
  assert.ok(!wonByMonument(era, "Lead"), story(era));
});
