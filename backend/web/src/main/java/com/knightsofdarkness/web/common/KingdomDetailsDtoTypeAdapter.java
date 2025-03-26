package com.knightsofdarkness.web.common;

import java.lang.reflect.Type;
import java.util.EnumMap;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.knightsofdarkness.web.common.kingdom.KingdomDetailName;
import com.knightsofdarkness.web.common.kingdom.KingdomDetailsDto;

public class KingdomDetailsDtoTypeAdapter implements JsonSerializer<KingdomDetailsDto>, JsonDeserializer<KingdomDetailsDto> {
    @Override
    public JsonElement serialize(KingdomDetailsDto src, Type typeOfSrc, JsonSerializationContext context)
    {
        return context.serialize(src.getDetails());
    }

    @Override
    public KingdomDetailsDto deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
    {
        Type enumMapType = new TypeToken<EnumMap<KingdomDetailName, Integer>>() {
        }.getType();
        return new KingdomDetailsDto(context.deserialize(json, enumMapType));
    }
}
