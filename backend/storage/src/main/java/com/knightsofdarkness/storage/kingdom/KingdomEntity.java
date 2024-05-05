package com.knightsofdarkness.storage.kingdom;

import java.util.UUID;

import com.knightsofdarkness.game.kingdom.Kingdom;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class KingdomEntity {
    @Id
    private UUID id;

    private String name;

    public KingdomEntity()
    {
    }

    public KingdomEntity(UUID id, String name)
    {
        this.id = id;
        this.name = name;
    }

    public Kingdom toDomainModel()
    {
        // TODO fix
        return new Kingdom(name, null, null, null, null);
    }

    public static KingdomEntity fromDomainModel(Kingdom kingdom)
    {
        return new KingdomEntity(kingdom.getId(), kingdom.getName());
    }
}
