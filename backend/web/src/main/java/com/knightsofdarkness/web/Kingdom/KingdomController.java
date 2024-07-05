package com.knightsofdarkness.web.Kingdom;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.knightsofdarkness.common.KingdomBuildingsDto;
import com.knightsofdarkness.common.KingdomDto;
import com.knightsofdarkness.common.KingdomUnitsDto;
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

    @GetMapping("/{name}")
    ResponseEntity<KingdomDto> getKingdomByName(@AuthenticationPrincipal UserData currentUser, @PathVariable String name)
    {
        if(currentUser != null)
        {
            log.info("User {} is trying to get kingdom {}", currentUser.getUsername(), name);
        }
        else
        {
            log.info("User not read from authentication context");
        }
        return kingdomService.getKingdomByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{name}/build")
    ResponseEntity<Integer> kingdomBuild(@PathVariable String name, @RequestBody KingdomBuildingsDto buildings)
    {
        return kingdomService.build(name, buildings);
    }

    @PostMapping("/{name}/train")
    ResponseEntity<KingdomDto> kingdomTrain(@PathVariable String name, @RequestBody KingdomUnitsDto unitsToTrain)
    {
        return kingdomService.train(name, unitsToTrain);
    }

    @PostMapping("/{name}/pass-turn")
    ResponseEntity<KingdomDto> kingdomPassTurn(@PathVariable String name)
    {
        return kingdomService.passTurn(name);
    }
}
