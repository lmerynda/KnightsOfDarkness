# What is left

## Game

1. Game messages (notifications) are plain text built in the services; a typed event with data would let the client render them richer.
1. The turtle strategy in the simulation stalls at about 140 acres: recruiting bowmen and guards costs the gold it would spend on land. Worth a look as a balance question once real players exist.

## Code

1. Controllers and services mix responsibilities: some services return DTOs and some controllers build them; result types differ per action.
1. Amount and count are used interchangeably for the same thing.
1. The Game scenarios CI workflow (simulation and browser jobs) has not run on GitHub yet.

## Ideas

1. Trial play with only a kingdom name.
1. Profession profiles that suggest what to build and train.
1. Random events.
