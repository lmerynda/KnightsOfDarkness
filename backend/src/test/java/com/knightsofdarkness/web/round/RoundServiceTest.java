package com.knightsofdarkness.web.round;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import com.knightsofdarkness.web.GameServerTest;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.market.MarketResource;
import com.knightsofdarkness.web.kingdom.model.KingdomRepository;
import com.knightsofdarkness.web.market.MarketService;

@GameServerTest
class RoundServiceTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoundService roundService;

    @Autowired
    private KingdomRepository kingdomRepository;

    @Autowired
    private MarketService marketService;

    @Test
    void endingRound_shouldArchiveRankingsResetKingdomsAndStartNextRound() throws Exception
    {
        var roundBefore = roundService.getCurrentRound().number();
        var kingdom = kingdomRepository.getKingdomByName("uprzejmy").orElseThrow();
        kingdom.getResources().setCount(ResourceName.land, 5000);
        kingdomRepository.update(kingdom);

        mockMvc.perform(get("/game/rankings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kingdoms[0].kingdomName").value("uprzejmy"))
                .andExpect(jsonPath("$.kingdoms[0].land").value(5000));

        roundService.endCurrentRound();

        mockMvc.perform(get("/game/round"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(roundBefore + 1))
                .andExpect(jsonPath("$.ticksElapsed").value(0));
        mockMvc.perform(get("/game/rounds"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].number").value(roundBefore))
                .andExpect(jsonPath("$[0].finalRankings.kingdoms[0].kingdomName").value("uprzejmy"))
                .andExpect(jsonPath("$[0].finalRankings.kingdoms[0].land").value(5000));

        var resetKingdom = kingdomRepository.getKingdomByName("uprzejmy").orElseThrow();
        assertEquals(100, resetKingdom.getResources().getCount(ResourceName.land));
        for (var resource : MarketResource.values())
        {
            assertEquals(0, marketService.getOffersByResource(resource).size());
        }
    }
}
