package com.knightsofdarkness.game;

import com.knightsofdarkness.game.gameconfig.Initializer;
import com.knightsofdarkness.game.utils.KingdomRepository;
import com.knightsofdarkness.game.utils.MarketBuilder;

public class TestGame {
    public Game get()
    {
        var config = Initializer.readGameConfig();
        var kingdomRepository = new KingdomRepository();
        var market = new MarketBuilder(config).withKingdomRepository(kingdomRepository).build();

        return new Game(config, market, kingdomRepository);
    }
}
