package com.knightsofdarkness.web.common.war;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.knightsofdarkness.web.common.kingdom.AttackType;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.kingdom.UnitsMapDto;

public record BattleReportDto(
        UUID id,
        Instant date,
        String attacker,
        String defender,
        AttackType attackType,
        UnitsMapDto unitsSent,
        UnitsMapDto attackerLosses,
        UnitsMapDto defenderLosses,
        double attackPower,
        double defensePower,
        boolean attackerWon,
        int landCaptured,
        Map<ResourceName, Integer> plunder) {
}
