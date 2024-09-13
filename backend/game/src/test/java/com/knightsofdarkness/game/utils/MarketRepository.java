package com.knightsofdarkness.game.utils;

import java.util.Optional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.knightsofdarkness.common.market.MarketResource;
import com.knightsofdarkness.game.market.MarketOffer;
import com.knightsofdarkness.game.market.MarketTransaction;
import com.knightsofdarkness.game.market.MarketTransactionTimeRangeAverage;
import com.knightsofdarkness.game.storage.IMarketOfferRepository;

public class MarketRepository implements IMarketOfferRepository {
    List<MarketOffer> offers = new ArrayList<>();
    List<MarketTransaction> transactions = new ArrayList<>();
    List<MarketTransactionTimeRangeAverage> transactionTimeRangeAverages = new ArrayList<>();

    @Override
    public MarketOffer add(MarketOffer marketOffer)
    {
        offers.add(marketOffer);
        return marketOffer;
    }

    @Override
    public void remove(MarketOffer marketOffer)
    {
        offers.remove(marketOffer);
    }

    @Override
    public List<MarketOffer> getOffersByResource(MarketResource resource)
    {
        return offers.stream().filter(offer -> offer.getResource().equals(resource)).toList();
    }

    @Override
    public Optional<MarketOffer> getCheapestOfferByResource(MarketResource resource)
    {
        return offers.stream()
                .filter(offer -> offer.getResource().equals(resource))
                .sorted((left, right) -> Integer.compare(left.getPrice(), right.getPrice()))
                .findFirst();
    }

    public List<MarketOffer> getOffersByKingdomName(String name)
    {
        return offers.stream().filter(offer -> offer.getSeller().getName().equals(name)).toList();
    }

    @Override
    public Optional<MarketOffer> findById(UUID marketOfferId)
    {
        return offers.stream().filter(offer -> offer.getId() == marketOfferId).findFirst();
    }

    @Override
    public void update(MarketOffer marketOffer)
    {
        var existingOffer = offers.stream().filter(offer -> offer.getId() == marketOffer.getId()).findFirst();
        if(existingOffer.isPresent())
        {
            offers.remove(existingOffer.get());
            offers.add(marketOffer);
        }
    }

    @Override
    public void registerMarketTransaction(MarketTransaction transaction)
    {
        transactions.add(transaction);
    }

    @Override
    public List<MarketTransaction> getTransactionsByResourceAndTimeRange(MarketResource resource, Instant hourAgo, Instant now)
    {
        return transactions.stream()
                .filter(transaction -> transaction.getResource().equals(resource))
                .filter(transaction -> transaction.getDate().isAfter(hourAgo))
                .filter(transaction -> transaction.getDate().isBefore(now))
                .sorted((left, right) -> right.getDate().compareTo(left.getDate()))
                .toList();
    }

    @Override
    public void addTransactionTimeRangeAverage(MarketTransactionTimeRangeAverage averageTransactionRecord)
    {
        transactionTimeRangeAverages.add(averageTransactionRecord);
    }

    @Override
    public List<MarketTransactionTimeRangeAverage> getTransactionTimeRangeAverages(MarketResource resource, int limit)
    {
        return transactionTimeRangeAverages.stream()
                .filter(average -> average.getResource().equals(resource))
                .limit(limit)
                .toList();
    }

    @Override
    public int getOffersCountByKingdomNameAndResource(String kingdomName, MarketResource resource)
    {
        return (int) offers.stream()
                .filter(offer -> offer.getSeller().getName().equals(kingdomName))
                .filter(offer -> offer.getResource().equals(resource))
                .count();
    }
}
