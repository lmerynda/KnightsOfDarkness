package com.knightsofdarkness.web.bots;

import com.knightsofdarkness.web.kingdom.model.KingdomDetailsProvider;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;

public interface IBot {
    boolean doUpkeepActions();

    boolean doAllActions();

    boolean doActionCycle();

    void passTurn();

    String getKingdomInfo();

    KingdomEntity getKingdom();

    KingdomDetailsProvider getKingdomDetailsProvider();

    boolean doesHaveEnoughUpkeep();
}
