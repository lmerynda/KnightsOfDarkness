package com.knightsofdarkness.web.market;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.knightsofdarkness.web.common.market.BuyMarketOfferDto;
import com.knightsofdarkness.web.common.market.BuyMarketOfferResult;
import com.knightsofdarkness.web.common.market.CreateMarketOfferResult;
import com.knightsofdarkness.web.common.market.MarketOfferDto;
import com.knightsofdarkness.web.common.market.MarketResource;
import com.knightsofdarkness.web.user.UserData;

@RestController
public class MarketController {
    private static final Logger log = LoggerFactory.getLogger(MarketController.class);
    private final MarketService marketService;

    public MarketController(MarketService marketService)
    {
        this.marketService = marketService;
    }

    @PostMapping("/market/create")
    ResponseEntity<CreateMarketOfferResult> createOffer(@AuthenticationPrincipal UserData currentUser, @RequestBody MarketOfferDto offer)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var createdOffer = marketService.createOffer(offer, currentUser.getKingdomName());

        return ResponseEntity.ok(createdOffer);
    }

    @GetMapping("/market/{resource}")
    List<MarketOfferDto> getOffersByResource(@PathVariable("resource") MarketResource resource)
    {
        return marketService.getOffersByResource(resource);
    }

    @PostMapping("/market/{id}/buy")
    ResponseEntity<BuyMarketOfferResult> buyOffer(@AuthenticationPrincipal UserData currentUser, @PathVariable UUID id, @RequestBody BuyMarketOfferDto buyerData)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return marketService.buyOffer(id, buyerData.count(), currentUser.getKingdomName()).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/market/{id}/withdraw")
    ResponseEntity<Boolean> withdrawOffer(@AuthenticationPrincipal UserData currentUser, @PathVariable UUID id)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return marketService.withdraw(id, currentUser.getKingdomName());
    }

    private void logUserUnauthenticated()
    {
        log.error("User not read from authentication context");
    }
}
