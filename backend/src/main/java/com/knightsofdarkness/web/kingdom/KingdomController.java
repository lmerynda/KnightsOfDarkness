package com.knightsofdarkness.web.kingdom;

import com.knightsofdarkness.web.common.kingdom.KingdomTurnReport;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.knightsofdarkness.web.common.kingdom.KingdomBuildingsDto;
import com.knightsofdarkness.web.common.kingdom.KingdomDetailsDto;
import com.knightsofdarkness.web.common.kingdom.KingdomDto;
import com.knightsofdarkness.web.common.ActionResult;
import com.knightsofdarkness.web.common.kingdom.KingdomSpecialBuildingBuildDto;
import com.knightsofdarkness.web.common.kingdom.KingdomSpecialBuildingDemolishDto;
import com.knightsofdarkness.web.common.kingdom.KingdomSpecialBuildingStartDto;
import com.knightsofdarkness.web.common.kingdom.LandTransaction;
import com.knightsofdarkness.web.common.kingdom.SendAttackDto;
import com.knightsofdarkness.web.common.kingdom.SendCarriersDto;
import com.knightsofdarkness.web.common.kingdom.SendReinforcementsDto;
import com.knightsofdarkness.web.common.kingdom.UnitsMapDto;
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

        return kingdomService.getKingdomByName(currentUser.getKingdomName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/build")
    ResponseEntity<ActionResult<KingdomBuildingsDto>> kingdomBuild(@AuthenticationPrincipal UserData currentUser, @RequestBody KingdomBuildingsDto buildings)
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
    ResponseEntity<ActionResult<KingdomBuildingsDto>> kingdomDemolish(@AuthenticationPrincipal UserData currentUser, @RequestBody KingdomBuildingsDto buildings)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return kingdomService.demolish(currentUser.getKingdomName(), buildings)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/start-special-building")
    ResponseEntity<ActionResult<UUID>> kingdomStartSpecialBuilding(@AuthenticationPrincipal UserData currentUser, @RequestBody KingdomSpecialBuildingStartDto specialBuildingStartDto)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return kingdomService.startSpecialBuilding(currentUser.getKingdomName(), specialBuildingStartDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/demolish-special-building")
    ResponseEntity<ActionResult<Void>> kingdomDemolishSpecialBuilding(@AuthenticationPrincipal UserData currentUser, @RequestBody KingdomSpecialBuildingDemolishDto specialBuildingDemolishDto)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return kingdomService.demolishSpecialBuilding(currentUser.getKingdomName(), specialBuildingDemolishDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/build-special-building")
    ResponseEntity<ActionResult<Integer>> kingdomBuildSpecial(@AuthenticationPrincipal UserData currentUser, @RequestBody KingdomSpecialBuildingBuildDto specialBuildingBuildDto)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return kingdomService.buildSpecialBuilding(currentUser.getKingdomName(), specialBuildingBuildDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/train")
    ResponseEntity<ActionResult<UnitsMapDto>> kingdomTrain(@AuthenticationPrincipal UserData currentUser, @RequestBody UnitsMapDto unitsToTrain)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return kingdomService.train(currentUser.getKingdomName(), unitsToTrain)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/fire")
    ResponseEntity<ActionResult<UnitsMapDto>> kingdomFireUnits(@AuthenticationPrincipal UserData currentUser, @RequestBody UnitsMapDto unitsToFire)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return kingdomService.fireUnits(currentUser.getKingdomName(), unitsToFire)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/pass-turn")
    ResponseEntity<ActionResult<KingdomTurnReport>> kingdomPassTurn(@AuthenticationPrincipal UserData currentUser, @RequestBody int weaponsProductionPercentage)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return kingdomService.passTurn(currentUser.getKingdomName(), weaponsProductionPercentage)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * derived numbers and a forecast of the next turn, weaponsProductionPercentage is the blacksmith split the player plans to use
     */
    @GetMapping("/details")
    ResponseEntity<KingdomDetailsDto> getKingdomDetails(@AuthenticationPrincipal UserData currentUser, @RequestParam(defaultValue = "0") int weaponsProductionPercentage)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return kingdomService.getKingdomDetails(currentUser.getKingdomName(), Math.clamp(weaponsProductionPercentage, 0, 100))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * how much of the requested land the kingdom can afford right now and what it would cost
     */
    @GetMapping("/land-quote")
    ResponseEntity<LandTransaction> getLandQuote(@AuthenticationPrincipal UserData currentUser, @RequestParam int count)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return kingdomService.quoteLand(currentUser.getKingdomName(), Math.max(0, count))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/buy-land")
    ResponseEntity<ActionResult<LandTransaction>> kingdomBuyLand(@AuthenticationPrincipal UserData currentUser, @RequestBody int count)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return kingdomService.buyLand(currentUser.getKingdomName(), count)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/send-carriers")
    ResponseEntity<ActionResult<SendCarriersDto>> kingdomSendCarriers(@AuthenticationPrincipal UserData currentUser, @RequestBody SendCarriersDto sendCarriersDto)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return kingdomService.sendCarriers(currentUser.getKingdomName(), sendCarriersDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/withdraw-carriers")
    ResponseEntity<ActionResult<Void>> kingdomWithdrawCarriers(@AuthenticationPrincipal UserData currentUser, @RequestBody UUID carriersOnTheMoveId)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return kingdomService.withdrawCarriers(currentUser.getKingdomName(), carriersOnTheMoveId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/send-attack")
    ResponseEntity<ActionResult<SendAttackDto>> kingdomSendAttack(@AuthenticationPrincipal UserData currentUser, @RequestBody SendAttackDto sendAttackDto)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return kingdomService.sendAttack(currentUser.getKingdomName(), sendAttackDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/withdraw-attack")
    ResponseEntity<ActionResult<Void>> kingdomWithdrawAttack(@AuthenticationPrincipal UserData currentUser, @RequestBody UUID attackId)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return kingdomService.withdrawAttack(currentUser.getKingdomName(), attackId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/send-reinforcements")
    ResponseEntity<ActionResult<Void>> kingdomSendReinforcements(@AuthenticationPrincipal UserData currentUser, @RequestBody SendReinforcementsDto order)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return kingdomService.sendReinforcements(currentUser.getKingdomName(), order)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/recall-reinforcements")
    ResponseEntity<ActionResult<Void>> kingdomRecallReinforcements(@AuthenticationPrincipal UserData currentUser, @RequestBody UUID reinforcementId)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return kingdomService.recallReinforcements(currentUser.getKingdomName(), reinforcementId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private void logUserUnauthenticated()
    {
        log.error("User not read from authentication context");
    }
}
