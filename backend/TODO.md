# What is left

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
1. Random events.
