package com.knightsofdarkness.web.alliance.model;

import java.time.Instant;
import java.util.UUID;

import com.knightsofdarkness.common.alliance.AllianceInvitationDto;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class AllianceInvitationEntity {
    @Id
    UUID id;

    Instant invitedAt;

    @ManyToOne
    @JoinColumn(name = "kingdom", nullable = false)
    KingdomEntity kingdom;

    @ManyToOne
    @JoinColumn(name = "alliance", nullable = false)
    AllianceEntity alliance;

    public AllianceInvitationEntity()
    {

    }

    public AllianceInvitationEntity(UUID id, Instant invitedAt, KingdomEntity kingdom, AllianceEntity alliance)
    {
        this.id = id;
        this.invitedAt = invitedAt;
        this.kingdom = kingdom;
        this.alliance = alliance;
    }

    public UUID getId()
    {
        return id;
    }

    public AllianceEntity getAlliance()
    {
        return alliance;
    }

    public KingdomEntity getKingdom()
    {
        return kingdom;
    }

    public AllianceInvitationDto toDto()
    {
        return new AllianceInvitationDto(id, kingdom.getName(), alliance.getName());
    }
}
