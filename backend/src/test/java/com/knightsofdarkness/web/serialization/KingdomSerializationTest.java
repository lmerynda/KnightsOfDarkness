package com.knightsofdarkness.web.serialization;

import java.util.EnumMap;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.knightsofdarkness.web.common.kingdom.KingdomBuildingsDto;
import com.knightsofdarkness.web.common.kingdom.KingdomDto;
import com.knightsofdarkness.web.common.kingdom.KingdomResourcesDto;
import com.knightsofdarkness.web.common.kingdom.KingdomUnitsDto;
import com.knightsofdarkness.web.common.kingdom.UnitsMapDto;

class KingdomSerializationTest
{
    @Test
    void testKingdomSerialization() throws Exception
    {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        // objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        var kingdomDto = generateDefaultKingdomDto();

        var serialized = objectMapper.writeValueAsString(kingdomDto);
        System.out.println(serialized);
        // assertEquals(generateDefaultKingdomDtoJson(), serialized);

        KingdomDto deserialized = objectMapper.readValue(generateDefaultKingdomDtoJson(), KingdomDto.class);
        System.out.println(deserialized);
        // assertEquals(generateDefaultKingdomDto().toString(), deserialized.toString());
    }

    private static final KingdomDto generateDefaultKingdomDto()
    {
        var kingdom = new KingdomDto();
        kingdom.name = "default_kingdom_name";
        kingdom.resources = new EnumMap<>(new KingdomResourcesDto(100, 10000, 20, 1000, 1000, 20000, 100, 100, 20).getResources());
        kingdom.buildings = new EnumMap<>(new KingdomBuildingsDto(10, 5, 5, 5, 5, 1, 1, 1, 0, 1, 0).getBuildings());
        kingdom.units = new KingdomUnitsDto(generateDefaultAvailableUnits().getUnits(), new UnitsMapDto().getUnits());
        return kingdom;
    }

    private static final UnitsMapDto generateDefaultAvailableUnits()
    {
        var units = new UnitsMapDto();
        units.setUnit("goldMiner", 5);
        units.setUnit("ironMiner", 5);
        units.setUnit("builder", 5);
        units.setUnit("blacksmith", 5);
        units.setUnit("farmer", 5);
        return units;
    }

    private static final String generateDefaultKingdomDtoJson()
    {
        return """
                {"name":"default_kingdom_name","resources":{"land":100,"buildingPoints":10000,"unemployed":20,"gold":1000,"iron":1000,"food":20000,"tools":100,"weapons":100,"turns":20},"buildings":{"house":10,"goldMine":5,"ironMine":5,"workshop":5,"farm":5,"market":1,"barracks":1,"guardHouse":1,"spyGuild":0,"tower":1,"castle":0},"units":{"availableUnits":{"goldMiner":5,"ironMiner":5,"farmer":5,"blacksmith":5,"builder":5,"carrier":0,"guard":0,"spy":0,"infantry":0,"bowman":0,"cavalry":0},"mobileUnits":{"goldMiner":0,"ironMiner":0,"farmer":0,"blacksmith":0,"builder":0,"carrier":0,"guard":0,"spy":0,"infantry":0,"bowman":0,"cavalry":0}},"details":{"usedLand":0},"marketOffers":[],"specialBuildings":[],"lastTurnReport":{"foodConsumed":0,"resourcesProduced":{},"arrivingPeople":0,"exiledPeople":0,"weaponsProductionPercentage":0,"kingdomSizeProductionBonus":0.0,"nourishmentProductionFactor":1.0,"specialBuildingBonus":{},"professionalsLeaving":{}},"carriersOnTheMove":[],"ongoingAttacks":[],"allianceName":null}
                """;
    }
}
