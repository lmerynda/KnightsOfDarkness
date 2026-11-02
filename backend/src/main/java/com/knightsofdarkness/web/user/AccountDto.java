package com.knightsofdarkness.web.user;

/**
 * who is signed in; a trial account lives only in the browser that started it until the ruler keeps the kingdom
 */
public record AccountDto(String email, String kingdomName, boolean trial) {
}
