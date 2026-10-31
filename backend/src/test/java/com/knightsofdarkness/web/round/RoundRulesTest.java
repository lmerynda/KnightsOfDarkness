package com.knightsofdarkness.web.round;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.web.Game;
import com.knightsofdarkness.web.alliance.model.AllianceEntity;
import com.knightsofdarkness.web.common.kingdom.BuildingName;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.kingdom.SpecialBuildingType;
import com.knightsofdarkness.web.common.kingdom.UnitName;
import com.knightsofdarkness.web.kingdom.model.KingdomSpecialBuildingAction;
import com.knightsofdarkness.web.legacy.TestGame;
import com.knightsofdarkness.web.utils.KingdomBuilder;

class RoundRulesTest {
    private Game game;

    @BeforeEach
    void setUp()
    {
        game = new TestGame().get();
    }

    @Test
    void rankings_shouldOrderKingdomsByLandAndSumAlliances()
    {
        var small = new KingdomBuilder(game).withName("Small").withLand(120).build();
        var big = new KingdomBuilder(game).withName("Big").withLand(300).build();
        var middle = new KingdomBuilder(game).withName("Middle").withLand(200).build();
        var alliance = new AllianceEntity("Pact", "Small");
        alliance.addKingdom(small);
        alliance.addKingdom(middle);

        var rankings = RankingCalculator.calculate(List.of(small, big, middle), Set.of());

        assertEquals(List.of("Big", "Middle", "Small"), rankings.kingdoms().stream().map(rank -> rank.kingdomName()).toList());
        assertEquals(1, rankings.kingdoms().get(0).rank());
        assertNull(rankings.kingdoms().get(0).allianceName());
        assertEquals("Pact", rankings.kingdoms().get(1).allianceName());
        assertEquals(1, rankings.alliances().size());
        assertEquals(320, rankings.alliances().get(0).land());
        assertEquals(2, rankings.alliances().get(0).members());
    }

    @Test
    void reset_shouldReturnKingdomToStartingConfiguration()
    {
        var config = game.getConfig();
        var kingdom = new KingdomBuilder(game).withRichConfiguration().build();
        new KingdomSpecialBuildingAction(kingdom, config).startSpecialBuilding(SpecialBuildingType.forge);

        RoundReset.reset(List.of(kingdom), Set.of(), config);

        var start = config.kingdomStartConfiguration();
        assertEquals(start.resources().toMap().get(ResourceName.land), kingdom.getResources().getCount(ResourceName.land));
        assertEquals(start.resources().toMap().get(ResourceName.gold), kingdom.getResources().getCount(ResourceName.gold));
        assertEquals(start.buildings().toMap().get(BuildingName.house), kingdom.getBuildings().getCount(BuildingName.house));
        assertEquals(start.units().toMap().get(UnitName.cavalry), kingdom.getUnits().getAvailableCount(UnitName.cavalry));
        assertTrue(kingdom.getSpecialBuildings().isEmpty());
    }

    @Test
    void reset_shouldLetBotsKeepTheirSpecialBuildingTypes()
    {
        var config = game.getConfig();
        var bot = new KingdomBuilder(game).withName("FarmerBot").build();
        var action = new KingdomSpecialBuildingAction(bot, config);
        action.startSpecialBuilding(SpecialBuildingType.granary);
        action.startSpecialBuilding(SpecialBuildingType.granary);

        RoundReset.reset(List.of(bot), Set.of("FarmerBot"), config);

        assertEquals(2, bot.getSpecialBuildings().size());
        assertTrue(bot.getSpecialBuildings().stream().allMatch(building -> building.getBuildingType() == SpecialBuildingType.granary && building.toDto().level() == 0));
    }

    @Test
    void rankings_shouldListBotsUnrankedAfterPlayersAndLeaveThemOutOfAllianceLand()
    {
        var player = new KingdomBuilder(game).withName("Player").withLand(150).build();
        var bot = new KingdomBuilder(game).withName("FarmerBot").withLand(5000).build();
        var alliance = new AllianceEntity("Pact", "Player");
        alliance.addKingdom(player);
        alliance.addKingdom(bot);

        var rankings = RankingCalculator.calculate(List.of(player, bot), Set.of("FarmerBot"));

        assertEquals("Player", rankings.kingdoms().get(0).kingdomName());
        assertEquals(1, rankings.kingdoms().get(0).rank());
        assertEquals("FarmerBot", rankings.kingdoms().get(1).kingdomName());
        assertEquals(0, rankings.kingdoms().get(1).rank());
        assertTrue(rankings.kingdoms().get(1).bot());
        assertEquals(150, rankings.alliances().get(0).land());
        assertEquals(2, rankings.alliances().get(0).members());
    }
}
