# Dump list for what needs to be fixed

## Missing major game features
1. Attacks
1. Resource transfers using carriers
1. Spies
1. Rankings and statistics
1. Messages
1. Notifications
1. Coalitions
1. Game log separation
1. Game related messages should be coming from game module, we probably need to create type with message and data for every possible action to take

## Problems

1. Player registration, we should rebrand from username to kingdomName, we should check if repeated password match, we should ask for email, we should make sure no kingdomNames are repeated, move players storage to database
1. There should be some separation between civilians and military units (archers, infantry, cavarly, spies). They should eat the same, but shouldn't be counted into all units, for example they should return as unemployed on the next turn, and shouldn't be exiled if there is not enough houses
1. We do a lot of calculations for small kingdom bonus, maybe we should have a table of consts for each land value?
1. Browser module was created using react tools which is deprecated. Migrate to current standard project organization
1. There is a mix of layer responsibility between controller and service, DTOs should be translated in controller but often are in service, as well as ResponseEntity
1. More validations?
1. Make up mind about amount vs count, it's all mixed up
1. Show the cost of buying land near the transaction form
1. Market offer should show total value
1. Clicking MAX on buildings does max based on occupancy instead of calculating based on cost
1. Buy report has price per unit two times
1. Token doesn't autoextend with use, just hard expires after 15 minutes

## Ideas

1. Profession profile, it should inform building and training UI
1. Random Game Events? Might not be the best idea
1. Warning on passing turn with negative implications
1. Move read actions into game module, create separate readData module? there we should not build complicated domain types with dependency hierarchy, just dtos
