package com.knightsofdarkness.storage.kingdom;

import com.knightsofdarkness.common.kingdom.KingdomUnitsDto;
import com.knightsofdarkness.game.kingdom.KingdomUnits;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class KingdomUnitsEntity {
    // TODO make sure the definition is json for production db if it supports json
    @Column(name = "units", columnDefinition = "TEXT")
    private String unitsJson;

    public KingdomUnitsEntity()
    {
    }

    public KingdomUnitsEntity(String unitsJson)
    {
        this.unitsJson = unitsJson;
    }

    public KingdomUnitsDto toDto()
    {
        return KingdomUnitsDto.fromJson(unitsJson);
    }

    public static KingdomUnitsEntity fromDto(KingdomUnitsDto dto)
    {
        return new KingdomUnitsEntity(dto.toJson());
    }

    public KingdomUnits toDomainModel()
    {
        var dto = toDto();
        return new KingdomUnits(dto.getAvailableUnits(), dto.getMobileUnits());
    }

    public static KingdomUnitsEntity fromDomainModel(KingdomUnits units)
    {
        var dto = new KingdomUnitsDto(units.getAvailableUnits(), units.getMobileUnits());
        return new KingdomUnitsEntity(dto.toJson());
    }
}
