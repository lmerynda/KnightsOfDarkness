package com.knightsofdarkness.web.kingdom;

import com.knightsofdarkness.web.common.market.MarketResource;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
import com.knightsofdarkness.web.kingdom.model.KingdomOngoingAttackEntity;

public interface IKingdomInteractor {
    void transferResources(KingdomEntity from, String to, MarketResource resource, int amount);

    void resolveAttack(KingdomEntity kingdom, KingdomOngoingAttackEntity attack);
}
