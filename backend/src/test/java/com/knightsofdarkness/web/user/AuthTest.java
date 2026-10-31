package com.knightsofdarkness.web.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class AuthTest {
    @Autowired
    private MockMvc mockMvc;

    private ResultActions register(String email, String kingdomName, String password) throws Exception
    {
        return mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"kingdomName\":\"" + kingdomName + "\",\"password\":\"" + password + "\"}"));
    }

    private ResultActions login(String email, String password) throws Exception
    {
        return mockMvc.perform(post("/auth/authenticate").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"));
    }

    @Test
    void registration_shouldExplainWhatIsWrong() throws Exception
    {
        register("not-an-email", "Valid Kingdom", "long-enough-password").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Enter a valid email address"));
        register("someone@example.com", "x", "long-enough-password").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
        register("someone@example.com", "Valid Kingdom", "short").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Password must be at least 8 characters long"));
        register("someone@example.com", "UPRZEJMY", "long-enough-password").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A kingdom with this name already exists"));
    }

    @Test
    void registeredPlayer_shouldLogInWithAnyEmailCaseAndOwnTheNewKingdom() throws Exception
    {
        register("Founder@Example.com", "Iron Vale", "long-enough-password").andExpect(status().isOk());
        register("founder@example.com", "Other Vale", "long-enough-password").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("An account with this email already exists"));

        var response = login(" FOUNDER@example.com ", "long-enough-password").andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(response, "$.token");

        mockMvc.perform(get("/kingdom").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Iron Vale"));

        var refreshed = mockMvc.perform(post("/auth/refresh").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String refreshedToken = JsonPath.read(refreshed, "$.token");
        mockMvc.perform(get("/kingdom").header("Authorization", "Bearer " + refreshedToken)).andExpect(status().isOk());
    }

    @Test
    void wrongPassword_shouldBeUnauthorizedWithMessage() throws Exception
    {
        login("uprzejmy", "wrong-password").andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Wrong email or password"));
    }

    @Test
    void seededDevUsers_shouldReachTheirKingdoms() throws Exception
    {
        var response = login("umbar", "umbar_password").andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(response, "$.token");

        mockMvc.perform(get("/kingdom").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Umbar"));
    }

    @Test
    void refresh_withoutToken_shouldBeRejected() throws Exception
    {
        mockMvc.perform(post("/auth/refresh")).andExpect(status().isUnauthorized());
    }

    @Test
    void registeredPlayer_shouldActAsTheirKingdomNotTheirEmail() throws Exception
    {
        register("herald@example.com", "Herald Keep", "long-enough-password").andExpect(status().isOk());
        var response = login("herald@example.com", "long-enough-password").andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(response, "$.token");

        mockMvc.perform(post("/alliance/create").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Heralds\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.alliance.emperor").value("Herald Keep"));
        mockMvc.perform(get("/alliance").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members[0]").value("Herald Keep"));
    }
}
