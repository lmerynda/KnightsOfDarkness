package com.knightsofdarkness.web.Kingdom;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.knightsofdarkness.common.KingdomBuildingsDto;
import com.knightsofdarkness.common.KingdomDto;
import com.knightsofdarkness.common.KingdomSpecialBuildingStartDto;
import com.knightsofdarkness.common.KingdomUnitsDto;
import com.knightsofdarkness.game.kingdom.KingdomSpecialBuilding;
import com.knightsofdarkness.game.kingdom.KingdomTurnReport;
import com.knightsofdarkness.game.kingdom.LandTransaction;
import com.knightsofdarkness.web.User.UserData;

@RestController
@RequestMapping("/kingdom")
public class KingdomController {
    private static final Logger log = LoggerFactory.getLogger(KingdomController.class);
    private final KingdomService kingdomService;

    public KingdomController(KingdomService kingdomService)
    {
        this.kingdomService = kingdomService;
    }

    @PostMapping("/")
    ResponseEntity<KingdomDto> createKingdom(@RequestBody KingdomDto kingdom)
    {
        var createdKingdom = kingdomService.createKingdom(kingdom);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{name}")
                .buildAndExpand(createdKingdom.name)
                .toUri();

        return ResponseEntity.created(location).body(createdKingdom);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolationException(DataIntegrityViolationException ex)
    {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Kingdom already exists");
    }

    @GetMapping()
    ResponseEntity<KingdomDto> getKingdom(@AuthenticationPrincipal UserData currentUser)
    {
        if (currentUser == null)
        {
            log.error("User not read from authentication context");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("User {} requested kingdom", currentUser);

        return kingdomService.getKingdomByName(currentUser.getKingdom())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/build")
    ResponseEntity<KingdomBuildingsDto> kingdomBuild(@AuthenticationPrincipal UserData currentUser, @RequestBody KingdomBuildingsDto buildings)
    {
        if (currentUser == null)
        {
            log.error("User not read from authentication context");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return kingdomService.build(currentUser.kingdom, buildings);
    }

    @PostMapping("/start-special-building")
    ResponseEntity<KingdomSpecialBuilding> kingdomSelectSpecial(@AuthenticationPrincipal UserData currentUser, @RequestBody KingdomSpecialBuildingStartDto specialBuildingStartDto)
    {
        if (currentUser == null)
        {
            log.error("User not read from authentication context");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return kingdomService.startSpecialBuilding(currentUser.kingdom, specialBuildingStartDto);
    }

    @PostMapping("/build-special")
    ResponseEntity<KingdomBuildingsDto> kingdomBuildSpecial(@AuthenticationPrincipal UserData currentUser, @RequestBody KingdomBuildingsDto buildings)
    {
        if (currentUser == null)
        {
            log.error("User not read from authentication context");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return kingdomService.build(currentUser.kingdom, buildings);
    }

    @PostMapping("/train")
    ResponseEntity<KingdomUnitsDto> kingdomTrain(@AuthenticationPrincipal UserData currentUser, @RequestBody KingdomUnitsDto unitsToTrain)
    {
        if (currentUser == null)
        {
            log.error("User not read from authentication context");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return kingdomService.train(currentUser.kingdom, unitsToTrain);
    }

    @PostMapping(value = "/pass-turn", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<KingdomTurnReport> kingdomPassTurn(@AuthenticationPrincipal UserData currentUser)
    {
        if (currentUser == null)
        {
            log.error("User not read from authentication context");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var passTurnResult = kingdomService.passTurn(currentUser.kingdom);

        return passTurnResult;
    }

    @PostMapping(value = "/buy-land", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<LandTransaction> kingdomBuyLand(@AuthenticationPrincipal UserData currentUser, @RequestBody int amount)
    {
        if (currentUser == null)
        {
            log.error("User not read from authentication context");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var landTransaction = kingdomService.buyLand(currentUser.kingdom, amount);

        return landTransaction;
    }
}
