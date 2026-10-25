# Knights of Darkness: gameplay and UX assessment

Date: 2026-10-25. Reviewed revision: `7c20237073bda77a15a93986242d95db76ea30b3`.

## Overall judgment

**A promising economy prototype, currently unsuitable for an uncoached multiplayer playtest.** The ingredients for a kingdom strategy game exist: production chains, staffing and building capacity, land expansion, trade, specialist upgrades, armies, and alliances. The current browser does not reliably execute its central actions, and several server actions violate basic state invariants. Those defects prevent a fair assessment of balance or sustained enjoyment.

The UX presents an operational console more readily than a kingdom the player understands and wants to develop. It exposes many numbers and actions, but gives little help answering: What should I do next? What will this cost? What changes when I spend a turn? Why should I cooperate or attack? What happened when an action failed?

The recommendation is to complete one trustworthy economic and military journey, then test it with newcomers. Preserve the existing systems while correcting their contracts and consequences. A broad visual or combat redesign would be premature.

## Method, council, and limits

The assessment used three complementary perspectives:

- **Opus 5.5, high effort:** independent gameplay source review, followed by a second round challenging conclusions against runtime evidence and Astra's objections.
- **GPT-6 Astra, high reasoning effort:** independent UX source review, followed by a second round ranking blockers and checking evidence limits.
- **Primary agent:** local setup, rendered Chrome inspection, browser actions, isolated API probes, verification of important source claims, and reconciliation of both council rounds.

All reviewers examined the same revision. The initial checkout was clean. No gameplay implementation was changed. The only committed artifact from this work is this assessment; browser profiles, test credentials, probe scripts, build outputs, transcripts, and screenshots remain local artifacts.

Runtime used Java 21, Vite, the checked-in game configuration, and a disposable H2 in-memory database. H2 is a test dependency but absent from the normal runtime classpath, so a temporary Gradle init script added it for this session. This does not establish PostgreSQL or deployed-environment acceptance. T3 browser navigation worked, but snapshots timed out twice; the primary walkthrough used headless Chrome through Playwright instead. Desktop inspection used 1440×1000; narrow-screen inspection used 390×844 desktop-browser emulation.

Evidence labels below:

- **Observed:** executed against this local running game, with browser or API state inspected.
- **Source:** established by the inspected code; broader runtime consequences remain untested where stated.
- **Design risk:** reasoned concern requiring an intent decision, simulation, or human playtest.

There were no recruited human participants, long-running multiplayer sessions, real-device touch tests, screen-reader sessions, production restarts, or retention measurements. No numerical fun or balance score is warranted.

## What already works and is worth preserving

- Fresh registration, login, and passing a turn succeeded through the browser. Registration checks repeated passwords and tells the player to log in afterward.
- A real browser market purchase delivered one food for 15 gold and displayed quantity, unit price, and total cost correctly.
- Buying one land succeeded, taking land from 100 to 101 and spending 1,020 gold. Feedback before and after that purchase still needs work.
- The economy has useful interdependence: food supports production; iron feeds blacksmiths; tools and weapons support recruitment; building points and land constrain development. The tools/weapons split offers a potentially meaningful allocation decision.
- Costs and occupancy are visible in the management tables. The turn report exposes production, consumption, arrivals, and bonuses, providing the basis for a better explanatory interface.
- Timed expeditions and carrier capacity offer a foundation for commitments between kingdoms. Their timing and reporting need to become understandable.
- Rules are largely configurable, and action classes allow focused correctness work without rewriting the game.

These strengths establish useful foundations, not completed gameplay acceptance.

## Release and playtest blockers

### B1. Registering another account can reset an existing kingdom

**Observed; critical for persistent multiplayer.** With one account's kingdom already advanced by a turn, registration under a different email using the same kingdom name succeeded. The original account's gold changed from 350,075 back to the starting 200,000; building points changed from 100,050 to 10,000, and unemployed population from 95 to 10. Both the original and new account tokens could read the reset kingdom.

