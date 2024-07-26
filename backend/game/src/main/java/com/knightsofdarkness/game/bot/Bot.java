package com.knightsofdarkness.game.bot;

import com.knightsofdarkness.game.kingdom.Kingdom;

public interface Bot {
	boolean doAllActions();
    boolean doActionCycle();

	void passTurn();

	String getKingdomInfo();

	Kingdom getKingdom();
}