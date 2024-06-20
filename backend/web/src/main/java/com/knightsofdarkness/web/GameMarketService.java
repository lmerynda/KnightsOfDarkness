package com.knightsofdarkness.web;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.knightsofdarkness.game.market.IMarket;
import com.knightsofdarkness.game.market.Market;
import com.knightsofdarkness.game.storage.IMarketOfferRepository;

@Service
public class GameMarketService {
    private final IMarketOfferRepository marketOfferRepository;

    public GameMarketService(IMarketOfferRepository marketOfferRepository)
    {
        this.marketOfferRepository = marketOfferRepository;
    }

    @Bean
    public IMarket market()
    {
        Market market = new Market(marketOfferRepository);
        System.out.println("Market created");
        return market;
    }
}
