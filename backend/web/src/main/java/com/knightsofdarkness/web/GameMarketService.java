package com.knightsofdarkness.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.knightsofdarkness.game.market.IMarket;
import com.knightsofdarkness.game.market.Market;
import com.knightsofdarkness.game.storage.IMarketOfferRepository;

@Service
public class GameMarketService {
    @Autowired
    private IMarketOfferRepository marketOfferRepository;

    @Bean
    public IMarket market()
    {
        Market market = new Market(marketOfferRepository);
        System.err.println("Market created");
        return market;
    }
}
