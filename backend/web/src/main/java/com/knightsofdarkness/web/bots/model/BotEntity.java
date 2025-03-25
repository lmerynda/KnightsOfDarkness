package com.knightsofdarkness.web.bots.model;

import java.util.UUID;

import com.knightsofdarkness.web.kingdom.model.KingdomEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class BotEntity {

    @Id
    UUID id;

    @OneToOne
    KingdomEntity kingdom;

    public BotEntity()
    {
    }

    public BotEntity(UUID uuid, KingdomEntity botKingdom)
    {
        id = uuid;
        kingdom = botKingdom;
    }
}
