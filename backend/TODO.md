# What is left

## Coalitions

1. Coalitions of ten measured on 2026-10-31 (`npm run coalitions`, 150 turns, three groups of 5 economists, 3 warlords and 2 turtles, before garrisons existed): the coalition whose warlords strike together ended at 15,973 acres, an uncoordinated coalition at 14,473, ten unallied kingdoms at 13,929. Coordinated strikes win fewer but bigger battles (168 acres per victory against 93); being allied mostly helps by leaving fewer enemies. Both edges are real but modest.
1. Standing guard measured on 2026-10-31 (150 turns, Order's two turtles as guardians; run-to-run noise about 2%). Guards placed ahead of time did nothing: half the army every six turns left Order beaten 26 times instead of 15 because the guardians weakened themselves; a third, only when clearly stronger, was neutral (beaten 18 times, 1,700 acres lost against 1,595). Guards sent when an enemy army is seen marching on an ally cut Order's land lost by a third (beaten 12 times, 1,097 acres lost) and Order ended largest (16,045). Garrisons rarely turn a defeat into a win; they raise the defense the attacker must beat, so a won battle takes less land. Attackers scout and pick targets they can beat, so help is worth most once an army is on the way.
1. A fully coordinated coalition (guards sent on threat, and every warlord striking in the same attack turn through the war council) ended 150 turns at 19,393 acres against 14,335 for an uncoordinated coalition and 13,213 for ten unallied kingdoms (2026-10-31). Coordination is now the decisive edge. It is beaten more often (33 times, its armies are often away) but loses less land than either rival. Watch for runaway coalitions once real players exist; raid protection and the land capture ratio are the levers.
1. Allies pass gold through the market (decided 2026-10-31): a member offers a few pieces of goods at a dear price and an ally buys them. The market shows only the cheapest offers, so allies' offers are now always listed for their allies. The same trick lets a second account feed a main one, as it always could.
1. Real players time strikes by passing turns: an army lands after four of its owner's turns, so allies landing together must pass turns together. The war room shows each army's turns left; nothing else helps them meet.

## Game

1. Building points come in abundance (a new kingdom gets 100,000 a turn for 41 free acres); their lasting use is the great works. Measured on 2026-10-31: at the old prices every simulated ruler maxed all five great works within 120 turns of a 4,320-turn round, so the prices are now ten times higher and the works stay a goal for most of a round. Cutting builder output alone (to a tenth or a fifth) changed little.
1. A garrison of bowmen alone is a trap since bowmen strike nobody in melee: a raider lost only its arrow casualties while killing up to 94 bowmen per charge and broke through within a few charges. The steward now warns a ruler whose army is nearly all bowmen, and the simulated turtle mixes in infantry.
1. Protection after a defeat lasts 24 turns. Measured on 2026-10-31 in a 60-turn world: the economist was raided 2 times instead of 7 and the turtle once instead of 5, while the warlord ended at 461 acres instead of 688 and the raider at 183 instead of 330. Tune the length once real players exist.
1. Balance over 300 simulated turns (2026-10-31): before today economy with defense won clearly (turtle 1,833 acres, economist 1,690, warlord 1,453). With 15% land capture, raid protection, random events, great works at ten times the price and rulers that raise them, the three styles end within 16% of each other: turtle 2,332, economist 2,120, warlord 2,010. Scripted rulers are a rough stand-in; check again once real players exist.

## Code

1. Controllers and services mix responsibilities: some services return DTOs and some controllers build them; result types differ per action.
1. Amount and count are used interchangeably for the same thing.
1. The Game scenarios CI workflow (simulation and browser jobs) has not run on GitHub yet.

## Ideas

1. Trial play with only a kingdom name.
1. Profession profiles that suggest what to build and train.
