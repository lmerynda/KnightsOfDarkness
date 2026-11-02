package com.knightsofdarkness.web.game.config;

import java.util.Map;

import com.knightsofdarkness.web.common.kingdom.UnitName;

public record Attack(
        int turnsToArrive,
        // how many of its owner's turns an army that joined an ally's strike waits at the border before it fights alone
        int turnsToWaitAtBorder,
        double minTargetLandRatio,
        double maxLandCaptureRatio,
        double maxPlunderRatio,
        double towerDefenseBonusPerTower,
        double maxTowerDefenseBonus,
        double castleDefenseBonusPerCastle,
        double warehousePlunderProtectionPerLevel,
        double maxWarehousePlunderProtection,
        double maxGuardLossRatio,
        int protectionTicksAfterDefeat,
        double smallerTargetCaptureExponent,
        Map<UnitName, Double> unitAttackPower,
        Map<UnitName, Double> unitDefensePower) {

    public double getAttackPower(UnitName unit)
    {
        return unitAttackPower.getOrDefault(unit, 0.0);
    }

    public double getDefensePower(UnitName unit)
    {
        return unitDefensePower.getOrDefault(unit, 0.0);
    }
}
