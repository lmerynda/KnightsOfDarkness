package com.knightsofdarkness.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import com.knightsofdarkness.game.market.IMarket;
import com.knightsofdarkness.game.market.Market;
import com.knightsofdarkness.game.storage.IKingdomRepository;
import com.knightsofdarkness.game.storage.IMarketOfferRepository;

@Service
@Configuration
public class GameMarketService {
    private final IMarketOfferRepository marketOfferRepository;
    private final IKingdomRepository kingdomRepository;

    public GameMarketService(IMarketOfferRepository marketOfferRepository, IKingdomRepository kingdomRepository)
    {
        this.marketOfferRepository = marketOfferRepository;
        this.kingdomRepository = kingdomRepository;
    }

    @Bean
    public IMarket market()
    {
        return new Market(marketOfferRepository, kingdomRepository);
    }
}
