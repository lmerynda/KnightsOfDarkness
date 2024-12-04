package com.knightsofdarkness.common.kingdom;

// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.databind.JsonNode;
// import com.fasterxml.jackson.databind.ObjectMapper;

public class TestUtils {

    // public static String kingdomDtoToJson(KingdomDto kingdom)
    // {
    // ObjectMapper objectMapper = new ObjectMapper();
    // try
    // {
    // return objectMapper.writeValueAsString(kingdom);
    // } catch (Exception e)
    // {
    // e.printStackTrace();
    // System.out.println(e.getMessage());
    // throw new RuntimeException("Failed to convert kingdom to json");
    // }
    // }

    // public static KingdomDto jsonToKingdomDto(String kingdomJson)
    // {
    // ObjectMapper objectMapper = new ObjectMapper();
    // try
    // {
    // return objectMapper.readValue(kingdomJson, KingdomDto.class);
    // } catch (Exception e)
    // {
    // e.printStackTrace();
    // throw new RuntimeException("Failed to read kingdom from json");
    // }
    // }

    // public static String kingdomUnitsDtoToJson(KingdomUnitsDto kingdomUnits)
    // {
    // ObjectMapper objectMapper = new ObjectMapper();
    // try
    // {
    // return objectMapper.writeValueAsString(kingdomUnits);
    // } catch (Exception e)
    // {
    // e.printStackTrace();
    // System.out.println(e.getMessage());
    // throw new RuntimeException("Failed to convert kingdom units to json");
    // }
    // }

    // public static KingdomUnitsDto jsonToKingdomUnitsDto(String kingdomUnitsJson)
    // {
    // ObjectMapper objectMapper = new ObjectMapper();
    // try
    // {
    // return objectMapper.readValue(kingdomUnitsJson, KingdomUnitsDto.class);
    // } catch (Exception e)
    // {
    // e.printStackTrace();
    // throw new RuntimeException("Failed to read kingdom units from json");
    // }
    // }

    // public static String emptyKingdomUnitsJson()
    // {
    // var kingdomUnitsJson = """
    // {
    // "availableUnits": {
    // "goldMiner": 0,
    // "ironMiner": 0,
    // "farmer": 0,
    // "blacksmith": 0,
    // "builder": 0,
    // "carrier": 0,
    // "guard": 0,
    // "spy": 0,
    // "infantry": 0,
    // "bowman": 0,
    // "cavalry": 0
    // },
    // "mobileUnits": {
    // "goldMiner": 0,
    // "ironMiner": 0,
    // "farmer": 0,
    // "blacksmith": 0,
    // "builder": 0,
    // "carrier": 0,
    // "guard": 0,
    // "spy": 0,
    // "infantry": 0,
    // "bowman": 0,
    // "cavalry": 0
    // }
    // }
    // """;

    // ObjectMapper objectMapper = new ObjectMapper();
    // try
    // {
    // Object jsonObject = objectMapper.readValue(kingdomUnitsJson, Object.class);
    // return objectMapper.writeValueAsString(jsonObject);
    // } catch (JsonProcessingException e)
    // {
    // e.printStackTrace();
    // throw new RuntimeException("Failed to read empty units json");
    // }
    // }

    // public static String emptyKingdomJson()
    // {
    // var kingdomJson = """
    // {
    // "name": "unknown",
    // "resources": {
    // "land": 0,
    // "buildingPoints": 0,
    // "unemployed": 0,
    // "gold": 0,
    // "iron": 0,
    // "food": 0,
    // "tools": 0,
    // "weapons": 0,
    // "turns": 0
    // },
    // "buildings": {
    // "house": 0,
    // "goldMine": 0,
    // "ironMine": 0,
    // "workshop": 0,
    // "farm": 0,
    // "market": 0,
    // "barracks": 0,
    // "guardHouse": 0,
    // "spyGuild": 0,
    // "tower": 0,
    // "castle": 0
    // },
    // "units": {
    // "availableUnits": {
    // "goldMiner": 0,
    // "ironMiner": 0,
    // "farmer": 0,
    // "blacksmith": 0,
    // "builder": 0,
    // "carrier": 0,
    // "guard": 0,
    // "spy": 0,
    // "infantry": 0,
    // "bowman": 0,
    // "cavalry": 0
    // },
    // "mobileUnits": {
    // "goldMiner": 0,
    // "ironMiner": 0,
    // "farmer": 0,
    // "blacksmith": 0,
    // "builder": 0,
    // "carrier": 0,
    // "guard": 0,
    // "spy": 0,
    // "infantry": 0,
    // "bowman": 0,
    // "cavalry": 0
    // }
    // },
    // "details": {
    // "usedLand": 0
    // },
    // "marketOffers": [],
    // "specialBuildings": [],
    // "lastTurnReport": {
    // "foodConsumed": 0,
    // "resourcesProduced": {},
    // "arrivingPeople": 0,
    // "exiledPeople": 0,
    // "kingdomSizeProductionBonus": 0.0,
    // "nourishmentProductionFactor": 1.0,
    // "specialBuildingBonus": {},
    // "professionalsLeaving": {}
    // },
    // "carriersOnTheMove": []
    // }
    // """;

    // ObjectMapper objectMapper = new ObjectMapper();
    // try
    // {
    // Object jsonObject = objectMapper.readValue(kingdomJson, Object.class);
    // return objectMapper.writeValueAsString(jsonObject);
    // } catch (JsonProcessingException e)
    // {
    // e.printStackTrace();
    // throw new RuntimeException("Failed to read empty kingdom json");
    // }
    // }

    // public static boolean areJsonStringsEqual(String json1, String json2)
    // {
    // ObjectMapper objectMapper = new ObjectMapper();
    // try
    // {
    // JsonNode tree1 = objectMapper.readTree(json1);
    // JsonNode tree2 = objectMapper.readTree(json2);
    // return tree1.equals(tree2); // Structural comparison
    // } catch (Exception e)
    // {
    // throw new RuntimeException("Failed to compare JSON strings", e);
    // }
    // }
}
