package com.uprzejmy.kod.bot;

import com.uprzejmy.kod.kingdom.Kingdom;

public interface Bot
{
	boolean doAllActions();

	void passTurn();

	String getKingdomInfo();

	Kingdom getKingdom();
}