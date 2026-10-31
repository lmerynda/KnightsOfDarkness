package com.knightsofdarkness.web.common.alliance;

/**
 * how strong an ally stands: its land and its defense at home, allied garrisons included
 */
public record AllianceMemberStrengthDto(String kingdomName, int land, double defense) {
}
