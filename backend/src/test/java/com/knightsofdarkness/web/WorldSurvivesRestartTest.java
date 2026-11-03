package com.knightsofdarkness.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import com.jayway.jsonpath.JsonPath;
import com.knightsofdarkness.web.kingdom.KingdomService;
import com.knightsofdarkness.web.round.RoundService;

/**
 * The world outlives the server: a ruler who played before a restart finds their kingdom, the round and the whole world
 * as they left them, still signed in, and the server's own start-up creates nothing twice.
 */
class WorldSurvivesRestartTest {
    private static final List<String> tables = List.of("kingdom_entity", "bot_entity", "user_entity", "alliance_entity", "game_round_entity", "market_offer_entity");

    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void aRestart_shouldFindTheWorldAsItWasLeft() throws Exception
    {
        var database = TestDatabase.create();
        String token;
        String kingdomBefore;
        String roundBefore;
        Map<String, Integer> rowsBefore;

        try (var server = start(database))
        {
            var url = url(server);
            send(url, "POST", "/auth/register", null, "{\"email\":\"restart@example.com\",\"kingdomName\":\"Restart\",\"password\":\"restart-password\"}");
            token = JsonPath.read(send(url, "POST", "/auth/authenticate", null, "{\"email\":\"restart@example.com\",\"password\":\"restart-password\"}"), "$.token");
            assertEquals(true, JsonPath.read(send(url, "POST", "/kingdom/pass-turn", token, "0"), "$.success"));
            // time moves on: turns come in and the round counts the ticks
            for (int tick = 0; tick < 3; tick++)
            {
                server.getBean(KingdomService.class).addTurnForEveryone();
                server.getBean(RoundService.class).tick();
            }
            kingdomBefore = send(url, "GET", "/kingdom", token, null);
            roundBefore = send(url, "GET", "/game/round", null, null);
            rowsBefore = rows(server);
        }

        try (var server = start(database))
        {
            var url = url(server);
            assertEquals(kingdomBefore, send(url, "GET", "/kingdom", token, null));
            assertEquals(roundBefore, send(url, "GET", "/game/round", null, null));
            assertEquals(rowsBefore, rows(server));
            assertEquals(true, JsonPath.read(send(url, "POST", "/kingdom/pass-turn", token, "0"), "$.success"));
        }
    }

    private static ConfigurableApplicationContext start(TestDatabase.Connection database)
    {
        // as arguments, which outrank application.yaml, the way a deploy passes its settings
        return new SpringApplicationBuilder(GameApplication.class).run(
                "--server.port=0",
                "--spring.datasource.url=" + database.url(),
                "--spring.datasource.username=" + database.username(),
                "--spring.datasource.password=" + database.password(),
                "--app.jwt.secret=dependency-upgrade-test-only-key-0123456789012345678901234567890123456789",
                "--app.jwt.expiration.minutes=5");
    }

    private static String url(ConfigurableApplicationContext server)
    {
        return "http://localhost:" + server.getEnvironment().getProperty("local.server.port");
    }

    private static Map<String, Integer> rows(ConfigurableApplicationContext server)
    {
        var jdbc = server.getBean(JdbcTemplate.class);
        return tables.stream().collect(Collectors.toMap(table -> table, table -> jdbc.queryForObject("select count(*) from " + table, Integer.class)));
    }

    private String send(String url, String method, String path, String token, String body) throws Exception
    {
        var request = HttpRequest.newBuilder(URI.create(url + path)).header("Content-Type", "application/json")
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        if (token != null)
        {
            request.header("Authorization", "Bearer " + token);
        }
        var response = http.send(request.build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), () -> method + " " + path + " answered " + response.statusCode() + ": " + response.body());
        return response.body();
    }
}
