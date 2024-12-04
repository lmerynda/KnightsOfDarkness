package com.knightsofdarkness.storage;

import java.util.EnumMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.knightsofdarkness.common.EnumMapGsonAdapter;
import com.knightsofdarkness.common.kingdom.BuildingName;
import com.knightsofdarkness.common.kingdom.KingdomDetailName;
import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.common.kingdom.SpecialBuildingType;
import com.knightsofdarkness.common.kingdom.UnitName;
import com.knightsofdarkness.common.market.MarketResource;

public class GsonFactory {
    public static Gson createGson()
    {
        return new GsonBuilder()
                .registerTypeAdapter(new TypeToken<EnumMap<UnitName, String>>()
                {
                }.getType(), new EnumMapGsonAdapter<>(UnitName.class))
                .registerTypeAdapter(new TypeToken<EnumMap<ResourceName, Integer>>()
                {
                }.getType(), new EnumMapGsonAdapter<>(ResourceName.class))
                .registerTypeAdapter(new TypeToken<EnumMap<MarketResource, Integer>>()
                {
                }.getType(), new EnumMapGsonAdapter<>(MarketResource.class))
                .registerTypeAdapter(new TypeToken<EnumMap<SpecialBuildingType, Integer>>()
                {
                }.getType(), new EnumMapGsonAdapter<>(SpecialBuildingType.class))
                .registerTypeAdapter(new TypeToken<EnumMap<BuildingName, Integer>>()
                {
                }.getType(), new EnumMapGsonAdapter<>(BuildingName.class))
                .registerTypeAdapter(new TypeToken<EnumMap<KingdomDetailName, Integer>>()
                {
                }.getType(), new EnumMapGsonAdapter<>(KingdomDetailName.class))
                .setPrettyPrinting()
                .create();
    }
}
