package com.knightsofdarkness.web;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * the PostgreSQL that production runs, in one container for the whole test run: every test context gets a fresh
 * database of its own, which Flyway migrates as it would production's
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestDatabase {
    private static final PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse(composeImage()).asCompatibleSubstituteFor("postgres"));
    private static final AtomicInteger databases = new AtomicInteger();

    public record Connection(String url, String username, String password) {
    }

    @Bean
    DynamicPropertyRegistrar database()
    {
        var database = create();
        return registry ->
        {
            registry.add("spring.datasource.url", database::url);
            registry.add("spring.datasource.username", database::username);
            registry.add("spring.datasource.password", database::password);
        };
    }

    /** a new, empty database */
    public static synchronized Connection create()
    {
        if (!postgres.isRunning())
        {
            postgres.start();
        }
        var name = "world_" + databases.incrementAndGet();
        try (var connection = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
                var statement = connection.createStatement())
        {
            statement.execute("create database " + name);
        } catch (SQLException e)
        {
            throw new IllegalStateException("Could not create the test database " + name, e);
        }
        var url = "jdbc:postgresql://" + postgres.getHost() + ":" + postgres.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT) + "/" + name;
        return new Connection(url, postgres.getUsername(), postgres.getPassword());
    }

    /** the image the local world runs on, so the tests and bootRun never drift apart */
    private static String composeImage()
    {
        try
        {
            return Files.readAllLines(Path.of("compose.yaml")).stream().map(String::strip).filter(line -> line.startsWith("image:"))
                    .map(line -> line.substring("image:".length()).strip()).findFirst()
                    .orElseThrow(() -> new IllegalStateException("compose.yaml names no image"));
        } catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }
}
