package com.knightsofdarkness.game.market;

import java.util.Optional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knightsofdarkness.game.Id;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.storage.IKingdomRepository;
import com.knightsofdarkness.game.storage.IMarketOfferRepository;

public class Market implements IMarket {
    private static final Logger log = LoggerFactory.getLogger(Market.class);
    IMarketOfferRepository offersRepository;
    IKingdomRepository kingdomRepository;

    public Market(IMarketOfferRepository repository, IKingdomRepository kingdomRepository)
    {
        this.offersRepository = repository;
        this.kingdomRepository = kingdomRepository;
    }

    @Override
    public MarketOffer addOffer(Kingdom kingdom, MarketResource resource, int count, int price)
    {
        var countToOffer = kingdom.postMarketOffer(resource, count);
        var offer = new MarketOffer(Id.generate(), kingdom, resource, countToOffer, price);
        offersRepository.add(offer);
        kingdomRepository.update(kingdom);

        return offer;
    }

    @Override
    public void removeOffer(MarketOffer offer)
    {
        var seller = offer.seller;
        seller.withdrawMarketOffer(offer);
        kingdomRepository.update(seller);
        offersRepository.remove(offer);
    }

    @Override
    public List<MarketOffer> getOffersByResource(MarketResource resource)
    {
        return offersRepository.getOffersByResource(resource);
    }

    @Override
    public Optional<MarketOffer> getCheapestOfferByResource(MarketResource resource)
    {
        return offersRepository.getCheapestOfferByResource(resource);
    }

    @Override
    public List<MarketOffer> getOffersByKingdomName(String name)
    {
        return offersRepository.getOffersByKingdomName(name);
    }

    @Override
    public Optional<MarketOffer> findOfferById(UUID id)
    {
        return offersRepository.findById(id);
    }

    // TODO make a LOT of tests for this
    @Override
    public MarketOfferBuyResult buyExistingOffer(MarketOffer offer, Kingdom seller, Kingdom buyer, int amount)
    {
        log.info("Transaction request: buyer: {} seller: {} amount: {} offer: {}", buyer.getName(), seller.getName(), amount, offer);
        var maxToSell = Math.min(offer.count, amount);
        var buyerGold = buyer.reserveGoldForOffer(offer.price, maxToSell);
        var buyerAmount = buyerGold / offer.price;

        if (buyerAmount == 0)
        {
            log.info("Not enough gold to buy any amount of resource");
            return new MarketOfferBuyResult(offer.resource, buyerAmount, offer.price, buyerGold);
        }
        offer.count -= buyerAmount;
        seller.acceptMarketOffer(buyerGold);
        buyer.deliverResourcesFromOffer(offer.resource, buyerAmount);

        assert (offer.count >= 0);

        var result = new MarketOfferBuyResult(offer.resource, buyerAmount, offer.price, buyerGold);

        kingdomRepository.update(seller);
        kingdomRepository.update(buyer);
        if (offer.count == 0)
        {
            // TODO this should be debug log
            log.info("Offer sold completely, removing " + offer);
            offersRepository.remove(offer);
        } else
        {
            offersRepository.update(offer);
        }

        var transaction = new MarketTransaction(Id.generate(), offer.resource, seller.getName(), buyer.getName(), offer.price, buyerAmount, Instant.now());
        offersRepository.registerMarketTransaction(transaction);

        return result;
    }

    static int offerComparator(MarketOffer offer1, MarketOffer offer2)
    {
        return offer1.price - offer2.price;
    }

    @Override
    public void updateMarketTransactionsAverages(Instant from, Instant to)
    {
        for (var resource : MarketResource.values())
        {
            var transactions = offersRepository.getTransactionsByResourceAndTimeRange(resource, from, to);
            log.info("Transactions for resource {} between: {} - {}: ", resource, from, to, transactions.size());
            var volume = transactions.stream().mapToInt(t -> t.count).sum();
            if (volume > 0)
            {
                var weightedAveragePrice = transactions.stream().mapToInt(t -> t.price * t.count).sum() / volume;
                log.info("[Market Data Update] Resource: {} volume: {} weighted average price: {}", resource, volume, weightedAveragePrice);
                var averageSaleRecord = new MarketTransactionTimeRangeAverage(Id.generate(), resource, weightedAveragePrice, volume, from, to);
                offersRepository.addTransactionTimeRangeAverage(averageSaleRecord);
            } else
            {
                log.info("[Market Data Update] Resource: {} no transactions within specified range from {} to {}", resource, from, to);
            }
        }
    }

    public double getLast24TransactionAverages(MarketResource resource)
    {
        var transactions = offersRepository.getTransactionTimeRangeAverages(resource, 24);
        if (transactions.isEmpty())
        {
            log.info("[Market Data] No transactions for resource {} in last 24 hours", resource);
            return 0;
        }
        var weightedAverage = (double) transactions.stream().mapToInt(t -> t.averagePrice * t.volume).sum() / transactions.stream().mapToInt(t -> t.volume).sum();
        log.info("[Market Data] Last 24 hours weighted average for resource {} is: {}", resource, weightedAverage);
        return weightedAverage;
    }
}
