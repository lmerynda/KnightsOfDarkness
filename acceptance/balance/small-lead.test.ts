import assert from "node:assert/strict";
import { test } from "node:test";
import { evenRivals, finalLead, playEra, story, wonByMonument } from "./eras.ts";

// Target 1: a coalition about 10% ahead that founds a monument does not win the era with it, and ends it further behind
// than it began: the rivals raze it, and paying for it and guarding it starve the builder's economy.
test("a small lead that founds a monument does not win the era with it, and falls behind", { timeout: 3 * 60 * 60_000 }, async () => {
  const era = await playEra(evenRivals, 0.1);
  assert.ok(!wonByMonument(era, "Lead"), story(era));
  assert.ok(finalLead(era, "Lead") < era.leadAtFounding, `Lead ended at a lead of ${Math.round(finalLead(era, "Lead") * 100)}%; ${story(era)}`);
});
