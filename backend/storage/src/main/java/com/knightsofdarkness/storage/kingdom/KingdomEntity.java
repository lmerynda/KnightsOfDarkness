package com.knightsofdarkness.storage.kingdom;

import com.knightsofdarkness.game.kingdom.Kingdom;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class KingdomEntity {

    @Id
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    public KingdomEntity()
    {
    }

    public KingdomEntity(String name)
    {
        this.name = name;
    }

    public Kingdom toDomainModel()
    {
        // TODO fix
        return new Kingdom(name, null, null, null, null, null);
    }

    public static KingdomEntity fromDomainModel(Kingdom kingdom)
    {
        return new KingdomEntity(kingdom.getName());
    }
}
