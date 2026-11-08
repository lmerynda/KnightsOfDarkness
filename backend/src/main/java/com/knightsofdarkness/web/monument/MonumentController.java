package com.knightsofdarkness.web.monument;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.knightsofdarkness.web.common.ActionResult;
import com.knightsofdarkness.web.common.alliance.FoundMonumentDto;
import com.knightsofdarkness.web.common.alliance.MonumentContributionDto;
import com.knightsofdarkness.web.common.alliance.MonumentDto;
import com.knightsofdarkness.web.common.alliance.MonumentLedgerDto;
import com.knightsofdarkness.web.user.UserData;

@RestController
public class MonumentController {
    private final MonumentService monumentService;

    public MonumentController(MonumentService monumentService)
    {
        this.monumentService = monumentService;
    }

    /**
     * public, the landing page shows every monument
     */
    @GetMapping("/game/monuments")
    ResponseEntity<List<MonumentDto>> getMonuments()
    {
        return ResponseEntity.ok(monumentService.getMonuments());
    }

    @GetMapping("/alliance/monument")
    ResponseEntity<MonumentLedgerDto> getLedger(@AuthenticationPrincipal UserData currentUser)
    {
        return monumentService.getLedger(currentUser.getKingdomName()).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/alliance/monument/found")
    ResponseEntity<ActionResult<Void>> found(@AuthenticationPrincipal UserData currentUser, @RequestBody FoundMonumentDto foundDto)
    {
        return ResponseEntity.ok(monumentService.found(currentUser.getKingdomName(), foundDto));
    }

    @PostMapping("/alliance/monument/contribute")
    ResponseEntity<ActionResult<Void>> contribute(@AuthenticationPrincipal UserData currentUser, @RequestBody MonumentContributionDto goods)
    {
        return ResponseEntity.ok(monumentService.contribute(currentUser.getKingdomName(), goods));
    }
}
