# Knights of Darkness: redesign blueprint

**Status:** design proposal for owner review, 2026-10-25. Nothing here is implemented.
**Mandate:** the owner asked for a complete redesign ("let opus 5.5 redesign it completely"). That supersedes the assessment's advice against broad redesign. The assessment's evidence still applies, and its integrity gates are built into slice S0 below.
**Inputs:** `docs/assessments/2026-10-25-gameplay-and-ux.md`, `creative/intro.md`, `creative/characters.md`, backend and browser source at `073f88f`.
**Companion:** `docs/design/redesign-concept.html`, an interactive design prototype with invented sample data. It is not the working game.
- **What it shows**, in two scenarios (mid-season "Hollowmere" on day 9, and a first session in Founding):
  - Kingdom planning with live forecasts and Max buttons that report their limiting reason.
  - Founding bells whose full forecast (economy and world events) is reconciled line by line with the result.
  - March planning that forecasts the Hold, the home timeline and scheduled departures.
  - The coalition War Table, Oath and caravans.
  - Chronicle reports.
  - Conflict, unknown-outcome, offline and session-expired states, switched on from its prototype controls.
  - Ending Founding early, as a confirmed order.
- **Shown but not simulated:** recall, turn back, scouting, pacts and other kingdoms' moves. Appendix C lists the prototype's other limits.

> **About the numbers.** Every quantity below is a launch default, chosen so that the rules agree with each other. Every worked example was computed with a scratch model of these exact rules, and the prototype embeds the same model. That shows the examples are internally consistent. It does not show the game is balanced or fun. Tune by simulation (slice S8) and human playtests (section 19).

---

## 0. The redesign on one page

**Knights of Darkness is a seasonal coalition strategy game played in short check-ins.** Each player rules one kingdom. Kingdoms swear into coalitions. Coalitions compete for **Renown**, mainly by holding the seven frontier **Holds** against the **Hollow Host**, an enemy that attacks on a published schedule and hits hardest whoever holds the most. A season lasts 28 days and ends in **Nightfall**. One coalition is crowned **Wardens of the Realm**. If at least half the Holds still stand at the end, the whole realm sees **Dawn**.

The ten decisions that define the redesign:

1. **One world clock.** Everything advances at each hourly **bell**, for every kingdom at once. The turn bank is gone, along with sender-controlled marches and rewards for constant presence.
2. **Founding bells for the first session.** New kingdoms get 12 personal bells that move only their own lands. Players experience decide → bell → reconcile in their first 15 minutes.
3. **A smaller economy where each piece forces a choice.** Five stores (gold, grain, iron, tools, arms) and six building types. Jobs are filled automatically. Construction crews replace building points, and land clearing gets steeper as the kingdom grows. Nothing expires.
4. **Three troop types with published numbers.** Spearmen hold, knights strike, archers do both. Battles are deterministic, with a formula anyone can check. Every march shows its arrival time, outcome and home exposure before dispatch.
5. **A campaign with an end.** Holds, Dark Tides, Fallen Holds and Dread give the season a shared threat, a scoreboard, a final week and a realm-wide stake.
6. **Coalitions have real work.** Members garrison holds together, reinforce each other's homes, send caravans, swear Oaths and coordinate on a War Table of structured orders. Roles, invitations, petitions and pacts are included.
7. **Losses hurt but never eliminate.** Land is never taken. Buildings are never destroyed. Vaults protect basic stores. Respite, power brackets and slumber prevent griefing. Late joiners get a catch-up package.
8. **Every number shown is the server's own result.** Every action is previewed by the server, then committed with an idempotency key and confirmed by a receipt. Forecasts run on the same engine code.
9. **Five screens** (Kingdom, Muster, Frontier, Coalition, Chronicle), designed for 390px phones and 1440px desktops alike.
10. **A dark, grounded look.** The visual style is a candlelit war-room ledger: dark warm surfaces, parchment text, heraldic accents, restrained ornament.

---

## 1. Concept and fantasy

**Logline.** Rule a scarred kingdom at the edge of an endless dusk. Grow it bell by bell, swear it to a coalition, and keep the watchfires of the western March burning against the Hollow Host, while rival coalitions wait for you to overreach.

**The fantasy.** You are not a lone champion. You are the ruler who decides who farms, who forges and who marches west. In this realm, power is not what you hoard but what you can *hold*: the Holds of the March, whose watchfires keep the Hollow Host back. No kingdom can hold one alone for long. A coalition can, but every Hold it takes draws the Darkness toward it, and every soldier sent west is one fewer guarding home. The season ends in Nightfall. Either the realm sees Dawn or it does not, and one coalition's banners are remembered.

**Tone.** Grounded and dark medieval: scarcity, oaths, cold logistics, small mercies. No gore and no cartoon style. The Hollow Host shows up through schedules, losses and dread rather than spectacle. This follows `creative/intro.md` ("unity is a fragile shield", "trembling coalitions", the unholy legion from the west) and reuses its place names: Aurewynd and Valemire. For interface and illustration direction, it supersedes the whimsical character prompts in `creative/characters.md` (see section 18, A5).

---

## 2. Target player and session shape

