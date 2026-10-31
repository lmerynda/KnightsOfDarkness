# What is left

## Game

1. Building points never limit anyone: a builder makes 1,000 a turn, a kingdom starts with 20 and the small-realm bonus multiplies them up to 5 times, so a new kingdom gets 100,000 points for 41 free acres. Land price is what limits growth. Measured on 2026-10-31: builders at a tenth or a fifth of their output (`--set production.builder.rate=100` or `200`) changed final land by under 35 acres for every ruler in a 60-turn world. Making building a real decision needs a different lever than builder output.
1. A garrison of bowmen alone is a trap since bowmen strike nobody in melee: a raider lost only its arrow casualties while killing up to 94 bowmen per charge and broke through within a few charges. The steward now warns a ruler whose army is nearly all bowmen, and the simulated turtle mixes in infantry.
1. Protection after a defeat lasts 24 turns. Measured on 2026-10-31 in a 60-turn world: the economist was raided 2 times instead of 7 and the turtle once instead of 5, while the warlord ended at 461 acres instead of 688 and the raider at 183 instead of 330. Tune the length once real players exist.

1. Over 300 turns (2026-10-31) economy with defense wins: the turtle ends at 1,833 acres with an army larger than the warlord's, the economist at 1,690 after nine raids, the warlord at 1,453. Attacking pays but less than building; watch this once real players exist.

## Code

1. Controllers and services mix responsibilities: some services return DTOs and some controllers build them; result types differ per action.
1. Amount and count are used interchangeably for the same thing.
1. The Game scenarios CI workflow (simulation and browser jobs) has not run on GitHub yet.

## Ideas

1. Trial play with only a kingdom name.
1. Profession profiles that suggest what to build and train.
1. Random events.
