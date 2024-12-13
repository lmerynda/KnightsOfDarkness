package com.knightsofdarkness.common;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.knightsofdarkness.common.kingdom.CarriersOnTheMoveDto;
import com.knightsofdarkness.common.kingdom.KingdomBuildingsDto;
import com.knightsofdarkness.common.kingdom.KingdomDto;
import com.knightsofdarkness.common.kingdom.KingdomResourcesDto;
import com.knightsofdarkness.common.kingdom.KingdomSpecialBuildingDto;
import com.knightsofdarkness.common.kingdom.KingdomTurnReport;
import com.knightsofdarkness.common.kingdom.KingdomUnitsDto;
import com.knightsofdarkness.common.kingdom.OngoingAttackDto;
import com.knightsofdarkness.common.market.MarketOfferDto;

public class KingdomDtoAdapter implements JsonSerializer<KingdomDto>, JsonDeserializer<KingdomDto> {

    @Override
    public JsonElement serialize(KingdomDto src, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", src.name);
        jsonObject.add("resources", context.serialize(src.resources));
        jsonObject.add("buildings", context.serialize(src.buildings));
        jsonObject.add("units", context.serialize(src.units));
        jsonObject.add("details", context.serialize(src.details));
        jsonObject.add("marketOffers", context.serialize(src.marketOffers));
        jsonObject.add("specialBuildings", context.serialize(src.specialBuildings));
        jsonObject.add("lastTurnReport", context.serialize(src.lastTurnReport));
        jsonObject.add("carriersOnTheMove", context.serialize(src.carriersOnTheMove));
        jsonObject.add("ongoingAttacks", context.serialize(src.ongoingAttacks));
        return jsonObject;
    }

    @Override
    public KingdomDto deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject jsonObject = json.getAsJsonObject();
        String name = jsonObject.get("name").getAsString();
        KingdomResourcesDto resources = context.deserialize(jsonObject.get("resources"), KingdomResourcesDto.class);
        KingdomBuildingsDto buildings = context.deserialize(jsonObject.get("buildings"), KingdomBuildingsDto.class);
        KingdomUnitsDto units = context.deserialize(jsonObject.get("units"), KingdomUnitsDto.class);
        List<MarketOfferDto> marketOffers = context.deserialize(jsonObject.get("marketOffers"), ArrayList.class);
        List<KingdomSpecialBuildingDto> specialBuildings = context.deserialize(jsonObject.get("specialBuildings"), ArrayList.class);
        KingdomTurnReport lastTurnReport = context.deserialize(jsonObject.get("lastTurnReport"), KingdomTurnReport.class);
        List<CarriersOnTheMoveDto> carriersOnTheMove = context.deserialize(jsonObject.get("carriersOnTheMove"), ArrayList.class);
        List<OngoingAttackDto> ongoingAttacks = context.deserialize(jsonObject.get("ongoingAttacks"), ArrayList.class);

        return new KingdomDto(name, resources, buildings, units, marketOffers, specialBuildings, lastTurnReport, carriersOnTheMove, ongoingAttacks);
    }
}
