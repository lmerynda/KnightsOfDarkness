# Knights of Darkness art spec

Every raster image in `browser/public/art` comes from this spec. Regenerate an image by sending its prompt, prefixed with the shared style, to an image model, then run `node art/process.mjs <source-dir>` to resize and convert to webp.

## Shared style

> Dark medieval painterly illustration, realistic proportions, oil painting with visible brushwork, muted desaturated palette of slate blue, charcoal and moss green with warm ember-orange and tarnished gold light accents, twilight atmosphere with drifting mist. No text, no letters, no logos, no watermark, no border or frame.

Vignette images (buildings, units) add:

> Single centered subject, square composition, the edges fade smoothly into near-black (#100d0b) so the image blends into a dark interface card.

Scene images (banners) add:

> Wide cinematic landscape composition 16:9, the lower third darker and calm so interface text can sit over it.

## Scenes (1536x864, wide)

| file | prompt |
|---|---|
| hero | A lone fortified castle on a craggy hill at dusk, torches burning on the walls, tattered banners in the wind, a winding road with a few riders approaching, distant dark storm clouds over the western horizon |
| realmHamlet | A small frontier hamlet in a forest clearing at twilight, a handful of thatched huts behind a wooden palisade, a single watch fire, freshly cleared tree stumps and muddy fields |
| realmTown | A growing market town on a river at twilight, timber houses crowding inside a new stone wall, a stone church tower, a mill wheel and barges, fields stretching to the hills |
| realmCity | A great walled city seen from a hill at twilight, double rings of towered walls, a cathedral and a strong keep, thousands of warm windows, roads with caravans leading to the gates |
| realmCitadel | The seat of a mighty realm at twilight, a vast citadel of black stone crowning a mountain above a sprawling city, banners on every tower, beacon fires on distant hills marking its borders |
| war | Two armies facing each other across a misty field at dawn, spears and banners, cavalry on the flank, archers on a ridge |
| marketplace | A crowded medieval market square at evening under lanterns, stalls with grain sacks, iron bars, crates of tools |
| alliance | A torchlit stone council hall with a long oak table, several lords standing around a map, banners of different houses on the walls |
| rankings | A dark throne hall with tall pillars, a single shaft of cold light falling on an empty iron throne |
| messages | A scribe's desk by candlelight with parchment letters, wax seals, a quill and an hourglass, rain on a leaded window |
| spies | A hooded figure on a rooftop at night watching a lit castle courtyard below, moonlight through clouds |
| transfers | A train of laden carriers and a mule cart crossing a stone bridge in fog, carrying sacks and crates |
| login | A massive iron-bound castle gate at night, two braziers burning on either side, mist on the ground |
| defeat | A beaten army retreating in the rain at dusk, wounded soldiers carried on shields, a torn banner dragged through the mud, the enemy fortress still standing behind them with torches on its walls |
| breached | A breached town wall at night, a gap in the stones with rubble and smoke, enemy banners pouring through, burning roofs behind, villagers fleeing with carts |
| victory | A victorious lord raising a great banner atop castle walls at sunrise, soldiers cheering below, golden light breaking through clouds |

## Buildings (1024x1024, vignette)

| file | prompt |
|---|---|
| house | A cluster of humble timber-framed peasant houses with thatched roofs and smoking chimneys |
| goldMine | The timber-propped entrance of a gold mine in a rocky hillside, a mine cart with glinting gold ore, lanterns |
| ironMine | An iron mine entrance cut into dark rock, piles of rust-red ore, a wooden hoist |
| workshop | A blacksmith's workshop with an anvil, glowing forge fire, hammers and tongs on the wall |
| farm | A farmstead with wheat fields, a barn and haystacks under a dim golden sky |
| market | A small covered market hall with wooden stalls, sacks and barrels |
| barracks | A long stone barracks with weapon racks and a training yard with straw dummies |
| guardHouse | A sturdy stone guard house by a gate, a brazier and a halberd leaning on the wall |
| spyGuild | A narrow dark townhouse in an alley, shuttered windows, one candle lit, a raven on the sill |
| tower | A tall round stone watchtower with arrow slits and a lit beacon at the top |
| castle | A grand stone keep with towers, crenellated walls and banners |

## Special buildings (1024x1024, vignette)

| file | prompt |
|---|---|
| goldShaft | A deep vertical gold mine shaft with a great wooden winch and chains, golden glow from below |
| ironShaft | A deep iron mine shaft with a stone headframe, ore buckets and soot-stained timbers |
| granary | A large raised stone granary with wooden ventilation slats and grain sacks |
| forge | A great forge hall with a huge bellows, rivers of molten metal and racks of swords |
| warehouse | A heavy stone warehouse with iron-banded doors, stacked crates and barrels inside |

## Units (1024x1024, vignette, character portrait from the waist up)

| file | prompt |
|---|---|
| goldMiner | A weathered miner with a pickaxe and a lantern, dust on his face, holding a gold nugget |
| ironMiner | A broad-shouldered miner with a sledgehammer, a sack of iron ore on his back |
| farmer | A peasant farmer with a scythe and a straw hat, sheaf of wheat |
| blacksmith | A blacksmith in a leather apron holding a hammer over a glowing blade |
| builder | A mason with a chisel and mallet, a coil of rope and a measuring rod |
| carrier | A porter carrying a large wooden frame pack loaded with sacks |
| guard | A town guard in a kettle helmet and padded gambeson holding a halberd |
| spy | A hooded spy with a half-hidden face, a dagger and a folded letter |
| infantry | A foot soldier in chainmail with a kite shield and a spear |
| bowman | A longbowman in a leather cap drawing a longbow, quiver at the hip |
| cavalry | An armored knight on a barded warhorse, lance raised |
