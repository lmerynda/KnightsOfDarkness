package com.knightsofdarkness.web.security;

public class AuthResponseDto {
    public String token;

    public AuthResponseDto(String token)
    {
        this.token = token;
    }
}
