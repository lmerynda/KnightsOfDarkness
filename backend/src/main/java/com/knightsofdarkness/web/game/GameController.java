package com.knightsofdarkness.web.game;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.knightsofdarkness.web.common.game.EraDto;
import com.knightsofdarkness.web.common.game.RankingsDto;
import com.knightsofdarkness.web.era.EraService;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.user.UserData;

@RestController
public class GameController {
    private static final Logger log = LoggerFactory.getLogger(GameController.class);
    private final GameConfig gameConfig;
    private final EraService eraService;

    public GameController(GameConfig gameConfig, EraService eraService)
    {
        this.gameConfig = gameConfig;
        this.eraService = eraService;
    }

    @PostMapping("/config")
    ResponseEntity<GameConfig> getConfig(@AuthenticationPrincipal UserData currentUser)
    {
        if (currentUser == null)
        {
            log.error("User not read from authentication context");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(gameConfig);
    }

    /**
     * public, the landing page shows the ongoing era
     */
    @GetMapping("/game/era")
    ResponseEntity<EraDto> getCurrentEra()
    {
        return ResponseEntity.ok(eraService.getCurrentEra());
    }

    @GetMapping("/game/eras")
    ResponseEntity<List<EraDto>> getFinishedEras()
    {
        return ResponseEntity.ok(eraService.getFinishedEras());
    }

    @GetMapping("/game/rankings")
    ResponseEntity<RankingsDto> getRankings()
    {
        return ResponseEntity.ok(eraService.getRankings());
    }
}
