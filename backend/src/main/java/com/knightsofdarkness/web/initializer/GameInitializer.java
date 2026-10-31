package com.knightsofdarkness.web.initializer;

import java.util.EnumMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
import com.knightsofdarkness.web.common.kingdom.BuildingName;
import com.knightsofdarkness.web.common.kingdom.SpecialBuildingType;
import com.knightsofdarkness.web.common.kingdom.UnitName;
import com.knightsofdarkness.web.common.kingdom.UnitsMapDto;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.IKingdomRepository;
import com.knightsofdarkness.web.kingdom.KingdomService;
import com.knightsofdarkness.web.kingdom.model.KingdomCreator;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
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
    private final GameConfig gameConfig;
    private final boolean seedDevAccounts;
    private final int numberOfSpecialBuildings = 5;

    public GameInitializer(KingdomService kingdomService, IKingdomRepository kingdomRepository, IBotRepository botRepository, IAllianceRepository allianceRepository, GameConfig gameConfig,
            @Value("${game.seed-dev-accounts:false}") boolean seedDevAccounts)
    {
        this.seedDevAccounts = seedDevAccounts;
        this.gameConfig = gameConfig;
        this.kingdomService = kingdomService;
        this.botRepository = botRepository;
        this.kingdomRepository = kingdomRepository;
        this.allianceRepository = allianceRepository;
    }

    @Override
    public void run(String... args)
    {

        if (seedDevAccounts)
        {
            createPlayerKingdom("uprzejmy");
            createPlayerKingdom("Umbar");
        }
        createBot("BlacksmithBot", SpecialBuildingType.forge);
        createBot("FarmerBot", SpecialBuildingType.granary);
        createBot("IronMinerBot", SpecialBuildingType.ironShaft);
        createBot("GoldMinerBot", SpecialBuildingType.goldShaft);
        log.info("Kingdoms initialized");
        if (seedDevAccounts)
        {
            createAlliance("Legion", "Umbar", "uprzejmy");
            log.info("Alliances initialized");
        }
    }

    @Transactional
    private void createAlliance(String allianceName, String emperor, String kingdom)
    {
        var kingdomEntity = kingdomRepository.getKingdomByName(kingdom).get();
        var emperorEntity = kingdomRepository.getKingdomByName(emperor).get();
        var alliance = new AllianceEntity(allianceName, kingdomEntity.getName());
        alliance.addKingdom(emperorEntity);
        alliance.addKingdom(kingdomEntity);
        // saving the alliance cascades to its member kingdoms
        allianceRepository.create(alliance);
    }

    @Transactional
    KingdomEntity createKingdom(String name)
    {
        log.info("Initializing kingdom {}", name);
        return kingdomService.createKingdom(name);
    }

    /**
     * development kingdoms get a small garrison so war and espionage can be tried right away
     */
    void createPlayerKingdom(String name)
    {
        log.info("Initializing kingdom {}", name);
        var kingdom = new KingdomCreator(gameConfig).createKingdom(name);
        kingdom.getBuildings().addCount(BuildingName.barracks, 1);
        kingdom.getBuildings().addCount(BuildingName.spyGuild, 1);
        kingdom.getUnits().addAvailableCount(UnitName.infantry, 40);
        kingdom.getUnits().addAvailableCount(UnitName.bowman, 40);
        kingdom.getUnits().addAvailableCount(UnitName.cavalry, 20);
        kingdom.getUnits().addAvailableCount(UnitName.spy, 10);
        kingdomRepository.add(kingdom);
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


}
