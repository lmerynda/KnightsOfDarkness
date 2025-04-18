package com.knightsofdarkness.web.kingdom.model;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knightsofdarkness.web.common.kingdom.KingdomTurnReport;
import com.knightsofdarkness.web.game.config.GameConfig;

public class KingdomCreator
{
    private static final Logger log = LoggerFactory.getLogger(KingdomCreator.class);
    private final GameConfig gameConfig;

    public KingdomCreator(GameConfig gameConfig)
    {
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
                new KingdomUnitsEntity(startingUnitsDto.getAvailableUnits(), startingUnitsDto.getMobileUnits()),
                new KingdomTurnReport());

        return kingdom;
    }
}
