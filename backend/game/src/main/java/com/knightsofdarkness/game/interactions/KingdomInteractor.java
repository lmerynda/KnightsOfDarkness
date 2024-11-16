package com.knightsofdarkness.game.interactions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knightsofdarkness.common.market.MarketResource;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.storage.IKingdomRepository;

public class KingdomInteractor implements IKingdomInteractor {
    private static final Logger log = LoggerFactory.getLogger(KingdomInteractor.class);
    IKingdomRepository kingdomRepository;

    public KingdomInteractor(IKingdomRepository kingdomRepository)
    {
        this.kingdomRepository = kingdomRepository;
    }

    public void transferResources(Kingdom from, String to, MarketResource resource, int amount)
    {
        var toKingdom = kingdomRepository.getKingdomByName(to);
        if (toKingdom.isEmpty())
        {
            log.warn("[KingdomInteractor] Kingdom with name {} not found", to);
            return;
        }

        toKingdom.get().receiveResourceTransfer(resource, amount);
        kingdomRepository.update(toKingdom.get());
    }
}
