package com.knightsofdarkness.web.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.web.user.UserData;

@RestController
public class GameController {
    private static final Logger log = LoggerFactory.getLogger(GameController.class);
    private final GameConfig gameConfig;

    public GameController(GameConfig gameConfig)
    {
        this.gameConfig = gameConfig;
    }

    @PostMapping("/config")
    ResponseEntity<GameConfig> getConfig(@AuthenticationPrincipal UserData currentUser)
    {
        log.info("User {} requested game config", currentUser);
        if (currentUser == null)
        {
            log.error("User not read from authentication context");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(gameConfig);
    }
}
