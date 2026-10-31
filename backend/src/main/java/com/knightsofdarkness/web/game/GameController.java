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

import com.knightsofdarkness.web.common.game.RankingsDto;
import com.knightsofdarkness.web.common.game.RoundDto;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.round.RoundService;
import com.knightsofdarkness.web.user.UserData;

@RestController
public class GameController {
    private static final Logger log = LoggerFactory.getLogger(GameController.class);
    private final GameConfig gameConfig;
    private final RoundService roundService;

    public GameController(GameConfig gameConfig, RoundService roundService)
    {
        this.gameConfig = gameConfig;
        this.roundService = roundService;
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
     * public, the landing page shows the ongoing round
     */
    @GetMapping("/game/round")
    ResponseEntity<RoundDto> getCurrentRound()
    {
        return ResponseEntity.ok(roundService.getCurrentRound());
    }

    @GetMapping("/game/rounds")
    ResponseEntity<List<RoundDto>> getFinishedRounds()
    {
        return ResponseEntity.ok(roundService.getFinishedRounds());
    }

    @GetMapping("/game/rankings")
    ResponseEntity<RankingsDto> getRankings()
    {
        return ResponseEntity.ok(roundService.getRankings());
    }
}
