package com.knightsofdarkness.web.Security;

public class AuthResponseDto {
    public String token;
    
    public AuthResponseDto(String token)
    {
        this.token = token;
    }
}
