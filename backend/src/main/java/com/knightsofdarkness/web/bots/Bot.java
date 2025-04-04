package com.knightsofdarkness.web.bots;

import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.model.KingdomDetailsProvider;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;

public abstract class Bot implements IBot
{
    final KingdomEntity kingdom;
    final KingdomDetailsProvider kingdomDetailsProvider;
    final GameConfig gameConfig;
    final BotFunctions botFunctions;

    protected Bot(KingdomEntity kingdom, GameConfig gameConfig)
    {
        this.kingdom = kingdom;
        this.gameConfig = gameConfig;
        this.kingdomDetailsProvider = new KingdomDetailsProvider(gameConfig);
        this.botFunctions = new BotFunctions(this, gameConfig);
    }

    public KingdomEntity getKingdom()
    {
        return kingdom;
    }

    public GameConfig getConfig()
    {
        return gameConfig;
    }

    public KingdomDetailsProvider getKingdomDetailsProvider()
    {
        return kingdomDetailsProvider;
    }

    public boolean shouldPassTurn()
    {
        return kingdomDetailsProvider.hasMaxTurns(kingdom) || doesHaveEnoughUpkeep();
    }
}
