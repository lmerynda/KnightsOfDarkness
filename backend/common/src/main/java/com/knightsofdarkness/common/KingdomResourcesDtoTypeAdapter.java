package com.knightsofdarkness.common;

import java.lang.reflect.Type;
import java.util.EnumMap;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.knightsofdarkness.common.kingdom.KingdomResourcesDto;
import com.knightsofdarkness.common.kingdom.ResourceName;

public class KingdomResourcesDtoTypeAdapter implements JsonSerializer<KingdomResourcesDto>, JsonDeserializer<KingdomResourcesDto> {
    @Override
    public JsonElement serialize(KingdomResourcesDto src, Type typeOfSrc, JsonSerializationContext context)
    {
        return context.serialize(src.getResources());
    }

    @Override
    public KingdomResourcesDto deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
    {
        Type enumMapType = new TypeToken<EnumMap<ResourceName, Integer>>() {
        }.getType();
        return new KingdomResourcesDto(context.deserialize(json, enumMapType));
    }
}
