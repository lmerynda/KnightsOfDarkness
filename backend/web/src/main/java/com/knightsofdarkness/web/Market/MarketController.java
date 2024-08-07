package com.knightsofdarkness.web.Market;

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

import com.knightsofdarkness.common.MarketBuyerDto;
import com.knightsofdarkness.common.MarketOfferDto;
import com.knightsofdarkness.game.market.MarketOfferBuyResult;
import com.knightsofdarkness.game.market.MarketResource;
import com.knightsofdarkness.web.User.UserData;

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
            log.error("User not read from authentication context");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        offer.sellerName = currentUser.getKingdom();
        marketService.createOffer(offer);
        return ResponseEntity.ok(offer);
    }

    @GetMapping("/market")
    List<MarketOfferDto> getAllOffers()
    {
        return marketService.getAllOffers();
    }

    @GetMapping("/market/{resource}")
    List<MarketOfferDto> getAllOffersByResource(@PathVariable MarketResource resource)
    {
        return marketService.getAllOffersByResource(resource);
    }

    @PostMapping("/market/{id}/buy")
    ResponseEntity<MarketOfferBuyResult> buyOffer(@AuthenticationPrincipal UserData currentUser, @PathVariable UUID id, @RequestBody MarketBuyerDto buyerData)
    {
        if (currentUser == null)
        {
            log.error("User not read from authentication context");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return marketService.buyOffer(id, buyerData.count, currentUser.getKingdom());
    }

    @PostMapping("/market/{id}/withdraw")
    ResponseEntity<Boolean> buyOffer(@AuthenticationPrincipal UserData currentUser, @PathVariable UUID id)
    {
        if (currentUser == null)
        {
            log.error("User not read from authentication context");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return marketService.withdraw(id, currentUser.getKingdom());
    }
}
