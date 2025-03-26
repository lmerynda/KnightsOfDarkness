package com.knightsofdarkness.web.common;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.EnumMap;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class EnumMapGsonAdapter<K extends Enum<K>, V> implements JsonDeserializer<EnumMap<K, V>>, JsonSerializer<EnumMap<K, V>> {
    private final Class<K> keyType;

    public EnumMapGsonAdapter(Class<K> keyType)
    {
        this.keyType = keyType;
    }

    @Override
    public JsonElement serialize(EnumMap<K, V> src, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonObject jsonObject = new JsonObject();
        for (K key : src.keySet())
        {
            jsonObject.add(key.name(), context.serialize(src.get(key)));
        }
        return jsonObject;
    }

    @Override
    public EnumMap<K, V> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        EnumMap<K, V> map = new EnumMap<>(keyType);
        JsonObject jsonObject = json.getAsJsonObject();
        for (String key : jsonObject.keySet())
        {
            K enumKey = Enum.valueOf(keyType, key);
            V value = context.deserialize(jsonObject.get(key), ((ParameterizedType) typeOfT).getActualTypeArguments()[1]);
            map.put(enumKey, value);
        }
        return map;
    }
}
