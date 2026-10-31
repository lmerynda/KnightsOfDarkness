package com.knightsofdarkness.web.game.config;

import java.util.Map;

import com.knightsofdarkness.web.common.kingdom.UnitName;

public record Attack(
        int turnsToArrive,
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
