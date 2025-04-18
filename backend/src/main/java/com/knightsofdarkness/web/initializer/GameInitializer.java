package com.knightsofdarkness.web.initializer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.knightsofdarkness.web.alliance.IAllianceRepository;
import com.knightsofdarkness.web.alliance.model.AllianceEntity;
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
import com.knightsofdarkness.web.common.market.MarketResource;
import com.knightsofdarkness.web.kingdom.IKingdomRepository;
import com.knightsofdarkness.web.kingdom.KingdomService;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
import com.knightsofdarkness.web.market.MarketService;
import com.knightsofdarkness.web.utils.Id;

import jakarta.transaction.Transactional;

@Component
public class GameInitializer implements CommandLineRunner
{
    private static final Logger log = LoggerFactory.getLogger(GameInitializer.class);

    private final IBotRepository botRepository;
    private final IKingdomRepository kingdomRepository;
    private final IAllianceRepository allianceRepository;
    private final KingdomService kingdomService;
    private final MarketService marketService;
    private final int numberOfSpecialBuildings = 5;

    public GameInitializer(KingdomService kingdomService, MarketService marketService, IKingdomRepository kingdomRepository, IBotRepository botRepository, IAllianceRepository allianceRepository)
    {
        this.kingdomService = kingdomService;
        this.marketService = marketService;
        this.botRepository = botRepository;
        this.kingdomRepository = kingdomRepository;
        this.allianceRepository = allianceRepository;
    }

    @Override
    public void run(String... args)
    {

        createKingdom("uprzejmy");
        createKingdom("Umbar");
        createBot("BlacksmithBot", SpecialBuildingType.forge);
        createBot("FarmerBot", SpecialBuildingType.granary);
        createBot("IronMinerBot", SpecialBuildingType.ironShaft);
        createBot("GoldMinerBot", SpecialBuildingType.goldShaft);
        log.info("Kingdoms initialized");
        createAlliance("Legion", "Umbar", "uprzejmy");
        log.info("Alliances initialized");
        marketService.createOffers(generateMarketOffers());
        log.info("Market offers initialized");
    }

    @Transactional
    private void createAlliance(String allianceName, String emperor, String kingdom)
    {
        var kingdomEntity = kingdomRepository.getKingdomByName(kingdom).get();
        var emperorEntity = kingdomRepository.getKingdomByName(emperor).get();
        var alliance = new AllianceEntity(allianceName, kingdomEntity.getName());
        alliance.addKingdom(emperorEntity);
        alliance.addKingdom(kingdomEntity);
        allianceRepository.create(alliance);
        kingdomRepository.update(kingdomEntity);
        kingdomRepository.update(emperorEntity);
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
        List<MarketOfferDto> marketOffers = new ArrayList<>();
        marketOffers.add(new MarketOfferDto(Id.generate(), "uprzejmy", MarketResource.food, 10000, 10));
        marketOffers.add(new MarketOfferDto(Id.generate(), "uprzejmy", MarketResource.food, 500, 30));
        marketOffers.add(new MarketOfferDto(Id.generate(), "uprzejmy", MarketResource.iron, 100, 50));
        marketOffers.add(new MarketOfferDto(Id.generate(), "uprzejmy", MarketResource.iron, 150, 70));
        marketOffers.add(new MarketOfferDto(Id.generate(), "uprzejmy", MarketResource.food, 100, 30));
        return marketOffers;
    }

    private static final KingdomDto generateDefaultKingdomDto()
    {
        var kingdom = new KingdomDto();
        kingdom.name = "default_kingdom_name";
        kingdom.resources = new EnumMap<>(new KingdomResourcesDto(100, 10000, 20, 1000, 1000, 20000, 100, 100, 20).getResources());
        kingdom.buildings = new EnumMap<>(new KingdomBuildingsDto(10, 5, 5, 5, 5, 1, 1, 1, 0, 1, 0).getBuildings());
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
}
