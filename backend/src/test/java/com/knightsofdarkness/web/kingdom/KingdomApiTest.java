package com.knightsofdarkness.web.kingdom;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.jayway.jsonpath.JsonPath;

/**
 * Requests as the browser sends them: unit, building and resource maps are flat objects keyed by name.
 */
@SpringBootTest(properties = {
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "app.jwt.secret=dependency-upgrade-test-only-key-0123456789012345678901234567890123456789",
    "app.jwt.expiration.minutes=5"
})
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@ComponentScan(basePackages =
{ "com.knightsofdarkness.*" })
@EntityScan(basePackages =
{ "com.knightsofdarkness.*" })
class KingdomApiTest {
    @Autowired
    private MockMvc mockMvc;

    private String token;

    @BeforeEach
    void logIn() throws Exception
    {
        var email = "api-" + System.nanoTime() + "@example.com";
        var kingdomName = "Api " + (System.nanoTime() % 1000000);
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"kingdomName\":\"" + kingdomName + "\",\"password\":\"long-enough-password\"}"))
                .andExpect(status().isOk());
        var response = mockMvc.perform(post("/auth/authenticate").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"long-enough-password\"}")).andReturn().getResponse().getContentAsString();
        token = JsonPath.read(response, "$.token");
    }

    private ResultActions postJson(String path, String body) throws Exception
    {
        return mockMvc.perform(post(path).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    @Test
    void buildAndDemolish_shouldUseFlatBuildingCounts() throws Exception
    {
        postJson("/kingdom/build", "{\"house\":2}").andExpect(status().isOk()).andExpect(jsonPath("$.buildings.house").value(2));
        postJson("/kingdom/demolish", "{\"house\":1}").andExpect(status().isOk()).andExpect(jsonPath("$.buildings.house").value(1));
    }

    @Test
    void trainAndFire_shouldUseFlatUnitCounts() throws Exception
    {
        postJson("/kingdom/train", "{\"farmer\":3}").andExpect(status().isOk()).andExpect(jsonPath("$.units.farmer").value(3));
        postJson("/kingdom/fire", "{\"farmer\":2}").andExpect(status().isOk()).andExpect(jsonPath("$.units.farmer").value(2));
    }

    @Test
    void sendAttack_shouldMoveTroopsAndShowThemOnTheKingdom() throws Exception
    {
        postJson("/kingdom/build", "{\"barracks\":1}").andExpect(status().isOk());
        postJson("/kingdom/train", "{\"infantry\":1}").andExpect(jsonPath("$.units.infantry").value(1));

        postJson("/kingdom/send-attack", "{\"destinationKingdomName\":\"GoldMinerBot\",\"attackType\":\"land\",\"units\":{\"infantry\":1}}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/kingdom").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.ongoingAttacks[0].to").value("GoldMinerBot"))
                .andExpect(jsonPath("$.ongoingAttacks[0].units.infantry").value(1))
                .andExpect(jsonPath("$.units.mobileUnits.infantry").value(1));
    }
}
