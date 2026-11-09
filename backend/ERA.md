# Eras and the coalition monument

Designed on 2026-11-07. An era ends when a coalition completes its monument and holds it. All of that is built (steps 3 to 6, 2026-11-08 and 2026-11-09): a monument can be founded, paid for, fed, watched, garrisoned and assaulted, and one that stands complete and fed for `monument.holdTicks` wins the era for its alliance. Otherwise the clock ends the era after 2,880 ticks (four months) and the alliance with the most land wins it. No simulated ruler builds or assaults a monument yet, and the prices are the starting ones: step 7.

## Why

- A coalition far ahead of the rest should be able to win, and the game then starts a new era. In the original game an era lasted 2 to 4 months.
- Winning takes a monument: a huge, standing effort that the whole economy must feed and that rivals can attack. Only a coalition with a big lead survives building one.
- The coalitions behind fight back by banding together. Coalitions 2 and 3 strike number 1, and the monument is the obvious target.
- So a lead in land is not a balance flaw in itself. One is if a small lead is enough to finish a monument, or if rivals who band together cannot stop a large one. The balance tests below check exactly that.

## What exists to build on

- Alliances of up to 10 kingdoms (`common.allianceMaxMembers`). Allies cannot attack each other, and each alliance has a war room.
- Joined strikes (`joins` on `POST /kingdom/send-attack`): armies land one right after the other.
- Reinforcements: soldiers stand guard with an ally after 2 turns. The sender pays and feeds them.
- Attack types `land` and `economy` (plunder), with 24 turns of protection after a defeat.
- Kingdoms are ranked by land and alliances by their members' land (`RankingCalculator`). An era's winning alliance, its ending and its final rankings go into the hall of fame, and `EraReset` starts every kingdom anew while alliances are kept.
- Great works belong to one kingdom each. Nothing is built at the alliance level.

## The rules

### The era

- `round` became `era` everywhere: config, entity, API, browser, docs. Nothing keeps the old name working. Past balance entries that describe rounds are left as measured.
- An era ends when a monument stands complete for `monument.holdTicks`. The alliance that built it wins.
- A clock cap ends a stalled era at `era.maxLengthInTicks`: 2,880 ticks, four months of hourly turns. The alliance with the most land wins it. See decision 1.
- The hall of fame records the winning alliance, how the era ended (monument or clock), and the final rankings.
- The reset stays as it is: every kingdom starts again, alliances are kept, and every monument is gone.

### Raising a monument

- **One per alliance per era.** The emperor founds it in a member's kingdom, the seat. It does not use land or building points. Any alliance may try: the cost is what limits it.
- **Stages.** It has `monument.stages` stages, 10 to start with. Each stage costs `monument.stageCost` in gold, iron and tools. That draws on every craft at once: gold producers, the iron miner and the smiths. A stage is 5 billion gold, more than a kingdom's store can hold, so a monument's store and costs count in `long`.
- **Contributions.** Members pay resources straight into the monument's store with `POST /alliance/monument/contribute`. A stage is raised as soon as the store covers its cost.
  - I'd skip carriers. Carrying would add a delay and another strike target, but it isn't needed to make the monument costly.
- **Upkeep.** Each tick the monument eats `monument.upkeepPerStage` in gold and food for every stage built, so a nearly finished monument costs the most to keep. When the store can't pay all of it, it pays nothing and loses `monument.decayRatio` of a stage that tick, 5% to start with. Holding a monument without feeding it decays it. Repairing a crumbled stage costs only the share that crumbled. Its members hear once when it starts to crumble. A stage that has fallen rises again with the next paid tick if the store covers it, so goods held back in the store rebuild a monument without anyone paying in again.
- **Visibility.** Everyone sees every monument: its alliance, seat, stage, and the size of its standing garrison to two figures, as with marching armies. The rankings and the landing page show this. The chronicle tells every kingdom when a monument is founded, at each stage from the half-way mark, and when it is complete. A stage repaired after crumbling is not news again unless a whole stage was lost. Rivals must know in time to band together. Only the alliance's members see its store.
- **The seat.** A member's kingdom. If that member leaves, the seat moves to the emperor, who leaves last, and armies on the march follow it, so leaving is no way to dodge them. A monument falls with its alliance.

### Defending it

- **The garrison** is made up only of soldiers that members send. Infantry, bowmen and cavalry go to the monument the way reinforcements do: they arrive after `common.turnsToReinforce` turns, and the sender pays, feeds and recalls them (`POST /alliance/monument/garrison`, `POST /kingdom/recall-reinforcements`). They go home when their ruler leaves the alliance, and when the monument falls or the era ends.
- **No other defense counts.** No towers, castles or guards, and not the seat's own army at home. The seat's ruler can send soldiers to the monument like any other member.

### Assaulting it

