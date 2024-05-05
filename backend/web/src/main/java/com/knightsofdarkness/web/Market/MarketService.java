package com.knightsofdarkness.web.Market;

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
    public void createOffers(ArrayList<MarketOfferDto> offers)
    {
        log.info("Creating new offers");
        for (var offer : offers)
        {
            log.info(offer.toString());
            marketOfferRepository.add(offer.toDomain());
        }
    }

    @Transactional
    public void createOffer(MarketOfferDto offer)
    {
        log.info("Creating new offer" + offer.toString());
        marketOfferRepository.add(offer.toDomain());
    }

    public List<MarketOfferDto> getAllOffers()
    {
        log.info("Getting all offers");
        var allOffers = new ArrayList<MarketOffer>();
        allOffers.addAll(marketOfferRepository.getOffersByResource(MarketResource.food));
        allOffers.addAll(marketOfferRepository.getOffersByResource(MarketResource.iron));
        allOffers.addAll(marketOfferRepository.getOffersByResource(MarketResource.tools));
        allOffers.addAll(marketOfferRepository.getOffersByResource(MarketResource.weapons));
        log.info("Found " + allOffers.size() + " offers");

        return allOffers.stream().map(MarketOfferDto::fromDomain).toList();
    }

    public List<MarketOfferDto> getAllOffersByResource(MarketResource resource)
    {
        log.info("Getting all offers for " + resource);
        var allOffers = marketOfferRepository.getOffersByResource(resource).stream().map(MarketOfferDto::fromDomain).toList();
        log.info("Found " + allOffers.size() + " offers for " + resource);
        return allOffers;
    }
}
