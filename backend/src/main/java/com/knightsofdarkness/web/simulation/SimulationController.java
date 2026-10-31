package com.knightsofdarkness.web.simulation;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.knightsofdarkness.web.bots.Scheduler;
import com.knightsofdarkness.web.common.game.RoundDto;
import com.knightsofdarkness.web.common.kingdom.KingdomDto;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.KingdomService;
import com.knightsofdarkness.web.round.RoundService;

/**
 * The edge a simulation harness drives the world through. It exists only in the simulation profile, where the wall
 * clock is off: time moves only when the harness advances it.
 */
@Profile("simulation")
@RestController
@RequestMapping("/simulation")
public class SimulationController
{
    private final KingdomService kingdomService;
    private final RoundService roundService;
    private final Scheduler botScheduler;
    private final GameConfig gameConfig;

    public SimulationController(KingdomService kingdomService, RoundService roundService, Scheduler botScheduler, GameConfig gameConfig)
    {
        this.kingdomService = kingdomService;
        this.roundService = roundService;
        this.botScheduler = botScheduler;
        this.gameConfig = gameConfig;
    }

    /**
     * one tick is what the wall clock does every newTurnPeriodicity: every kingdom gets a turn, the round moves on and,
     * unless bots=false, every bot acts once
     */
    @PostMapping("/advance")
    ResponseEntity<RoundDto> advance(@RequestParam(defaultValue = "1") int ticks, @RequestParam(defaultValue = "true") boolean bots)
    {
        for (int tick = 0; tick < ticks; tick++)
        {
            kingdomService.addTurnForEveryone();
            roundService.tick();
            if (bots)
            {
                botScheduler.runBots();
            }
        }
        return ResponseEntity.ok(roundService.getCurrentRound());
    }

    @PostMapping("/end-round")
    ResponseEntity<RoundDto> endRound()
    {
        roundService.endCurrentRound();
        return ResponseEntity.ok(roundService.getCurrentRound());
    }

    /**
     * the full state of every kingdom, which no player can see, for measuring the world
     */
    @GetMapping("/kingdoms")
    ResponseEntity<List<KingdomDto>> kingdoms()
    {
        return ResponseEntity.ok(kingdomService.getAllKingdoms());
    }

    @GetMapping("/config")
    ResponseEntity<GameConfig> config()
    {
        return ResponseEntity.ok(gameConfig);
    }
}
