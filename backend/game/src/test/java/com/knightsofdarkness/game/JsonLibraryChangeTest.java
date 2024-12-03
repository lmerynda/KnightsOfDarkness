package com.knightsofdarkness.game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Type;
import java.util.EnumMap;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

class JsonLibraryChangeTest {
    @Test
    void testEnumMapSerialization()
    {
        Gson gson = new Gson();

        var data = new EnumMap<DataName, Integer>(DataName.class);
        for (DataName name : DataName.values())
        {
            data.put(name, 0);
        }
        var serialized = gson.toJson(data);
        // assertEquals("example data", serialized);

        Type type = new TypeToken<EnumMap<DataName, Integer>>() {
        }.getType();
        var deserialized = gson.fromJson(serialized, type);
        assertEquals("{height=0, weight=0, length=0, width=0}", deserialized.toString());
    }

    @Test
    void testClassWithEnumMapSerialization()
    {
        Gson gson = new GsonBuilder().registerTypeAdapter(EnumMap.class, new EnumMapGsonAdapter<>(DataName.class)).create();

        var data = new DataClass();

        var serialized = gson.toJson(data);
        // assertEquals("example data", serialized);

        var deserialized = gson.fromJson(serialized, DataClass.class);
        assertEquals("data={height=0, weight=0, length=0, width=0}", deserialized.toString());
    }
}
