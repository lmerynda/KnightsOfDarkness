package com.knightsofdarkness.web.common.war;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.knightsofdarkness.web.common.kingdom.BuildingName;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.kingdom.UnitName;

/**
 * Intel maps are empty when the mission failed.
 */
public record SpyReportDto(
        UUID id,
        Instant date,
        String kingdomName,
        String targetKingdomName,
        boolean success,
        int spiesSent,
        int spiesLost,
        Map<ResourceName, Integer> resources,
        Map<BuildingName, Integer> buildings,
        Map<UnitName, Integer> units) {
}
