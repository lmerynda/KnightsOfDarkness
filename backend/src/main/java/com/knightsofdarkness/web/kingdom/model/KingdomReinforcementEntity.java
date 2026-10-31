package com.knightsofdarkness.web.kingdom.model;

import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.knightsofdarkness.web.common.kingdom.ReinforcementDto;
import com.knightsofdarkness.web.common.kingdom.UnitName;
import com.knightsofdarkness.web.common.kingdom.UnitsMapDto;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * Soldiers a kingdom sent to stand guard in an ally's kingdom. They stay the sender's: away from home, fed by the sender,
 * and back home when recalled or when either kingdom leaves the alliance. Once arrived they fight beside the host.
 */
@Entity
public class KingdomReinforcementEntity
{
    @Id
    UUID id;

    @ManyToOne
    @JoinColumn(name = "kingdom_name", nullable = false)
    KingdomEntity kingdom;

    @ManyToOne
    @JoinColumn(name = "host_name", nullable = false)
    KingdomEntity host;

    int turnsLeft;

    @JdbcTypeCode(SqlTypes.JSON)
    UnitsMapDto units;

    public KingdomReinforcementEntity()
    {
    }

    public KingdomReinforcementEntity(UUID id, KingdomEntity kingdom, KingdomEntity host, int turnsLeft, UnitsMapDto units)
    {
        this.id = id;
        this.kingdom = kingdom;
        this.host = host;
        this.turnsLeft = turnsLeft;
        this.units = units;
    }

    public ReinforcementDto toDto()
    {
        return new ReinforcementDto(id, kingdom.getName(), host.getName(), turnsLeft, units);
    }

    public UUID getId()
    {
        return id;
    }

    public KingdomEntity getKingdom()
    {
        return kingdom;
    }

    public KingdomEntity getHost()
    {
        return host;
    }

    public UnitsMapDto getUnits()
    {
        return units;
    }

    public boolean isStanding()
    {
        return turnsLeft <= 0;
    }

    /**
     * the soldiers are home at once and the garrison is gone from both kingdoms
     */
    void goHome()
    {
        for (var unit : UnitName.getMilitaryUnits())
        {
            kingdom.getUnits().moveMobileToAvailable(unit, units.getCount(unit));
        }
        kingdom.getReinforcements().remove(this);
        host.getGarrisons().remove(this);
    }

    /**
     * soldiers who fell defending the host, gone from the garrison and from the sender's soldiers away
     */
    void lose(UnitName unit, int count)
    {
        units.subtractCount(unit, count);
        kingdom.getUnits().subtractMobileCount(unit, count);
    }
}
