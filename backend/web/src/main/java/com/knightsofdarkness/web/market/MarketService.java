package com.knightsofdarkness.web.market;

import java.util.Optional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.knightsofdarkness.common.market.MarketOfferDto;
import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.market.IMarket;
import com.knightsofdarkness.game.market.MarketOfferBuyResult;
import com.knightsofdarkness.game.market.MarketResource;
import com.knightsofdarkness.storage.kingdom.KingdomRepository;
import com.knightsofdarkness.storage.market.MarketOfferReadRepository;

@Service
public class MarketService {
    private final Logger log = LoggerFactory.getLogger(MarketService.class);
    private final IMarket market;
    private final KingdomRepository kingdomRepository;
    private final MarketOfferReadRepository marketOfferReadRepository;
    private final GameConfig gameConfig;

    public MarketService(IMarket market, KingdomRepository kingdomRepository, MarketOfferReadRepository marketOfferReadRepository, GameConfig gameConfig)
    {
        this.market = market;
        this.kingdomRepository = kingdomRepository;
        this.marketOfferReadRepository = marketOfferReadRepository;
        this.gameConfig = gameConfig;
    }

    @Transactional
    public void createOffers(List<MarketOfferDto> offers)
    {
        for (var offer : offers)
        {
            var kingdom = kingdomRepository.getKingdomByName(offer.sellerName);
            if (kingdom.isEmpty())
            {
                log.warn("Kingdom with name {} not found", offer.sellerName);
                continue;
            }

            market.addOffer(kingdom.get(), offer.resource, offer.count, offer.price);
        }
    }

    @Transactional
    public Optional<MarketOfferDto> createOffer(MarketOfferDto offer)
    {
        log.info("Creating new offer {}", offer);
        var kingdom = kingdomRepository.getKingdomByName(offer.sellerName);
        if (kingdom.isEmpty())
        {
            log.warn("Kingdom with name {} not found", offer.sellerName);
            return Optional.empty();
        }

        var createdOffer = market.addOffer(kingdom.get(), offer.resource, offer.count, offer.price);
        return createdOffer.map(MarketOfferDto::fromDomain);
    }

    @Deprecated
    public List<MarketOfferDto> getAllOffers()
    {
        log.info("Getting all offers");
        var allOffers = new ArrayList<MarketOfferDto>();
        allOffers.addAll(marketOfferReadRepository.getOffersByResource(MarketResource.food));
        allOffers.addAll(marketOfferReadRepository.getOffersByResource(MarketResource.iron));
        allOffers.addAll(marketOfferReadRepository.getOffersByResource(MarketResource.tools));
        allOffers.addAll(marketOfferReadRepository.getOffersByResource(MarketResource.weapons));
        log.info("Found {} offers", allOffers.size());

        return allOffers;
    }

    public List<MarketOfferDto> getOffersByResource(MarketResource resource)
    {
        log.info("Getting last 7 offers for {}", resource);
        var allOffers = marketOfferReadRepository.findCheapestOffersByResource(resource, gameConfig.market().visibleMarketOffersCap());
        log.info("Found {} offers for {}", allOffers.size(), resource);
        return allOffers;
    }

    @Transactional
    public ResponseEntity<MarketOfferBuyResult> buyOffer(UUID id, int amount, String buyerName)
    {
        var maybeOffer = market.findOfferById(id);
        var maybeBuyerKingdom = kingdomRepository.getKingdomByName(buyerName);
        if (maybeOffer.isEmpty() || maybeBuyerKingdom.isEmpty())
        {
            log.warn("Offer or buyer not found");
            return ResponseEntity.notFound().build();
        }

        var buyer = maybeBuyerKingdom.get();
        var offer = maybeOffer.get();
        // a case when buyer and seller is the same kingdom is handled here
        // to avoid complications in persistence layer deserialization
        var seller = offer.getSeller().getName().equals(buyerName) ? buyer : offer.getSeller();

        log.info("Transaction on {} with {} and amount {}", offer, buyerName, amount);

        var result = market.buyExistingOffer(offer, seller, buyer, amount);

        // TODO report? what if the action failed?
        return ResponseEntity.ok(result);
    }

    @Transactional
    public ResponseEntity<Boolean> withdraw(UUID id, String kingdomName)
    {
        var maybeOffer = market.findOfferById(id);
        if (maybeOffer.isEmpty())
        {
            log.warn("Offer not found");
            return ResponseEntity.notFound().build();
        }

        var offer = maybeOffer.get();
        if (!offer.getSeller().getName().equals(kingdomName))
        {
            log.warn("Offer does not belong to kingdom {}", kingdomName);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("Withdrawing offer {}", offer);

        market.removeOffer(offer);
        return ResponseEntity.ok(true);
    }
}
