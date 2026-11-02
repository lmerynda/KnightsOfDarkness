package com.knightsofdarkness.web.market;

import java.util.Optional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.knightsofdarkness.web.common.market.MarketPurchaseDto;
import com.knightsofdarkness.web.common.ActionResult;
import com.knightsofdarkness.web.utils.Utils;
import com.knightsofdarkness.web.common.market.MarketOfferDto;
import com.knightsofdarkness.web.common.market.MarketResource;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
import com.knightsofdarkness.web.kingdom.model.KingdomRepository;
import com.knightsofdarkness.web.market.model.MarketOfferReadRepository;

@Service
public class MarketService
{
    private static final Logger log = LoggerFactory.getLogger(MarketService.class);
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
    public ActionResult<MarketOfferDto> createOffer(MarketOfferDto offer, String sellerName)
    {
        log.info("Creating new offer {}", offer);

        return market.createOffer(sellerName, offer.resource(), offer.count(), offer.price());
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

    /**
     * every offer the kingdom's allies keep on the market, whatever its price: the cheapest-offers list hides the dear ones
     * allies post to pass each other gold
     */
    @Transactional(readOnly = true)
    public List<MarketOfferDto> getAlliesOffers(String kingdomName)
    {
        var allies = kingdomRepository.getKingdomByName(kingdomName).flatMap(KingdomEntity::getAlliance).map(alliance -> alliance.getKingdoms()).orElse(List.of());
        return allies.stream().map(KingdomEntity::getName).filter(name -> !name.equals(kingdomName))
                .flatMap(name -> marketOfferReadRepository.findByKingdomName(name).stream())
                .sorted(Comparator.comparing(MarketOfferDto::sellerName).thenComparing(MarketOfferDto::resource)).toList();
    }

    @Transactional
    public ActionResult<MarketPurchaseDto> buyOffer(UUID id, int count, String buyerName)
    {
        var maybeOffer = market.findOfferById(id);
        var maybeBuyerKingdom = kingdomRepository.getKingdomByName(buyerName);
        if (maybeOffer.isEmpty() || maybeBuyerKingdom.isEmpty())
        {
            log.warn("Offer or buyer not found");
            return ActionResult.failure("That offer is gone from the market");
        }

        var buyer = maybeBuyerKingdom.get();
        var offer = maybeOffer.get();
        // TODO still needed?
        // a case when buyer and seller is the same kingdom is handled here
        // to avoid complications in persistence layer deserialization
        var seller = offer.getSeller().getName().equals(buyerName) ? buyer : offer.getSeller();

        log.info("Transaction on {} with {} and count {}", offer, buyerName, count);

        var result = market.buyExistingOffer(offer, seller, buyer, count);
        if (result.count() <= 0)
        {
            return ActionResult.failure("Not enough gold for any of it");
        }
        return ActionResult.success(Utils.format("Bought {} {} for {} gold", result.count(), result.resource(), result.totalCost()), result);
    }

    @Transactional
    public ActionResult<Void> withdraw(UUID id, String kingdomName)
    {
        var maybeOffer = market.findOfferById(id);
        if (maybeOffer.isEmpty())
        {
            log.warn("Offer not found");
            return ActionResult.failure("Offer not found");
        }

        var offer = maybeOffer.get();
        if (!offer.getSeller().getName().equals(kingdomName))
        {
            log.warn("Offer does not belong to kingdom {}", kingdomName);
            return ActionResult.failure("Offer does not belong to kingdom " + kingdomName);
        }
        log.info("Withdrawing offer {}", offer);

        market.removeOffer(offer);
        return ActionResult.success("Offer withdrawn successfully");
    }
}
