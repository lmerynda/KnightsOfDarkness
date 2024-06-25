package com.knightsofdarkness.common;

import com.knightsofdarkness.game.kingdom.BuildingName;
import com.knightsofdarkness.game.kingdom.KingdomBuildings;

public class KingdomBuildingsDto {
    public int houses;
    public int goldMines;
    public int ironMines;
    public int workshops;
    public int farms;
    public int markets;
    public int barracks;
    public int guardHouses;
    public int spyGuilds;
    public int towers;
    public int castles;

    public KingdomBuildingsDto()
    {
    }

    public KingdomBuildingsDto(int houses, int goldMines, int ironMines, int workshops, int farms, int markets, int barracks, int guardHouses, int spyGuilds, int towers, int castles)
    {
        this.houses = houses;
        this.goldMines = goldMines;
        this.ironMines = ironMines;
        this.workshops = workshops;
        this.farms = farms;
        this.markets = markets;
        this.barracks = barracks;
        this.guardHouses = guardHouses;
        this.spyGuilds = spyGuilds;
        this.towers = towers;
        this.castles = castles;
    }

    public KingdomBuildings toDomain()
    {
        var kingdomBuildings = new KingdomBuildings();
        kingdomBuildings.setCount(BuildingName.house, houses);
        kingdomBuildings.setCount(BuildingName.goldMine, goldMines);
        kingdomBuildings.setCount(BuildingName.ironMine, ironMines);
        kingdomBuildings.setCount(BuildingName.workshop, workshops);
        kingdomBuildings.setCount(BuildingName.farm, farms);
        kingdomBuildings.setCount(BuildingName.market, markets);
        kingdomBuildings.setCount(BuildingName.barracks, barracks);
        kingdomBuildings.setCount(BuildingName.guardHouse, guardHouses);
        kingdomBuildings.setCount(BuildingName.spyGuild, spyGuilds);
        kingdomBuildings.setCount(BuildingName.tower, towers);
        kingdomBuildings.setCount(BuildingName.castle, castles);
        return kingdomBuildings;
    }

    public static KingdomBuildingsDto fromDomain(KingdomBuildings kingdomBuildings)
    {
        return new KingdomBuildingsDto(
                kingdomBuildings.getCount(BuildingName.house),
                kingdomBuildings.getCount(BuildingName.goldMine),
                kingdomBuildings.getCount(BuildingName.ironMine),
                kingdomBuildings.getCount(BuildingName.workshop),
                kingdomBuildings.getCount(BuildingName.farm),
                kingdomBuildings.getCount(BuildingName.market),
                kingdomBuildings.getCount(BuildingName.barracks),
                kingdomBuildings.getCount(BuildingName.guardHouse),
                kingdomBuildings.getCount(BuildingName.spyGuild),
                kingdomBuildings.getCount(BuildingName.tower),
                kingdomBuildings.getCount(BuildingName.castle));
    }

    public String toString()
    {
        return "KingdomBuildingsDto{" +
                "houses=" + houses +
                ", goldMines=" + goldMines +
                ", ironMines=" + ironMines +
                ", workshops=" + workshops +
                ", farms=" + farms +
                ", markets=" + markets +
                ", barracks=" + barracks +
                ", guardHouses=" + guardHouses +
                ", spyGuilds=" + spyGuilds +
                ", towers=" + towers +
                ", castles=" + castles +
                '}';
    }
}
