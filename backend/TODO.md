# What is left

1. The Game scenarios CI workflow (simulation and browser jobs) has never run on GitHub. Every command it runs passes locally (checked 2026-11-02: Gradle build with coverage, simulation typecheck and scenarios, browser build, lint and scenarios, `gradlew` executable, lockfiles in step); it runs on the next push to master.
1. Once real players exist, tune what scripted rulers cannot judge, from [BALANCE.md](BALANCE.md): the protection length after a defeat (24 turns), the land capture ratio, whether coordinated coalitions run away with a round, and whether economy, war and defense still end within reach of each other.