- **New attack type `monument`.** Any kingdom outside the alliance may march on a monument, naming its seat as the destination of `POST /kingdom/send-attack`. The march takes `attack.turnsToArrive`, and `joins` works as for any strike, though an army joins only a strike on the same thing: the kingdom or its monument. Every member hears of an army marching on the monument, and the seat and the war room see it coming. An army that finds no enemy monument when it lands, because it fell or its alliance took the attacker in, comes home without a fight.
- **Usual limits lifted.** `minTargetLandRatio` doesn't apply, the seat's protection doesn't cover the monument, peace included, and a monument gets no protection after a defeat. Rivals can strike it again and again: that's the counterplay.
- **The battle** is the usual one: archers first, then melee. The defender is the garrison alone, and both sides take losses as usual.
- **A won assault** knocks down up to `monument.maxRazeRatio` (25%) of the stages standing, scaled by dominance as land capture is. It takes no land and no stores, and everyone hears. See decision 2. Since dominance is full against nobody, a single soldier takes the full share of an unguarded monument: a garrison is not optional. Step 7 checks whether that needs softening.
- **The cost to attackers:** their armies are away from home, so the leader's warriors can strike back at their homes.

### Winning

- A completed monument must stand `monument.holdTicks` (72 ticks, three days) with its upkeep paid. That gives rivals one last window to attack it.
- A won assault in that window knocks it down below the final stage, and the hold starts again once it is rebuilt. So does a tick its store cannot pay, since it crumbles.
- Everyone sees how many ticks a complete monument has left (`ticksToWin` on `GET /game/monuments`), hears when it is complete with the length of the hold, and hears once more a day before it wins. Your turn puts a rival's complete monument first, with a link to march on it, and tells the members theirs is winning.
- The hold counts ticks that a monument stood complete through, with its upkeep paid. One completed between ticks counts from the next tick on; one rebuilt by the tick itself counts from the tick after.
- If two monuments reach the end of their hold on the same tick, the one that has stood longer wins. If both have stood equally long, the first alliance name in alphabetical order wins.

### Coalitions of coalitions

- **No pact feature at first.** Rival alliances coordinate by letter, which every kingdom can already write to every other.
- **Joined strikes stay within an alliance**, but two alliances don't need them here. A monument has no protection after a defeat, and its garrison regrows only as members send more soldiers, so assaults landing a few turns apart each count.
- **Add pacts only if the simulations show a need:** if alliances that strike separately can't stop a leader they would stop with joined strikes. A pact would allow joined strikes across the two alliances and a shared enemy list in both war rooms. See decision 3.

## Balance targets

These start as acceptance worlds in `acceptance/worlds/`, then become `BALANCE.md` entries as the numbers are tuned. The lead is measured as the alliance's land against the average of the other alliances, so a lead of 40% means 1.4 times the average.

1. **A small lead fails.** An alliance at a 10% lead that founds a monument does not complete it within the era, and ends the era smaller than it would have without trying. Either rivals raze it, or the upkeep and garrison starve its economy.
2. **A large lead wins.** An alliance at a 40% lead completes and holds a monument within 4 to 6 weeks of founding it, while two rival alliances assault it.
3. **Banding together works.** Two rival alliances that both assault a monument stop an alliance at a 20% lead. One rival alone does not.
4. **Eras end by the monument.** A typical world of four alliances ends its era by monument within 2 to 4 months. The clock cap is a backstop that rarely fires.

The 10%, 20% and 40% leads are first guesses, to be tuned against what the long simulations show.

## Simulation work

- **Long runs first.** Economy runs today play 30 to 60 turns, but an era is 1,440 to 2,880. Before setting any cost, play one long run to see what coalitions earn and how large they are at months 1, 2, 3 and 4. Monument costs are set in multiples of a leading coalition's income at that point.
  - If a full run is too slow, play a scaled world instead: a shorter era and a cheaper monument. Economies don't scale linearly, though, so check the scaled world against at least one full run.
- **A builder policy for the simulated coalitions.** Found a monument when the alliance's lead passes a threshold. Each member pays in a share of its surplus each turn. Warriors and craftsmen send part of their army to the garrison.
- **A rival policy.** Once a rival monument passes a set stage, warriors strike it first. Rival alliances stagger their strikes a few turns apart, which simulates agreeing by letter.
- **Load.** Keep long runs to about six at a time, with no test suite alongside them (see the memory on parallel load).

## Measured

Step 1, on 2026-11-08: two runs of four coalitions of nine over an era, with no round reset. The table is in `BALANCE.md`. Per coalition:

| | Day 30 | Day 60 | Day 90 |
|---|---|---|---|
| Land | 50,000 to 65,000 | 73,000 to 107,000 | 93,000 to 135,000 |
| Gold a turn | 170 to 190M | 250 to 300M | 320 to 410M |
| Tools a turn | 15,000 | 12,000 to 13,000 | 11,000 |
| Iron a turn, with a miner | 93,000 | 92,000 | 68,000 to 85,000 |
| Food a turn | 1.7M | 3.3M | 3.4 to 4.6M |

The figures give these starting prices. Step 7 tunes them.

