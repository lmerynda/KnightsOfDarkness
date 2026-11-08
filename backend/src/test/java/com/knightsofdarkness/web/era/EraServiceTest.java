package com.knightsofdarkness.web.era;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import com.knightsofdarkness.web.GameServerTest;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.market.MarketResource;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.model.KingdomRepository;
import com.knightsofdarkness.web.market.MarketService;

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
