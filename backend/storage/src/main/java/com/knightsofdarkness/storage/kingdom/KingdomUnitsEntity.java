package com.knightsofdarkness.storage.kingdom;

import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.knightsofdarkness.common.kingdom.KingdomUnitsDto;
import com.knightsofdarkness.common.kingdom.UnitName;
import com.knightsofdarkness.game.kingdom.KingdomUnits;

import jakarta.persistence.Embeddable;

@Embeddable
public class KingdomUnitsEntity {
    @JdbcTypeCode(SqlTypes.JSON)
    public Map<UnitName, Integer> availableUnits;

    @JdbcTypeCode(SqlTypes.JSON)
    public Map<UnitName, Integer> mobileUnits;

    public KingdomUnitsEntity()
    {
    }

    public KingdomUnitsEntity(Map<UnitName, Integer> availableUnits, Map<UnitName, Integer> mobileUnits)
    {
        this.availableUnits = availableUnits;
        this.mobileUnits = mobileUnits;
    }

    public KingdomUnitsDto toDto()
    {
        return new KingdomUnitsDto(availableUnits, mobileUnits);
    }

    public static KingdomUnitsEntity fromDto(KingdomUnitsDto dto)
    {
        return new KingdomUnitsEntity(dto.getAvailableUnits(), dto.getMobileUnits());
    }

    public KingdomUnits toDomainModel()
    {
        return new KingdomUnits(availableUnits, mobileUnits);
    }

    public static KingdomUnitsEntity fromDomainModel(KingdomUnits buildings)
    {
        return new KingdomUnitsEntity(buildings.getAvailableUnits(), buildings.getMobileUnits());
    }
}
