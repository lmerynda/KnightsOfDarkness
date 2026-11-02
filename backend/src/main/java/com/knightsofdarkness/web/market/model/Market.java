package com.knightsofdarkness.web.market.model;

import com.knightsofdarkness.web.common.market.MarketOfferDto;
import java.util.Optional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knightsofdarkness.web.common.market.MarketPurchaseDto;
import com.knightsofdarkness.web.common.ActionResult;
import com.knightsofdarkness.web.common.market.MarketResource;
import com.knightsofdarkness.web.common.messaging.NotificationKind;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.IKingdomRepository;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
import com.knightsofdarkness.web.kingdom.model.KingdomMarketAction;
import com.knightsofdarkness.web.market.IMarket;
import com.knightsofdarkness.web.messaging.INotificationSystem;
import com.knightsofdarkness.web.utils.Id;
import com.knightsofdarkness.web.utils.Utils;

public class Market implements IMarket {
    private static final Logger log = LoggerFactory.getLogger(Market.class);
    IMarketOfferRepository offersRepository;
    IKingdomRepository kingdomRepository;
    INotificationSystem notificationSystem;
    GameConfig gameConfig;

    public Market(IMarketOfferRepository repository, IKingdomRepository kingdomRepository, INotificationSystem notificationSystem, GameConfig gameConfig)
    {
        this.offersRepository = repository;
        this.kingdomRepository = kingdomRepository;
        this.notificationSystem = notificationSystem;
        this.gameConfig = gameConfig;
    }

    @Override
    public ActionResult<MarketOfferDto> createOffer(String kingdomName, MarketResource resource, int count, int price)
    {
        var maybeKingdom = kingdomRepository.getKingdomByName(kingdomName);
        if (maybeKingdom.isEmpty())
        {
            log.warn("Kingdom with name {} not found", kingdomName);
            return ActionResult.failure(Utils.format("Kingdom with name {} not found", kingdomName));
        }
        KingdomEntity kingdom = maybeKingdom.get();

        int offerCount = offersRepository.getOffersCountByKingdomNameAndResource(kingdom.getName(), resource);
        if (offerCount >= gameConfig.market().maxKingdomOffers())
        {
            log.info("Kingdom {} already has max {} offers for resource {}", kingdom.getName(), offerCount, resource);
            return ActionResult.failure(Utils.format("Your kingdom already has maximum number of offers for {}", resource));
        }

        if (price < gameConfig.market().minPrice())
        {
            log.info("Price {} is lower than minimum {}", price, gameConfig.market().minPrice());
            return ActionResult.failure(Utils.format("Price {} is lower than minimum {}", price, gameConfig.market().minPrice()));
        }
        var action = new KingdomMarketAction(kingdom);
        if (count <= 0)
        {
            return ActionResult.failure("Offer must contain at least one unit");
        }

        var countToOffer = action.postOffer(resource, count);
        if (countToOffer <= 0)
        {
            return ActionResult.failure(Utils.format("Not enough {} to post an offer", resource));
        }
        var offer = new MarketOfferEntity(Id.generate(), kingdom, resource, countToOffer, price);
        offersRepository.add(offer);
        kingdomRepository.update(kingdom);

        return ActionResult.success(Utils.format("Offer created for {} {} at price {}", countToOffer, resource, price), offer.toDto());
    }

    // TODO why do we need to removeOffer?
    @Override
    public ActionResult<Void> removeOffer(MarketOfferEntity offer)
    {
        var seller = offer.getSeller();
        var action = new KingdomMarketAction(seller);
        action.withdrawMarketOffer(offer);
        kingdomRepository.update(seller);
        offersRepository.remove(offer);
        return ActionResult.success("Offer removed succesfully");
    }

    @Override
    public ActionResult<Void> removeOffer(UUID offerId)
    {
        var maybeOffer = offersRepository.findById(offerId);
        if (maybeOffer.isEmpty())
        {
            log.warn("Offer with id {} not found", offerId);
            return ActionResult.failure(Utils.format("Requested offer not found"));
        }
        var offer = maybeOffer.get();
        var seller = offer.getSeller();
        var action = new KingdomMarketAction(seller);
        action.withdrawMarketOffer(offer);
        kingdomRepository.update(seller);
        offersRepository.remove(offer);

        return ActionResult.success("Offer removed succesfully");
    }

    @Override
    public Optional<MarketOfferEntity> findOfferById(UUID id)
    {
        return offersRepository.findById(id);
    }

