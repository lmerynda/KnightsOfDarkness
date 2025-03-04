package com.knightsofdarkness.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import com.knightsofdarkness.web.kingdom.IKingdomInteractor;
import com.knightsofdarkness.web.kingdom.IKingdomRepository;
import com.knightsofdarkness.web.kingdom.model.KingdomInteractor;

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
