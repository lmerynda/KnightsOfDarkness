package com.knightsofdarkness.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@GameServerTest
class WebTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthAndAuthenticationWorkWithTheUpgradedFramework() throws Exception
    {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("OK"));

        mockMvc.perform(post("/auth/validate-token"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"dependency-upgrade@example.invalid","kingdomName":"DependencyUpgrade",
                     "password":"Dependency-upgrade-fixture-123!"}
                    """))
                .andExpect(status().isOk());

        var login = mockMvc.perform(post("/auth/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"dependency-upgrade@example.invalid","password":"Dependency-upgrade-fixture-123!"}
                    """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = com.jayway.jsonpath.JsonPath.read(login, "$.token");

        mockMvc.perform(post("/auth/validate-token").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}
