package com.knightsofdarkness.game.bot;

import com.knightsofdarkness.game.kingdom.Kingdom;

public interface Bot {
	boolean doAllActions();

	void passTurn();

	String getKingdomInfo();

	Kingdom getKingdom();
}