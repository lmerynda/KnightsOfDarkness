package com.knightsofdarkness.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.knightsofdarkness.game.market.MarketOffer;

@RestController
public class MarketController {
    @Autowired
    private MarketService marketService;

    @PostMapping("/market_fixtures")
    void createOffers(@RequestBody ArrayList<MarketOffer> offers)
    {
        System.out.println("Creating new offers");
        marketService.createOffers(offers);
    }

    @PostMapping("/market/add")
    void createOffer(@RequestBody MarketOffer offer)
    {
        System.out.println("Creating new offer");
        marketService.createOffer(offer);
    }

    @GetMapping("/market")
    List<MarketOffer> getAllOffers()
    {
        System.out.println("Getting all offers");
        return marketService.getAllOffers();
    }
}
