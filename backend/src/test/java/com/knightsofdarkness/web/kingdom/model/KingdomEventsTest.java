package com.knightsofdarkness.web.kingdom.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.web.Game;
import com.knightsofdarkness.web.common.kingdom.KingdomTurnReport;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.kingdom.UnitName;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.legacy.TestGame;
import com.knightsofdarkness.web.utils.KingdomBuilder;

class KingdomEventsTest
{
    private GameConfig config;
    private KingdomEntity kingdom;
    private KingdomTurnReport report;
    private KingdomEvents events;

    @BeforeEach
    void setUp()
    {
        Game game = new TestGame().get();
        config = game.getConfig();
        kingdom = new KingdomBuilder(game).withResource(ResourceName.land, 200).withResource(ResourceName.gold, 100000).withResource(ResourceName.food, 5000).withUnit(UnitName.guard, 0)
                .build();
        report = new KingdomTurnReport();
        report.resourcesProduced.put(ResourceName.food, 1000);
        report.resourcesProduced.put(ResourceName.gold, 20000);
        events = new KingdomEvents(config);
    }

    @Test
    void aRollAboveTheChance_shouldBringNothing()
    {
        var event = events.happen(kingdom, report, config.events().chancePerTurn(), KingdomEvent.harvest);

        assertTrue(event.isEmpty());
        assertEquals(5000, kingdom.getResources().getCount(ResourceName.food));
    }

    @Test
    void harvest_shouldBringInPartOfTheTurnsFoodAgain()
    {
        var event = events.happen(kingdom, report, 0, KingdomEvent.harvest);

        var extra = (int) (1000 * config.events().windfallShare());
        assertEquals(5000 + extra, kingdom.getResources().getCount(ResourceName.food));
        assertThat(event).hasValueSatisfying(text -> assertThat(text).contains(String.valueOf(extra)));
        assertEquals(event.get(), report.event);
    }

    @Test
    void bandits_shouldRobAnUnguardedRealm()
    {
        var event = events.happen(kingdom, report, 0, KingdomEvent.bandits);

        var stolen = (int) (100000 * config.events().lossShare());
        assertEquals(100000 - stolen, kingdom.getResources().getCount(ResourceName.gold));
        assertThat(event).hasValueSatisfying(text -> assertThat(text).contains("robbed"));
    }

    @Test
    void bandits_shouldBeDrivenOffByEnoughGuards()
    {
        kingdom.getUnits().setCount(UnitName.guard, (int) Math.ceil(200 * config.events().guardsPerAcreAgainstBandits()));

        var event = events.happen(kingdom, report, 0, KingdomEvent.bandits);

        assertEquals(100000, kingdom.getResources().getCount(ResourceName.gold));
        assertThat(event).hasValueSatisfying(text -> assertThat(text).contains("drove them off"));
    }

    @Test
    void fire_shouldBurnPartOfTheTools()
    {
        kingdom.getResources().setCount(ResourceName.tools, 1000);

        events.happen(kingdom, report, 0, KingdomEvent.fire);

        assertEquals(1000 - (int) (1000 * config.events().lossShare()), kingdom.getResources().getCount(ResourceName.tools));
    }
}
