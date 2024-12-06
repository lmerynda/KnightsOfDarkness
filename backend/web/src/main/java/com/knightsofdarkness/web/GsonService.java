package com.knightsofdarkness.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;

import com.google.gson.Gson;
import com.knightsofdarkness.common.GsonFactory;

@Configuration
public class GsonService {
    @Bean
    public Gson gson()
    {
        return GsonFactory.createGson();
    }

    @Bean
    public HttpMessageConverter<Object> gsonHttpMessageConverter(Gson gson)
    {
        GsonHttpMessageConverter converter = new GsonHttpMessageConverter();
        converter.setGson(gson);
        return converter;
    }
}
