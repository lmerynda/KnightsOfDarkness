package com.knightsofdarkness.web;

import java.util.EnumMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;

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

@Configuration
public class GsonService {
    @Bean
    public Gson gson()
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

    @Bean
    public HttpMessageConverter<Object> gsonHttpMessageConverter(Gson gson)
    {
        GsonHttpMessageConverter converter = new GsonHttpMessageConverter();
        converter.setGson(gson);
        return converter;
    }
}
