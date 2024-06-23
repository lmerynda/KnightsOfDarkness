package com.knightsofdarkness.web.Market;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.knightsofdarkness.game.market.IMarket;
import com.knightsofdarkness.game.market.MarketOffer;
import com.knightsofdarkness.game.market.MarketResource;
import com.knightsofdarkness.storage.kingdom.KingdomRepository;

import jakarta.persistence.EntityManager;

@Service
public class MarketService {
    private final Logger log = LoggerFactory.getLogger(MarketService.class);

    private final IMarket market;

    final KingdomRepository kingdomRepository;

    private final EntityManager entityManager;

    public MarketService(IMarket market, KingdomRepository kingdomRepository, EntityManager entityManager)
    {
        this.market = market;
        this.kingdomRepository = kingdomRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public void createOffers(ArrayList<MarketOfferDto> offers)
    {
        log.info("Creating new offers");
        for (var offer : offers)
        {
            log.info(offer.toString());
            var kingdom = kingdomRepository.getKingdomByName(offer.sellerName);
            if (kingdom.isPresent())
            {
                market.addOffer(kingdom.get(), offer.resource, offer.count, offer.price);
            } else
            {
                log.warn("Kingdom with name " + offer.sellerName + " not found");
            }
        }
    }

    @Transactional
    public void createOffer(MarketOfferDto offer)
    {
        log.info("Creating new offer" + offer.toString());
        var kingdom = kingdomRepository.getKingdomByName(offer.sellerName);
        if (kingdom.isPresent())
        {
            market.addOffer(kingdom.get(), offer.resource, offer.count, offer.price);
        } else
        {
            log.warn("Kingdom with name " + offer.sellerName + " not found");
        }
    }

    public List<MarketOfferDto> getAllOffers()
    {
        log.info("Getting all offers");
        var allOffers = new ArrayList<MarketOffer>();
        allOffers.addAll(market.getOffersByResource(MarketResource.food));
        allOffers.addAll(market.getOffersByResource(MarketResource.iron));
        allOffers.addAll(market.getOffersByResource(MarketResource.tools));
        allOffers.addAll(market.getOffersByResource(MarketResource.weapons));
        log.info("Found " + allOffers.size() + " offers");

        return allOffers.stream().map(MarketOfferDto::fromDomain).toList();
    }

    public List<MarketOfferDto> getAllOffersByResource(MarketResource resource)
    {
        log.info("Getting all offers for " + resource);
        var allOffers = market.getOffersByResource(resource).stream().map(MarketOfferDto::fromDomain).toList();
        log.info("Found " + allOffers.size() + " offers for " + resource);
        return allOffers;
    }

    @Transactional
    public ResponseEntity<Integer> buyOffer(UUID id, MarketBuyerDto buyerData)
    {
        var maybeOffer = market.findOfferById(id);
        var maybeBuyerKingdom = kingdomRepository.getKingdomByName(buyerData.buyer);
        if (maybeOffer.isEmpty() || maybeBuyerKingdom.isEmpty())
        {
            log.warn("Offer or buyer not found");
            return ResponseEntity.notFound().build();
        }

        var offer = maybeOffer.get();
        log.info("Transaction on " + offer + " with " + buyerData.toString());

        int boughtAmount = market.buyExistingOffer(maybeOffer.get(), maybeBuyerKingdom.get(), buyerData.count);

        // TODO report?
        return ResponseEntity.ok(boughtAmount);
    }
}
