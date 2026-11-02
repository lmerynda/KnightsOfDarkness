package com.knightsofdarkness.web.kingdom;

import com.knightsofdarkness.web.common.market.MarketResource;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
import com.knightsofdarkness.web.kingdom.model.KingdomOngoingAttackEntity;
import com.knightsofdarkness.web.kingdom.model.KingdomReinforcementEntity;

public interface IKingdomInteractor {
    void transferResources(KingdomEntity from, String to, MarketResource resource, int count);

    void resolveAttack(KingdomEntity kingdom, KingdomOngoingAttackEntity attack);

    /**
     * an army that joined an ally's strike holds at the border while that army is still on the march
     */
    boolean waitsAtBorder(KingdomOngoingAttackEntity attack);

    void reinforcementsArrived(KingdomReinforcementEntity reinforcement);
}
