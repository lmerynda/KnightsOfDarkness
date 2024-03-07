package com.knightsofdarkness.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.knightsofdarkness.game.market.MarketOffer;
import com.knightsofdarkness.storage.market.MarketOfferEntity;

@RestController
public class MarketController {
    @Autowired
    private MarketService marketService;

    @PostMapping("/market_fixtures")
    void createOffers(@RequestBody ArrayList<MarketOfferEntity> offers)
    {
        System.err.println("Creating new offers");
        marketService.createOffers(offers);
    }

    @PostMapping("/market/add")
    void createOffer(@RequestBody MarketOfferEntity offer)
    {
        System.err.println("Creating new offer");
        System.out.println(offer);
        marketService.createOffer(offer);
    }

    @GetMapping("/market")
    List<MarketOffer> getAllOffers()
    {
        System.err.println("Getting all offers");
        return marketService.getAllOffers();
    }

    @GetMapping("/")
    String helloWorld()
    {
        System.err.println("Hello World!");

        return "Hello World!";
    }
}
