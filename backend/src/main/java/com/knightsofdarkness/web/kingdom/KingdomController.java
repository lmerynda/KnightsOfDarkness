package com.knightsofdarkness.web.kingdom;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.knightsofdarkness.web.common.kingdom.KingdomBuildingsActionResult;
import com.knightsofdarkness.web.common.kingdom.KingdomBuildingsDto;
import com.knightsofdarkness.web.common.kingdom.KingdomDto;
import com.knightsofdarkness.web.common.kingdom.KingdomPassTurnActionResult;
import com.knightsofdarkness.web.common.kingdom.KingdomSpecialBuildingBuildDto;
import com.knightsofdarkness.web.common.kingdom.KingdomSpecialBuildingDemolishDto;
import com.knightsofdarkness.web.common.kingdom.KingdomSpecialBuildingStartDto;
import com.knightsofdarkness.web.common.kingdom.KingdomUnitsActionResult;
import com.knightsofdarkness.web.common.kingdom.LandTransaction;
import com.knightsofdarkness.web.common.kingdom.SendAttackDto;
import com.knightsofdarkness.web.common.kingdom.SendAttackResult;
import com.knightsofdarkness.web.common.kingdom.SendCarriersDto;
import com.knightsofdarkness.web.common.kingdom.SendCarriersResult;
import com.knightsofdarkness.web.common.kingdom.UnitsMapDto;
import com.knightsofdarkness.web.kingdom.model.KingdomSpecialBuildingEntity;
import com.knightsofdarkness.web.user.UserData;

@RestController
@RequestMapping("/kingdom")
public class KingdomController
{
    private static final Logger log = LoggerFactory.getLogger(KingdomController.class);
    private final KingdomService kingdomService;

    public KingdomController(KingdomService kingdomService)
    {
        this.kingdomService = kingdomService;
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
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("User {} requested kingdom", currentUser.email);

        return kingdomService.getKingdomByName(currentUser.getKingdomName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/build")
    ResponseEntity<KingdomBuildingsActionResult> kingdomBuild(@AuthenticationPrincipal UserData currentUser, @RequestBody KingdomBuildingsDto buildings)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return kingdomService.build(currentUser.getKingdomName(), buildings).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/demolish")
    ResponseEntity<KingdomBuildingsActionResult> kingdomDemolish(@AuthenticationPrincipal UserData currentUser, @RequestBody KingdomBuildingsDto buildings)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return kingdomService.demolish(currentUser.getKingdomName(), buildings);
    }

    @PostMapping("/start-special-building")
    ResponseEntity<KingdomSpecialBuildingEntity> kingdomStartSpecialBuilding(@AuthenticationPrincipal UserData currentUser, @RequestBody KingdomSpecialBuildingStartDto specialBuildingStartDto)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return kingdomService.startSpecialBuilding(currentUser.getKingdomName(), specialBuildingStartDto);
    }

    @PostMapping("/demolish-special-building")
    ResponseEntity<Boolean> kingdomDemolishSpecialBuilding(@AuthenticationPrincipal UserData currentUser, @RequestBody KingdomSpecialBuildingDemolishDto specialBuildingDemolishDto)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return kingdomService.demolishSpecialBuilding(currentUser.getKingdomName(), specialBuildingDemolishDto);
    }

    @PostMapping("/build-special-building")
    ResponseEntity<Integer> kingdomBuildSpecial(@AuthenticationPrincipal UserData currentUser, @RequestBody KingdomSpecialBuildingBuildDto specialBuildingBuildDto)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return kingdomService.buildSpecialBuilding(currentUser.getKingdomName(), specialBuildingBuildDto);
    }

    @PostMapping("/train")
    ResponseEntity<KingdomUnitsActionResult> kingdomTrain(@AuthenticationPrincipal UserData currentUser, @RequestBody UnitsMapDto unitsToTrain)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return kingdomService.train(currentUser.getKingdomName(), unitsToTrain);
    }

    @PostMapping("/fire")
    ResponseEntity<KingdomUnitsActionResult> kingdomFireUnits(@AuthenticationPrincipal UserData currentUser, @RequestBody UnitsMapDto unitsToFire)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return kingdomService.fireUnits(currentUser.getKingdomName(), unitsToFire);
    }

    @PostMapping(value = "/pass-turn")
    ResponseEntity<KingdomPassTurnActionResult> kingdomPassTurn(@AuthenticationPrincipal UserData currentUser, @RequestBody int weaponsProductionPercentage)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return kingdomService.passTurn(currentUser.getKingdomName(), weaponsProductionPercentage);
    }

    @PostMapping(value = "/buy-land")
    ResponseEntity<LandTransaction> kingdomBuyLand(@AuthenticationPrincipal UserData currentUser, @RequestBody int amount)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return kingdomService.buyLand(currentUser.getKingdomName(), amount);
    }

    @PostMapping(value = "/send-carriers")
    ResponseEntity<SendCarriersResult> kingdomSendCarriers(@AuthenticationPrincipal UserData currentUser, @RequestBody SendCarriersDto sendCarriersDto)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return kingdomService.sendCarriers(currentUser.getKingdomName(), sendCarriersDto);
    }

    @PostMapping(value = "/withdraw-carriers")
    ResponseEntity<Boolean> kingdomWithdrawCarriers(@AuthenticationPrincipal UserData currentUser, @RequestBody UUID carriersOnTheMoveId)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return kingdomService.withdrawCarriers(currentUser.getKingdomName(), carriersOnTheMoveId);
    }

    @PostMapping(value = "/send-attack")
    ResponseEntity<SendAttackResult> kingdomSendAttack(@AuthenticationPrincipal UserData currentUser, @RequestBody SendAttackDto sendAttackDto)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return kingdomService.sendAttack(currentUser.getKingdomName(), sendAttackDto);
    }

    @PostMapping(value = "/withdraw-attack")
    ResponseEntity<Boolean> kingdomWithdrawAttack(@AuthenticationPrincipal UserData currentUser, @RequestBody UUID attackId)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return kingdomService.withdrawAttack(currentUser.getKingdomName(), attackId);
    }

    private void logUserUnauthenticated()
    {
        log.error("User not read from authentication context");
    }
}
