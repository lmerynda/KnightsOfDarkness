package com.knightsofdarkness.web.utils;

import java.util.Optional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.knightsofdarkness.common.market.MarketResource;
import com.knightsofdarkness.web.market.model.IMarketOfferRepository;
import com.knightsofdarkness.web.market.model.MarketOfferEntity;
import com.knightsofdarkness.web.market.model.MarketTransactionEntity;
import com.knightsofdarkness.web.market.model.MarketTransactionTimeRangeAveragesEntity;

public class MarketRepository implements IMarketOfferRepository {
    List<MarketOfferEntity> offers = new ArrayList<>();
    List<MarketTransactionEntity> transactions = new ArrayList<>();
    List<MarketTransactionTimeRangeAveragesEntity> transactionTimeRangeAverages = new ArrayList<>();

    @Override
    public MarketOfferEntity add(MarketOfferEntity marketOffer)
    {
        offers.add(marketOffer);
        return marketOffer;
    }

    @Override
    public void remove(MarketOfferEntity marketOffer)
    {
        offers.remove(marketOffer);
    }

    @Override
    public List<MarketOfferEntity> getOffersByResource(MarketResource resource)
    {
        return offers.stream().filter(offer -> offer.getResource().equals(resource)).toList();
    }

    @Override
    public Optional<MarketOfferEntity> getCheapestOfferByResource(MarketResource resource)
    {
        return offers.stream()
                .filter(offer -> offer.getResource().equals(resource))
                .sorted((left, right) -> Integer.compare(left.getPrice(), right.getPrice()))
                .findFirst();
    }

    public List<MarketOfferEntity> getOffersByKingdomName(String name)
    {
        return offers.stream().filter(offer -> offer.getSeller().getName().equals(name)).toList();
    }

    @Override
    public Optional<MarketOfferEntity> findById(UUID marketOfferId)
    {
        return offers.stream().filter(offer -> offer. == marketOfferId).findFirst();
    }

    @Override
    public void update(MarketOfferEntity marketOffer)
    {
        var existingOffer = offers.stream().filter(offer -> offer.getId() == marketOffer.getId()).findFirst();
        if (existingOffer.isPresent())
        {
            offers.remove(existingOffer.get());
            offers.add(marketOffer);
        }
    }

    @Override
    public void registerMarketTransaction(MarketTransactionEntity transaction)
    {
        transactions.add(transaction);
    }

    @Override
    public List<MarketTransactionEntity> getTransactionsByResourceAndTimeRange(MarketResource resource, Instant hourAgo, Instant now)
    {
        return transactions.stream()
                .filter(transaction -> transaction.getResource().equals(resource))
                .filter(transaction -> transaction.getDate().isAfter(hourAgo))
                .filter(transaction -> transaction.getDate().isBefore(now))
                .sorted((left, right) -> right.getDate().compareTo(left.getDate()))
                .toList();
    }

    @Override
    public void addTransactionTimeRangeAverage(MarketTransactionTimeRangeAveragesEntity averageTransactionRecord)
    {
        transactionTimeRangeAverages.add(averageTransactionRecord);
    }

    @Override
    public List<MarketTransactionTimeRangeAveragesEntity> getTransactionTimeRangeAverages(MarketResource resource, int limit)
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
