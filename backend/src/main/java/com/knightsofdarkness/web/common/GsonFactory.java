package com.knightsofdarkness.web.common;

import java.util.Optional;

import java.time.Instant;
import java.util.EnumMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.knightsofdarkness.web.common.kingdom.BuildingName;
import com.knightsofdarkness.web.common.kingdom.KingdomBuildingsDto;
import com.knightsofdarkness.web.common.kingdom.KingdomDetailsDto;
import com.knightsofdarkness.web.common.kingdom.KingdomDto;
import com.knightsofdarkness.web.common.kingdom.KingdomResourcesDto;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.kingdom.SpecialBuildingType;
import com.knightsofdarkness.web.common.kingdom.UnitName;
import com.knightsofdarkness.web.common.kingdom.UnitsMapDto;
import com.knightsofdarkness.web.common.market.MarketResource;

public class GsonFactory {

    private static final GsonBuilder gsonBuilder = new GsonBuilder();

    static
    {
        gsonBuilder
                .registerTypeAdapter(UnitsMapDto.class, new UnitsMapDtoDtoTypeAdapter())
                .registerTypeAdapter(KingdomBuildingsDto.class, new KingdomBuildingsDtoTypeAdapter())
                .registerTypeAdapter(KingdomResourcesDto.class, new KingdomResourcesDtoTypeAdapter())
                .registerTypeAdapter(KingdomDetailsDto.class, new KingdomDetailsDtoTypeAdapter())
                .registerTypeAdapter(KingdomDto.class, new KingdomDtoAdapter())
                .registerTypeAdapter(Optional.class, new OptionalTypeAdapter())
                .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
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