**Primary player.** Adults who enjoy asynchronous strategy and light diplomacy (players of Utopia, Travian, OGame or Neptune's Pride, and fans of Diplomacy-style board games). They have 10–30 minutes a day, split across several check-ins. They play on a phone during the day and a desktop in the evening, and want to matter to a small group of 3–5 allies.

**Secondary player.** The solo optimizer. They are served by personal honors and by being the specialist their coalition needs: the breadbasket, the forge or the anchor garrison.

| Session | Frequency | Length | Content |
|---|---|---|---|
| Check-in | 2–4 per day | 5–12 min | Read the Chronicle digest, act on counsel, make 2–5 decisions, then leave with a known next milestone. |
| War council | about 1 per day | 15–25 min | Plan holds, Oaths and caravans on the War Table; make larger construction plans. |
| First session | once | 10–15 min | Founding objectives (section 5.3). |
| Away | 12–72 h | none | The kingdom keeps working. Defense is passive and coalition members can cover. Slumber starts at 72 h (section 11.2). |

**Population assumption.** 12–60 human kingdoms per season. Hold count, tide strength and coalition caps scale with active kingdoms (sections 9 and 10).

---

## 3. Design pillars and anti-goals

| Pillar | What it means | How we check it |
|---|---|---|
| **A few weighty decisions per visit** | A check-in is read, decide, commit, leave. | Median check-in ≤ 12 min, with ≥ 2 decisions that change tomorrow's state. |
| **Trust the ledger** | Every cost and outcome is previewed by the server before commit, and every receipt and report matches its preview. | Every exact line of the next-bell forecast, world events included, equals the processed bell in 100% of automated checks (13.5). |
| **Stand together or fall apart** | The best play needs other people. The shared enemy makes cooperation rational, and rivalry keeps it fragile. | ≥ 70% of active players are in a coalition by day 3, and War Table orders get pledges. |
| **Fair to the absent** | The world moves for everyone at once. Defense is passive, and protections cap losses while away. | Renown per active day for 2-check-in players stays within 30% of 6-check-in players. |
| **Losses hurt, never eliminate** | No elimination, no land loss, no destroyed buildings. A recovery step is always visible. | A raided kingdom returns to its pre-raid Might within 24 bells (median). |

**Anti-goals:**
- No turn banks to hoard and dump, and no reward for constant presence beyond making better decisions.
- No reflex play, and no action that needs the player online at a particular minute.
- No griefing loops: no land theft, no farming new or inactive kingdoms, no dogpiling.
- No hidden multipliers. Nothing that matters is invisible before commit.
- No pay-to-win, energy timers or loot boxes.
- No endless sandbox. Every season has a climax and an end.
- No global chat at launch. Coordination is structured (section 10.6).
- No cartoon tone and no gore.

---

## 4. Time: bells and seasons

### 4.1 Bells

A **bell** is one tick of the world clock. The live default is 1 bell = 1 hour (config `bellSeconds`; development 60, compressed alpha 600). At each bell, every kingdom produces, eats, grows and builds. Every march, tide and battle due that bell resolves for everyone at once, whether they are online or not.

The UI always pairs relative and absolute time: "arrives in 4 bells · ≈ 19:00".

### 4.2 Founding bells (onboarding only)

Every new kingdom receives **12 founding bells**, usable whenever the player likes during **Founding** (its first 72 world bells, the protection period described in section 11.2). A founding bell advances **only that kingdom's own lands**: economy, construction, training, and marches to its private outlaw camps (section 8.10). Nothing that involves other kingdoms, Holds or caravans moves. Unused founding bells disappear when Founding ends, at its 72nd world bell or earlier if the player ends it (11.2), and the header shows that countdown.

**Kingdom bells and world bells.** A founding bell does not move the world clock. Timers count bells remaining, so a founding bell advances every timer of that kingdom by one, exactly as a world bell does. The kingdom's own count of bells experienced since founding therefore runs ahead of the world bells elapsed since then, by the founding bells it has rung. The first-session numbers in 5.3 count these **kingdom bells**. Founding's end, tides and other kingdoms' marches stay on world bells, and scheduled departures (8.4) run on world bells only. The prototype's first-session header shows both clocks.

This lets a newcomer complete the full loop several times in one sitting. Everyone, including late joiners, receives the same 12, so there is no inequity. Founding kingdoms cannot send caravans, so founding bells cannot be turned into resource farms for another kingdom (section 12).

### 4.3 Season structure

A season is **672 bells (28 days)**. Bell 0 is the opening. Day *d* covers bells 24(*d*−1)+1 to 24*d*.

| Phase | Days | Bells | Rules |
|---|---|---|---|
| **Kindling** | 1–3 | 1–72 | All season-start kingdoms are in Founding. Holds can be claimed. Tides probe from day 2 but cannot take a Hold. Hold renown is paid at the Kindling rate. |
| **Long Dusk** | 4–21 | 73–504 | Full rules. Each Hold gets a tide every 24 bells. Unclaimed Holds fall to their next tide. |
| **Nightfall** | 22–28 | 505–672 | Tides come every 12 bells and hit harder. Hold renown is paid at the Nightfall rate. Coalition rosters lock from bell 601 (day 26). New kingdoms cannot join. |
| **Reckoning** | after bell 672 | n/a | Standings freeze, titles are awarded, and the realm gets Dawn or the Long Night. The realm stays read-only for 48 bells (48 hours at live speed), then the next season opens. |

### 4.4 Downtime

The world never skips bells silently. After an outage, the server catches up on at most **6** missed bells. Beyond that, the season clock pauses: every schedule shifts, and players see a notice. All arrival, tide and season times are stored as bell numbers, not timestamps, so pausing is safe.

---

## 5. Core loop and the first 15 minutes

### 5.1 Loops

- **Check-in loop.** Chronicle digest → counsel (up to 3 prioritized items) → decide (build, staff, recruit, march, supply, pledge) → preview → commit → receipt → leave with the next milestone in view ("Walls level 3 complete at bell 210 · Tide at Greywatch in 9 bells").
- **Day loop.** Production and construction compound, tides arrive on schedule, the coalition plans and runs garrisons and reclaims, and renown accrues every bell.
- **Season loop.** Found a kingdom → choose a specialization and grow → join or found a coalition → claim and defend Holds → reclaim fallen Holds and contest rivals → survive Nightfall → Reckoning.

### 5.2 Starting kingdom

| | |
|---|---|
| Land | 50 acres: 28 built, 22 free |
| Buildings | 12 Cottages, 8 Farms, 3 Mines, 2 Forges, 2 Markets, 1 Barracks; Keep level 1, Walls 0, no Guild |
| People | 190 commoners; housing 220 (12 × 15 + Keep 40) |
| Jobs | 150 (Farms 80, Mines 30, Forges 20, Markets 20), so 40 idle |
| Troops | 20 Spearmen, 10 Archers; capacity 80 (Barracks 40 + Keep 40) |
| Stores | 2,000 gold · 1,500 grain · 100 iron · 150 tools · 40 arms |
| Forge split | 30% arms |
| Crews | 2 |
| Might / home defense | 400 / 99 (Spearmen 60 + Archers 20 + levy 19) |

**Forecast for the first bell, with no orders:** gold +240 (tithe 190 + merchants 80 − wages 30), grain +20 (farms 240 − eaten 220), iron +10 (mined 30 − forged 20), tools +14, arms +6, people +3.

### 5.3 The first 15 minutes (recommended path; every step is a real choice)

| Min | Step | What the player sees and decides | Numbers |
|---|---|---|---|
| 0–2 | **Found** | Name the kingdom; pick a banner (two tinctures and a charge) and a ruler's title. Banner: "Founding: protected for 72 world bells · 12 founding bells". | |
| 2–4 | **Choice: put idle hands to work** | Counsel: "40 people are idle; each workplace employs 10." Four options, each showing its effect: Farm +30 grain/bell, Mine +10 iron/bell, Forge +10 tools or arms per bell (iron-limited), Market +40 gold/bell. The recommended pair is highlighted, not forced. | Path: Farm + Market = 350 gold, 20 tools. Farm ready at bell 4, Market at bell 5. Crews 2 of 2. Stores after: 1,650 gold, 130 tools. |
| 4–5 | **Shelter newcomers** | Counsel: "Housing 190 of 220: newcomers arrive each bell while there is room (+3 next bell), and growth slows as it fills." Queue a Cottage; it starts when a crew frees up. | 120 gold, 8 tools. Starts at bell 4, ready at bell 7. |
| 5–6 | **Choice: raise a levy?** | Recruit now, or keep the gold. Recruits leave the commoners immediately and draw wages from the moment they are ordered. | 10 Spearmen = 250 gold, 20 arms, ready in 2 bells. Commoners 190 → 180. |
| 6–7 | **Ring founding bell 1** | The forecast is shown first, then the reconciliation: every line matches. | Gold +260 tithe and merchants − 40 wages → 1,500. Grain +240 − 220 → 1,520. Iron 110, tools 136, arms 26, people +4 → 184. |
| 7–9 | **Ring bells 2–3** | Spearmen are ready at bell 2. Counsel: "Outlaws hold Ashford, 1 bell away (defense 30)." | At bell 3: 1,952 gold, 1,548 grain, people 192/220. |
| 9–11 | **Choice: first march** | Compose a force; the preview compares options honestly (table below). | Dispatch at bell 3, battle at bell 4, home at bell 5. |
| 11–13 | **Ring bells 4–5** | Bell 4: Farm complete, battle report (victory), Cottage starts. Order a Mine; it queues. Bell 5: Market complete; troops and loot home. Each forecast already includes its bell's world events: bell 4's shows the battle and its losses, and bell 5's adds the returning loot, so both reconcile line by line. | Loot: 150 gold, 100 grain, 20 iron. Mine ready at bell 9. Bell 5 forecast and result: 2,391 gold (+150 from the loot), 1,693 grain (+100), 170 iron (+20). |
| 13–14 | **Choice: what to do with 2,391 gold** | Keep level 2 (+1 crew, +40 housing, +40 troop capacity, vault 800), Walls 1 (+10% home defense), or clear land (next acre 83 gold). | Path: Keep = 1,500 gold, 100 tools, 12 bells. It starts at bell 7 and is ready at bell 19. |
| 14–15 | **Choose a banner; look west** | Join an open coalition, petition one, found one, or stand alone for now. The Frontier shows the 7 Holds and the first tide schedule. Leave with 7 founding bells unspent, plus a "while you're away" forecast. | End state at bell 5: 891 gold · 1,693 grain · 170 iron · 80 tools · 50 arms; people 198/220; 29 Spearmen, 9 Archers. |

**The first march (Ashford, outlaw defense 30; loot 270 units: 150 gold, 100 grain, 20 iron):**

| Force | Attack | Outcome | Expected losses | Carry capacity |
|---|---|---|---|---|
| 10 Archers | 20 | Repelled | 1 Archer | n/a |
| 20 Spearmen + 10 Archers | 40 | Victory (+33%) | 1 Spearman, 1 Archer | 280 ≥ 270 ✓ |
| 30 Spearmen + 10 Archers | 50 | Victory (+67%) | 2 Spearmen | 380 ≥ 270 ✓ |

The session teaches the whole grammar of the game: jobs, housing, crews, forecasts, recruitment, travel, battle math, carry capacity and coalitions. It ends with reasons to come back: the Keep completes, invitations arrive, and the Frontier is lit.

---

## 6. Launch scope

**In scope at launch**, by slice:
- **S0, platform integrity:** account and season identity; server-authoritative preview, commit and receipt; strict validation; idempotency; migrations; session refresh.
- **S1–S2, economy and kingdom:** the bell engine; stores; housing; automatic staffing with priorities; construction crews and queue; Keep, Walls and Guild; land clearing; the forge split; the Exchange; forecasts; founding bells and objectives; the new navigation shell and Kingdom view.
- **S3, military:** three troop types; recruitment and disbanding; outlaw camps; expeditions (garrison, assault, raid, reinforce, recall, turn back, scheduled departure, combined same-bell attacks); the battle engine; plunder; scouting; battle reports; incoming warnings.
- **S4, coalitions:** create; invite; petition; accept and decline; roles; leave, kick, succession and disband; caravans; reinforcement; War Table orders; Marshal's word; call for aid.
- **S5, Frontier and campaign:** Holds; tides; Fallen Holds; Dread; the renown ledger; Oaths; standings; season phases.
- **S6, PvP and diplomacy:** raids between kingdoms; Respite; power brackets; pacts.
- **S7, season lifecycle:** late join with Rising Banner; slumber; Reckoning; honors; archive; next season.
- **S8, measurement:** simulation harness (reworked bots), balance reports, playtest telemetry.

**Out of scope at launch:** player-to-player market, global or free-form chat, spies and sabotage, heroes and equipment, per-kingdom map geography, web push and email digests (first post-launch candidate), cosmetic unlocks across seasons, localization (strings are externalized from day one), native apps, monetization.

---

## 7. Kingdom economy

### 7.1 Stores

| Store | Comes from | Goes to |
|---|---|---|
| **Gold** | Tithe (1 per commoner per bell), merchants (4 each), loot, Exchange sales | Construction, works, land, recruits, wages, Exchange purchases, scouting |
| **Grain** | Farmers (3 each), loot, caravans, Exchange | Everyone eats each bell (commoners 1, Spearmen and Archers 1, Knights 2) |
| **Iron** | Miners (1 each), loot, caravans, Exchange | The Forge |
| **Tools** | The Forge (1 iron → 1 tool) | Construction and works |
| **Arms** | The Forge (1 iron → 1 arm) | Recruits |

Land and people are capacities, not stores. Gold cannot travel by caravan (section 12).

### 7.2 People, housing and jobs

- **Housing** = 15 × Cottages + 40 × Keep level. Only commoners live in housing; troops live in barracks.
- **Growth.** Each bell, if nobody went hungry and grain remains above 0, commoners increase by ⌈10% × (housing − commoners)⌉. If housing shrinks below the current population, the excess leaves at the next bell.
- **Jobs.** Every workplace offers 10 jobs, filled automatically each bell in the player's **staffing priority** order (default: Farms, Mines, Forges, Markets). Commoners left over are **idle**: they still pay tithe but produce nothing else. Professions are no longer trained or fired, which removes the assessment's population-destroying Fire defect by design.
- **The forge split** (0–100% arms, in steps of 10) is a standing order that persists until changed. It replaces the per-turn weapons percentage.

### 7.3 Buildings (1 acre each)

| Building | Cost | Build time | Effect |
|---|---|---|---|
| Cottage | 120 gold, 8 tools | 3 bells | +15 housing |
| Farm | 150 gold, 10 tools | 4 bells | 10 jobs; 3 grain per farmer |
| Mine | 180 gold, 12 tools | 4 bells | 10 jobs; 1 iron per miner |
| Forge | 220 gold, 15 tools | 5 bells | 10 jobs; each smith turns 1 iron into 1 tool or 1 arm |
| Market | 200 gold, 10 tools | 5 bells | 10 jobs; 4 gold per merchant; +150 caravan cargo capacity |
| Barracks | 300 gold, 20 tools | 6 bells | +40 troop capacity |

Demolishing returns the acre at once with no refund, and the jobs or housing disappear at the next bell. The preview lists exactly who becomes idle or leaves.

### 7.4 Works (levels; they use a crew but no acre)

| Works | Level costs (gold / tools / bells) | Effect per level |
|---|---|---|
| **Keep** (starts at 1, max 5) | L2 1,500/100/12 · L3 4,000/250/18 · L4 9,000/500/24 · L5 18,000/900/30 | +40 housing, +40 troop capacity, +1 crew (crews = 1 + level), vault +400 for each store |
| **Walls** (0–5) | L1 600/60/8 · L2 1,500/150/12 · L3 3,500/300/16 · L4 7,000/550/20 · L5 12,000/900/24 | +10% defense at home |
| **Guild** (0–3; specialization chosen once per season) | L1 800/80/10 · L2 2,400/200/16 · L3 6,000/450/24 | +10% to its trade: **Millers** (grain), **Miners** (iron), **Smiths** (forge throughput), **Merchants** (merchant gold); or **Drillmasters** (+5% attack and defense for your troops) |

Only one level of each works can be under construction at a time. The Guild makes specialization a commitment, and specialization is a reason to trade and cooperate inside a coalition.

### 7.5 Crews and the construction queue

- **Crews** = 1 + Keep level. Each crew builds one project at a time.
- **Payment happens at order time.** That makes the queue a promise: whatever is queued will be built. Cancelling a project that has not started refunds 100%. Cancelling a started project refunds 50% and frees its acre.
- A project ordered with a free crew completes after its build time in bells. A queued project starts in the bell its crew frees up. Example: a Farm ordered at bell 0 completes at bell 4, and a Cottage queued behind it completes at bell 7.
- Within one order, projects start in the order listed: workplaces (Farm, Mine, Forge, Market), then Cottages, Barracks and works. The preview shows each project's start and completion bell.
- Idle crews raise a counsel item. Nothing ever expires; building points are removed entirely.

### 7.6 Land

Clearing is instant and paid in gold. **The next acre costs round(A² ÷ 40) + 20 gold**, where A is current acres. Rounding is half-up everywhere in this blueprint. The preview quotes the exact total for any quantity.

| Current acres | Next acre | 10 acres |
|---|---|---|
| 50 | 83 | 945 |
| 100 | 270 | 2,932 |
| 164 | 692 | 7,301 |
| 200 | 1,020 | 10,657 |
| 300 | 2,270 | 23,382 |
| 500 | 6,270 | 63,832 |

The steepening cost is the main brake on runaway growth (see 7.10). Land is never taken in war, and no ranking counts idle land.

### 7.7 Bell order for one kingdom (authoritative)

```text
1. Staffing     workers = fill jobs from commoners in priority order; the rest are idle
2. Production   grain  += floor(farmers × 3 × guild(millers) × (1 − dread))
                iron   += floor(miners × 1 × guild(miners))
                forged  = min(iron, floor(smiths × 1 × guild(smiths)))
                arms   += floor(forged × armsPct / 100); tools += forged − armsMade; iron −= forged
                gold   += floor((commoners × 1 + merchants × 4 × guild(merchants)) × (1 − dread))
3. Wages        wages = Σ troops (home + away + training) × {Spearman 1, Archer 1, Knight 3}
                if gold ≥ wages: gold −= wages
                else: gold = 0; ⌈5% of troops at home⌉ desert (largest-remainder by type)
4. Food         troopGrain = Σ troops × {1, 1, 2};  need = troopGrain + commoners
                if grain ≥ need: grain −= need
                else: troops eat first (if even that fails, ⌈5% of home troops⌉ desert);
                      grain = 0; ⌈5% of commoners⌉ leave; hungry = true
5. Growth       if commoners > housing: commoners = housing
                elif not hungry and grain > 0: commoners += ⌈10% × (housing − commoners)⌉
6. Progress     each active project and training batch −1 bell; completions take effect
                (new jobs count from the next bell); free crews start queued projects
```

`guild(x)` = 1 + 0.10 × Guild level if your Guild is *x*, otherwise 1. `dread` = min(0.15, 0.03 × Fallen Holds) (section 9.3). Production comes before upkeep, which fixes the current TODO. Players can see all six steps in the forecast breakdown.

### 7.8 The Exchange (system market)

The player-offer market is replaced by a **system Exchange** with fixed prices per 10 units:

| Good | Buy | Sell |
|---|---|---|
| Grain | 20 gold | 10 gold |
| Iron | 60 gold | 30 gold |
| Tools | 100 gold | 50 gold |
| Arms | 120 gold | 60 gold |

Beyond 500 units of one good per kingdom in any rolling 24 bells, buying costs ×1.5 and selling pays ×0.5, rounded in the system's favor. The Exchange guarantees a way out of any shortage, runs no arbitrage loop (producing always beats trading), and cannot move wealth between players. Quotes are exact and valid until the next bell.

### 7.9 Worked examples

**A. Ordinary bell (starting kingdom).** See 5.2: +240 gold, +20 grain, +10 iron, +14 tools, +6 arms, +3 people.

**B. Hunger, and the trap it reveals.** A kingdom with 7 Farms and an empty granary: farmers produce 210 grain against a need of 220 (30 troops + 190 commoners). Troops eat 30; commoners get 180 of their 190. That makes them hungry, and ⌈9.5⌉ = 10 leave. In the next bell, 180 commoners need exactly 210 = production, so nobody starves, but grain sits at 0 and growth stays blocked. The counsel names the trap ("No grain to spare: people will not settle. Build a Farm, or buy 100 grain for 200 gold.") and offers the actions.

**C. Unpaid wages.** Wages 392, gold 300. Gold drops to 0, and ⌈5% × 174 troops at home⌉ = 9 desert, apportioned as 5 Spearmen, 3 Archers and 1 Knight.

**D. Planning a shortage away (prototype sample "Hollowmere", bell 201).** Grain is 1,950. Production is 1,280 (Millers +10%, Dread −3%) against a need of 1,372, so grain falls 92 per bell. The forecast assumes no new orders and holds Dread at −3% (13.5). It also counts the 16 Hollowmere troops expected to fall, because fewer troops eat less: 6 when Ironhold's raid is repelled at bell 207, and 10 when the tide takes Greywatch at bell 210. The granary runs dry at bell 220, and people start leaving that same bell. (Without those two battles it would be bell 219.) Ordering 2 Farms leaves grain still falling 42 per bell by the twelfth bell. **Four Farms make it +22 per bell by the twelfth bell.** The preview shows each option's 24-bell grain curve before the player commits. The curve also marks the published tides that would take Greywatch (bell 210), Vigil of Aurewynd (213) and Blackwater Ford (215) at today's garrisons; each fall would deepen Dread by 3% from the following bell.

### 7.10 Season growth curve (simulated upper bound)

A greedy scripted builder (always building, recruiting 12–25% of its population, upgrading the Keep when affordable) reaches roughly:

| Day | 3 | 7 | 14 | 21 | 28 |
|---|---|---|---|---|---|
| Acres | about 70 | about 180 | about 390–430 | about 530–590 | about 640–710 |

Crews limit the early game; gold limits the late game, through land prices. Humans are expected to reach 50–80% of these figures. Before the steepening land rule, the same script ended the season with millions of unspent gold; that is why the rule exists. Tools and iron can pile up when the forge split is ignored, so the counsel flags any surplus over 12 bells' worth.

---

## 8. Military

### 8.1 Troops

| | Attack | Defense | Cost | Training | Wage per bell | Grain per bell | Carry |
|---|---|---|---|---|---|---|---|
| **Spearman** | 1 | 3 | 25 gold, 2 arms | 2 bells | 1 | 1 | 10 |
| **Archer** | 2 | 2 | 35 gold, 3 arms | 3 bells | 1 | 1 | 10 |
| **Knight** | 4 | 1 | 90 gold, 6 arms | 4 bells | 3 | 2 | 25 |

There is no hidden counter table. The real decision is how to split strength between home defense and striking power, priced in gold, arms, wages, grain and people. All-Knight marches travel at 75% of normal time, rounded up.

### 8.2 Recruitment and disbanding

- Recruits come from commoners (idle first, then workers in reverse staffing priority). Costs are paid at order time.
- Recruits in training count toward **troop capacity** (40 × Barracks + 40 × Keep level), eat, and draw wages. They do not defend until training completes.
- **Disbanding** returns troops at home to the commoners at once, with no refund. If housing cannot hold them, the excess leaves at the next bell, and the preview says so. Population is conserved, and troops away cannot be disbanded.

### 8.3 Defense and attack power

```text
for each contingent c (owner, type, count n):
    bonus(c) = 1 + 0.05 × DrillmastersLevel(owner)
AP = round(Σ n × Attack(type) × bonus(c))                         # marching force
DP = round((Σ n × Defense(type) × bonus(c) + levy) × M)           # defenders present
levy = floor(commoners / 10) at a kingdom (0 at a Hold); levy never takes losses
M = 1 + 0.10 × Walls at a kingdom; M = 1.5 at a Hold
```

Allied troops stationed at your kingdom (section 10.5) add to your defense.

### 8.4 Expeditions

The player picks a **destination**, and the order follows from it:

| Destination | Implied order | Travel (bells) | On arrival |
|---|---|---|---|
| Your private outlaw camp | Clear | 1 / 2 / 3 by tier | Battle; survivors return with loot |
| Hold that is yours or unclaimed | Garrison | That Hold's distance (4–6) | Join the garrison or claim the Hold |
| Hold held by a rival, or Fallen | Assault | That Hold's distance | Battle for control |
| Rival kingdom | Raid | 8 | Battle; survivors return with plunder |
| Coalition member's kingdom | Reinforce | 4 | Stationed there; defends it |

**Validity checks at dispatch:** at least one troop (a zero-troop march is rejected: 8.5), availability, ownership, phase, protections, brackets and pacts (sections 10.8 and 11.2). Power brackets are measured only at dispatch, so a Might change on either side never halts a march en route. **Checks again at arrival:** if the target changed (a Hold fell, a target entered Respite, an ally left the coalition, a pact formed), the expedition halts and returns home with a report saying why. Garrisons arriving at a Hold that is now their own coalition's simply join; a garrison arriving at a Hold that has since fallen or passed to a rival halts and returns. An assault that arrives at an unclaimed Hold claims it.

**Recall** brings stationed troops home over that route's travel time. **Turn back** reverses a march in progress; the return takes as many bells as had elapsed. Wages and grain are paid for troops wherever they are.

**Scheduled departure.** A march may be set to leave after a later world bell D, at most 24 bells ahead, so that it arrives exactly when the coalition needs it.
- **When it leaves:** at the close of bell D, as step 7 of the world phase below, after that bell's battles. The troops defend home through bell D, are on the road from bell D + 1, and arrive at D + travel, exactly as if the player had dispatched them by hand once bell D resolved. A march dispatched now leaves at once (D is the current bell).
- **Checks:** when it is scheduled, every dispatch check runs as if it were leaving now, and the home timeline must show the troops still at home after bell D. All checks run again at departure. A march that can no longer leave (troops lost at bell D, a target now protected, a pact formed) is cancelled with a report.
- **Visibility:** a scheduled march is visible to its own coalition only. Its target sees it from the moment it departs, as with any march.
- **Eligibility:** scheduled departures run on world bells only, so founding bells never trigger them, and camp marches always leave at once. A kingdom in Founding cannot schedule a raid or an assault on a rival-held Hold, whatever the departure bell (11.2).
- **Home exposure:** previews and forecasts count a march as at home for every battle up to and including its departure bell, and away from the next bell on. The preview's home-defense timeline uses this rule, including any incoming attack that lands after the troops leave.

**Combined attacks.** Expeditions from one coalition that reach the same target in the same bell fight as **one** attacking force. Each contingent keeps its owner, and losses, plunder and renown are shared by Attack contribution. Defenders at a location always fight together. War Table assault orders therefore name a **muster bell**, not a deadline.

**Resolution order within a bell (world phase, after every economy):**

1. Arrivals: returns home with their cargo, reinforcements, garrisons and claims, caravans.
2. Tides at Holds, and entrenchment at Fallen Holds.
3. Hold assaults.
4. Raids and outlaw-camp battles.
5. Renown accrual for control at the end of step 4.
6. Scheduling of new tides.
7. Scheduled departures for this bell, in the order they were scheduled.

Because arrivals come first, troops that arrive in a bell fight in its battles: a reinforcement landing with a raid defends against it, and a garrison arriving in a tide's bell meets the tide. Within each step, the earliest dispatch goes first, with ties broken by expedition id. When two garrisons from different coalitions reach an unclaimed Hold in the same bell, the higher DP claims it, and the other halts and returns. If their DP is equal, the earlier dispatch claims it, then the lower expedition id.

### 8.5 Battle algorithm (deterministic)

```text
victory          = AP > DP                       # whole numbers; AP = DP is a defender win
La (attacker)    = clamp(0.08 × DP / AP, 0.03, 0.20)
Ld (defender)    = clamp(0.06 × AP / DP, 0.02, 0.15)     # DP = 0: nobody to lose
casualties(side) = roundHalfUp(L × troops on that side)  # troops only: never levy, militia or Darkness
apportion casualties over contingents (owner, type) by count, largest remainder;
    contingent order: earliest-arriving owner first, then Spearman, Archer, Knight;
    ties: larger fractional part, then larger contingent, then contingent order
```

**Boundaries:**
- **No empty forces.** A march needs at least one troop. Preview and commit reject a zero-troop march (`VALIDATION`, no state change), and the engine refuses a battle without one. Every troop has Attack and Defense of at least 1, so every march has AP ≥ 1. Desertion and starvation take only troops at home, so a march still has its troops when it arrives. No formula ever divides by zero.
- **Zero defense.** DP = 0 only where nobody defends: a kingdom with no troops at home, no stationed allies and fewer than 10 commoners, or a militia or Darkness strength that rounds to 0 in a near-empty realm. A held Hold always has troops, because it becomes unclaimed when the last one leaves. Against DP = 0 the attack wins, the attacker takes La's 3% floor, the defender has nobody to lose, and plunder takes its cap, p = 0.20. Reports say "undefended" instead of showing a ratio.
- **Strength without troops.** Levy, militia, Darkness garrisons and the Hollow Host add strength but never take losses or count toward casualties. A kingdom defended only by levy loses no troops, while the attacker's losses still follow La, so at least 3%. A failed reclaim leaves the Darkness garrison unchanged, and a repelled tide simply dissipates.
- **Exact ties.** AP, DP and tide strength are whole numbers after rounding, compared exactly. At AP = DP the defender wins with La = 0.08 and Ld = 0.06 (the ratio is 1, so neither clamp applies): no plunder, no capture, no Respite. Every threshold uses the same rule. An assault or reclaim needs AP ≥ DP + 1, so "attack above 810" means at least 811. A Hold stands at its tide while DP ≥ TP. The minimum reinforcement is the smallest that reaches DP ≥ TP.
- **No side is wiped out.** Losses are at most 20% of a side, rounded half up, which is always fewer than its troops (a side of 1 or 2 loses none), so survivors always remain to hold or return home.

Effects:

| Battle | Attacker wins | Defender wins |
|---|---|---|
| Raid on a kingdom | Plunder (8.6); target enters 12-bell Respite; attacker +25 renown if target Might ≥ 75% of attacker's | Defender +15 renown |
| Assault on a rival-held Hold | Defending survivors are routed home; attacking survivors become the garrison; control passes; +60 renown | Attacking survivors return home |
| Assault on a Fallen Hold | Reclaimed: survivors garrison it for the attacker's coalition; +150 renown | Attacking survivors return home |
| Tide at a Hold | The Hold falls: garrison survivors routed home; Darkness garrison = round(0.5 × tide strength); Dread +1 | Tide dissipates; +40 renown, split by defense share |
| Outlaw camp | Fixed loot, limited by carry | Survivors return home |

### 8.6 Plunder

```text
p      = 0.10 + 0.10 × min(1, AP/DP − 1)          # DP = 0: p = 0.20
vault  = 400 × target Keep level  (for each store)
take_s = floor(p × max(0, stock_s − vault))   for s in gold, grain, iron, tools, arms
if Σ take > carry(survivors): take_s = floor(take_s × carry ÷ Σ take)
```

Plunder travels home with the survivors and arrives on their return bell.

### 8.7 Scouting and intelligence

- **Scout:** 100 gold, instant. Reveals the target's current home defense, broken down by type, walls, levy and stationed allies, plus its stores above the vault. The target is told who scouted them. Limit: one scout per target per 6 bells. Scouting is both intelligence and a visible threat.
- Every march preview names its information source and age: "Ironhold defense 455, scouted 5 bells ago. They will see your army for 8 bells."
- Defenders always see incoming expeditions the moment they depart: sender, exact composition and attack power, and arrival bell. A scheduled march stays private to its coalition until it leaves. Holds publish their total garrison DP, and composition is visible only to the holding coalition. The Hollow Host hides nothing.

### 8.8 Reports

Each battle report contains:
- Location and bell (with local time).
- Both forces and how AP and DP were computed, listing every modifier.
- The outcome rule ("370 is less than 583: the attack failed").
- Losses by contingent, with the percentage and formula.
- Plunder, with vault protection shown.
- Renown changes and return times.
- A **reconciliation** with the dispatch preview ("Previewed 1,245 vs 1,180; actual matched"). If an input changed after the preview, the reconciliation names it instead of claiming a match ("Previewed against 455, scouted at bell 196; their defense at arrival was 470").
- For the defender, "What you can do next" actions: raise Walls, recruit Spearmen, request reinforcement.

### 8.9 Worked examples

**Raid.** Attacker: 60 Knights and 40 Archers, AP 320. Defender: 50 Spearmen, 20 Archers, 400 commoners (levy 40), Walls 2. DP = round((150 + 40 + 40) × 1.2) = 276. Since 320 > 276, the attack wins (ratio 1.16).
- Attacker loses 6.9% → 7 troops: 4 Knights, 3 Archers.
- Defender loses 7.0% → 5 troops: 4 Spearmen, 1 Archer.
- Plunder p = 11.6%. With a Keep level 2 vault of 800 against stores of 5,200 gold, 3,000 grain, 600 iron, 400 tools and 300 arms, the take is 510 gold and 255 grain. Carry capacity is 56 × 25 + 37 × 10 = 1,770, so nothing is cut.
- The defender enters a 12-bell Respite.

**Holding against a raid at home (prototype sample, forecast at bell 201).** Ironhold's raid (60 Knights, 50 Archers, 30 Spearmen, AP 370) left at bell 199 and arrives at bell 207. Hollowmere's home DP is 617 now. The forecast for bell 207 is 691: 20 recruits finish training at bell 202, and the levy rises to 102 because 1,024 commoners are forecast for that bell. After sending 30 Spearmen west, the forecast for bell 207 is 583, which still holds. If nothing else changes, the raid is repelled: Ironhold loses 18 troops (12.6%) and Hollowmere loses 6 (3.8%). Sending 60 Spearmen and 60 Archers away instead would drop it to 331, and the raid would break through and take 1,571 gold; the march preview says so in red before dispatch. These are exact forecasts, not estimates, because the defender sees the raid's composition and its own defense. They change only if someone gives a new order first: Ironhold turns back, an ally reinforces or recalls, or Hollowmere sends troops away.

### 8.10 Outlaw camps (private practice)

| Tier | Defense | Loot | Travel | Respawn |
|---|---|---|---|---|
| Outlaw camp | 30 | 150 gold, 100 grain, 20 iron | 1 | 24 bells |
| Brigand fort | 120 | 500 gold, 300 grain, 80 iron, 40 tools | 2 | 24 bells |
| Reaver hall | 400 | 1,500 gold, 800 grain, 200 iron, 100 arms | 3 | 48 bells |

Each kingdom has its own camps. They are fully scouted, give no renown, and their loot never scales, so they fade into irrelevance as the kingdom grows. They exist to teach war safely and to cover the first battle the assessment found missing.

---

## 9. The Frontier

### 9.1 Holds

The Hold count is fixed at season start: clamp(round(registered kingdoms ÷ 4), 3, 9). A 28-kingdom realm gets 7. Each Hold has a fixed travel time from every kingdom, which makes some Holds easier to reinforce than others:

| Hold | Travel | | Hold | Travel |
|---|---|---|---|---|
| Greywatch | 4 | | Blackwater Ford | 5 |
| Emberstead | 4 | | Saltcairn | 6 |
| Ravenmoor | 5 | | Vigil of Aurewynd | 6 |
| Mirefold | 5 | | | |

**States:**
- **Unclaimed:** defended by local militia. It can be claimed by the first garrison to arrive.
- **Held:** controlled by a coalition, and defended by every member's stationed troops (DP multiplier 1.5).
- **Fallen:** taken by the Darkness. It holds a Darkness garrison and adds to Dread until reclaimed.

Only coalition members can garrison or assault Holds; standing alone limits you to raids, camps and trade. If every garrison troop leaves, the Hold becomes unclaimed (not Fallen).

### 9.2 Dark Tides

- **Schedule.** Hold *i* (0-based, of *H*) gets its first tide at bell 25 + floor(24*i* ÷ *H*). Each later tide comes 24 bells after the previous one (12 bells once Nightfall has begun). When a tide resolves, the next tide at that Hold is scheduled and **its strength is fixed and published at once**, so every Hold always shows its next tide exactly.
- **Strength.** TP = round(factor(arrival day) × realm median defense × brightness). The median and brightness are measured when the tide is scheduled, and the factor is the one for the day the tide arrives. All three are known at scheduling, so TP is fixed from that moment.
  - **realm median defense** = median over active kingdoms of (Σ troop Defense + levy), wherever the troops are, without walls;
  - **factor:** Kindling 0.5 (tides cannot take a Hold); Long Dusk 0.80 + 0.06 × (day − 4), so 0.80 on day 4, 1.10 on day 9 and 1.82 on day 21; Nightfall 2.0 + 0.1 × (day − 22), up to 2.6;
  - **brightness** = 1 + 0.25 × (Holds the controlling coalition holds − 1) for a held Hold. An unclaimed or Fallen Hold has no controlling coalition, so its brightness is 1. "The brightest fire draws the dark." Like the median, brightness is counted at scheduling, so a Hold that is claimed or changes hands afterwards keeps its published tide.
- **Unclaimed Holds** are defended by militia of ceil(1.5 × TP) during Kindling, so they stand, and floor(0.75 × TP) afterwards, so they fall. From day 4, the Darkness advances wherever no coalition stands.

### 9.3 Fallen Holds and Dread

- A Fallen Hold's Darkness garrison starts at round(0.5 × the tide that took it). Each later scheduled tide adds round(0.25 × that tide's strength) ("the dark entrenches"). The Hold defends with DP = round(1.5 × Darkness garrison), and reclaiming needs AP ≥ DP + 1: a garrison of 540 has DP 810 and needs 811 (one of 541 has DP 812 and needs 813). The Darkness takes no losses, so a failed reclaim leaves it unchanged.
- **Dread:** every Fallen Hold reduces grain and gold production for **all** kingdoms by 3%, to a maximum of 15%. The forecast shows Dread as its own line. Everyone pays for a Fallen Hold, which gives rival coalitions a reason to defend.

