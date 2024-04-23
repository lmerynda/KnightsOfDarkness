package com.knightsofdarkness.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.knightsofdarkness.game.market.MarketOffer;
import com.knightsofdarkness.game.market.MarketResource;

@RestController
public class MarketController {
    @Autowired
    private MarketService marketService;

    @PostMapping("/market_fixtures")
    void createOffers(@RequestBody ArrayList<MarketOffer> offers)
    {
        marketService.createOffers(offers);
    }

    @PostMapping("/market/add")
    void createOffer(@RequestBody MarketOffer offer)
    {
        marketService.createOffer(offer);
    }

    @GetMapping("/market")
    List<MarketOffer> getAllOffers()
    {
        return marketService.getAllOffers();
    }

    @GetMapping("/market/{resource}")
    List<MarketOffer> getAllOffersByResource(@PathVariable MarketResource resource)
    {
        return marketService.getAllOffersByResource(resource);
    }
}