- **Stage cost:** 5 billion gold, 1.5 million iron and 200,000 tools a stage. Over 10 stages that is 50 billion gold, 15 million iron and 2 million tools. That's about a week of a leading coalition's whole output at day 60 for each good.
- **How long that takes:** a coalition paying in a third of its output finishes in about:
  - 33 days if it starts at day 30;
  - 21 days at day 60;
  - 15 days at day 90.

  Income keeps growing, so a fixed price gets cheaper as the era goes on, and eras end even without a clock.
- **Upkeep:** 7.5 million gold and 15,000 food per stage each tick. That's 75 million gold at 10 stages: a quarter of a leader's gold income at day 60, and 30% of an average coalition's. The food is about 5% of a coalition's harvest.

**What the run showed beyond prices:**

- **The build cost won't sort leaders from the rest.** A leader 40–50% ahead in land is only 10–25% ahead in gold, so it builds barely faster than an average coalition. The upkeep and rivals' assaults must do that sorting.
- **Rivals can't break the leader's garrison.** In these runs the leader is the coalition with two warriors, and the pair holds 230,000–360,000 soldiers from day 60. Each rival's warrior holds 0–80,000. If the leader parks its warriors at the monument, two rivals can't break it. The price the leader pays is that parked soldiers take no land.
- **Much of that gap is a scripted-ruler gap.** Simulated warriors keep no defense at home, so the pair breaks the rival warriors (21 strikes on them in 14 days). Players would keep a garrison.
- **So before building, fix the simulated warriors' home defense and measure again.** Until then, balance target 3 can't be judged.

Step 2, on 2026-11-08, on top of the two-week peace and the towers and castles with fixed places: simulated warriors keep half their soldiers at home, half bowmen and half infantry, with towers and castles for them. Table in `BALANCE.md`.

- **The leader is still the two-warrior coalition,** 22 to 29% ahead in land at day 60 and 12 to 16% in gold, where it was 38 to 46% and 19 to 20%. It now gets there by striking craftsmen, not rival warriors.
- **Rivals can now fight back.** At day 60 each rival's warrior holds 27,000 to 90,000 soldiers against 86,000 to 103,000 for each of the leader's two, where rivals' warriors often stood at nothing before.
- **The prices above were measured before these changes.** A coalition's land and gold at day 60 changed by under 10%, so they stand as starting prices.

## Plan

Each step lands with its scenarios, docs and checks, and is committed on its own.

1. **Measure.** Done on 2026-11-08: see "Measured" above.
2. **Warriors keep a garrison at home**, then measure the era again. Done on 2026-11-08: see "Measured" above.
3. **Era.** Done on 2026-11-08: round is renamed to era, the clock cap is 2,880 ticks, and the hall of fame records the winning alliance and the way the era ended. The `era` scenarios play a short era of 120 ticks, and `npm run test:long` plays two at full length.
4. **Monument.** Done on 2026-11-08: `POST /alliance/monument/found` and `/contribute`, `GET /alliance/monument` for members and `GET /game/monuments` for everyone, a section on the Alliance page, listings on the rankings and the landing page, and the `monument` scenarios. Completing one does nothing yet, and the prices are the starting ones from "Measured": no simulated ruler builds one until step 7.
5. **Garrison and assault.** Done on 2026-11-08: the `monument` attack type, soldiers standing guard at a monument, razing and joined assaults, battle reports, a Garrison section on the Alliance page, a goal on the Army page, a March on it link on the rankings, and the `assault` scenarios. Prices are still the starting ones: no simulated ruler garrisons or assaults one until step 7.
6. **Winning.** Done on 2026-11-09: `monument.holdTicks` (72), the era ending `monument` with its winner in the hall of fame, the countdown everywhere a monument shows, Your turn's warnings, the `winning` scenarios, and `npm run test:long`, whose first era ends by a monument that a simulated mason pays for. That monument is priced in gold alone, cheap enough for one economist, so the run checks the ending, not the balance.
7. **Balance.** Add the builder and rival policies to the simulation, then the four balance targets as worlds. Tune the costs, upkeep, raze ratio and hold time, and record them in `BALANCE.md`.
8. **Pacts**, only if step 7 shows that alliances striking separately can't stop a leader.

## Open decisions

Each open one has a default the plan follows unless changed.

1. **Clock cap.** Settled and built: a stalled era ends at four months, with the most land winning. Without a cap, a world where nobody builds never restarts.
2. **What a won assault does.** Should it knock down a share of the stages, scaled by dominance, or raze the whole monument? The default is a share: one lucky strike shouldn't end a month of work, while repeated strikes by rivals who band together still bring it down.
3. **Pacts.** Should pacts between alliances be a formal game feature? The default is no, until the simulations show a need: letters plus the lack of protection after a defeat may be enough.
4. **Hold period.** Built as the default: three days, so players who come back once a day still get a chance to answer. Step 7 tunes the length.
5. **Naming.** Settled and built: "round" became "era" throughout.
