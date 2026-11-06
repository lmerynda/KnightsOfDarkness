package com.knightsofdarkness.web.bots;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.knightsofdarkness.web.Game;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.market.MarketResource;
import com.knightsofdarkness.web.legacy.TestGame;
import com.knightsofdarkness.web.market.model.MarketOfferEntity;
import com.knightsofdarkness.web.utils.KingdomBuilder;

class BotPricesTest {
    private final Game game = new TestGame().get();

    private void assertAsksTheConfiguredPrice(String name, MarketResource resource, java.util.function.Function<com.knightsofdarkness.web.kingdom.model.KingdomEntity, IBot> bot)
    {
        var kingdom = new KingdomBuilder(game).withName(name).withResource(ResourceName.food, 100_000).withResource(ResourceName.iron, 10_000)
                .withResource(ResourceName.tools, 10_000).build();
        game.addKingdom(kingdom);

        bot.apply(kingdom).doAllActions();

        var offers = game.getMarket().getOffersByKingdomName(name).stream().filter(offer -> offer.getResource() == resource).toList();
        assertThat(offers).isNotEmpty();
        assertThat(offers).extracting(MarketOfferEntity::getPrice).containsOnly(game.getConfig().bots().getPrice(resource));
    }

    @Test
    void merchants_askThePricesTheGameConfigSets()
    {
        var market = game.getMarket();
        var interactor = game.getKingdomInteractor();
        var config = game.getConfig();
        assertAsksTheConfiguredPrice("FarmerBot", MarketResource.food, kingdom -> new FarmerBot(kingdom, market, interactor, config));
        assertAsksTheConfiguredPrice("IronMinerBot", MarketResource.iron, kingdom -> new IronMinerBot(kingdom, market, interactor, config));
        assertAsksTheConfiguredPrice("BlacksmithBot", MarketResource.tools, kingdom -> new BlacksmithBot(kingdom, market, interactor, config));
    }
}
