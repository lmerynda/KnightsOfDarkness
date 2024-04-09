package com.knightsofdarkness.web;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.knightsofdarkness.game.market.MarketOffer;
import com.knightsofdarkness.game.market.MarketResource;
import com.knightsofdarkness.storage.market.MarketOfferRepository;

import jakarta.persistence.EntityManager;

@Service
public class MarketService {
    private final Logger log = LoggerFactory.getLogger(MarketController.class);

    @Autowired
    private MarketOfferRepository marketOfferRepository;

    @Autowired
    private EntityManager entityManager;

    @Transactional
    public void createOffers(ArrayList<MarketOffer> offers)
    {
        log.info("Creating new offers");
        for (var offer : offers)
        {
            log.info(offer.toString());
            marketOfferRepository.add(offer);
        }
    }

    @Transactional
    public void createOffer(MarketOffer offer)
    {
        log.info("Creating new offer" + offer.toString());
        marketOfferRepository.add(offer);
    }

    public List<MarketOffer> getAllOffers()
    {
        log.info("Getting all offers");
        return marketOfferRepository.getOffersByResource(MarketResource.food);
    }
}
