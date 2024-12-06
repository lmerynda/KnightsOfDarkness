package com.knightsofdarkness.common;

import java.util.Optional;

import java.util.EnumMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.knightsofdarkness.common.kingdom.BuildingName;
import com.knightsofdarkness.common.kingdom.KingdomBuildingsDto;
import com.knightsofdarkness.common.kingdom.KingdomDetailsDto;
import com.knightsofdarkness.common.kingdom.KingdomDto;
import com.knightsofdarkness.common.kingdom.KingdomResourcesDto;
import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.common.kingdom.SpecialBuildingType;
import com.knightsofdarkness.common.kingdom.UnitName;
import com.knightsofdarkness.common.market.MarketResource;

public class GsonFactory {

    private static final GsonBuilder gsonBuilder = new GsonBuilder();

    static
    {
        gsonBuilder
                .registerTypeAdapter(KingdomBuildingsDto.class, new KingdomBuildingsDtoTypeAdapter())
                .registerTypeAdapter(KingdomResourcesDto.class, new KingdomResourcesDtoTypeAdapter())
                .registerTypeAdapter(KingdomDetailsDto.class, new KingdomDetailsDtoTypeAdapter())
                .registerTypeAdapter(KingdomDto.class, new KingdomDtoAdapter())
                .registerTypeAdapter(Optional.class, new OptionalTypeAdapter())
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
                }.getType(), new EnumMapGsonAdapter<>(BuildingName.class));
    }

    public static Gson createGson()
    {
        return gsonBuilder.create();
    }

    public static Gson createPrettyPrintingGson()
    {
        return gsonBuilder.setPrettyPrinting().create();
    }
}
