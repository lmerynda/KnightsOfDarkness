package com.knightsofdarkness.web.alliance.model;

import java.time.Instant;
import java.util.UUID;

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

    public AllianceInvitationEntity(Instant invitedAt, KingdomEntity kingdom, AllianceEntity alliance)
    {
        this.invitedAt = invitedAt;
        this.kingdom = kingdom;
        this.alliance = alliance;
    }
}
