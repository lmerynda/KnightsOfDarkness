package com.knightsofdarkness.web.era;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import com.knightsofdarkness.web.GameServerTest;
import com.knightsofdarkness.web.common.alliance.FoundMonumentDto;
import com.knightsofdarkness.web.common.alliance.MonumentContributionDto;
import com.knightsofdarkness.web.common.alliance.MonumentDto;
import com.knightsofdarkness.web.common.messaging.NotificationDto;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.market.MarketResource;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.model.KingdomRepository;
import com.knightsofdarkness.web.market.MarketService;
import com.knightsofdarkness.web.messaging.INotificationSystem;
import com.knightsofdarkness.web.monument.MonumentService;

@GameServerTest
class EraServiceTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EraService eraService;

    @Autowired
    private KingdomRepository kingdomRepository;

    @Autowired
    private MarketService marketService;

    @Autowired
    private GameConfig gameConfig;

    @Autowired
    private MonumentService monumentService;

    @Autowired
    private INotificationSystem notificationSystem;

    /**
     * pays goods into the monument of uprzejmy's alliance, as many times as a kingdom's store needs to carry them
     */
    private void payIntoTheMonument(long gold, long iron, long tools, long food)
    {
        var chunk = 2_000_000_000L;
        while (gold + iron + tools + food > 0)
        {
            var paid = new MonumentContributionDto((int) Math.min(chunk, gold), (int) Math.min(chunk, iron), (int) Math.min(chunk, tools), (int) Math.min(chunk, food));
            var kingdom = kingdomRepository.getKingdomByName("uprzejmy").orElseThrow();
            kingdom.getResources().setCount(ResourceName.gold, paid.gold());
            kingdom.getResources().setCount(ResourceName.iron, paid.iron());
            kingdom.getResources().setCount(ResourceName.tools, paid.tools());
            kingdom.getResources().setCount(ResourceName.food, paid.food());
            kingdomRepository.update(kingdom);
            var result = monumentService.contribute("uprzejmy", paid);
            assertTrue(result.success(), result.message());
            gold -= paid.gold();
            iron -= paid.iron();
            tools -= paid.tools();
            food -= paid.food();
        }
    }

    private MonumentDto theMonumentOfLegion()
    {
        return monumentService.getMonuments().stream().filter(monument -> monument.allianceName().equals("Legion")).findFirst().orElseThrow();
    }

    @Test
    void aMonumentStandingCompleteAndFedThroughTheHold_shouldWinTheEraForItsAlliance() throws Exception
    {
        var eraBefore = eraService.getCurrentEra().number();
        var rules = gameConfig.monument();
        // the seeded Legion: uprzejmy is its emperor, Umbar its member
        var founded = monumentService.found("uprzejmy", new FoundMonumentDto("Umbar"));
        assertTrue(founded.success(), founded.message());
        var cost = rules.stageCost();
        var upkeep = rules.upkeepPerStage();
        payIntoTheMonument(cost.gold() * rules.stages() + upkeep.gold() * rules.stages() * rules.holdTicks(), cost.iron() * rules.stages(), cost.tools() * rules.stages(), upkeep.food() * rules.stages() * rules.holdTicks());
        assertEquals(rules.holdTicks(), theMonumentOfLegion().ticksToWin());

        for (int tick = 1; tick < rules.holdTicks(); tick++)
        {
            eraService.tick();
            assertEquals(eraBefore, eraService.getCurrentEra().number());
            assertEquals(rules.holdTicks() - tick, theMonumentOfLegion().ticksToWin());
        }
        assertThat(notificationSystem.getNotifications("Umbar")).map(NotificationDto::message)
                .contains("The monument of Legion at Umbar wins the era in 24 turns unless an army knocks it down");

        eraService.tick();

        mockMvc.perform(get("/game/era"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(eraBefore + 1))
                .andExpect(jsonPath("$.ticksElapsed").value(0));
        mockMvc.perform(get("/game/eras").with(user("uprzejmy")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].number").value(eraBefore))
                .andExpect(jsonPath("$[0].endedBy").value("monument"))
                .andExpect(jsonPath("$[0].winningAlliance").value("Legion"));
        assertThat(monumentService.getMonuments()).isEmpty();
        assertThat(notificationSystem.getNotifications("Umbar")).map(NotificationDto::message).anySatisfy(message -> assertThat(message)
                .startsWith("Era " + eraBefore + " has ended: the monument of Legion at Umbar stood complete for " + rules.holdTicks() + " turns, and Legion wins the era."));
    }

    @Test
    void theClockRunningOut_shouldArchiveRankingsResetKingdomsAndStartNextEra() throws Exception
    {
        var eraBefore = eraService.getCurrentEra().number();
        var kingdom = kingdomRepository.getKingdomByName("uprzejmy").orElseThrow();
        kingdom.getResources().setCount(ResourceName.land, 5000);
        // beaten just before the end: the protection belongs to the era that is over
        kingdom.setProtectionTicks(3);
        kingdomRepository.update(kingdom);

        mockMvc.perform(get("/game/rankings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kingdoms[0].kingdomName").value("uprzejmy"))
                .andExpect(jsonPath("$.kingdoms[0].land").value(5000));

        for (int tick = 0; tick < gameConfig.era().maxLengthInTicks(); tick++)
        {
            assertEquals(eraBefore, eraService.getCurrentEra().number());
            eraService.tick();
        }

        mockMvc.perform(get("/game/era"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(eraBefore + 1))
                .andExpect(jsonPath("$.ticksElapsed").value(0));
        mockMvc.perform(get("/game/eras").with(user("uprzejmy")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].number").value(eraBefore))
                .andExpect(jsonPath("$[0].endedBy").value("clock"))
                .andExpect(jsonPath("$[0].finalRankings.kingdoms[0].kingdomName").value("uprzejmy"))
                .andExpect(jsonPath("$[0].finalRankings.kingdoms[0].land").value(5000));

        var resetKingdom = kingdomRepository.getKingdomByName("uprzejmy").orElseThrow();
        assertEquals(100, resetKingdom.getResources().getCount(ResourceName.land));
        assertEquals(gameConfig.attack().protectionTicksAtStart(), resetKingdom.getProtectionTicks());
        for (var resource : MarketResource.values())
        {
            assertEquals(0, marketService.getOffersByResource(resource).size());
        }
    }
}
