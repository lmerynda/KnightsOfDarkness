package com.knightsofdarkness.web.security;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

    private List<String> allowedOrigins;

    public List<String> getAllowedOrigins()
    {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins)
    {
        this.allowedOrigins = allowedOrigins;
    }
}