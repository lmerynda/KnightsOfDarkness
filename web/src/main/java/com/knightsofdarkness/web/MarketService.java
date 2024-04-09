package com.knightsofdarkness.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.knightsofdarkness.game.market.MarketOffer;
import com.knightsofdarkness.game.market.MarketResource;
import com.knightsofdarkness.storage.market.MarketOfferRepository;

import jakarta.persistence.EntityManager;

@Service
public class MarketService {
    @Autowired
    private MarketOfferRepository marketOfferRepository;

    @Autowired
    private EntityManager entityManager;

    @Transactional
    public void createOffers(ArrayList<MarketOffer> offers)
    {
        System.out.println("Creating new offers");
        for (var offer : offers)
        {
            System.out.println(offer.toString());
            marketOfferRepository.add(offer);
        }
    }

    @Transactional
    public void createOffer(MarketOffer offer)
    {
        System.out.println(offer.toString());
        marketOfferRepository.add(offer);
    }

    public List<MarketOffer> getAllOffers()
    {
        System.out.println("Getting all offers");
        var offers = marketOfferRepository.getOffersByResource(MarketResource.food);

        return offers;
    }
}
