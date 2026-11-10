import assert from "node:assert/strict";
import { playEconomy, ticksPerDay, type EraOutcome } from "../src/simulation/economyWorld.ts";
import { parseCraft, type Craft } from "../src/simulation/trade.ts";

// The worlds the balance targets of backend/ERA.md are measured on, as the game is configured: a leading coalition,
// Lead, against two rival coalitions, Bay and Cog, over a whole era. Lead is the same in every world; how far ahead it
// is depends on how strong its rivals are. It founds its monument from day 30 once it leads by the share given, its
// members pay in and keep most of their soldiers on guard, and the rivals march on it as whole coalitions, each as one
// army, landing on the same turns.

const crafts = (list: string): Craft[] => list.split(",").map(parseCraft);

const leader = crafts("gold,gold,gold,gold,gold,food,smith,smith,iron,warrior");

/** rivals of nine, about 10% behind Lead by day 30 */
export const evenRivals = crafts("gold,gold,gold,gold,food,smith,smith,iron,warrior");
/** rivals of eight, about 20% behind */
export const weakerRivals = crafts("gold,gold,gold,food,smith,smith,iron,warrior");
/** rivals of seven, about 40% behind */
export const weakRivals = crafts("gold,gold,food,smith,smith,iron,warrior");

export type Era = EraOutcome & { foundedOnTurn: number; leadAtFounding: number };

/** one era of Lead against two rivals of the given make-up, which assault Lead's monument unless only Bay is named */
export const playEra = async (rivals: Craft[], foundAt: number, assault: string[] | null = null): Promise<Era> => {
  const { era, run } = await playEconomy({
    coalitions: [
      ["Lead", leader],
      ["Bay", rivals],
      ["Cog", rivals],
    ],
    turns: 120 * ticksPerDay,
    overrides: {},
    monuments: { found: new Map([["Lead", foundAt]]), foundFromDay: 30, assault, band: true, payShare: 0.5, guardShare: 0.9 },
  });
  assert.deepEqual(run.violations, []);
  assert.ok(era, "the world played no era");
  const founded = era.events.find(event => event.alliance === "Lead" && event.what.startsWith("founded"));
  assert.ok(founded, "Lead never founded its monument");
  const day = [...era.days].reverse().find(entry => entry.turn <= founded.turn) ?? era.days[0];
  return { ...era, foundedOnTurn: founded.turn, leadAtFounding: day.alliances.Lead.lead };
};

export const wonByMonument = (era: Era, alliance: string): boolean => era.endedBy === "monument" && era.winner === alliance;

/** the alliance's land lead on the era's last day */
export const finalLead = (era: Era, alliance: string): number => era.days[era.days.length - 1].alliances[alliance].lead;

/** what happened, for a failed target */
export const story = (era: Era): string =>
  `founded on day ${Math.floor(era.foundedOnTurn / ticksPerDay)} at a lead of ${Math.round(era.leadAtFounding * 100)}%; ` +
  `the era ended ${era.endedOnTurn === null ? "never" : `on day ${Math.floor(era.endedOnTurn / ticksPerDay)} by ${era.endedBy}, won by ${era.winner}`}; ` +
  `Lead's monument by day: ${era.days.filter((_, index) => index % 10 === 0).map(day => `${Math.floor(day.turn / ticksPerDay)}:${day.monuments.find(monument => monument.alliance === "Lead")?.stages ?? "-"}`).join(" ")}`;
