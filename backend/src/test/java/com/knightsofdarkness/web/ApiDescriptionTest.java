package com.knightsofdarkness.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * the browser and the simulation generate their API types from api/openapi.json, so it must be the API the server
 * serves: when they differ the file is rewritten and the test fails, and the change is committed with the clients'
 * regenerated types (npm run api:types in browser/ and simulation/)
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
class ApiDescriptionTest {
    private static final Path committed = Path.of("..", "api", "openapi.json");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void theCommittedApiDescription_shouldBeTheOneTheServerServes() throws Exception
    {
        var served = mockMvc.perform(get("/v3/api-docs")).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        var mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        var described = mapper.writeValueAsString(sorted(mapper.readValue(served, Object.class))).replace("\r\n", "\n") + "\n";
        var before = Files.exists(committed) ? Files.readString(committed).replace("\r\n", "\n") : "";
        if (!described.equals(before))
        {
            Files.createDirectories(committed.getParent());
            Files.writeString(committed, described);
        }

        assertEquals(before, described, "api/openapi.json was out of date and has been rewritten; regenerate the clients' types with npm run api:types and commit them together");
    }

    // keys in a fixed order, so the file changes only when the API does
    @SuppressWarnings("unchecked")
    private static Object sorted(Object node)
    {
        if (node instanceof Map<?, ?> map)
        {
            var ordered = new TreeMap<String, Object>();
            ((Map<String, Object>) map).forEach((key, value) -> ordered.put(key, sorted(value)));
            return ordered;
        }
        if (node instanceof List<?> list)
        {
            return list.stream().map(ApiDescriptionTest::sorted).toList();
        }
        return node;
    }
}
