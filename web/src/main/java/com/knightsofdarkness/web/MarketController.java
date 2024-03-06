package com.knightsofdarkness.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.knightsofdarkness.game.market.MarketOffer;
import com.knightsofdarkness.game.market.MarketResource;
import com.knightsofdarkness.storage.market.MarketOfferRepository;

@RestController
public class MarketController {
    @Autowired
    MarketOfferRepository marketOfferRepository;

    @GetMapping("/market")
    List<MarketOffer> getAllOffers()
    {
        System.err.println("Getting all offers");
        var offers = marketOfferRepository.getOffersByResource(MarketResource.food);

        return offers;
    }

    @GetMapping("/")
    String helloWorld()
    {
        System.err.println("Hello World!");

        return "Hello World!";
    }
}
