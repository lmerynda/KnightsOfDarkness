package com.knightsofdarkness.web.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.knightsofdarkness.web.GameServerTest;

/**
 * every endpoint the server has, present and future, turns away a caller without a token, except the few a visitor
 * needs before signing in
 */
@GameServerTest
class SignInRequiredTest {
    private static final Set<String> open = Set.of(
            "GET /health",
            "GET /game/era",
            "GET /game/rankings",
            "POST /auth/authenticate",
            "POST /auth/register",
            "POST /auth/trial",
            "GET /error");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping mappings;

    @Test
    void everyEndpoint_withoutAToken_shouldBeTurnedAwayUnlessAVisitorNeedsIt() throws Exception
    {
        var endpoints = new TreeSet<String>();
        for (var info : mappings.getHandlerMethods().keySet())
        {
            var methods = info.getMethodsCondition().getMethods().isEmpty() ? Set.of(RequestMethod.GET) : info.getMethodsCondition().getMethods();
            for (var method : methods)
            {
                info.getPatternValues().forEach(pattern -> endpoints.add(method + " " + pattern));
            }
        }

        var reachable = new TreeSet<String>();
        for (var endpoint : endpoints)
        {
            var parts = endpoint.split(" ");
            var path = parts[1].replaceAll("\\{[^}]+}", "1");
            if (mockMvc.perform(request(HttpMethod.valueOf(parts[0]), path)).andReturn().getResponse().getStatus() != 401)
            {
                reachable.add(endpoint);
            }
        }

        assertEquals(new TreeSet<>(open), reachable, "exactly these answer a caller without a token");
    }

    @Test
    void theApiDescription_shouldNeedASignedInCaller() throws Exception
    {
        mockMvc.perform(request(HttpMethod.GET, "/v3/api-docs")).andExpect(status().isUnauthorized());
    }

    @Test
    void aForgedToken_shouldBeTurnedAway() throws Exception
    {
        mockMvc.perform(request(HttpMethod.GET, "/kingdom").header("Authorization", "Bearer not-a-real-token")).andExpect(status().isUnauthorized());
    }
}
