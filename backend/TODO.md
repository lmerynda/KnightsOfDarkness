# What is left

## Game

1. Melee is fought type against type (infantry only meets infantry), so a pure cavalry army is never hurt by defending infantry. Decide whether that is the intended rock-paper-scissors; the simulation can compare variants.
1. The castle's housing capacity and the tower's capacity in `gameconfig.json` are unused; towers and castles only add defense.
1. Game messages (notifications) are plain text built in the services; a typed event with data would let the client render them richer.
1. The turtle strategy in the simulation stalls at about 140 acres: recruiting bowmen and guards costs the gold it would spend on land. Worth a look as a balance question once real players exist.

## Code

1. Controllers and services mix responsibilities: some services return DTOs and some controllers build them; result types differ per action.
1. Amount and count are used interchangeably for the same thing.
1. The periodic turn is a bulk update outside optimistic locking, so a player action that read the kingdom before the tick can still overwrite that tick's extra turn.
1. The Game scenarios CI workflow (simulation and browser jobs) has not run on GitHub yet.

## Ideas

1. Trial play with only a kingdom name.
1. Profession profiles that suggest what to build and train.
1. Random events.
