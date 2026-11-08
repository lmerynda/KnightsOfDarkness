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
import com.knightsofdarkness.web.common.game.EraDto;
import com.knightsofdarkness.web.common.kingdom.KingdomDto;
import com.knightsofdarkness.web.common.market.MarketTradeDto;
import com.knightsofdarkness.web.era.EraService;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.KingdomService;
import com.knightsofdarkness.web.market.model.MarketTransactionJpaRepository;

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
    private final EraService eraService;
    private final Scheduler botScheduler;
    private final GameConfig gameConfig;
    private final MarketTransactionJpaRepository trades;

    public SimulationController(KingdomService kingdomService, EraService eraService, Scheduler botScheduler, GameConfig gameConfig, MarketTransactionJpaRepository trades)
    {
        this.trades = trades;
        this.kingdomService = kingdomService;
        this.eraService = eraService;
        this.botScheduler = botScheduler;
        this.gameConfig = gameConfig;
    }

    /**
     * one tick is what the wall clock does every newTurnPeriodicity: every kingdom gets a turn, the era moves on and,
     * unless bots=false, every bot acts once
     */
    @PostMapping("/advance")
    ResponseEntity<EraDto> advance(@RequestParam(defaultValue = "1") int ticks, @RequestParam(defaultValue = "true") boolean bots)
    {
        for (int tick = 0; tick < ticks; tick++)
        {
            kingdomService.addTurnForEveryone();
            eraService.tick();
            if (bots)
            {
                botScheduler.runBots();
            }
        }
        return ResponseEntity.ok(eraService.getCurrentEra());
    }

    /**
     * the full state of every kingdom, which no player can see, for measuring the world
     */
    @GetMapping("/kingdoms")
    ResponseEntity<List<KingdomDto>> kingdoms()
    {
        return ResponseEntity.ok(kingdomService.getAllKingdoms());
    }

    /**
     * every sale the market has seen, for measuring who trades with whom
     */
    @GetMapping("/trades")
    ResponseEntity<List<MarketTradeDto>> trades()
    {
        return ResponseEntity.ok(trades.findAll().stream().map(trade -> trade.toTradeDto()).toList());
    }

    @GetMapping("/config")
    ResponseEntity<GameConfig> config()
    {
        return ResponseEntity.ok(gameConfig);
    }
}
