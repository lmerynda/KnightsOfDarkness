package com.knightsofdarkness.web.initializer;

import java.lang.reflect.Type;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.knightsofdarkness.common.kingdom.KingdomDto;
import com.knightsofdarkness.common.kingdom.UnitsMapDto;
import com.knightsofdarkness.common.market.MarketOfferDto;
import com.knightsofdarkness.web.kingdom.KingdomService;
import com.knightsofdarkness.web.market.MarketService;

@Component
public class GameInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(GameInitializer.class);

    private final KingdomService kingdomService;
    private final MarketService marketService;
    private final Gson gson;

    public GameInitializer(KingdomService kingdomService, MarketService marketService, Gson gson)
    {
        this.kingdomService = kingdomService;
        this.marketService = marketService;
        this.gson = gson;
    }

    @Override
    public void run(String... args)
    {
        kingdomService.createKingdom(generateKingdom("uprzejmy"));
        // kingdomService.startSpecialBuilding("uprzejmy", new KingdomSpecialBuildingStartDto(SpecialBuildingType.goldShaft));
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
        for (int i = 0; i < numberOfSpecialBuildings; i++)
        {
            // kingdomService.startSpecialBuilding("FarmerBot", new KingdomSpecialBuildingStartDto(SpecialBuildingType.granary));
            // kingdomService.startSpecialBuilding("BlacksmithBot", new KingdomSpecialBuildingStartDto(SpecialBuildingType.forge));
            // kingdomService.startSpecialBuilding("IronMinerBot", new KingdomSpecialBuildingStartDto(SpecialBuildingType.ironShaft));
            // kingdomService.startSpecialBuilding("GoldMinerBot", new KingdomSpecialBuildingStartDto(SpecialBuildingType.goldShaft));
        }
    }

    private KingdomDto generateKingdom(String name)
    {
        var kingdom = generateDefaultKingdomDto();
        kingdom.name = name;
        return kingdom;
    }

    private List<MarketOfferDto> generateMarketOffers()
    {
        Type listType = new TypeToken<List<MarketOfferDto>>() {
        }.getType();
        return gson.fromJson(marketOffersPayload, listType);
    }

    private static final KingdomDto generateDefaultKingdomDto()
    {
        var kingdom = new KingdomDto();
        kingdom.name = "default_kingdom_name";
        kingdom.resources = new KingdomResourcesEntityDto(100, 10000, 20, 1000, 1000, 20000, 100, 100, 20);
        kingdom.buildings = new KingdomBuildingsEntityDto(10, 5, 5, 5, 5, 1, 1, 1, 0, 1, 0);
        kingdom.units = new KingdomUnitsEntityDto(generateDefaultAvailableUnits().getUnits(), new UnitsMapDto().getUnits());
        return kingdom;
    }

    private static final UnitsMapDto generateDefaultAvailableUnits()
    {
        var units = new UnitsMapDto();
        units.setUnit("goldMiner", 5);
        units.setUnit("ironMiner", 5);
        units.setUnit("builder", 5);
        units.setUnit("blacksmith", 5);
        units.setUnit("farmer", 5);
        return units;
    }

    private static final String marketOffersPayload = """
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
