package com.knightsofdarkness.web.war;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.knightsofdarkness.web.common.war.BattleReportDto;
import com.knightsofdarkness.web.common.war.SendSpiesDto;
import com.knightsofdarkness.web.common.war.SendSpiesResult;
import com.knightsofdarkness.web.common.war.SpyReportDto;
import com.knightsofdarkness.web.user.UserData;

@RestController
@RequestMapping("/war")
public class WarController {
    private static final Logger log = LoggerFactory.getLogger(WarController.class);
    private final WarService warService;

    public WarController(WarService warService)
    {
        this.warService = warService;
    }

    @PostMapping("/send-spies")
    ResponseEntity<SendSpiesResult> sendSpies(@AuthenticationPrincipal UserData currentUser, @RequestBody SendSpiesDto sendSpiesDto)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return warService.sendSpies(currentUser.getKingdomName(), sendSpiesDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/battle-reports")
    ResponseEntity<List<BattleReportDto>> getBattleReports(@AuthenticationPrincipal UserData currentUser)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(warService.getBattleReports(currentUser.getKingdomName()));
    }

    @GetMapping("/spy-reports")
    ResponseEntity<List<SpyReportDto>> getSpyReports(@AuthenticationPrincipal UserData currentUser)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(warService.getSpyReports(currentUser.getKingdomName()));
    }

    private void logUserUnauthenticated()
    {
        log.error("User not read from authentication context");
    }
}
