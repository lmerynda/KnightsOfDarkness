# What is left

## Game

1. Building points come in abundance (a new kingdom gets 100,000 a turn for 41 free acres); their lasting use is the great works. Measured on 2026-10-31: at the old prices every simulated ruler maxed all five great works within 120 turns of a 4,320-turn round, so the prices are now ten times higher and the works stay a goal for most of a round. Cutting builder output alone (to a tenth or a fifth) changed little.
1. A garrison of bowmen alone is a trap since bowmen strike nobody in melee: a raider lost only its arrow casualties while killing up to 94 bowmen per charge and broke through within a few charges. The steward now warns a ruler whose army is nearly all bowmen, and the simulated turtle mixes in infantry.
1. Protection after a defeat lasts 24 turns. Measured on 2026-10-31 in a 60-turn world: the economist was raided 2 times instead of 7 and the turtle once instead of 5, while the warlord ended at 461 acres instead of 688 and the raider at 183 instead of 330. Tune the length once real players exist.
1. Over 300 turns (2026-10-31, before land capture went from 10% to 15%) economy with defense won: the turtle ended at 1,833 acres with an army larger than the warlord's, the economist at 1,690 after nine raids, the warlord at 1,453. At 15% the three styles ended within 90 acres of each other after 150 turns. Watch this once real players exist.

## Code

1. Controllers and services mix responsibilities: some services return DTOs and some controllers build them; result types differ per action.
1. Amount and count are used interchangeably for the same thing.
1. The Game scenarios CI workflow (simulation and browser jobs) has not run on GitHub yet.

## Ideas

1. Trial play with only a kingdom name.
1. Profession profiles that suggest what to build and train.
1. Random events.