### 9.4 Renown

| Source | Renown |
|---|---|
| Holding Holds, per bell, by how many the coalition holds (oldest-held first) | Long Dusk 12 / 9 / 6 / 3 / 3…; Kindling 6 / 4 / 3 / 1 / 1…; Nightfall 18 / 13 / 9 / 4 / 4… |
| Repelling a tide | 40 |
| Reclaiming a Fallen Hold | 150 |
| Capturing a rival's Hold | 60 |
| Winning a raid (target Might ≥ 75% of yours) / repelling a raid | 25 / 15 |
| Completing an Oath | 80–200 (section 10.7) |

Renown is **earned by kingdoms and attributed to the coalition they belonged to at that bell**, recorded in an append-only ledger. A coalition's standing is the sum of ledger entries tagged with it. Renown stays with the coalition when a member leaves, so switching sides cannot move the score.

Per-bell hold renown and tide renown are split among the garrison's owners by their share of Defense, using largest remainder. Assault and reclaim renown is split by share of Attack.

### 9.5 Worked example: Greywatch at bell 201 (prototype sample, day 9)

- When this tide was scheduled at bell 186, the Ashen Oath held Greywatch and Emberstead (brightness 1.25), and the realm median defense was 858.
- The tide at bell 210, published since then: TP = round(1.10 × 858 × 1.25) = **1,180**.
- Garrison: Ser Aldric's 80 Spearmen, your 120 Spearmen and 30 Archers, Brother Cael's 40 Archers. Defense 740 × 1.5 = **1,110**, so the Hold **falls** unless reinforced. The minimum is 16 Spearmen or 24 Archers (DP 1,182 ≥ 1,180).
- You send 30 Spearmen (4 bells; they arrive at bell 205): DP **1,245**, so the Hold holds (ratio 0.95) unless someone recalls troops before bell 210.
- The garrison loses 5.7%, or 17 troops: Aldric 5, you 8 Spearmen and 2 Archers, Cael 2.
- Renown +40 is split by Defense share (240 / 510 / 80): Aldric 11, you 25, Cael 4.
- While Greywatch is held, its 12 renown per bell splits 4 / 7 / 1.
- Your forecast home defense when Ironhold's raid lands at bell 207 (recruits and the forecast levy counted) drops from 691 to 583, still above the raid's 370.

