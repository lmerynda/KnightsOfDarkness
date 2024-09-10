package com.knightsofdarkness.web.market;

import java.util.ArrayList;
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

import com.knightsofdarkness.common_legacy.market.MarketBuyerDto;
import com.knightsofdarkness.common_legacy.market.MarketOfferDto;
import com.knightsofdarkness.game.market.MarketOfferBuyResult;
import com.knightsofdarkness.game.market.MarketResource;
import com.knightsofdarkness.web.user.UserData;

@RestController
public class MarketController {
    private static final Logger log = LoggerFactory.getLogger(MarketController.class);
    private final MarketService marketService;

    public MarketController(MarketService marketService)
    {
        this.marketService = marketService;
    }

    @PostMapping("/market_fixtures")
    void createOffers(@RequestBody ArrayList<MarketOfferDto> offers)
    {
        marketService.createOffers(offers);
    }

    @PostMapping("/market/create")
    ResponseEntity<MarketOfferDto> createOffer(@AuthenticationPrincipal UserData currentUser, @RequestBody MarketOfferDto offer)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        offer.sellerName = currentUser.getKingdom();
        var createdOffer = marketService.createOffer(offer);
        if (createdOffer.isEmpty())
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(createdOffer.get());
    }

    @Deprecated
    @GetMapping("/market")
    List<MarketOfferDto> getAllOffers()
    {
        return marketService.getAllOffers();
    }

    @GetMapping("/market/{resource}")
    List<MarketOfferDto> getOffersByResource(@PathVariable MarketResource resource)
    {
        return marketService.getOffersByResource(resource);
    }

    @PostMapping("/market/{id}/buy")
    ResponseEntity<MarketOfferBuyResult> buyOffer(@AuthenticationPrincipal UserData currentUser, @PathVariable UUID id, @RequestBody MarketBuyerDto buyerData)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return marketService.buyOffer(id, buyerData.count, currentUser.getKingdom());
    }

    @PostMapping("/market/{id}/withdraw")
    ResponseEntity<Boolean> buyOffer(@AuthenticationPrincipal UserData currentUser, @PathVariable UUID id)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return marketService.withdraw(id, currentUser.getKingdom());
    }

    private void logUserUnauthenticated()
    {
        log.error("User not read from authentication context");
    }
}
