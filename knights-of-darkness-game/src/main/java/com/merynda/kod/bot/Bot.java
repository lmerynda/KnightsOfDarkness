package com.merynda.kod.bot;

import com.merynda.kod.kingdom.Kingdom;

public interface Bot {
	boolean doAllActions();

	void passTurn();

	String getKingdomInfo();

	Kingdom getKingdom();
}