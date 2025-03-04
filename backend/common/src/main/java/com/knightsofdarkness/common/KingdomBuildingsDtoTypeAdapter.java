package com.knightsofdarkness.common;

import java.lang.reflect.Type;
import java.util.EnumMap;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.knightsofdarkness.common.kingdom.BuildingName;
import com.knightsofdarkness.common.kingdom.KingdomBuildingsDto;

public class KingdomBuildingsDtoTypeAdapter implements JsonSerializer<KingdomBuildingsDto>, JsonDeserializer<KingdomBuildingsDto> {
    @Override
    public JsonElement serialize(KingdomBuildingsDto src, Type typeOfSrc, JsonSerializationContext context)
    {
        return context.serialize(src.getBuildings());
    }

    @Override
    public KingdomBuildingsDto deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
    {
        Type enumMapType = new TypeToken<EnumMap<BuildingName, Integer>>() {
        }.getType();
        return new KingdomBuildingsDto(context.deserialize(json, enumMapType));
    }
}