    // TODO make a LOT of tests for this
    @Override
    public MarketPurchaseDto buyExistingOffer(MarketOfferEntity offer, KingdomEntity seller, KingdomEntity buyer, int count)
    {
        log.info("Transaction request: buyer: {} seller: {} count: {} offer: {}", buyer.getName(), seller.getName(), count, offer.getId());
        var maxToSell = Math.max(0, Math.min(offer.count, count));
        var action = new KingdomMarketAction(seller);
        var buyerGold = action.reserveGoldForOffer(buyer, offer.getPrice(), maxToSell);
        var buyerCount = buyerGold / offer.getPrice();

        if (buyerCount == 0)
        {
            log.info("Not enough gold to buy any count of resource");
            return new MarketPurchaseDto(offer.getResource(), buyerCount, offer.getPrice(), buyerGold);
        }
        offer.count -= buyerCount;
        action.acceptOffer(buyerGold);
        action.deliverResourcesFromOffer(buyer, offer.getResource(), buyerCount);

        assert (offer.count >= 0);

        var result = new MarketPurchaseDto(offer.getResource(), buyerCount, offer.getPrice(), buyerGold);

        kingdomRepository.update(seller);
        kingdomRepository.update(buyer);
        if (offer.count == 0)
        {
            // TODO this should be debug log
            log.info("Offer sold completely, removing {}, seller {}", offer.getId(), seller.getName());
            offersRepository.remove(offer);
        } else
        {
            offersRepository.update(offer);
        }

        var transaction = new MarketTransactionEntity(Id.generate(), offer.getResource(), seller.getName(), buyer.getName(), offer.getPrice(), buyerCount, Instant.now());
        offersRepository.registerMarketTransaction(transaction);

        var message = Utils.format("{} bought {} {} from us for {} gold", buyer.getName(), buyerCount, offer.getResource(), buyerGold);
        notificationSystem.create(seller.getName(), NotificationKind.trade, message);

        return result;
    }

    static int offerComparator(MarketOfferEntity offer1, MarketOfferEntity offer2)
    {
        return offer1.getPrice() - offer2.getPrice();
    }

    @Override
    public void updateMarketTransactionsAverages(Instant from, Instant to)
    {
        for (var resource : MarketResource.values())
        {
            var transactions = offersRepository.getTransactionsByResourceAndTimeRange(resource, from, to);
            log.info("Transactions for resource {} between: {} - {}: {}", resource, from, to, transactions.size());
            var volume = transactions.stream().mapToInt(t -> t.count).sum();
            if (volume > 0)
            {
                var weightedAveragePrice = transactions.stream().mapToInt(t -> t.price * t.count).sum() / volume;
                log.info("[Market Data Update] Resource: {} volume: {} weighted average price: {}", resource, volume, weightedAveragePrice);
                var averageSaleRecord = new MarketTransactionTimeRangeAveragesEntity(Id.generate(), resource, weightedAveragePrice, volume, from, to);
                offersRepository.addTransactionTimeRangeAverage(averageSaleRecord);
            } else
            {
                log.info("[Market Data Update] Resource: {} no transactions within specified range from {} to {}", resource, from, to);
            }
        }
    }

    public Optional<Double> getLast24TransactionAverages(MarketResource resource)
    {
        var transactions = offersRepository.getTransactionTimeRangeAverages(resource, 24);
        if (transactions.isEmpty())
        {
            log.info("[Market Data] No transactions for resource {} in last 24 hours", resource);
            return Optional.empty();
        }
        var weightedAverage = (double) transactions.stream().mapToInt(t -> t.averagePrice * t.volume).sum() / transactions.stream().mapToInt(t -> t.volume).sum();
        log.info("[Market Data] Last 24 hours weighted average for resource {} is: {}", resource, weightedAverage);
        return Optional.of(weightedAverage);
    }

    public List<MarketTransactionEntity> getTransactionsByResourceAndTimeRange(MarketResource resource, Instant from, Instant to)
    {
        return offersRepository.getTransactionsByResourceAndTimeRange(resource, from, to);
    }

    @Override
    public List<MarketOfferEntity> getOffersByResource(MarketResource resource)
    {
        return offersRepository.getOffersByResource(resource);
    }

    @Override
    public Optional<MarketOfferEntity> getCheapestOfferByResource(MarketResource resource)
    {
        return offersRepository.getCheapestOfferByResource(resource);
    }

    @Override
    public List<MarketOfferEntity> getOffersByKingdomName(String name)
    {
        return offersRepository.getOffersByKingdomName(name);
    }

}
