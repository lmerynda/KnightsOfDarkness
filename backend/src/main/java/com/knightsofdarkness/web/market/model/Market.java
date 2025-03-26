package com.knightsofdarkness.web.market.model;

import java.util.Optional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knightsofdarkness.web.common.market.BuyMarketOfferResult;
import com.knightsofdarkness.web.common.market.CreateMarketOfferResult;
import com.knightsofdarkness.web.common.market.MarketResource;
import com.knightsofdarkness.web.common.market.RemoveMarketOfferResult;
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
    public CreateMarketOfferResult createOffer(String kingdomName, MarketResource resource, int count, int price)
    {
        var maybeKingdom = kingdomRepository.getKingdomByName(kingdomName);
        if (maybeKingdom.isEmpty())
        {
            log.warn("Kingdom with name {} not found", kingdomName);
            return CreateMarketOfferResult.failure(Utils.format("Kingdom with name {} not found", kingdomName), Optional.empty());
        }
        KingdomEntity kingdom = maybeKingdom.get();

        int offerCount = offersRepository.getOffersCountByKingdomNameAndResource(kingdom.getName(), resource);
        if (offerCount >= gameConfig.market().maxKingdomOffers())
        {
            log.info("Kingdom {} already has max {} offers for resource {}", kingdom.getName(), offerCount, resource);
            return CreateMarketOfferResult.failure(Utils.format("Your kingdom already has maximum number of offers for {}", resource), Optional.empty());
        }

        if (price < gameConfig.market().minPrice())
        {
            log.info("Price {} is lower than minimum {}", price, gameConfig.market().minPrice());
            return CreateMarketOfferResult.failure(Utils.format("Price {} is lower than minimum {}", price, gameConfig.market().minPrice()), Optional.empty());
        }
        var action = new KingdomMarketAction(kingdom);
        var countToOffer = action.postOffer(resource, count); // TODO, what if the kingdom doesn't have enough resources?
        var offer = new MarketOfferEntity(Id.generate(), kingdom, resource, countToOffer, price);
        offersRepository.add(offer);
        kingdomRepository.update(kingdom);

        return CreateMarketOfferResult.success(Utils.format("Offer created for {} {} at price {}", countToOffer, resource, price), offer.toDto());
    }

    // TODO why do we need to removeOffer?
    @Override
    public RemoveMarketOfferResult removeOffer(MarketOfferEntity offer)
    {
        var seller = offer.getSeller();
        var action = new KingdomMarketAction(seller);
        action.withdrawMarketOffer(offer);
        kingdomRepository.update(seller);
        offersRepository.remove(offer);
        return RemoveMarketOfferResult.success("Offer removed succesfully");
    }

    @Override
    public RemoveMarketOfferResult removeOffer(UUID offerId)
    {
        var maybeOffer = offersRepository.findById(offerId);
        if (maybeOffer.isEmpty())
        {
            log.warn("Offer with id {} not found", offerId);
            return RemoveMarketOfferResult.failure(Utils.format("Requested offer not found"));
        }
        var offer = maybeOffer.get();
        var seller = offer.getSeller();
        var action = new KingdomMarketAction(seller);
        action.withdrawMarketOffer(offer);
        kingdomRepository.update(seller);
        offersRepository.remove(offer);

        return RemoveMarketOfferResult.success("Offer removed succesfully");
    }

    @Override
    public Optional<MarketOfferEntity> findOfferById(UUID id)
    {
        return offersRepository.findById(id);
    }

    // TODO make a LOT of tests for this
    @Override
    public BuyMarketOfferResult buyExistingOffer(MarketOfferEntity offer, KingdomEntity seller, KingdomEntity buyer, int amount)
    {
        log.info("Transaction request: buyer: {} seller: {} amount: {} offer: {}", buyer.getName(), seller.getName(), amount, offer);
        var maxToSell = Math.min(offer.count, amount);
        var action = new KingdomMarketAction(seller);
        var buyerGold = action.reserveGoldForOffer(offer.getPrice(), maxToSell);
        var buyerAmount = buyerGold / offer.getPrice();

        if (buyerAmount == 0)
        {
            log.info("Not enough gold to buy any amount of resource");
            return new BuyMarketOfferResult(offer.getResource(), buyerAmount, offer.getPrice(), buyerGold);
        }
        offer.count -= buyerAmount;
        action.acceptOffer(seller, buyerGold);
        action.deliverResourcesFromOffer(offer.getResource(), buyerAmount);

        assert (offer.count >= 0);

        var result = new BuyMarketOfferResult(offer.getResource(), buyerAmount, offer.getPrice(), buyerGold);

        kingdomRepository.update(seller);
        kingdomRepository.update(buyer);
        if (offer.count == 0)
        {
            // TODO this should be debug log
            log.info("Offer sold completely, removing {}", offer);
            offersRepository.remove(offer);
        } else
        {
            offersRepository.update(offer);
        }

        var transaction = new MarketTransactionEntity(Id.generate(), offer.getResource(), seller.getName(), buyer.getName(), offer.getPrice(), buyerAmount, Instant.now());
        offersRepository.registerMarketTransaction(transaction);

        var message = Utils.format("Transaction: {} bought {} {} for {} gold", buyer.getName(), buyerAmount, offer.getResource(), buyerGold);
        notificationSystem.create(seller.getName(), message);

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
