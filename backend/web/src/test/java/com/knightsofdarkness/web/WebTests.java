package com.knightsofdarkness.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knightsofdarkness.game.market.MarketOffer;
import com.knightsofdarkness.game.market.MarketResource;
import com.knightsofdarkness.web.Market.MarketOfferDto;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@ComponentScan(basePackages =
{ "com.knightsofdarkness.*" })
@EntityScan(basePackages =
{ "com.knightsofdarkness.*" })
public class WebTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void foo() throws Exception
    {
        MarketOffer offer1 = new MarketOffer(null, MarketResource.food, 10, 100);
        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/market/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(offer1)))
                .andExpect(status().isOk());

        MarketOffer offer2 = new MarketOffer(null, MarketResource.iron, 30, 100);
        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/market/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(offer2)))
                .andExpect(status().isOk());

        var foodResult = this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/market/food")
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk());
        List<MarketOfferDto> foodOffers = objectMapper.readValue(foodResult.andReturn().getResponse().getContentAsString(), new TypeReference<List<MarketOfferDto>>() {
        });
        assertEquals(1, foodOffers.size());

        var ironResult = this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/market/iron")
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk());
        List<MarketOfferDto> ironOffers = objectMapper.readValue(ironResult.andReturn().getResponse().getContentAsString(), new TypeReference<List<MarketOfferDto>>() {
        });
        assertEquals(1, ironOffers.size());
    }
}