---

## 10. Coalitions

### 10.1 Founding a coalition

Any kingdom outside a coalition can found one. Requirements:
- A name of 3–28 characters, unique per season (case-insensitive) and passing the name filter.
- A banner (two tinctures and a charge).
- An optional motto.
- A setting for **open to petitions** (on or off).

The founder becomes **Marshal**. The maximum size is **6 kingdoms** (config `coalitionMaxMembers`), which keeps several coalitions viable in a small realm.

### 10.2 Roles and permissions

| Action | Marshal | Captain (up to 2) | Sworn |
|---|---|---|---|
| Garrison or assault Holds, reinforce members, send caravans, pledge to orders | ✓ | ✓ | ✓ |
| Post or close War Table orders; edit Marshal's word | ✓ | ✓ | |
| Invite; accept or decline petitions | ✓ | ✓ | |
| Choose the Oath | ✓ | ✓ | |
| Kick (Captains can kick only Sworn members) | ✓ | ✓ | |
| Promote or demote; transfer leadership; propose, accept or cancel pacts; rename; change banner; disband | ✓ | | |
| Leave | after transferring leadership | ✓ | ✓ |

### 10.3 Joining

- **Invite.** A Marshal or Captain invites a kingdom by name, using autocomplete over the Realm directory. The invitee sees it in Coalition and in the Chronicle, and can accept or decline. Invites expire after 48 bells.
- **Petition.** A kingdom petitions a coalition that is open to petitions. Officers accept or decline. Petitions expire after 48 bells.
- **On acceptance** the server validates:
  - the kingdom is not already in a coalition;
  - the coalition is not full;
  - the roster is not locked (bell 601+);
  - the kingdom is not in an Oathbreaker cooldown.

  Then the kingdom joins as Sworn, members are notified, and its other pending invites and petitions are withdrawn.

### 10.4 Leaving, kicking, succession and disbanding

- **Leave** at any time (except during the roster lock). Your stationed troops at coalition Holds and allies' homes are recalled. Renown already earned stays with the coalition. **Oathbreaker:** you cannot join another coalition for 24 bells.
- **Kick:** same recall rules; the kicked kingdom gets no cooldown.
- **Succession:** if the Marshal slumbers (section 11.2), leadership passes to the longest-serving active Captain, or else to the longest-serving active Sworn member.
- **Disband:** only when the Marshal is the last member.

### 10.5 Cooperation verbs

- **Garrison Holds together.** A Hold's defense is the sum of all members' stationed troops, and renown is shared by contribution.
- **Reinforce a member's home.** Travel takes 4 bells, which is faster than the 8 a raider needs, so allies who answer a warning arrive in time. Stationed allies defend that kingdom, share its battle losses, and can be recalled. The owner keeps paying their upkeep.
- **Caravans.** Grain, iron, tools and arms only; never gold. Rules:
  - Travel takes 4 bells, and only between members.
  - Total cargo in transit is capped at 150 × your Markets.
  - The receiver must have been a member for at least 24 bells.
  - Kingdoms in Founding cannot send.
- **Call for aid.** One tap on an incoming threat or a forecast shortage posts an order to the War Table and notifies every member.

### 10.6 The War Table (structured coordination instead of chat)

- **Orders** are created by the Marshal or Captains. Types:
  - Reinforce Hold *X* (needed defense, deadline bell);
  - Reinforce member *Y*;
  - Supply member *Y* (goods, quantity);
  - Assault or reclaim Hold *X* (needed attack, muster bell; the planner offers the departure bell that arrives on it);
  - Rally (a note of 140 characters at most).
- Members **pledge** (an intention) and can open the matching march or caravan planner with one tap. **Progress counts only real dispatches** linked to the order: pledges are promises, and progress bars show only what has actually been sent. Orders expire at their deadline.
- **Marshal's word:** one 280-character banner message, editable by officers, reportable by anyone.
- No free-form chat exists inside the game at launch (section 18, A4).

### 10.7 Oaths (shared goals)

Each day, the coalition is offered 3 Oaths, drawn deterministically from a seeded pool, and officers swear one. Only one Oath is active at a time, with a 48-bell deadline. Progress is visible to all members, including each member's contribution. Failing an Oath only imposes a 12-bell cooldown.

| Oath | Goal | Renown |
|---|---|---|
| Hold the Line | Hold a named Hold continuously for 24 bells | 120 |
| Break the Tide | Repel 2 tides | 150 |
| Reclaim the Fallen | Reclaim a Fallen Hold | 200 |
| Shield-Brothers | Reinforce a member under raid, and repel that raid | 100 |
| Raise the Banners | Members complete training of 200 troops combined | 80 |

Renown is split by each member's contribution to the goal: defense share, attack share, troops trained, or the reinforcing kingdoms.

### 10.8 Pacts

Two Marshals can agree a **pact**: 72 bells of non-aggression, renewable. Raids and Hold assaults between the two coalitions are impossible, and the server checks this at both dispatch and arrival. Cancelling takes effect 12 bells after notice, and both sides are told. Pacts give the intro's "uneasy truces" a mechanical form while keeping betrayal slow and visible.

### 10.9 What coalition members can see of each other

- Home defense and incoming threats.
- Grain runway in bells (on by default; a member can opt out).
- Stationed forces.
- Activity, shown coarsely: within 1 h, today, or 2 days or more.

Rivals see only name, banner, coalition, Might and acres; anything more needs scouting.

---

## 11. Progression, victory and fairness

### 11.1 Victory and honors

- **Wardens of the Realm:** the coalition with the most renown at Reckoning.
- **Dawn:** if at least half the Holds, rounded up (4 of 7), stand after the final bell (held or unclaimed, not Fallen), every kingdom active at least 7 days earns the season mark *Saw the Dawn*. Otherwise the realm enters the Long Night. Rival coalitions therefore share one goal in the final week.
- **Honors for kingdoms:**
  - *Champion:* highest personal renown.
  - *Shield of the March:* most tide-defense renown.
  - *Reclaimer:* most reclaim renown.
  - *Rising Banner:* highest renown among late joiners.

  These give players outside the winning coalition something to aim for.
- **Account persistence:** accounts, banners, season marks and titles carry over. Kingdoms reset every season.

### 11.2 Protections

