import assert from "node:assert/strict";
import { test } from "node:test";
import { playEra, story, weakerRivals, wonByMonument } from "./eras.ts";

// Target 3: one rival coalition alone does not stop a coalition about 20% ahead, which wins the era by its monument.
test("one rival alone does not stop a 20% lead, which wins the era by its monument", { timeout: 3 * 60 * 60_000 }, async () => {
  const era = await playEra(weakerRivals, 0.18, ["Bay"]);
  assert.ok(wonByMonument(era, "Lead"), story(era));
});
