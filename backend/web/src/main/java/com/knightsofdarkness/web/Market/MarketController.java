package com.knightsofdarkness.web.Market;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.knightsofdarkness.common.MarketBuyerDto;
import com.knightsofdarkness.common.MarketOfferDto;
import com.knightsofdarkness.game.market.MarketResource;

@RestController
public class MarketController {
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
    void createOffer(@RequestBody MarketOfferDto offer)
    {
        marketService.createOffer(offer);
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
    ResponseEntity<Integer> buyOffer(@PathVariable UUID id, @RequestBody MarketBuyerDto buyerData)
    {
        return marketService.buyOffer(id, buyerData);
    }

    @PostMapping("/market/{id}/withdraw")
    ResponseEntity<Boolean> buyOffer(@PathVariable UUID id)
    {
        return marketService.withdraw(id);
    }
}