| Protection | Rule |
|---|---|
| **Founding** | First 72 world bells after founding. Nobody can raid you, and you cannot raid player kingdoms or assault rival-held Holds. Garrisons, claims, reclaims, reinforcing members and outlaw camps stay open, and none of them ends Founding. It protects your home, not troops you send to Holds. |
| **Respite** | 12 bells of raid immunity after any successful raid on you. A second raid landing in the same bell halts and returns. |
| **Power brackets** | You may raid only kingdoms with Might ≥ 60% of yours; renown requires ≥ 75%. Might = 10 × buildings + Σ troops at home or away × (Attack + Defense). |
| **Vault** | 400 × Keep level of each store can never be plundered. |
| **No destruction** | Raids never take land or destroy buildings. |
| **Slumber** | After 72 bells without a login: immune to raids; excluded from realm medians and active counts; production continues; stationed troops stay where they are. Logging in ends slumber. |

**Ending Founding.** Founding ends at its 72nd world bell, or earlier through the separate order `END_FOUNDING`, and in no other way.
- **The opt-out is confirmed.** Its review states that protection ends at once, so raids can be dispatched against you from that moment and the first could land 8 bells later. Unused founding bells are lost. Raids, rival-Hold assaults and caravans unlock. It cannot be undone.
- **No dispatch ends it as a side effect.** While Founding lasts, a raid or rival-Hold assault fails its preview with `FOUNDING_ACTIVE`, plus an "End Founding first" action that opens the review above. After confirming, the player returns to the same march plan and reviews it as usual.
- **Scheduled departures cannot pre-arrange an attack.** A kingdom in Founding cannot schedule a raid or rival-Hold assault, even to depart after Founding ends. Raids against a kingdom in Founding are refused when dispatched or scheduled (`TARGET_PROTECTED`).

### 11.3 Anti-snowball rules

- **Diminishing hold renown:** 12 / 9 / 6 / 3 for each additional Hold.
- **Brightness:** tides against a coalition's Holds grow 25% stronger for each extra Hold it owns.
- **Defense is easier than attack:** Holds ×1.5, Walls up to +50%.
- **Upkeep:** wages and grain make large armies costly whether or not they fight.
- **Land:** steepening clearing costs.
- **Coalition cap** of 6.
- **Comeback:** Nightfall raises hold renown 1.5× for every coalition, and rival coalitions can form pacts against the leader.

### 11.4 Recovery

- Losses are bounded: no land loss, no building loss, vaults, Respite.
- Every shortage has a visible way out:
  - grain → build a Farm, buy at the Exchange, or call for aid;
  - gold → tithe always flows (every commoner pays 1 per bell), or disband or recall troops;
  - troops → recruit, and training takes only 2–4 bells.
- Starvation can only remove commoners at 5% per bell. Because Farms are staffed first by default, a starving kingdom keeps farming and settles at the population its farms can feed (7.9 B).

### 11.5 Late join

New kingdoms may found until the end of day 21. Package by founding day:

| Founded | Package |
|---|---|
| Days 1–3 | Standard start |
| Days 4–10 | Keep level 2 at start; stores ×2 |
| Days 11–21 | Keep level 3 at start; stores ×3; 20 extra cleared acres (70 total, 42 free) |

All late joiners also get Founding protection, 12 founding bells, and **Rising Banner**: +40% production and −25% construction time while their Might is under 60% of the realm median. Rising Banner ends permanently once the threshold is reached, or at Nightfall.

### 11.6 Season reset

At Reckoning the season archive stores, read-only:
- final standings;
- the renown ledger;
- battle reports;
- each kingdom's final summary.

A new season starts after 48 bells.

---

## 12. Bots, system actors, multi-accounts and exploits

- **No bot kingdoms in live seasons.** The only system actors are the Hollow Host (a deterministic, published schedule) and private outlaw camps. Both are labeled as such everywhere.
- **Current bots become test tools.** `BlacksmithBot`, `FarmerBot` and the others turn into strategy simulators for the S8 harness, running headless against the engine to produce balance curves. If a future low-population season needs AI kingdoms, they must be labeled, kept out of coalitions and Holds, and excluded from medians.
- **The "add bot to alliance" feature is removed.** It let a leader create unlimited kingdoms.
- **Multi-accounts.**
  - One kingdom per account per season; email verification before joining a season; registration throttling.
  - Shared device or IP signals between kingdoms that trade, raid or ally go to an admin review queue (no automatic bans).
  - Structural limits matter more than detection: gold cannot be transferred; caravans are coalition-only, cargo-capped, blocked from Founding senders and for receivers with under 24 bells of membership; plunder is capped by vault and carry capacity; raid renown needs a target ≥ 75% of your Might; renown cannot move between coalitions; the coalition cap means alts use up scarce member slots.
- **Exploit-proofing contract:**
  - Strict server validation rejects negative, zero-where-invalid, oversized (more than 100,000 per field) and unknown-field input with no state change.
  - All arithmetic uses checked 64-bit math.
  - Each kingdom row is locked (`SELECT … FOR UPDATE`) per action and per bell.
  - Idempotency keys stop double spends.
  - Object-level authorization covers every expedition, caravan, invite and order id.
  - Private data is reachable only through scouting or coalition visibility.

---

## 13. UX blueprint

### 13.1 Information architecture

| Destination | Route | Purpose | Contents |
|---|---|---|---|
| **Kingdom** | `/kingdom` | What should I build and change? | Counsel, stores and forecast, construction (crews, queue, planner, works), people and work (housing, jobs, staffing priority, forge split), land, Exchange panel, founding objectives during Founding |
| **Muster** | `/muster` | Am I safe? Where should my troops go? | Home defense breakdown, incoming threats, troops and recruitment, expeditions (marching, stationed, returning), march planner (`/muster/march?to=…`), recent reports |
| **Frontier** | `/frontier` | What is happening on the March? | Season phase, Dread, Hold list and schematic map, next tides, Hold detail (`/frontier/:hold`) |
| **Coalition** | `/coalition` | What does my coalition need from me? | Marshal's word, Oath, War Table, members, caravans, invites and petitions, pacts. For non-members (`/coalition/browse`): open coalitions, petition, found |
| **Chronicle** | `/chronicle` | What happened? | "Since your last visit" digest, filters, event feed, full reports (`/chronicle/:id`) |

Secondary routes, reached from the header menu: `/standings`, `/realm` (kingdom directory), `/settings`, `/help` (codex and glossary). **The selected navigation item always comes from the route**, which fixes U5. A landing page (logged out) explains the game in three panels with a sample screenshot, then offers Found a kingdom and Sign in.

### 13.2 Layouts

- **Desktop (≥ 1100px):**
  - Left rail (about 216px): wordmark, the five destinations, season progress.
  - Header across the main and right columns: kingdom banner, name and ruler; bell clock and phase; the stores strip with per-bell deltas.
  - Main column (fluid, readable width).
  - Right column (320px): Next bell, counsel, incoming.
- **Tablet (700–1099px):** the rail collapses to labeled icons; the right column moves under the stores strip as a collapsible "Next bell" panel.
- **Phone (< 700px; designed at 390px, checked at 320px):**
  - Compact header: banner, name, bell clock, menu.
  - Stores strip as a 5-column grid (value plus delta; a tap opens the breakdown).
  - **Sticky top navigation** of five 44px-tall tabs with icon and label. It sits at the top, not the bottom, to avoid mobile browser toolbars and on-screen keyboards.
  - A collapsible "Next bell" summary card, then content.
  - Tables become card lists. No horizontal scrolling: `scrollWidth ≤ viewport` is an acceptance test.

### 13.3 Key screen content

- **Kingdom:**
  - Up to 3 counsel cards, ordered danger, warning, opportunity. Each has a direct action ("Plan 4 Farms", "Buy 500 grain", "Plan reinforcement").
  - Crew cards with progress and completion bell.
  - The construction planner: one row per building with effect, cost and time; steppers; a live server preview of total cost, resulting stores, completion bells, acres and crews.
  - The staffing priority list (reorderable).
  - The forge split slider with the next bell's tools and arms.
  - Land clearing quote; works upgrade cards that show "affordable in N bells" from the forecast.
