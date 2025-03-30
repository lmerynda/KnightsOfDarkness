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

import com.knightsofdarkness.web.common.alliance.AcceptAllianceInvitationDto;
import com.knightsofdarkness.web.common.alliance.AcceptAllianceInvitationResult;
import com.knightsofdarkness.web.common.alliance.AddBotToAllianceDto;
import com.knightsofdarkness.web.common.alliance.AddBotToAllianceResult;
import com.knightsofdarkness.web.common.alliance.AllianceDto;
import com.knightsofdarkness.web.common.alliance.AllianceInvitationDto;
import com.knightsofdarkness.web.common.alliance.AllianceWithMembersDto;
import com.knightsofdarkness.web.common.alliance.CreateAllianceDto;
import com.knightsofdarkness.web.common.alliance.CreateAllianceResult;
import com.knightsofdarkness.web.common.alliance.InviteAllianceResult;
import com.knightsofdarkness.web.common.alliance.LeaveAllianceResult;
import com.knightsofdarkness.web.common.alliance.RemoveFromAllianceDto;
import com.knightsofdarkness.web.common.alliance.RemoveFromAllianceResult;
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

    @PostMapping("/leave")
    public ResponseEntity<LeaveAllianceResult> leaveAlliance(@AuthenticationPrincipal UserData currentUser)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Leaving alliance");
        var result = allianceService.leaveAlliance(currentUser.getUsername());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/invite")
    public ResponseEntity<InviteAllianceResult> inviteToAlliance(@AuthenticationPrincipal UserData currentUser, @RequestBody AllianceInvitationDto inviteDto)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Inviting to alliance");
        var result = allianceService.inviteToAlliance(inviteDto.kingdomName(), currentUser.getUsername(), inviteDto.allianceName());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/accept")
    public ResponseEntity<AcceptAllianceInvitationResult> acceptAllianceInvitation(@AuthenticationPrincipal UserData currentUser, @RequestBody AcceptAllianceInvitationDto inviteDto)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Accepting alliance invitation");
        var result = allianceService.acceptAllianceInvitation(currentUser.getUsername(), inviteDto.invitationId());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/remove")
    public ResponseEntity<RemoveFromAllianceResult> removeFromAlliance(@AuthenticationPrincipal UserData currentUser, @RequestBody RemoveFromAllianceDto removeDto)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Removing from alliance");
        var result = allianceService.removeFromAlliance(removeDto.kingdomName(), currentUser.getUsername());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/add-bot")
    public ResponseEntity<AddBotToAllianceResult> addBotToAlliance(@AuthenticationPrincipal UserData currentUser, @RequestBody AddBotToAllianceDto addBotDto)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Adding bot to alliance");
        var result = allianceService.createNewBotAndAddToAlliance(currentUser.getUsername(), addBotDto.botName());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/all")
    public ResponseEntity<List<AllianceDto>> getAlliances()
    {
        var alliances = allianceService.getAlliances();
        return ResponseEntity.ok(alliances);
    }

    @GetMapping()
    public ResponseEntity<AllianceWithMembersDto> getAlliance(@AuthenticationPrincipal UserData currentUser)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return allianceService.getKingdomAlliance(currentUser.getUsername())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private void logUserUnauthenticated()
    {
        log.error("User not read from authentication context");
    }
}
