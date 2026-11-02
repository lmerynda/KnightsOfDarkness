package com.knightsofdarkness.web.user;

/**
 * what a ruler who started with only a kingdom name needs to come back: the placeholder address and password the browser keeps
 */
public record TrialAccountDto(String email, String password, String kingdomName) {
}
