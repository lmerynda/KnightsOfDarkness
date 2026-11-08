# Eras and the coalition monument

Design, not built yet (2026-11-07). An era ends when a coalition completes its monument. Today a round ends on a fixed clock of 672 ticks (four weeks), and the largest kingdom is crowned.

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
- Kingdoms are ranked by land and alliances by their members' land (`RankingCalculator`). A round's final rankings go into the hall of fame, and `RoundReset` starts every kingdom anew while alliances are kept.
- Great works belong to one kingdom each. Nothing is built at the alliance level.

## The rules

### The era

- `round` becomes `era` everywhere: config, entity, API, browser, docs. This applies only if decision 5 below is yes. Nothing keeps the old name working.
- An era ends when a monument stands complete for `monument.holdTicks`. The alliance that built it wins.
- A clock cap ends a stalled era at `era.maxLengthInTicks`: 2,880 ticks, four months of hourly turns. The alliance with the most land wins it. See decision 1.
- The hall of fame records the winning alliance, how the era ended (monument or clock), and the final rankings.
- The reset stays as it is: every kingdom starts again, alliances are kept, and every monument is gone.

### Raising a monument

- **One per alliance per era.** The emperor founds it in a member's kingdom, the seat. It does not use land or building points. Any alliance may try: the cost is what limits it.
- **Stages.** It has `monument.stages` stages, 10 to start with. Each stage costs `monument.stageCost` in gold, iron and tools. That draws on every craft at once: gold producers, the iron miner and the smiths.
- **Contributions.** Members pay resources straight into the monument's store with `POST /alliance/monument/contribute`. A stage is raised as soon as the store covers its cost.
  - I'd skip carriers. Carrying would add a delay and another strike target, but it isn't needed to make the monument costly.
- **Upkeep.** Each tick the monument eats `monument.upkeepPerStage` in gold and food for every stage built, so a nearly finished monument costs the most to keep. When the store can't pay, it loses `monument.decayRatio` of a stage that tick. Holding a monument without feeding it decays it.
- **Visibility.** Everyone sees every monument: its alliance, seat, stage, and the size of its garrison to two figures, as with marching armies. The rankings and the landing page show this. The chronicle tells every kingdom when a monument is founded, at each stage from the half-way mark, and when it is complete. Rivals must know in time to band together.

### Defending it

- **The garrison** is made up only of soldiers that members send. Infantry, bowmen and cavalry go to the monument the way reinforcements do: they arrive after `common.turnsToReinforce` turns, and the sender pays, feeds and recalls them.
- **No other defense counts.** No towers, castles or guards, and not the seat's own army at home. The seat's ruler can send soldiers to the monument like any other member.

### Assaulting it

- **New attack type `monument`.** Any kingdom outside the alliance may march on a monument. The march takes `attack.turnsToArrive`, and `joins` works as for any strike.
- **Usual limits lifted.** `minTargetLandRatio` doesn't apply, and a monument gets no protection after a defeat. Rivals can strike it again and again: that's the counterplay.
- **The battle** is the usual one: archers first, then melee. The defender is the garrison alone, and both sides take losses as usual.
- **A won assault** knocks down up to `monument.maxRazeRatio` (25%) of the built stages, scaled by dominance as land capture is. It takes no land and no stores. See decision 2.
- **The cost to attackers:** their armies are away from home, so the leader's warriors can strike back at their homes.

### Winning

- A completed monument must stand `monument.holdTicks` (72 ticks, three days) with its upkeep paid. That gives rivals one last window to attack it.
- A won assault in that window knocks it down below the final stage, and the hold starts again once it is rebuilt.

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

## Plan

Each step lands with its scenarios, docs and checks, and is committed on its own.

1. **Measure.** Play one long economy run to see the size and income of the simulated coalitions at months 1 to 4. Record the numbers in `BALANCE.md`.
2. **Era.** Rename round to era. Add the clock cap and record the winning alliance and the way the era ended in the hall of fame. Update the `clock` and `round` scenarios and `npm run test:long`.
3. **Monument.** Found it and pay into it. Add the stages, the upkeep and decay, the chronicle notices, and the public view. This needs:
   - the API, plus regenerated types in both clients;
   - an Alliance page section and a listing on the rankings;
   - scenarios.
4. **Garrison and assault.** Add the `monument` attack type, the garrison as a reinforcement target, razing, and joined assaults. Add scenarios and battle reports.
5. **Winning.** Add the hold period. The era ends by monument and the next era begins. Add scenarios, and a `test:long` world that ends by monument.
6. **Balance.** Add the builder and rival policies to the simulation, then the four balance targets as worlds. Tune the costs, upkeep, raze ratio and hold time, and record them in `BALANCE.md`.
7. **Pacts**, only if step 6 shows that alliances striking separately can't stop a leader.

## Open decisions

Each has a default the plan follows unless changed.

1. **Clock cap.** Should a stalled era end at four months, with the most land winning? The default is yes. Without a cap, a world where nobody builds never restarts.
2. **What a won assault does.** Should it knock down a share of the stages, scaled by dominance, or raze the whole monument? The default is a share: one lucky strike shouldn't end a month of work, while repeated strikes by rivals who band together still bring it down.
3. **Pacts.** Should pacts between alliances be a formal game feature? The default is no, until the simulations show a need: letters plus the lack of protection after a defeat may be enough.
4. **Hold period.** How long must a completed monument stand: three days, or no hold? The default is three days, so players who come back once a day still get a chance to answer.
5. **Naming.** Should "round" become "era" throughout? The default is yes, as it's your word for it.
