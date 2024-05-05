package com.knightsofdarkness.web.Kingdom;

import java.util.UUID;

import com.knightsofdarkness.game.kingdom.Kingdom;

public class KingdomDto {
    public UUID id;
    public String name;

    public KingdomDto()
    {
    }

    public KingdomDto(String name)
    {
        this.name = name;
    }

    public KingdomDto(UUID id, String name)
    {
        this.id = id;
        this.name = name;
    }

    public Kingdom toDomain()
    {
        // TODO, fix
        return new Kingdom(name, null, null, null, null);
    }

    public static KingdomDto fromDomain(Kingdom kingdom)
    {
        return new KingdomDto(kingdom.getId(), kingdom.getName());
    }

    public String toString()
    {
        return "MarketOfferDto{" +
                "id=" + id +
                ", name=" + name +
                '}';
    }
}
