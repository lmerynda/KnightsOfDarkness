package com.knightsofdarkness.web.common;

import java.util.Optional;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class OptionalTypeAdapter implements JsonSerializer<Optional<?>>, JsonDeserializer<Optional<?>> {
    @Override
    public JsonElement serialize(Optional<?> src, Type typeOfSrc, JsonSerializationContext context)
    {
        return src.isPresent() ? context.serialize(src.get()) : JsonNull.INSTANCE;
    }

    @Override
    public Optional<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        if (json.isJsonNull())
        {
            return Optional.empty();
        } else
        {
            Type actualType = ((ParameterizedType) typeOfT).getActualTypeArguments()[0];
            return Optional.of(context.deserialize(json, actualType));
        }
    }
}
