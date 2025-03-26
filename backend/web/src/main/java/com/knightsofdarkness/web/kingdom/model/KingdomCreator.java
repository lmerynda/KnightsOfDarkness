package com.knightsofdarkness.web.kingdom.model;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knightsofdarkness.web.common.kingdom.KingdomTurnReport;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.IKingdomRepository;
import com.knightsofdarkness.web.kingdom.KingdomService;

public class KingdomCreator {
    private final Logger log = LoggerFactory.getLogger(KingdomService.class);
    private final IKingdomRepository kingdomRepository;
    private final GameConfig gameConfig;

    public KingdomCreator(IKingdomRepository kingdomRepository2, GameConfig gameConfig)
    {
        this.kingdomRepository = kingdomRepository2;
        this.gameConfig = gameConfig;
    }

    public KingdomEntity createKingdom(String name)
    {
        log.info("Creating new kingdom with name {}", name);

        var startConfiguration = gameConfig.kingdomStartConfiguration();
        var startingUnitsDto = startConfiguration.units().toDto();
        var kingdom = new KingdomEntity(
                name,
                new KingdomResourcesEntity(startConfiguration.resources().toMap()),
                new KingdomBuildingsEntity(startConfiguration.buildings().toMap()),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new KingdomUnitsEntity(startingUnitsDto.getAvailableUnits().getUnits(), startingUnitsDto.getMobileUnits().getUnits()),
                new KingdomTurnReport());

        kingdomRepository.add(kingdom);
        return kingdom;
    }
}
