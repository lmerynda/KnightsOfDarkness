package com.knightsofdarkness.web.bots.legacy;

import com.knightsofdarkness.web.kingdom.legacy.Kingdom;

public interface IBot {
    boolean doUpkeepActions();

    boolean doAllActions();

    boolean doActionCycle();

    void passTurn();

    String getKingdomInfo();

    Kingdom getKingdom();

    boolean doesHaveEnoughUpkeep();
}
