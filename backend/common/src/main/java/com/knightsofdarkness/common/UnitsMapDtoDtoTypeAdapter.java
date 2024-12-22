package com.knightsofdarkness.common;

import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.Map;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.knightsofdarkness.common.kingdom.UnitName;
import com.knightsofdarkness.common.kingdom.UnitsMapDto;

public class UnitsMapDtoDtoTypeAdapter implements JsonSerializer<UnitsMapDto>, JsonDeserializer<UnitsMapDto> {
    @Override
    public JsonElement serialize(UnitsMapDto src, Type typeOfSrc, JsonSerializationContext context)
    {
        return context.serialize(src.getUnits());
    }

    @Override
    public UnitsMapDto deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
    {
        Type enumMapType = new TypeToken<EnumMap<UnitName, Integer>>() {
        }.getType();
        Map<UnitName, Integer> deserialized = context.deserialize(json, enumMapType);
        return new UnitsMapDto(deserialized);
    }
}
