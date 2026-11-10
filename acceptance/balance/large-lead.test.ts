import assert from "node:assert/strict";
import { test } from "node:test";
import { finalLead, playEra, story, weakRivals } from "./eras.ts";

// Target 2, as settled on 2026-11-10: a coalition about 40% ahead wins the era while both rivals assault its monument,
// and stays ahead in land. In four measured eras it completed its monument in two and won by it in one, some twelve
// weeks after founding; in the others the clock ended the era and it won as the alliance with the most land. One era
// cannot say which way it goes, so this checks the win and the lead.
test("a large lead wins the era while two rivals assault its monument, and stays ahead", { timeout: 3 * 60 * 60_000 }, async () => {
  const era = await playEra(weakRivals, 0.35);
  assert.equal(era.winner, "Lead", story(era));
  assert.ok(finalLead(era, "Lead") > 0, `Lead ended at a lead of ${Math.round(finalLead(era, "Lead") * 100)}%; ${story(era)}`);
});
