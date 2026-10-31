package com.knightsofdarkness.web.kingdom;

import java.util.List;
import java.util.Optional;

import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
import com.knightsofdarkness.web.kingdom.model.KingdomOngoingAttackEntity;

public interface IKingdomRepository {
    Optional<KingdomEntity> getKingdomByName(String name);

    KingdomEntity add(KingdomEntity kingdom);

    KingdomEntity update(KingdomEntity kingdom);

    /**
     * armies on the march against the kingdom
     */
    List<KingdomOngoingAttackEntity> getAttacksOn(String kingdomName);
}