`AuthController.java:58–75` checks email duplication, saves the user, and creates the named kingdom. `KingdomEntity.java` uses the kingdom name as its primary key; `KingdomRepository.java:25–31` saves the new entity. A database uniqueness declaration does not make this creation path safe against an update of an existing identity.

**Required outcome:** reject duplicate identities atomically; preserve original ownership and state. Verify concurrent registration as well as sequential duplicates. This probe establishes shared-state reset and access, without claiming every possible account-authorization consequence was tested.

### B2. The browser's central action contracts are broken

**Observed; critical for playability.** Entering one house and clicking Build returned HTTP 200 with `Succesfully built 0 buildings`; the house count stayed at 30. Entering two bowmen and clicking Train similarly reported zero trained and left the army empty.

The browser sends flat maps such as `{ "house": 1 }` and `{ "bowman": 2 }`. The backend DTOs expose nested `buildings` and `units` maps. Sending complete nested maps through the API built one house and trained two bowmen successfully. Partial nested maps also produced HTTP 500 in the tested cases, so simply wrapping the browser payload is not a demonstrated complete fix.

The attack request has the same unit-map mismatch: the browser-shaped request returned `Need to send at least one unit` even after two bowmen were trained through the corrected API shape. The nested request dispatched them successfully. Returned build/training results are nested too, while the browser's report components expect flat maps.

Sources: `browser/src/game-api-client/KingdomApi.tsx:55–149`, `browser/src/game-api-client/AttacksApi.tsx:43–56`, `KingdomController.java:88–173`, `KingdomBuildingsDto.java`, `UnitsMapDto.java`, and the BuildReport/TrainingReport components.

**Required outcome:** one explicit request/response contract, safe handling of omitted entries, visible rejection of unsupported shapes, and browser tests asserting actual state changes. Test Build, Demolish, Train, Fire, and Attack together with their displayed reports. HTTP 200 alone is not success evidence.

### B3. Invalid and oversized actions create impossible state or unpaid progress

**Observed; critical for competitive fairness.** The following probes used disposable accounts only:

- Dispatching two bowmen with **minus one infantry** was accepted. Available infantry became 1; mobile infantry became −1.
- Submitting a weapons-production percentage of **−100** passed a turn and persisted weapons at **−490**. A separate 150% request produced −250 tools and +750 weapons; that particular observation was a negative production delta, not evidence that its final tool stock was negative.
- A gold shaft reached level 1 by spending the initial 10,000 building points. Repeating an oversized build request at zero points advanced it to levels 2 and 3 while each response reported **zero spent**.
- Posting 10,000 food while owning 2,000 created a 2,000-food offer but left food at **−8,000**.

Sources: `KingdomMilitaryAction.java:20–46`, `KingdomTurnAction.java:218–245`, `KingdomSpecialBuildingAction.java:47–78`, and `KingdomMarketAction.java:15–24`. Assertions in resource methods are not sufficient request validation.

**Required outcome:** validate ranges and arithmetic on the server, reject invalid actions without mutation, charge only the amount actually committed, and advance an upgrade only after its required cost has been paid. Cover negative, zero, omitted, oversized, overflow, and mixed-sign cases. Further source-derived exploit chains exist; these four observations alone justify stopping competitive balance conclusions.

### B4. Firing a worker destroys population

**Observed; high.** Firing one builder through a complete nested request changed builders from 20 to 19 **and unemployed people from 10 to 9**. Under the ordinary retraining interpretation, unemployment should increase. This removes two people from the total population for one fired worker.

Source: `KingdomTrainAction.java:52–66`, especially the subtraction from unemployed at line 61.

**Required outcome:** define firing versus expulsion explicitly and enforce population conservation for retraining. Do not suggest Fire as a safe recovery mechanism until this is corrected and tested with low unemployment, military units, and mobile units.

### B5. The alliance promise is inaccessible to ordinary registered players

**Observed identity defect; source-confirmed missing human journey.** A browser account with a normal email and a different kingdom name submitted `{ "name": "CouncilAlliance" }`. The response was HTTP 200, `success: false`, `Kingdom not found`. The interface displayed no failure explanation.

