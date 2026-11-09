# What is left

1. Nothing deploys the game. The server image runs the `prod` profile, which needs a PostgreSQL 18 database (`DB_HOST`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`) and `KOD_JWT_SECRET`, and the browser image serves the built pages; both need a host, a managed database and a release step.
1. Eras won by a coalition monument, with rivals banding against the leader: designed in [ERA.md](ERA.md). The era, its clock, raising a monument, its garrison and assaults on it are built; winning the era with it is next.
1. Once real players exist, tune what scripted rulers cannot judge, from [BALANCE.md](BALANCE.md): the protection length after a defeat (24 turns), the land capture ratio, whether coordinated coalitions run away with an era, and whether economy, war and defense still end within reach of each other.
