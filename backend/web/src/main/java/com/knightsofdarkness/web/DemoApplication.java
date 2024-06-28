package com.knightsofdarkness.web;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@ComponentScan(basePackages =
{ "com.knightsofdarkness.*" })
@EntityScan(basePackages =
{ "com.knightsofdarkness.*" })
@EnableJpaRepositories(basePackages =
{ "com.knightsofdarkness.*" })
public class DemoApplication {
    public static void main(String[] args)
    {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate()
    {
        return new RestTemplate();
    }

    @Bean
    public HttpHeaders httpHeaders()
    {
        return new HttpHeaders();
    }

    @Bean
    public CommandLineRunner run(RestTemplate restTemplate, HttpHeaders headers) throws Exception
    {
        return args ->
        {
            headers.setContentType(MediaType.APPLICATION_JSON);

            // POST request to /kingdom
            String kingdomPayload = """
                    {
                    	"name": "uprzejmy",
                    	"resources": {
                    		"food": 20000,
                    		"gold": 1000,
                    		"iron": 1000,
                    		"land": 100,
                    		"tools": 100,
                    		"weapons": 100,
                    		"buildingPoints": 10000,
                    		"unemployed": 20,
                    		"turns": 36
                    	},
                    	"buildings": {
                    		"houses": 10,
                    		"goldMines": 5,
                    		"ironMines": 5,
                    		"workshops": 5,
                    		"farms": 5,
                    		"markets": 1,
                    		"barracks": 1,
                    		"guardHouses": 1,
                    		"spyGuilds": 0,
                    		"towers": 1,
                    		"castles": 0
                    	},
                    	"units": {
                    		"goldMiners": 5,
                    		"ironMiners": 5,
                    		"farmers": 5,
                    		"blacksmiths": 5,
                    		"builders": 5,
                    		"carriers": 0,
                    		"guards": 5,
                    		"spies": 0,
                    		"infantry": 0,
                    		"bowmen": 0,
                    		"cavalry": 0
                    	}
                    }
                    """;
            HttpEntity<String> kingdomEntity = new HttpEntity<>(kingdomPayload, headers);
            restTemplate.postForObject("http://localhost:8080/kingdom/", kingdomEntity, String.class);

            // POST request to /market/create
            String marketPayload = """
                    {
                    	"sellerName": "uprzejmy",
                    	"resource": "food",
                    	"count": 100,
                    	"price": 30
                    }
                    """;
            HttpEntity<String> marketEntity = new HttpEntity<>(marketPayload, headers);
            restTemplate.postForObject("http://localhost:8080/market/create", marketEntity, String.class);

            // POST request to /market_fixtures
            String marketFixturesPayload = """
                    [
                    	{
                    		"sellerName": "uprzejmy",
                    		"resource": "food",
                    		"count": 10000,
                    		"price": 10
                    	},
                    	{
                    		"sellerName": "uprzejmy",
                    		"resource": "food",
                    		"count": 500,
                    		"price": 30
                    	},
                    	{
                    		"sellerName": "uprzejmy",
                    		"resource": "iron",
                    		"count": 100,
                    		"price": 50
                    	},
                    	{
                    		"sellerName": "uprzejmy",
                    		"resource": "iron",
                    		"count": 150,
                    		"price": 70
                    	}
                    ]
                    """;
            HttpEntity<String> marketFixturesEntity = new HttpEntity<>(marketFixturesPayload, headers);
            restTemplate.postForObject("http://localhost:8080/market_fixtures", marketFixturesEntity, String.class);
        };
    }
}