`AllianceController.java:52,66,80,94,108,122,142` passes `getUsername()` where services expect a kingdom name; `UserData.java:67–69` returns the email. `CreateAlliance.tsx:14–18` discards the result. Seed accounts with matching identity strings can conceal the defect.

Invitation and acceptance API helpers exist, but the inspected browser has no complete human invite/accept journey. AllianceList displays names and emperors; the membership screen exposes bot recruitment and removal instead of completing player recruitment.

**Required outcome:** use authenticated kingdom identity consistently; surface failures; demonstrate creation, invitation, acceptance, membership updates, and leaving across two fresh browser accounts. This is a gate for presenting the game as coalition competition. Friendly fire, shared intelligence, and alliance scoring remain design choices rather than assumed requirements.

### B6. Persistent progress is at risk under the checked-in production profile

**Source; critical before persistent deployment.** `backend/src/main/resources/application-prod.yaml:16` sets Hibernate `ddl-auto: create`. The startup initializers also create demo kingdoms and accounts without an inspected development-profile guard.

This configuration is incompatible with trusting a persistent campaign across application starts if used without overrides. **No production database was accessed or restarted**, and this review does not establish which effective settings the deployed service uses.

**Required outcome:** verify effective deployment configuration, use controlled schema migrations and safe initialization, and prove state survives a restart against a disposable PostgreSQL database. Keep demonstration identities separate from player accounts.

## Gameplay assessment

### The economic loop has substance, but its decisions are hidden

**Observed and source.** A fresh kingdom starts with 85 professionals, 10 unemployed people, 2,000 food, 200,000 gold, and 10,000 building points. Its first 0%-weapons turn produced 150,075 gold, 100,050 building points, 5,003 food, 500 iron, and 500 tools. It consumed 850 food and immediately added 85 unemployed people, bringing unemployment to 95.

The report shows a size multiplier of `5.002514743837556`. The code applies it using **occupied building count**, with a floor of 100, rather than owned land (`KingdomTurnAction.java:221,286–312`). Houses plus base capacity refill population in one turn (`:265–279`). Food upkeep counts units but not unemployed people (`KingdomDetailsProvider.java:45–48`). Building points reset at the start of every spent turn (`KingdomTurnAction.java:40,184–187`).

These are consequential rules. The browser explains none of them before commitment. A player can discard unspent construction capacity or add staffing commitments without understanding the next turn. The first report also shows zero/empty historical data before a first turn rather than an actionable opening objective.

**Recommendation:** show an authoritative next-turn forecast: food needed and remaining, expected production, population arrivals/departures, building points that expire, and expedition advancement. Introduce build → staff → produce through a short starting objective. Explain that allocating blacksmith output to weapons reduces tool output.

**Design risk, not a balance verdict:** the early multiplier and immediate population refill accelerate development, while the occupied-building bonus decreases as a kingdom grows. Buying unused land can improve a land-based ranking without lowering that bonus. Gold-miner training costs 4,000 gold plus 15 tools; its gold-only payback near the starting bonus is less than one turn, but that omits tools, capacity, food, and opportunity cost. Measure full strategies before retuning this curve.

### Combat does not yet deliver an understandable strategic result

**Source, with a narrow observed scenario.** An API-dispatched land attack of two bowmen against a fresh kingdom arrived after four turns spent by the attacker, returned its bowmen, and changed no observed target land, resources, buildings, or units. This small attack is not a general combat test: integer rounding can explain zero casualties at that size.

However, the inspected resolver never branches on the stored `economy`/`land` attack type and performs no corresponding land/resource/building objective effect. It resolves casualties and returns survivors (`KingdomInteractor.java:36–62`). The interface offers these two objectives without explaining an implemented difference.

Additional source-confirmed problems:

- Attacker-salvo defender casualties are subtracted twice through two references to the same unit object (`KingdomInteractor.java:103–109`). Exact combat tests need to assert amounts, not merely that counts decreased.
- Melee uses same-type counts and ratios. Pure cavalry versus pure infantry can produce no damage in that phase; small fights can truncate all damage. Whether this model is intentional needs a combat specification.
- The resolver's damage calculations do not incorporate guards, towers, or castles. These names imply defensive value that the exposed combat does not substantiate.
- The reviewed flow provides no clear battle report or incoming-attack view for the defender. Disappearing expedition rows are insufficient outcome feedback.

**Required next step:** document one battle rule set, fix arithmetic, and give both players a report that reconciles commitments, losses, returns, and objective effects. Test pure and mixed armies at several scales. An accurate outcome model comes before a new counter system or probabilistic redesign.

### Expedition timing creates commitment without a clear reaction window

**Source and observed attack timing.** Four attacker-spent turns resolve an attack; carriers advance through the same sender turn processing with a configured two-turn delivery. These are not necessarily four or two future wall-clock ticks. A player with banked turns can progress an expedition immediately; withholding turns can leave it pending.

This can be a valid design, but the UI's “Turns Left” does not explain it. If reactive multiplayer warfare is intended, sender-controlled resolution and absent incoming warnings undermine that intent. If deliberate burst play is intended, teach that explicitly. Decide on the intended interaction before changing to real-time arrival.

### Coalition competition has not yet become the game's organizing goal

**Source and design risk.** The creative intro promises kingdoms cooperating against other coalitions. The current browser provides resource management tabs, a kingdom list with land/army, and alliance administration, but no clear coalition objective, campaign milestone, or end condition. There is no complete in-game human recruitment or communication journey in the inspected interface.

Open-ended sandbox play is valid. Even then, players need a legible reason to return and cooperate: a shared target, negotiated specialization, contested territory, or another chosen objective. A season, networth ranking, or no-friendly-fire rule is not automatically the answer. Choose one social promise and test that players can experience it.

### The checked-in cadence rewards continuous attention

**Source; production intent unverified.** Defaults award one turn every 10 seconds with a cap of 36. From empty, the bank fills in six minutes; a new kingdom's initial 20 turns leave roughly 160 seconds before the cap. Bots run every five seconds and attempt their development cycle.

For a persistent browser strategy game this risks rewarding constant presence and making short absences expensive. It may be a development clock; do not describe these numbers as verified production behavior. State the intended session pattern, then choose cadence and bank size that support it. Test casual versus frequent sessions under equal account ages and disclose the bots' role in competition.

### Failure and recovery need an economic design too

**Source risks requiring scenario tests.** Food is consumed before production, and insufficient nourishment scales every production resource, including food. At zero food with nonzero upkeep, passing a turn cannot itself restart farm production. Market purchases or deliveries may recover it, but that assumes affordable supply. Negative stocks from the observed market defect make this worse.

Capacity rules also disagree: backend military capacity is checked separately per type, whereas the UI pools some occupants; mobile units and housing/base capacity are presented inconsistently. These discrepancies make planning and recovery harder.

After correctness fixes, play a low-food scenario, lost-capacity scenario, and market-shortage scenario. Establish whether recoverability, dependency on allies, or elimination is intended. Make the consequence and available response visible before the player enters a trap.

## UX assessment

### U1. Narrow-screen play is substantially obstructed — observed, high

At 390px the authenticated Train and Overview documents measured **1,226px wide**. In a viewport screenshot, a permanent **240px sidebar** consumes most of the screen; the remaining slice shows a cropped kingdom header and the left edge of the main table. Primary inputs and actions require substantial horizontal travel. This is more than a typography issue.

Sources: `Sidebar.tsx:16–23`, `Kingdom.tsx:69–72`, `KingdomTabs.tsx:20–29`, and the management tables. Collapse secondary inventory, provide compact reachable navigation, and contain any necessary table scrolling. Recheck 320px, 390px, a common laptop size, zoom, and real touch input. Desktop emulation does not prove phone usability.

### U2. Action planning is not trustworthy enough — observed and source, high

Train showed zero iron-miner positions, yet Max filled **66**. The condition at `Train.tsx:53` treats zero capacity as an absent restriction; affordability also omits unemployed people and the budget already assigned to other rows. Shared plans are submitted in backend enum order, potentially partially fulfilling earlier rows first.

