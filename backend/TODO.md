# What is left

## Game

1. Building points never limit anyone: a builder makes 1,000 a turn, a kingdom starts with 20 and the small-realm bonus multiplies them up to 5 times, so a new kingdom gets 100,000 points for 41 free acres. Land and gold are the only real limits, and builders are close to decorative. Measured on 2026-10-31: cutting builder output tenfold (`--set production.builder.rate=100`) changed final land by under 25 acres for every ruler in a 60-turn world. Decide whether building should be a constraint.
1. A garrison of bowmen alone is a trap since bowmen strike nobody in melee: a raider lost only its arrow casualties while killing up to 94 bowmen per charge and broke through within a few charges. The simulation's turtle now mixes in infantry; worth saying so in the game text if players fall into it.
1. Protection after a defeat lasts 24 turns. Measured on 2026-10-31 in a 60-turn world: the economist was raided 2 times instead of 7 and the turtle once instead of 5, while the warlord ended at 461 acres instead of 688 and the raider at 183 instead of 330. Tune the length once real players exist.
1. Late-game gold piles up with nothing to spend it on: the economist ends a 60-turn world with 2.4 million gold and earns 935,000 a turn.
1. Game messages (notifications) are plain text built in the services; a typed event with data would let the client render them richer.

## Code

1. Controllers and services mix responsibilities: some services return DTOs and some controllers build them; result types differ per action.
1. Amount and count are used interchangeably for the same thing.
1. The Game scenarios CI workflow (simulation and browser jobs) has not run on GitHub yet.

## Ideas

1. Trial play with only a kingdom name.
1. Profession profiles that suggest what to build and train.
1. Random events.
