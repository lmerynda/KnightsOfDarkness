# What is left

## Game

1. Bot balance: the merchant bots grow to thousands of acres and offer millions of food within hours. They are unranked, but they flood the market and make easy plunder targets.
1. Combat is fought type against type in melee (infantry only meets infantry), so a pure cavalry army is never hurt by defending infantry. Decide whether that is the intended rock-paper-scissors.
1. Guards defend and catch spies but never die in battle.
1. Food is eaten before the harvest is counted, so a kingdom with exactly enough farmers still goes hungry on an empty granary.
1. The castle's housing capacity and the tower's capacity in `gameconfig.json` are unused; towers and castles only add defense.
1. Game messages (notifications) are plain text built in the services; a typed event with data would let the client render them richer.

## Code

1. Controllers and services mix responsibilities: some services return DTOs and some controllers build them; result types differ per action.
1. Amount and count are used interchangeably for the same thing.
1. The periodic turn is a bulk update outside optimistic locking, so a player action that read the kingdom before the tick can still overwrite that tick's extra turn.
1. Production serves the client with nginx without a fallback to `index.html`, so reloading a deep link such as `/kingdom/build` depends on whatever proxy sits in front.
1. The e2e scenarios run locally only; CI does not start the backend for them yet.

## Ideas

1. Trial play with only a kingdom name.
1. Profession profiles that suggest what to build and train.
1. Random events.
