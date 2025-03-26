package com.knightsofdarkness.web.initializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.knightsofdarkness.web.bots.IBotRepository;
import com.knightsofdarkness.web.bots.model.BotEntity;
import com.knightsofdarkness.web.common.kingdom.KingdomBuildingsDto;
import com.knightsofdarkness.web.common.kingdom.KingdomDto;
import com.knightsofdarkness.web.common.kingdom.KingdomResourcesDto;
import com.knightsofdarkness.web.common.kingdom.KingdomSpecialBuildingStartDto;
import com.knightsofdarkness.web.common.kingdom.KingdomTurnReport;
import com.knightsofdarkness.web.common.kingdom.KingdomUnitsDto;
import com.knightsofdarkness.web.common.kingdom.SpecialBuildingType;
import com.knightsofdarkness.web.common.kingdom.UnitsMapDto;
import com.knightsofdarkness.web.common.market.MarketOfferDto;
import com.knightsofdarkness.web.kingdom.IKingdomRepository;
import com.knightsofdarkness.web.kingdom.KingdomService;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
import com.knightsofdarkness.web.market.MarketService;
import com.knightsofdarkness.web.utils.Id;

import jakarta.transaction.Transactional;

@Component
public class GameInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(GameInitializer.class);

    private final IBotRepository botRepository;
    private final IKingdomRepository kingdomRepository;
    private final KingdomService kingdomService;
    private final MarketService marketService;
    private final Gson gson;
    private final int numberOfSpecialBuildings = 5;

    public GameInitializer(KingdomService kingdomService, MarketService marketService, IKingdomRepository kingdomRepository, IBotRepository botRepository, Gson gson)
    {
        this.kingdomService = kingdomService;
        this.marketService = marketService;
        this.botRepository = botRepository;
        this.kingdomRepository = kingdomRepository;
        this.gson = gson;
    }

    @Override
    public void run(String... args)
    {

        createKingdom("uprzejmy");
        createBot("BlacksmithBot", SpecialBuildingType.forge);
        createBot("FarmerBot", SpecialBuildingType.granary);
        createBot("IronMinerBot", SpecialBuildingType.ironShaft);
        createBot("GoldMinerBot", SpecialBuildingType.goldShaft);
        log.info("Kingdoms initialized");
        marketService.createOffers(generateMarketOffers());
        log.info("Market offers initialized");
    }

    @Transactional
    KingdomEntity createKingdom(String name)
    {
        log.info("Initializing kingdom " + name);
        KingdomDto kingdomDto = generateKingdom(name);
        kingdomDto.lastTurnReport = new KingdomTurnReport();
        kingdomDto.specialBuildings = new ArrayList<>();
        return kingdomRepository.add(KingdomEntity.fromDto(kingdomDto));
    }

    @Transactional
    void createBot(String name, SpecialBuildingType specialBuildingType)
    {
        var kingdom = createKingdom(name);
        botRepository.add(new BotEntity(Id.generate(), kingdom));
        for (int i = 0; i < numberOfSpecialBuildings; i++)
        {
            kingdomService.startSpecialBuilding(name, new KingdomSpecialBuildingStartDto(specialBuildingType));
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
        kingdom.resources = new KingdomResourcesDto(100, 10000, 20, 1000, 1000, 20000, 100, 100, 20);
        kingdom.buildings = new KingdomBuildingsDto(10, 5, 5, 5, 5, 1, 1, 1, 0, 1, 0);
        kingdom.units = new KingdomUnitsDto(generateDefaultAvailableUnits().getUnits(), new UnitsMapDto().getUnits());
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
