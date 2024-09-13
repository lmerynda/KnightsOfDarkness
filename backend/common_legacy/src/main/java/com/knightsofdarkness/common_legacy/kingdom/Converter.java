package com.knightsofdarkness.common_legacy.kingdom;

import java.util.ArrayList;

import com.knightsofdarkness.common.kingdom.KingdomTurnReport;
import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.kingdom.Kingdom;

public final class Converter {
    private Converter()
    {
    }

    public static Kingdom newKingdomFromDto(KingdomDto dto, GameConfig config)
    {
        // TODO new kingdom doesn't have turn reports or special buildings, this is domain level information and should be there
        dto.lastTurnReport = new KingdomTurnReport();
        dto.specialBuildings = new ArrayList<>();
        return new Kingdom(dto.name, config, dto.resources.toDomain(), dto.buildings.toDomain(), new ArrayList<>(), dto.units.toDomain(), dto.lastTurnReport);
    }
}