Show the complete planned cost, executable quantities, limiting reasons, and capacity after commitment. Distinguish zero capacity from unrestricted capacity. Preserve useful input when an action fails or is partly fulfilled.

Buy Land offers no advance quote and discards its transaction report; the successful 1,020-gold purchase had to be inferred from counters. The market has a useful post-purchase report, but one quantity field serves both “Buy Amount” and “Buy Price,” making units ambiguous. Separate quantity from gold budget and show the expected total and remaining balance.

### U3. Errors lack visible recovery — observed and source, high

- Incorrect login produced no visible explanation in the tested browser state.
- Alliance creation hid the returned business failure.
- After aborting initial `/config` requests and restoring the route, the page still displayed only `Loading...` after another 5.5 seconds. Kingdom polling did not reload the missing configuration. Two initial requests occurred under the development rendering behavior; this was not a production retry test.
- `Common.tsx:3–14` redirects handled 401s to the homepage without an expiry explanation or preserved destination. Real token expiry and its full recovery journey were not exercised.

Sources: `Login.tsx:23–38`, `CreateAlliance.tsx:14–18`, `Kingdom.tsx:27–51,76`, and the API wrappers. Provide loading, offline, stale-data, business-error, and retry states. Disable conflicting actions while a request is pending and reconcile an ambiguous result before encouraging resubmission. Double-spend under latency remains an untested risk, not a reproduced finding.

### U4. Important controls lack accessible names — observed structure and source, high

All 11 Train quantity inputs had no ID, associated label, or `aria-label` in the inspected DOM. Repeated Max buttons lack unit context in their names. Build and market inputs use similar patterns. SendAttack places InputLabel elements beside inputs without explicit matching associations; notifications attach click behavior to ListItem without button keyboard semantics.

Sources: `Train.tsx:87–99`, `Build.tsx:86–98`, `MarketBuyResource.tsx:109–114`, `SendAttack.tsx:50–79`, and `Notifications.tsx:47–64`.

Give every input and action a meaningful contextual name; use semantic interactive controls; announce errors and asynchronous results. DOM inspection establishes the missing associations, not complete screen-reader behavior. Verify keyboard and screen-reader journeys separately.

### U5. Navigation can contradict the displayed page — observed, medium

Directly loading `/train` displayed Train while highlighting Overview. Selection is local component state initialized to `/overview` (`KingdomTabs.tsx:15–20`). Nested market navigation independently initializes Buy/Food.

Derive selection from the route and test direct entry, refresh, redirects, and Back/Forward. Remove unnecessary ambiguity from wildcard link destinations.

### U6. Hierarchy and language make mechanics feel like implementation details — observed, medium

The sidebar continuously lists 11 buildings and 11 unit types, duplicating information relevant to only some tasks. Header resources run together as an undifferentiated strip. Raw names such as `buildingPoints`, `guardHouses`, and `goldMiners` and the long decimal production multiplier require interpretation. Registration dominates the landing page before any explanation of the experience.

Prioritize the current decision: readiness, shortages, next objective, planned spend, and outcome. Use human-readable names, grouped resources, formatted numbers, and concise explanations. An empty Notifications screen should explain its state; important unread events should be visible without visiting the tab. A theme can reinforce atmosphere later, once the player's decisions are clear.

## Recommended sequence and acceptance gates

### Gate 1: trustworthy state and actions

Fix B1–B4, validate all mutation boundaries, and test request/response shapes through the real browser. Correct special-building spending and market over-posting before using economy output as balance evidence. Add exact combat casualty assertions. Prove the intended persistence setup against disposable PostgreSQL.

Exit evidence: duplicate identities cannot reset or share an unintended kingdom; rejected requests do not mutate state; no negative counts or unpaid upgrades; firing conserves population; positive browser intents change state exactly as their results describe. Concurrent requests and large integer boundaries need coverage beyond this assessment's sequential probes.

### Gate 2: one comprehensible first session

