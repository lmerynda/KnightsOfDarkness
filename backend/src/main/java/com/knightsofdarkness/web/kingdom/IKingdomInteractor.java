package com.knightsofdarkness.web.kingdom;

import com.knightsofdarkness.web.common.market.MarketResource;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
import com.knightsofdarkness.web.kingdom.model.KingdomOngoingAttackEntity;
import com.knightsofdarkness.web.kingdom.model.KingdomReinforcementEntity;

public interface IKingdomInteractor {
    void transferResources(KingdomEntity from, String to, MarketResource resource, int count);

    void resolveAttack(KingdomEntity kingdom, KingdomOngoingAttackEntity attack);

    /**
     * an army that joined an ally's strike holds at the border while that army is still on the march, and the army that set
     * off a strike holds there while armies that joined it are still on the march, each for a few of its owner's turns at
     * most, so that armies sent together land together however fast their rulers pass turns
     */
    boolean waitsAtBorder(KingdomOngoingAttackEntity attack);

    void reinforcementsArrived(KingdomReinforcementEntity reinforcement);
}
