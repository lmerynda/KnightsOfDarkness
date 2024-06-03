package com.knightsofdarkness.storage.kingdom;

import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.storage.market.MarketOfferEntity;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class KingdomEntity {

    @Id
    @Column(name = "name", nullable = false, unique = true)
    String name;

    @OneToMany(mappedBy = "kingdom", cascade = CascadeType.ALL, orphanRemoval = true)
    List<MarketOfferEntity> marketOffers;

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
