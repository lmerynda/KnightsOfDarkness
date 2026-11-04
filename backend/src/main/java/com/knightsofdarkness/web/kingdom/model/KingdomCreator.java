package com.knightsofdarkness.web.kingdom.model;

import java.util.ArrayList;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knightsofdarkness.web.common.kingdom.KingdomTurnReport;
import com.knightsofdarkness.web.game.config.GameConfig;

public class KingdomCreator
{
    private static final Logger log = LoggerFactory.getLogger(KingdomCreator.class);
    private static final Pattern namePattern = Pattern.compile("[\\p{L}0-9][\\p{L}0-9 _'-]{1,22}[\\p{L}0-9]");
    public static final String nameRule = "Kingdom name must be 3 to 24 letters, digits, spaces, dashes or apostrophes";
    private final GameConfig gameConfig;

    public KingdomCreator(GameConfig gameConfig)
    {
        this.gameConfig = gameConfig;
    }

    /** the same rule for every kingdom, founded by a player or added to an alliance as a bot */
    public static boolean isValidName(String name)
    {
        return name != null && namePattern.matcher(name).matches();
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
