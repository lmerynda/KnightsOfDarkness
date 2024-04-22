package com.knightsofdarkness.web;

import org.springdoc.core.SpringDocUtils;
// import org.springdoc.EnableOpenApi;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;

@Configuration
@OpenAPIDefinition
// @EnableOpenApi
class OpenApiConfig {
    // Fix: Add method declaration
    void init()
    {
        SpringDocUtils.getConfig().addRestControllers(MarketController.class);
    }
}