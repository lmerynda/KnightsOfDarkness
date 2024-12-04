package com.knightsofdarkness.web.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.knightsofdarkness.common.kingdom.KingdomBuildingsDto;
import com.knightsofdarkness.common.kingdom.KingdomDto;

public class KingdomSerializationTest {
    @Test
    void testKingdomSerialization()
    {
        Gson gson = new Gson();

        var kingdomDto = generateEmptyKingdom();

        var serialized = gson.toJson(kingdomDto);
        // assertEquals("example data", serialized);

        KingdomDto deserialized = gson.fromJson(emptyKingdomJson(), KingdomDto.class);
        assertEquals("example data", deserialized.toString());

    }

    private KingdomDto generateEmptyKingdom()
    {
        var kingdom = new KingdomDto();
        kingdom.name = "example";
        var buildings = new KingdomBuildingsDto(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
        kingdom.buildings = buildings;
        return kingdom;
    }

    private String emptyKingdomJson()
    {
        return """
                {"name":"example","resources":{"resources":{"land":0,"buildingPoints":0,"unemployed":0,"gold":0,"iron":0,"food":0,"tools":0,"weapons":0,"turns":0}},"buildings":{"buildings":{"house":1,"goldMine":1,"ironMine":1,"workshop":1,"farm":1,"market":1,"barracks":1,"guardHouse":1,"spyGuild":1,"tower":1,"castle":1}},"units":{"availableUnits":{"goldMiner":0,"ironMiner":0,"farmer":0,"blacksmith":0,"builder":0,"carrier":0,"guard":0,"spy":0,"infantry":0,"bowman":0,"cavalry":0},"mobileUnits":{"goldMiner":0,"ironMiner":0,"farmer":0,"blacksmith":0,"builder":0,"carrier":0,"guard":0,"spy":0,"infantry":0,"bowman":0,"cavalry":0}},"details":{"details":{"usedLand":0}},"marketOffers":[],"specialBuildings":[],"lastTurnReport":{"foodConsumed":0,"resourcesProduced":{},"arrivingPeople":0,"exiledPeople":0,"kingdomSizeProductionBonus":0.0,"nourishmentProductionFactor":1.0,"specialBuildingBonus":{},"professionalsLeaving":{}},"carriersOnTheMove":[]}
                """;
    }
}
