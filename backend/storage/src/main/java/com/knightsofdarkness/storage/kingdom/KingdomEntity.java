package com.knightsofdarkness.storage.kingdom;

import java.util.List;

import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.storage.market.MarketOfferEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class KingdomEntity {

    @Id
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "kingdom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MarketOfferEntity> marketOffers;

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
