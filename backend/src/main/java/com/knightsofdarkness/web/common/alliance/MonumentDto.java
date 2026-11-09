package com.knightsofdarkness.web.common.alliance;

/**
 * what everyone sees of a monument, its garrison only to two significant figures
 */
public record MonumentDto(String allianceName, String seat, double stages, int totalStages, boolean complete, int garrison) {
}
