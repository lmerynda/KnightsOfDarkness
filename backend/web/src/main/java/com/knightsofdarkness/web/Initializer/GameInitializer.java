package com.knightsofdarkness.web.Initializer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knightsofdarkness.common.KingdomDto;
import com.knightsofdarkness.common.KingdomSpecialBuildingStartDto;
import com.knightsofdarkness.common.MarketOfferDto;
import com.knightsofdarkness.game.kingdom.SpecialBuildingType;
import com.knightsofdarkness.web.Kingdom.KingdomService;
import com.knightsofdarkness.web.Market.MarketService;

@Component
public class GameInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(GameInitializer.class);

    private final KingdomService kingdomService;
    private final MarketService marketService;

    public GameInitializer(KingdomService kingdomService, MarketService marketService)
    {
        this.kingdomService = kingdomService;
        this.marketService = marketService;
    }

    @Override
    public void run(String... args)
    {
        kingdomService.createKingdom(generateKingdom("uprzejmy"));
        kingdomService.createKingdom(generateKingdom("BlacksmithBot"));
        kingdomService.createKingdom(generateKingdom("FarmerBot"));
        kingdomService.createKingdom(generateKingdom("IronMinerBot"));
        kingdomService.createKingdom(generateKingdom("GoldMinerBot"));
        startBotsSpecialBuildings();
        log.info("Kingdoms initialized");
        marketService.createOffers(generateMarketOffers());
        log.info("Market offers initialized");
    }

    private void startBotsSpecialBuildings()
    {
        var numberOfSpecialBuildings = 5;
        for(int i = 0; i < numberOfSpecialBuildings; i++)
        {
            kingdomService.startSpecialBuilding("FarmerBot", new KingdomSpecialBuildingStartDto(SpecialBuildingType.granary));
            kingdomService.startSpecialBuilding("BlacksmithBot", new KingdomSpecialBuildingStartDto(SpecialBuildingType.forge));
            kingdomService.startSpecialBuilding("IronMinerBot", new KingdomSpecialBuildingStartDto(SpecialBuildingType.ironShaft));
            kingdomService.startSpecialBuilding("GoldMinerBot", new KingdomSpecialBuildingStartDto(SpecialBuildingType.goldShaft));
        }
    }

    private KingdomDto generateKingdom(String name)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        try
        {
            var kingdomDto = objectMapper.readValue(defaultKingdomPayload, KingdomDto.class);
            kingdomDto.name = name;
            return kingdomDto;
        } catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate kingdom");
        }
    }

    private List<MarketOfferDto> generateMarketOffers()
    {
        ObjectMapper objectMapper = new ObjectMapper();
        try
        {
            return objectMapper.readValue(marketOffersPayload, new TypeReference<List<MarketOfferDto>>() {
            });
        } catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate kingdom");
        }
    }

    private final String defaultKingdomPayload = """
            {
                "name": "default_kingdom_name",
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

    private final String marketOffersPayload = """
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
                },
                {
                    "sellerName": "uprzejmy",
                    "resource": "food",
                    "count": 100,
                    "price": 30
                }
            ]
            """;
}
