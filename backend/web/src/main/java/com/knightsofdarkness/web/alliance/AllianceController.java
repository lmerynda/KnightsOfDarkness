package com.knightsofdarkness.web.alliance;

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

import com.knightsofdarkness.common.alliance.AllianceDto;
import com.knightsofdarkness.common.alliance.CreateAllianceDto;
import com.knightsofdarkness.common.alliance.CreateAllianceResult;
import com.knightsofdarkness.web.user.UserData;

@RestController
@RequestMapping("/alliance")
public class AllianceController {
    private static final Logger log = LoggerFactory.getLogger(AllianceController.class);
    private final AllianceService allianceService;

    public AllianceController(AllianceService allianceService)
    {
        this.allianceService = allianceService;
    }

    @PostMapping("/create")
    public ResponseEntity<CreateAllianceResult> createAlliance(@AuthenticationPrincipal UserData currentUser, @RequestBody CreateAllianceDto allianceDto)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Creating alliance");
        var result = allianceService.createAlliance(allianceDto, currentUser.getUsername());
        return ResponseEntity.ok(result);
    }

    @GetMapping()
    public ResponseEntity<List<AllianceDto>> getAlliances()
    {
        var alliances = allianceService.getAlliances();
        return ResponseEntity.ok(alliances);
    }

    private void logUserUnauthenticated()
    {
        log.error("User not read from authentication context");
    }
}
