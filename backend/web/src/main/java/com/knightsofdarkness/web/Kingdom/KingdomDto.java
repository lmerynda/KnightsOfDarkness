package com.knightsofdarkness.web.Kingdom;

import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.market.IMarket;

public class KingdomDto {
    public String name;

    public KingdomDto()
    {
    }

    public KingdomDto(String name)
    {
        this.name = name;
    }

    public Kingdom toDomain(GameConfig config, IMarket market)
    {
        return new Kingdom(name, config, market, null, null, null);
    }

    public static KingdomDto fromDomain(Kingdom kingdom)
    {
        return new KingdomDto(kingdom.getName());
    }

    public String toString()
    {
        return "KingdomDto{" +
                "name=" + name +
                '}';
    }
}