A newcomer can register, understand a starting objective, build capacity, train workers, preview and spend a turn, and reconcile production. They can then train troops and resolve one controlled battle with a report. Support this journey on desktop and 390px, with named keyboard-operable controls, correct navigation, visible failures, and recovery from a transient configuration failure.

Exit evidence: the complete journey succeeds through ordinary browser controls, without direct API setup replacing player actions. A documented battle scenario explains its objective and exact expected outcome.

### Gate 3: fulfill the coalition promise

Two accounts with distinct email/kingdom identities create, invite, join, coordinate one useful action, and understand membership and outcomes through the UI. Decide what cooperation provides and what competition rewards. If this gate is deferred, label the build as a restricted economic/military prototype rather than presenting finished coalition play.

### Gate 4: measure strategy, pacing, and enjoyment

Run comparable strategies only after correctness gates pass: compact specialization, expansion, farm-and-trade, and military investment. Compare equal-turn budgets at 500 and 2,000 turns, then compare realistic casual and frequent session schedules. Record resource-equivalent output, capacity, idle land, military commitment, recovery costs, bot trades, and progression milestones. Vary occupied-building versus owned-land bonuses as an experiment, not a presumed fix. Use a battle matrix with pure and mixed armies at 10, 100, and 1,000 units before judging counters.

Recruit five newcomers for a first usability study: include two narrow-screen sessions, one keyboard-only run, and a separate screen-reader check. Give objectives rather than click instructions. Ask each player to predict material costs and turn outcomes, recover from one rejected action and one controlled connection failure, and explain a battle result. Record interventions, wrong predictions, task completion, and recovery errors.

Proposed initial gate: zero integrity violations or false-success outcomes; all essential actions operable; at least four of five newcomers complete the documented loop unaided within a session limit set before the study. Follow with repeated multi-session coalition play to assess fairness, cooperation, progression, and desire to return. Five participants cannot establish competitive balance or retention.

## Verification and unresolved coverage

Completed verification:

- Backend Gradle test task completed successfully. JUnit XML totals: **140 tests, 136 passed, 4 skipped, zero failures/errors**. Skipped cases include turn-cap and simulation coverage; this is not complete gameplay proof.
- TypeScript build checking (`tsc -b`) passed.
- Vite production build passed.
- Browser journeys: registration, login, first turn, attempted build/train, all primary tab visits, food purchase, land purchase, alliance failure, direct-route navigation, desktop/narrow-screen captures, incorrect login, and controlled initial-configuration failure.
- API probes: flat versus full/partial nested DTOs; attack dispatch and four-turn resolution; duplicate kingdom reset and both-account reads; mixed-sign troops; out-of-range production with persisted negative stock; firing conservation; zero-cost special-building level advances; oversized market posting.

Not established: a successful browser combat or human-alliance journey, end-to-end carrier delivery, large-army exact damage, starvation recovery, concurrent mutation safety, production persistence, deployed settings, physical-phone behavior, real assistive-technology experience, strategic equilibrium, or human enjoyment over time.

Local screenshots and probes were inspected during the assessment. They are intentionally excluded from Git along with credentials and browser state. The reproducible scenarios and numerical observations above are the durable evidence record. Re-run against the eventual fixes; do not infer acceptance from this report's descriptions.

## Council reconciliation

Both models agreed on internal-development readiness and the order **correctness → understandable play → measurement → design refinement**. Astra emphasized false success, identity, recovery, accessibility, and mobile obstruction. Opus emphasized unpaid progression, combat arithmetic/objectives, cooperation, and the pacing/economy relationship. Runtime checks upgraded several source concerns into confirmed defects.

The synthesis deliberately narrows stronger initial claims: the two-bowman result does not prove all combat is inert; a negative production delta does not always imply a negative stock; starting rewards do not prove imbalance; configured production schema creation is a deployment risk, not an observed production wipe. Exact cadence, ranking metric, friendly fire, expedition clock, seasons, and the size-bonus basis remain owner/design decisions. One reviewer reversed the Max example in a later summary; the observed result is unambiguous: **zero open positions produced a Max value of 66**.
