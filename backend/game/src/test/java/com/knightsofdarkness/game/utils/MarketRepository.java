package com.knightsofdarkness.game.utils;

import java.util.Optional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knightsofdarkness.game.market.MarketOffer;
import com.knightsofdarkness.game.market.MarketResource;
import com.knightsofdarkness.game.market.MarketTransaction;
import com.knightsofdarkness.game.market.MarketTransactionTimeRangeAverage;
import com.knightsofdarkness.game.storage.IMarketOfferRepository;

public class MarketRepository implements IMarketOfferRepository {
    private static final Logger log = LoggerFactory.getLogger(MarketRepository.class);
    List<MarketOffer> offers = new ArrayList<>();

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
        // TODO use kingdom repository to get kingdom by id and then get offers by kingdom
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
        log.info("should update offer with id: " + marketOffer.getId());
    }

    @Override
    public void registerMarketTransaction(MarketTransaction transaction)
    {
        // XXX Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'registerMarketTransaction'");
    }

    @Override
    public List<MarketTransaction> getTransactionsByResourceAndTimeRange(MarketResource resource, Instant hourAgo, Instant now)
    {
        // XXX Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTransactionsByResourceAndTimeRange'");
    }

    @Override
    public void addTransactionTimeRangeAverage(MarketTransactionTimeRangeAverage averageSaleRecord)
    {
        // XXX Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addTransactionTimeRangeAverage'");
    }
}
