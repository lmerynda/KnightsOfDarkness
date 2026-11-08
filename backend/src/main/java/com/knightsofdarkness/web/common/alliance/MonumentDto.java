package com.knightsofdarkness.web.common.alliance;

/**
 * what everyone sees of a monument
 */
public record MonumentDto(String allianceName, String seat, double stages, int totalStages, boolean complete) {
}
