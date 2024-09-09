# Dump list for what needs to be fixed

## Missing major game features
1. Attacks
1. Resource transfers using carriers
1. Spies
1. Rankings and statistics
1. Player registration
1. Firing of professionals when there is not enough buildings
1. Exiling of people when there is not enough houses, including professionals
1. Messages
1. Notifications
1. Coalitions
1. Game log separation
1. Game related messages should be coming from game module, we probably need to create type with message and data for every possible action to take

## Problems

1. Creating market offer should have minimum value
1. We do a lot of calculations for small kingdom bonus, maybe we should have a table of consts for each land value?
1. Browser module was created using react tools which is deprecated. Migrate to current standard project organization
1. There is a mix of layer responsibility between controller and service, DTOs should be translated in controller but often are in service, as well as ResponseEntity
1. In some cases (MarketController.createOffer as an example) we use http codes for valid request, but problematic domain action, in such a case http status should be just normal
1. More validations?

## Ideas

1. Profession profile, it should inform building and training UI
1. Random Events? Might not be the best idea
1. Warning on passing turn with negative implications
1. Move read actions into game module, create separate readData module? there we should not build complicated domain types with dependency hierarchy, just dtos
