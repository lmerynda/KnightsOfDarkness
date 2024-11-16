package com.knightsofdarkness.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import com.knightsofdarkness.game.interactions.IKingdomInteractor;
import com.knightsofdarkness.game.interactions.KingdomInteractor;
import com.knightsofdarkness.game.storage.IKingdomRepository;

@Service
@Configuration
public class KingdomInteractorService {
    private final IKingdomRepository kingdomRepository;

    public KingdomInteractorService(IKingdomRepository kingdomRepository)
    {
        this.kingdomRepository = kingdomRepository;
    }

    @Bean
    public IKingdomInteractor kingdomInteractor()
    {
        return new KingdomInteractor(kingdomRepository);
    }
}