- **Muster:**
  - Defense card: troops, levy and walls, as a formula with numbers.
  - Incoming cards: sender, composition, AP, arrival bell and time, the verdict against your defense forecast for that bell (recruits finishing and the forecast levy included), and actions (recall, request reinforcement, recruit Spearmen with "ready in 2 bells").
  - Troop cards with recruit steppers and the capacity meter.
  - The expedition list.
  - **March planner:** destination cards → composition steppers → live forecast (arrival and return bells and times; the outcome, exact against camps, tides and the Darkness, or an estimate against a rival's last known strength and its age; losses; plunder; home DP against each incoming threat from the bell after departure; effect on the Hold at its next tide) → Review → Dispatch.
- **Frontier:** phase timeline; Dread meter; Hold cards (controller, garrison DP, next tide bell and strength, verdict, your contribution, actions); a standings summary (the full table lives at `/standings`). A schematic map is a visual extra with a text equivalent; the list is the source of truth and the default on phones.
- **Coalition:** header (banner, rank, renown and rate, members out of 6, your role); Marshal's word; Oath progress with contributions; War Table orders with pledges, real progress and one-tap planners; members with defense, threats, runway and activity; caravan planner; invites and petitions; pacts.
- **Chronicle:** a digest at the top ("6 bells since your last visit: Market complete; tide repelled at Greywatch, +25 renown; Ironhold scouted you"). Filter chips: All, Battles, Kingdom, Coalition, Frontier. Entries expand into full reports with a reconciliation table. Unread entries are marked.

### 13.4 The action contract: plan → preview → review → commit → receipt

1. **Plan.** Inputs live on the client and are saved to `sessionStorage` per kingdom, so a reload or expired session does not lose them.
2. **Preview.** Debounced (250 ms) `POST /api/v2/kingdom/actions/preview` returns:
   - `valid`, plus `blocking[]` reasons with codes and need/have figures;
   - `warnings[]`;
   - exact costs and resulting stores;
   - effects and completion bells;
   - `stateVersion`.

   While it loads, the preview shows "Updating forecast…" without moving the layout. The preview uses the same server code as the commit.
3. **Review.** An inline step in the same panel, not a modal: it summarizes what will happen and what it will cost, with completion times. Focus moves to the review heading. A **Confirm** button and a **Back** link.
4. **Commit.** `POST /api/v2/kingdom/actions` with an `Idempotency-Key` header (generated when Review opens) and `expectedVersion`. Every other commit button for that kingdom is disabled, and the button reads "Ordering…".
5. **Outcome:**
   - **Applied:** an inline **receipt** with exact amounts and completion bells, also saved to the Chronicle. Focus moves to the receipt and the live region announces it.
   - **Rejected:** an inline error next to the cause, saying what is missing and how to fix it ("Need 30 tools, have 22: buy 10 for 100 gold, or wait 1 bell"). The plan is kept.
   - **Conflict (state changed):** "A bell rang while you reviewed (bell 202). Stores and crews changed." The refreshed preview highlights the differences, and the player must confirm again.
   - **Unknown (network):** "Checking whether your order went through…" The client polls `GET /api/v2/actions/{key}` for up to 30 s and shows the definitive result. If the result is still unknown, it says so, keeps the plan and links to the Chronicle. It never encourages blind resubmission.

### 13.5 Forecasts

A forecast is the authoritative engine run forward on a copy of the world **as the player can see it**, assuming nobody gives a new order. It runs the same code as bell processing: every economy, then the world phase of 8.4.

- **Modeled inputs:**
  - your kingdom and its standing orders;
  - your marches (dispatched and scheduled), stationed forces, and returns with their cargo;
  - everything you can see heading for you: incoming marches, allied reinforcements, caravans;
  - your coalition's Holds with their full garrisons, plus published tides and garrison totals elsewhere;
  - Darkness garrisons and your outlaw camps;
  - Dread at its current level.
- **Exact lines** depend only on modeled inputs: stores, people, construction, training, home troops, arrivals, and the battles at your home, your camps, Fallen Holds and Holds where your troops stand. They equal the result unless a modeled input changes first, through a new order from anyone (dispatch, recall, turn back), a coalition change, a pact, or another coalition reaching the same unclaimed or Fallen Hold first or in the same bell. When an input changes, the forecast updates, and the reconciliation names the change instead of claiming a match.
- **Estimates** depend on what you cannot see: the outcome, losses and plunder of your raid or assault on a rival. They appear beside the forecast with their source and age ("Ironhold defense 455, scouted 5 bells ago") and are never folded into exact lines. Until such a battle resolves, the forecast carries your troops at full strength and adds no plunder. Pledges are promises, not orders, so forecasts leave them out.
- **Dread is held at its current level,** because Hold outcomes outside your coalition depend on marches you cannot see. Instead, the runway marks each published tide that would take a Hold at today's garrisons, with the Dread it would add. A bell's tides change Dread only from the following bell's economy, so the next-bell forecast never depends on them.
- **Next bell:** the forecast's first bell. Per-store deltas with the six-step economy breakdown (7.7), plus a world line for each arrival, returning cargo, battle and plunder due that bell; people change; completions.
- **12- and 24-bell runway:** the same forecast continued, so its first bell always equals the next-bell forecast. Grain and gold curves; completion, arrival, battle and tide markers; a "Hunger in N bells" or "Wages unpaid in N bells" marker whenever one applies. Tides not yet scheduled are left out, because their strength is unknown until then.
- **Labels:** every forecast states its assumptions: "Assuming no new orders · Dread held at −3% · world events included."
- **Reconciliation:** founding-bell results, receipts and battle reports compare the forecast made just before the bell with the result, line by line, with economy and world events shown separately. A founding bell moves only your own lands, so its reconciliation must always match exactly.
- **Contract test (C1, 19):** for every kingdom at every bell, the next-bell forecast computed from the final pre-bell state, using only that kingdom's visible inputs, equals the processed bell on every exact line. Apart from estimates, a kingdom's next bell depends only on what it can see. The one exception is a claim or reclaim contested in the same bell; that is the only permitted difference, and the reconciliation must name it.
- Forecasts come from the server engine, never from client-side reimplementation. The prototype computes them client-side only because it has no server.

### 13.6 States

| State | Treatment |
|---|---|
| Loading | Skeletons sized like the final layout. If configuration or state fails to load, retry with backoff and show a "Retry now" button (fixes the stuck "Loading…" in U3). |
| Stale | The header shows "Updated 14:02". After 2 minutes without a successful refresh, a stale badge appears. Polling runs every 30 s while visible, at each bell boundary, and after every action. |
| Offline | Banner: "Connection lost: showing your kingdom as of 14:02. Orders are paused until you're back." Commit buttons are disabled, and plans stay intact. |
| Session expired | Blocking dialog: "Your session has ended. Sign in to continue; your plans are kept." After sign-in the app reruns previews, never commits automatically. Tokens: 15-minute access token plus a rotating 14-day refresh cookie (httpOnly). The prototype shows this panel inline at the top of the view, only because it renders inside a document. |
| Wrong credentials | Inline message: "Email or password is incorrect." Rate-limited with a visible retry time. |
| Server error | "Your order was not placed (KOD-5xx-…). Try again." This appears only when the server has confirmed nothing was applied; otherwise the Unknown flow applies. |
| Empty Chronicle | "Quiet so far. Your first report arrives when a bell rings; ring a founding bell to see one now." |
| No coalition | "You stand alone. Coalitions hold the March together and share renown." Actions: Browse coalitions, Found a coalition. |
| No expeditions | "No troops on the march." Action: Plan a march. |
| Before the season | "The March is quiet until the season opens on …" Founding and coalition setup are still available. |

### 13.7 Keyboard and accessibility (WCAG 2.2 AA target)

- **Landmarks:** header, nav (`aria-label="Primary"`), main, aside ("Next bell"). One `h1` per view, with a logical heading order.
- **Labels:** every input has a visible label, and its accessible name contains that label. Steppers are labeled "Decrease Farms", "Increase Farms" and "Set Farms to the most you can order". **Max** asks the preview for the executable maximum, accounting for everything else already in the plan. It then announces the limiting reason ("Farms set to 4: limited by free acres"), which fixes the assessment's 0-capacity Max defect. Number inputs support Arrow keys ±1 and Shift+Arrow ±10.
- **Reordering** (staffing priority): Move up and Move down buttons on each row; Alt+Arrow on a focused row; each move is announced.
- **Live regions:** previews are polite and debounced; commit results are polite; errors are assertive. Focus moves to the review heading, then to the receipt or error.
- **Shortcuts:** 1–5 switch destinations when focus is not in a text field; `?` lists shortcuts; Esc closes drawers and dialogs, returning focus to the control that opened them. Shortcuts can be turned off in Settings.
- **Visual and motor:**
  - Contrast ≥ 4.5:1 for text and ≥ 3:1 for UI elements.
  - Color is never the only signal: statuses carry text and a symbol.
  - Visible 2px focus rings.
  - Touch targets ≥ 44 × 44px.
  - Reflow at 320 CSS px and at 200% zoom.
  - `prefers-reduced-motion` turns off pulses.
- **The map** is decorative (`aria-hidden`); the Hold list carries all of its information.
- **Times** are always relative plus absolute local time; the time zone is shown in Settings.

### 13.8 Language and numbers

- **Plain names** everywhere: "Farm", "Spearmen", "Grain". Never `goldMiners` or `buildingPoints`.
- **Number formatting:** thousands separators. Abbreviations (12.4k) appear only in the compact phone strip, where the accessible name gives the full value. No raw decimals: show "+10%", never `1.1`.
- **Every derived number has a "why" disclosure** showing its formula with the player's own values.
- **Chronicle and counsel speak in short, concrete sentences:** what happened, why, and what can be done next.

---

## 14. Visual direction: "the candlelit ledger"

- **Mood.** A war-room ledger read by candlelight: dark warm surfaces, parchment-colored text, heraldic color used sparingly, engraved hairline rules. Atmosphere comes from type, color and restraint, not from textures or illustration.
- **Typography:**
  - Display: an old-style serif stack (`"Iowan Old Style", "Palatino Linotype", Palatino, "Book Antiqua", Georgia, serif`) for headings and names.
  - Interface: `system-ui`.
  - Numbers: `font-variant-numeric: tabular-nums` everywhere.
  - Labels: small caps with slight tracking.
  - Body text 15–16px; nothing below 12px (13px on phones).
- **Color tokens** (contrast measured on the panel color `#1b171d`):

| Token | Value | Use | Contrast |
|---|---|---|---|
| `--bg` | `#0f0d10` | page | n/a |
| `--panel` | `#1b171d` | cards | n/a |
| `--text` | `#ece3cf` | primary text (parchment) | 13.9:1 |
| `--text-2` | `#c2b59a` | secondary text | 8.7:1 |
| `--text-3` | `#968a73` | tertiary text | 5.2:1 |
| `--or` | `#d6ae5c` | primary action and emphasis (heraldic *or*); dark ink `#1a1408` on it is 8.8:1 | 8.5:1 |
| `--gules` | `#ec8b74` | danger text (heraldic *gules*) | 7.2:1 |
| `--vert` | `#86b071` | positive | 7.1:1 |
| `--azure` | `#7ea0d6` | coalition and info | 6.6:1 |
| `--amber` | `#e2a447` | warning | 8.1:1 |
| `--hollow` | `#9d8fb0` | the Hollow Host | 5.9:1 |
| `--control-line` | `#857788` | borders of inputs and controls (non-text contrast ≥ 3:1) | 4.2:1 |
| `--line` | `#4a3f4d` | decorative rules only; never the only boundary of a control | 1.8:1 |

- **Ornament:** a thin double rule and a small lozenge (◆) at section heads. Banners are CSS or SVG shields (two tinctures and one charge from a fixed set). Hold markers are small lozenges. No decorative frame around whole pages.
- **Motion:** a brief pulse when a bell rings and progress-bar easing; nothing loops. Respect reduced motion.
- **Charts** (runways, power bars, timelines): one hue per meaning, always labeled with numbers, plus a blue/orange pair that colorblind players can tell apart.
- **Implementation:** keep React, TypeScript, Vite and MUI. Express the tokens as an MUI theme; use MUI for accessible primitives (dialogs, menus, tabs) and custom responsive card lists instead of `Table` on phones.

---

## 15. What happens to each current system

| Current system (source) | Decision | New form |
|---|---|---|
| Turns resource, turn bank, `TurnAdd`, 10 s cadence, cap 36 | **Remove** | World bells (4.1); founding bells for onboarding only (4.2) |
| `buildingPoints`, builders, `KingdomBuildAction` | **Remove / rework** | Crews, pay-at-order queue, build times (7.5) |
| Gold mine and gold miners | **Rework** | Tithe plus Market merchants (7.1) |
| Iron mine and miners, farm and farmers | **Keep (renamed)** | Mine and Farm with automatic staffing (7.2) |
| Workshop and blacksmiths, `weaponsProductionPercentage` per turn | **Rework** | Forge with a standing tools/arms split |
| House | **Keep** | Cottage (+15 housing) |
| Market building and carriers, `KingdomCarriersAction` | **Rework** | Market (merchants plus cargo capacity); coalition caravans, gold excluded (10.5) |
| Barracks | **Keep** | Troop capacity |
| Guard house and guards, spy guild and spies | **Remove** | Defense comes from Spearmen and Walls; scouting is an action (8.7) |
| Tower, castle | **Rework** | Walls (levels); Keep (levels) (7.4) |
| Special buildings (shafts, granary, forge, warehouse), `KingdomSpecialBuildingAction` | **Rework** | One Guild specialization with 3 levels (7.4) |
| Training and firing professionals, `KingdomTrainAction` | **Replace** | Automatic staffing; recruitment and population-conserving disband apply to troops only (8.2) |
| Infantry, bowman, cavalry | **Rework** | Spearmen, Archers, Knights with published Attack and Defense (8.1) |
| `AttackType` economy/land, `KingdomInteractor.resolveAttack` | **Replace** | Destination-implied orders; deterministic battle engine (8.4–8.5) |
| Buy land (`KingdomOtherAction`) | **Rework** | Clear land with an exact quote and a steepening cost (7.6) |
| Kingdom size production bonus (5× at 100 occupied) | **Remove** | Explicit early mechanisms (founding bells, Rising Banner) |
| Player market (`Market`, offers, transactions, averages) | **Replace** | System Exchange (7.8); legacy tables archived |
| Alliances, invitations, emperor | **Rework** | Coalitions with roles, invites, petitions, War Table, Oaths, pacts (10) |
| Add bot to alliance | **Remove** | n/a |
| Bots (`bots/*`, 5 s scheduler) | **Rework** | Headless strategy simulators for S8; no bots in live seasons |
| Notifications | **Rework** | Typed Chronicle events, digests, unread state (13.3) |
| Stats (top kingdoms by land) | **Replace** | Standings by renown, plus the Realm directory |
| `UserEntity` and `UserData` (email used as the username in kingdom lookups), `AuthController` | **Rework** | Account id; season-scoped kingdom id; atomic, unique registration (16.3) |
| `KingdomEntity` with name as primary key | **Replace** | `kingdom` with a UUID, unique (season, account) and (season, name key) |
| `GameConfig` / `gameconfig.json` | **Rework** | Versioned season configuration snapshot with a JSON schema (Appendix A) |
| `GameInitializer`, `UserInitializer` | **Restrict** | Development profile only; sandbox seeding |
| `application-prod.yaml` `ddl-auto: create` | **Replace** | Flyway migrations and `ddl-auto: validate` |
| Browser shell: `Sidebar`, `KingdomToolbar`, `KingdomTabs`, nine tabs | **Replace** | Five-destination shell with route-derived navigation (13.1–13.2) |
| Browser API client and `GameTypes` | **Replace** | Typed v2 client from an OpenAPI contract, with runtime validation |
| MUI dark theme defaults | **Rework** | MUI theme from the section 14 tokens |

---

## 16. Architecture, contracts and migration

### 16.1 Authoritative engine

- **Pure, deterministic Java engine** with no clock, randomness or I/O:
  - `KingdomEconomy.advance(state, modifiers) → (state', report)`
  - `BattleEngine.resolve(attackers, defenders, context) → result`
  - `WorldPhase.resolve(world, bell) → (world', events)`, the seven steps of 8.4
  - `TideEngine.schedule(...)`
- **The same code serves** bell processing, founding bells, previews (run against a copy of the state) and forecasts (economy and world phase repeated on a copy of the player's visible world: 13.5). Forecast-equals-actual holds because both run this code on the same inputs, and a kingdom's next bell depends only on inputs it can see, apart from the estimates and the one exception named in 13.5.
- **Golden tests** reproduce every worked example in this document.

### 16.2 API (v2, alongside the legacy API until cutover)

- `GET /api/v2/me`: account, current season, kingdom summary.
- `GET /api/v2/kingdom`: full snapshot with `version`, `bell`, `nextBellAt`.
- `GET /api/v2/kingdom/forecast?bells=12|24`.
- `POST /api/v2/kingdom/actions/preview`; `POST /api/v2/kingdom/actions` (with `Idempotency-Key`); `GET /api/v2/actions/{key}`.
- **Action envelope:** `{"type":"BUILD","params":{"items":[{"building":"FARM","count":2}]},"expectedVersion":418}`. Lists replace maps; no field may be omitted; unknown fields are rejected.
- **Action types:** `BUILD`, `CANCEL_PROJECT`, `DEMOLISH`, `UPGRADE_WORKS`, `CHOOSE_GUILD`, `CLEAR_LAND`, `SET_STAFFING_PRIORITY`, `SET_FORGE_SPLIT`, `RECRUIT`, `DISBAND`, `DISPATCH`, `RECALL`, `TURN_BACK`, `SCOUT`, `EXCHANGE_TRADE`, `SEND_CARAVAN`, `RING_FOUNDING_BELL`, `END_FOUNDING`.
- **Coalition endpoints:** `/api/v2/coalitions/…`. Consequential actions (leave, kick, accept, pact) use the same preview and commit contract.
- **Errors** are `application/problem+json` with stable codes (`INSUFFICIENT_STORES`, `NO_FREE_ACRES`, `TROOP_CAPACITY`, `STATE_CONFLICT`, `TARGET_PROTECTED`, `FOUNDING_ACTIVE`, `OUT_OF_BRACKET`, `PACT_ACTIVE`, `PHASE_LOCKED`, `NOT_PERMITTED`, `VALIDATION`) and structured `need`/`have`/`field` details. HTTP 200 always means applied.

### 16.3 Data model (PostgreSQL)

**Identity, season and kingdom:**
- `account`: id, `email` (citext, unique), password hash, status.
- `season`: id, status, `start_at`, `bell_seconds`, `length_bells`, `current_bell`, `paused`, `config` (jsonb snapshot).
- `kingdom`: id, season, account, name, `name_key`, banner, Founding/Respite/slumber bells, `founding_bells_left`, acres, works levels, guild, commoners, forge split, staffing priority, five stores, troop counts, `version`. Unique (season, account) and (season, `name_key`).
- `kingdom_building` (kingdom, type, count).

**Orders and movement:**
- `construction_project` (state active or queued, sequence, bells left, cost paid).
- `training_batch`.
- `expedition` (purpose, target, composition, cargo, dispatch/arrive/return bells, state).
- `stationed_force`.
- `outlaw_camp`.

**Frontier and coalitions:**
- `hold` (status, controller, `controlled_since_bell`, Darkness garrison, next tide bell and strength).
- `coalition`, `coalition_member` (role, joined and left bells), `coalition_invite`, `coalition_petition`, `pact`, `oath`, `war_order`, `war_pledge`.

**Records and plumbing:**
- `renown_ledger`, append-only (season, kingdom, coalition, bell, source, amount, ref).
- `battle_report` and participants.
- `chronicle_event` (kingdom, bell, type, payload, `read_at`).
- `idempotency_record` (key, kingdom, request hash, response, created).
- `exchange_volume`.

### 16.4 Bell processing

- A scheduler fires at `start_at + (current_bell + 1) × bell_seconds`.
- `WorldBellService.process(season, bell)` takes a season advisory lock and checks that `bell = current_bell + 1`, so it is idempotent.
- It runs every kingdom's economy, then the world phase (8.4), writes Chronicle entries and the ledger, then increments `current_bell` in the same transaction. Batching by kingdom chunks is allowed as long as the world phase runs after all economies.
- Player actions take the kingdom row lock, so they serialize with bell processing.
- The catch-up and pause policy is in 4.4.

### 16.5 Migration boundaries (protecting existing data)

1. **M0, before any gameplay change:**
   - Add Flyway with a baseline of the current schema as it is, and switch production to `ddl-auto: validate`.
   - Guard the initializers to the development profile.
   - Take and verify a database export.
   - No data changes.
2. **M1, accounts:** create `account` from `user_entity`, keeping password hashes. Lower-case emails; conflicts go to admin resolution, never auto-merge. Tokens move to account ids. `user_entity` stays until the migration is verified.
   - M1 also classifies every legacy kingdom name into a read-only report, `legacy_name_claim` (name key, claiming accounts, archived kingdom row if any). Legacy `user_entity.kingdomName` is not unique, so this cannot assume one owner per name.
   - **Sole claim:** exactly one account names the kingdom.
   - **Disputed claim:** two or more accounts name the same kingdom, or names collide once case is folded into `name_key`. The assessment's B1 produced exactly this: a second registration reused a kingdom name, reset that kingdom's state, and left both accounts able to read it.
   - **Orphan:** a kingdom row that no account names, such as bots and seeds.
3. **M2, v2 schema:** v2 tables are created beside the legacy ones. Legacy kingdoms, alliances, market and notifications are **not converted**; they become read-only (renamed `legacy_*` or moved to a `legacy` schema) and the legacy UI and API are withdrawn at cutover.
4. **M3, first season:** accounts carry over, and legacy names are reserved by claim class:
   - A sole claim is reserved for its account for season 1's first 72 bells.
   - A disputed name is **quarantined**: reserved for nobody, including its claimants, until an admin decides. Both accounts stay fully usable. At sign-in, each claimant is told the name is under review and can found a season kingdom under another name at once, with one free rename if the decision later gives them the name.
   - The admin decision is one of: give the name to one claimant, release it, or keep it reserved. It is recorded with its evidence and reason in an append-only audit log, both claimants are notified, and it changes only the season name. Evidence is the archived rows, the M0 export and any surviving registration logs.
   - Legacy rows, including a shared and possibly reset kingdom, stay exactly as archived. Nothing is merged, restored or reassigned to either account.
   - No disputed name is given out automatically. An undecided quarantine continues past the 72 bells; it never lapses into first come, first served.
   - Orphan names reserve nothing.
5. **M4, cleanup:** legacy tables may be dropped only after season 1 ends, an export exists, and the owner explicitly decides to.

Every migration is tested in CI against a disposable PostgreSQL (Testcontainers), including a restart-persistence test (which covers assessment B6).

---

## 17. Implementation slices and acceptance criteria

Order matters: each slice ships behind the previous one's gates. The first human playtest happens after S3.

| Slice | Delivers | Acceptance criteria (automated unless stated) |
|---|---|---|
| **S0: integrity platform** | M0–M1; account and kingdom identity; v2 action envelope (preview, commit, idempotency, problem+json); strict validation; locking; refresh tokens; browser session, error and retry infrastructure | Sequential and concurrent duplicate registration → 409 with no mutation (B1). Replaying an idempotency key returns the original response without re-applying. Property tests over negative, zero, omitted, oversized, overflow and unknown-field payloads → 400 with an unchanged state hash (B2, B3). A restart against PostgreSQL preserves state (B6). A config-load failure recovers through retry (U3). The M1 dry run on a copy of the M0 export reports sole, disputed and orphan name claims; a fixture where two accounts share one legacy kingdom name yields a quarantined name that neither receives, both accounts sign in, and the archived rows' hashes are unchanged. |
| **S1: season and bell engine** | Season entity; scheduler with catch-up and pause; pure economy engine; construction and training; land; Exchange; forecast endpoint | Golden tests reproduce 5.2, 5.3, 7.6 and 7.9 exactly. Randomized invariants (10,000 cases): no negative stores or counts; population is conserved by recruit and disband; determinism; every exact line of forecast(1), world phase included, equals the processed bell. Reprocessing bell N is a no-op. Catch-up processes 6 bells and then pauses. |
| **S2: shell, Kingdom, Founding** | Five-destination shell; header, stores and forecast panel; counsel; construction planner; staffing; forge; land; works; Exchange; founding bells and objectives | Playwright: a new account completes founding objectives 1–5 at 390px and 1440px using the keyboard only (synthesized keys; human keyboard and screen-reader acceptance is U5). `document.scrollWidth ≤ innerWidth` at 320, 390, 768 and 1440. axe-core reports zero serious or critical violations. Direct loads, refreshes and Back/Forward keep the navigation selection correct. Every committed receipt equals its preview. |
| **S3: military** | Recruitment and disband; outlaw camps; expeditions with recall, turn-back and scheduled departure; combined same-bell attacks; battle engine; plunder; scouting; reports; incoming warnings | Golden battle tests (8.9; 30 scripted mixes at 10, 100 and 1,000 troops). Casualty totals equal roundHalfUp(L × N). Plunder respects vault and carry. Expedition state machine tests cover halting at arrival and at scheduled departure. A scheduled march defends home through its departure bell and leaves after that bell's battles. Zero-troop dispatches are rejected, and battles against DP 0, levy-only defense and exact ties follow 8.5 without NaN or infinite values. Same-coalition arrivals in one bell merge into one battle, with shares by Attack. The home-defense timeline in previews matches resolved battles. Reports reconcile with previews. **Human playtest 1** (section 19). |
| **S4: coalitions** | Create, invite, petition, accept, decline, leave, kick, promote, transfer, disband; caravans; reinforcement; War Table; Marshal's word; call for aid | Two fresh browser accounts complete create → invite → accept → caravan → reinforce → leave through the UI. A role × action authorization matrix test. Caravan caps and the 24-bell rule. Renown attribution survives leaving. |
| **S5: Frontier** | Holds; tide scheduling and strength; militia; Fallen Holds; Dread; renown ledger; Oaths; standings; phases | Golden test of 9.5. Tide schedule and published strength are deterministic and frozen at scheduling. Ledger totals equal standings. Dread changes production exactly. A 48-bell scenario (4 kingdoms, 2 Holds) matches its expected ledger. |
| **S6: PvP and pacts** | Raids; Respite; brackets; pacts; scouting notices | Protections are enforced at dispatch **and** at arrival. Two raids landing in the same bell resolve in order, and the second halts under Respite. A kingdom in Founding can neither raid, assault a rival-held Hold nor schedule either; `END_FOUNDING` is confirmed, immediate and irreversible, and no dispatch ends Founding by itself. |
| **S7: season lifecycle** | Late join and Rising Banner; slumber and succession; Reckoning; honors; archive; next season | Packages apply by founding day. Rising Banner ends at the threshold or at Nightfall. Slumber starts at 72 bells and ends at login. Reckoning freezes standings and awards titles. **Human playtest 2**: a compressed mechanics season (section 19). |
| **S8: measurement** | Bots reworked as strategy simulators; nightly balance report; privacy-respecting telemetry | Four strategies (builder, trader, militarist, holder) × 672 bells run in CI. Curves are published as artifacts. Telemetry has no third-party trackers and offers an opt-out. **Human playtest 3**: the real-time retention study (section 19), which gates launch. |

---

## 18. Risks and resolved assumptions

These assumptions resolve open intent questions so the work is not blocked. The owner can overturn any of them; the section that would change is noted.

| # | Assumption | If wrong, revisit |
|---|---|---|
| A1 | Players check in 2–4 times a day for 5–12 minutes | Bell length and travel times (4, 8) |
| A2 | 12–60 human kingdoms per season | Hold count, coalition cap (9.1, 10.1) |
| A3 | No monetization; any future monetization is cosmetic only | n/a |
| A4 | No in-game free chat at launch; coalitions use the War Table and their own Discord | 10.6 |
| A5 | Dark, grounded tone; the cartoon character prompts are superseded for UI and illustration | 1, 14 |
| A6 | 28-day seasons with hourly bells; alpha seasons may compress to 10-minute bells, which test mechanics but not daily rhythm (19) | 4, 19 |
| A7 | Legacy game state is archived, not converted; accounts carry over | 16.5 |
| A8 | Polling at 30 s plus bell boundaries is enough; server-sent events can come later | 13.6 |
| A9 | English first, with externalized strings | n/a |
| A10 | Deterministic combat (no dice): trust beats variance in an async game | 8.5 |

| Risk | Mitigation |
|---|---|
| Hourly bells feel slow at first | Founding bells; visible completions every check-in; compressed alpha for testing |
| Tiny population leaves the March empty | Holds scale with registrations; militia; tides scale with the realm median |
| One coalition dominates | Diminishing renown, brightness, upkeep, pacts, the Nightfall swing; watch F7 |
| Scope creep | Launch list in section 6; post-launch list is explicit; slices gate each other |
| Abusive names or messages | Name filter, report button, admin rename and remove; no free chat |
| Dynamic tide scaling produces odd numbers | Strength is fixed at scheduling, so it is always forecastable; S8 tunes the factor table |
| Implementation load for a small team | Pure engine, one action contract, v2 alongside legacy, slices ordered by risk |

---

## 19. Playtest plan: correctness, comprehension and fun

**Rule:** never tune for fun on a build that fails correctness. If comprehension fails while correctness passes, the problem is in UX or design. If fun fails while comprehension passes, the problem is in the systems.

**Correctness (automated, must pass before any human test):**
- **C1:** in staging runs of scripted kingdoms, for every kingdom at every bell, every exact line of the next-bell forecast equals the processed bell. Each forecast is computed from the final pre-bell state with only that kingdom's visible inputs (13.5). Before playtest 1 the runs cover the slices built so far; from playtest 2 on, they are full 672-bell seasons played by the S8 strategy simulators.
- **C2:** zero invariant violations (negative values, conservation, unpaid progress, duplicate identity).
- **C3:** every battle report reconciles to engine inputs; every receipt equals its preview.

**Comprehension (playtest 1, after S3; 5–8 newcomers, moderated, task-based):**
- **U1:** at least 4 of 5 finish founding objectives 1–5 unaided within 15 minutes.
- **U2:** after objective 2, at least 4 of 5 predict the sign and size (within 20%) of the next bell's grain change.
- **U3:** at least 4 of 5 explain a battle outcome from its report.
- **U4:** two 390px participants (one on a real phone) complete the tasks without horizontal scrolling or zooming.
- **U5:** a keyboard-only participant and a screen-reader participant complete objectives 1–4.
- **U6:** each participant recovers from one rejected action and one simulated connection loss without help.

**Fun and fairness: two playtests that answer different questions.** A compressed season is fast enough to play a whole arc. But at 10-minute bells one real day covers six game days, so nobody can play at the intended rhythm of 2–4 check-ins per game day. Its results never stand in for daily participation or for fairness to the absent.

| | Playtest 2: compressed mechanics season | Playtest 3: real-time retention study |
|---|---|---|
| When | After S7 | After playtest 2 passes and S8's telemetry ships; gates launch |
| Clock | `bellSeconds` 600: the 672 bells take 112 hours (4 days 16 hours), plus 48 bells (8 hours) of Reckoning | Live `bellSeconds` 3600: one game day per real day |
| Length | One full season | At least 7 consecutive real days (Kindling and Long Dusk days 4–7), ideally the full 28-day season |
| Players | 12–30 | 12–30, mostly people who were not in playtest 2 |
| Answers | F3–F7, measured in bells and game days | F1 and F2, plus F3, F5 and F6 at the real pace |
| Cannot answer | F1, F2, or any question about daily habit | F7, unless it runs the full season |

- **F1, return (playtest 3 only):** at least 60% of players return on at least 5 of the first 7 real days; median 2–4 check-ins per real day of 5–12 minutes each.
- **F2, fairness to the absent (playtest 3 only):** renown per active day for self-reported 2-check-in players is at least 70% of 6-check-in players; median Likert score of at least 4/5 for "I could compete without being online constantly".
- **F3, coalition value (both):** at least 70% are in a coalition by game day 3 (bell 72); at least 50% of War Table orders receive a pledge and a dispatch; median of at least 4/5 for "my coalition needed me".
- **F4, the Darkness matters (playtest 2):** at least one Fallen Hold every 72 bells during Long Dusk; at least 50% are reclaimed within 48 bells; players can say why Dread matters to them.
- **F5, raids without griefing (both):** the median raided kingdom regains its pre-raid Might within 24 bells. In playtest 3 only, quitting within 48 real hours of being raided is no more than 10 points above the baseline.
- **F6, real decisions (both):** in exit interviews, players name at least 2 trade-offs they struggled with (forge split, Hold versus home, Keep versus land, Guild choice).
- **F7, snowball check (playtest 2, then live seasons):** the leading coalition's renown share at bell 504 (end of game day 21) is at most 45%. Whether Nightfall changes the leader "at least sometimes" needs several seasons, so it is tracked across the first live seasons, not judged from one playtest.

**Instrumentation:**
- Objective completion times.
- Forecast-versus-actual differences: zero on exact lines; any other difference attributed to a named input change.
- Action outcome codes.
- Coalition membership over time.
- Hold control timeline.
- Raid and recovery curves.
- Check-in timing (judged against F1 only in playtest 3).

All of it is stored first-party, aggregated, with an opt-out.

---

## Appendix A. Launch constants (season configuration snapshot)

| Key | Default | Section |
|---|---|---|
| `bellSeconds` | 3600 | 4.1 |
| `seasonLengthBells` | 672 | 4.3 |
| `foundingBells` / `foundingProtectionBells` | 12 / 72 | 4.2, 11.2 |
| `catchUpMaxBells` | 6 | 4.4 |
| `start.acres` / `start.buildings` | 50 / Cottage 12, Farm 8, Mine 3, Forge 2, Market 2, Barracks 1 | 5.2 |
| `start.commoners` / `start.troops` | 190 / Spearman 20, Archer 10 | 5.2 |
| `start.stores` | gold 2000, grain 1500, iron 100, tools 150, arms 40 | 5.2 |
| `start.forgeArmsPct` | 30 | 7.2 |
| `tithe` / `rate.farmer` / `rate.miner` / `rate.smith` / `rate.merchant` | 1 / 3 / 1 / 1 / 4 | 7.1 |
| `jobsPerWorkplace` / `housingPerCottage` | 10 / 15 | 7.2 |
| `growthRate` / `hungerLeaveRate` / `desertionRate` | 0.10 / 0.05 / 0.05 | 7.7 |
| `keep.housing` / `keep.troopCap` / `keep.vault` per level | 40 / 40 / 400 | 7.4 |
| `walls.defensePerLevel` | 0.10 | 7.4 |
| `guild.bonusPerLevel` / `guild.drillPerLevel` | 0.10 / 0.05 | 7.4 |
| `land.nextAcre` | round(A² / 40) + 20 | 7.6 |
| `exchange.per10` buy/sell | grain 20/10, iron 60/30, tools 100/50, arms 120/60 | 7.8 |
| `exchange.tierUnits` / multipliers | 500 per 24 bells / buy ×1.5, sell ×0.5 | 7.8 |
| `troops` | see 8.1 | 8.1 |
| `knightOnlyTravelFactor` | 0.75 (round up) | 8.1 |
| `travel` | camp 1/2/3, Hold 4–6, raid 8, reinforce 4, caravan 4 | 8.4 |
| `schedule.maxAheadBells` | 24 | 8.4 |
| `battle.La` / `battle.Ld` | 0.08 clamped to [0.03, 0.20] / 0.06 clamped to [0.02, 0.15] | 8.5 |
| `plunder.base` / `plunder.slope` | 0.10 / 0.10 | 8.6 |
| `scout.cost` / `scout.cooldown` | 100 / 6 bells | 8.7 |
| `hold.multiplier` | 1.5 | 8.3 |
| `holds.count` | clamp(round(kingdoms/4), 3, 9) | 9.1 |
| `tide.interval` | 24 (Nightfall 12) | 9.2 |
| `tide.factor` | Kindling 0.5; Long Dusk 0.80 + 0.06(d−4); Nightfall 2.0 + 0.1(d−22) | 9.2 |
| `tide.brightnessStep` | 0.25 | 9.2 |
| `militia` | Kindling ceil(1.5 × TP); later floor(0.75 × TP) | 9.2 |
| `darkness.garrison` / `entrench` | 0.5 × TP / +0.25 × TP per scheduled tide | 9.3 |
| `dread.perFallen` / `dread.max` | 0.03 / 0.15 | 9.3 |
| `renown` | see 9.4 | 9.4 |
| `coalition.maxMembers` / `maxCaptains` | 6 / 2 | 10.1–10.2 |
| `invite.ttl` / `petition.ttl` / `oathbreaker` | 48 / 48 / 24 bells | 10.3–10.4 |
| `caravan.cargoPerMarket` / `caravan.memberAge` | 150 / 24 bells | 10.5 |
| `oath.deadline` / `oath.cooldown` | 48 / 12 bells | 10.7 |
| `pact.length` / `pact.cancelNotice` | 72 / 12 bells | 10.8 |
| `respite` / `bracket.raid` / `bracket.renown` | 12 bells / 0.60 / 0.75 | 11.2 |
| `slumberAfterBells` | 72 | 11.2 |
| `lateJoin` | days 4–10: Keep 2, stores ×2; days 11–21: Keep 3, stores ×3, +20 free acres | 11.5 |
| `risingBanner` | +40% production, −25% build time, while Might < 60% of median | 11.5 |
| `rosterLockBell` | 601 | 4.3 |
| `reckoningBells` | 48 | 4.3, 11.6 |
| `legacy.nameReserveBells` | 72 for a sole claim; a disputed name stays quarantined until an admin decides | 16.5 |

## Appendix B. Glossary

- **Bell:** one world tick (1 hour live).
- **Founding:** a new kingdom's protection for its first 72 world bells; it ends early only through the confirmed order `END_FOUNDING`.
- **Founding bell:** a personal bell during Founding that moves only your own lands.
- **Kingdom bell:** a kingdom's own count of bells experienced since founding; it runs ahead of the world bells elapsed since then by the founding bells rung.
- **Forecast:** the engine run forward on what you can see, assuming no new orders; each line is exact or marked as an estimate.
- **Commoners:** non-military population.
- **Idle:** commoners without a job.
- **Crew:** one construction slot.
- **Works:** Keep, Walls, Guild.
- **Levy:** home defense from commoners (one tenth of their number).
- **Might:** size measure used for brackets.
- **Expedition:** troops on the move.
- **Respite:** post-raid immunity.
- **Slumber:** inactivity protection.
- **Hold:** frontier stronghold.
- **Tide:** a scheduled attack by the Hollow Host.
- **Fallen:** a Hold taken by the Darkness.
- **Dread:** realm-wide production penalty from Fallen Holds.
- **Renown:** the season score.
- **Coalition roles:** Marshal (leader), Captain (officer), Sworn (member).
- **Oath:** a shared coalition goal.
- **War Table:** structured coalition orders.
- **Pact:** timed non-aggression between coalitions.
- **Wardens:** season winners.
- **Dawn:** the realm-wide outcome when half the Holds stand.
- **Quarantined name:** a legacy kingdom name claimed by more than one account, reserved for nobody until an admin decides.

## Appendix C. How the numbers were checked

- A scratch model of sections 7–9 (economy bell, construction, recruitment, battle, plunder, apportionment) produced every worked example here. It was not committed; the prototype embeds an equivalent copy, plus the world phase of 8.4, for its live forecasts.
- The checks confirmed:
  - next-bell forecasts, world events included, equal the results in the first-session script;
  - the hunger example settles where described;
  - casualty and renown apportionment totals are exact;
  - the steepening land rule prevents the gold runaway seen with linear land prices (7.10).
- The prototype was driven in headless Chrome through the first-session path of 5.3. Every intermediate store value and completion bell matched this document. The Greywatch, raid and Mirefold forecasts match 7.9, 8.9 and 9.5. No layout overflowed horizontally at 320, 390, 768 or 1440px.
- The checks do **not** establish balance, pacing quality or fun; sections 17 (S8) and 19 cover those.

**Review round 2 (2026-10-25): corrections and checks**
- **Forecast versus reconciliation.** The prototype's founding reconciliation had compared an economy-only forecast with a snapshot taken before the world phase. At bell 5 it showed 2,241 gold, 1,593 grain and 150 iron as "all matched", while the stores became 2,391, 1,693 and 170 once the loot landed. Forecasts now run the world phase too (13.5), and the reconciliation covers stores, people, troops and world events. The 5.3 end states were already right and are unchanged.
- **Corrected numbers.**
  - Home defense at bell 207 had used today's levy instead of the levy forecast for that bell: 689 → 691, 581 → 583, 329 → 331. Raid losses are unchanged (18 and 6).
  - Counting the forecast battles in the grain runway moved 7.9 D: dry at bell 220 (was 219), −42 per bell with 2 Farms (was −59), +22 with 4 Farms (was +5).
  - Ironhold's raid now leaves at bell 199: listed as sent at bell 198, it would have landed at 206.
  - The Vigil claim preview had treated arrival in the tide's own bell as too late, contrary to the arrivals-first order of 8.4.
- **New or sharpened rules.** Battle boundaries (8.5), the Fallen Hold threshold (9.3), brightness and militia rounding (9.2), scheduled-departure timing, visibility and eligibility (8.4), ending Founding (11.2), kingdom bells (4.2), Reckoning in bells (4.3), legacy name claims (16.5), and the split between compressed and real-time playtests (19).
- **Checks run.**
  - Headless Chrome, driven through the DevTools protocol with programmatic clicks and input events:
    - the 5.3 path end to end, with every line matching at founding bells 1, 4 and 5 (bell 5: 2,391 / 1,693 / 170 / 180 / 50, people 198, 29 Spearmen, 9 Archers);
    - ending Founding through preview, review and confirm;
    - the mid-season counsel, incoming warning and threat card;
    - march previews for Greywatch (16, 30, and 60 + 60 troops; a departure after bell 207), Mirefold (after bell 206; after bell 207, which is blocked with 58 Archers and 23 Knights left), Vigil (now; after bell 208), Ironhold, Reaver hall and an empty march;
    - the 2- and 4-Farm grain runways;
    - live bells 202 and 203, after which the bell simulation is withdrawn.

    There were no console errors or exceptions. Horizontal overflow (`scrollWidth − clientWidth`) was 0 at 320, 390, 768 and 1440px in all five mid-season views and in the first-session reconciliation and End Founding panels.
  - Node, against the prototype's embedded model: 20 battle-boundary assertions all pass, with no NaN or infinite value in any result. They cover zero, negative and fractional AP rejected; DP 0; levy-only defense; exact ties at 8% and 6% losses; reclaim thresholds of 811 and 813; no side wiped out from 1 to 100 troops; and the 8.9 raid's 18 and 6 losses. A separately written script of the mid-season forecast agrees with the prototype on 220, 691, 583 and 331.
  - A Markdown check of this file: every table row has its header's cell count, and code fences balance.
- **Not run.** T3 `html_preview` and `html_render`; physical-keyboard, screen-reader and real-device tests. Earlier keyboard checks used synthesized key events and DOM calls, which do not count as keyboard or screen-reader acceptance. Those need people and assistive technology (19, U4 and U5).
- **Remaining prototype limits.**
  - Recall, turn back, scouting, pacts and other kingdoms' moves are shown but not simulated.
  - Mid-season live bells stop at bell 203. Later world events (Ravenmoor's tide at 204, Ironhold's raid at 207, the tide at Greywatch at 210) exist only in forecasts.
  - The sample has no hidden defense for Ironhold, so your raid there stays an estimate and is never resolved.
  - Renown accrual and the scheduling of new tides are not modeled, so forecasts include only tides already published.
  - The prototype forecasts on the client from its one sample world, which stands in for "what the player can see".
  - The first-session sample shows ending Founding and its effects, but has no rival kingdom to raid afterwards.
  - Fonts vary by operating system, because only system fonts are used.
