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
        // how many more arrows a defending bowman with a place in a tower looses in the salvo, 1 is twice as many
        double towerFireBonus,
        // how much stronger a defending soldier sheltered in a castle stands: its defense, and the blows it takes to fall
        double castleSoldierBonus,
        double warehousePlunderProtectionPerLevel,
        double maxWarehousePlunderProtection,
        double maxGuardLossRatio,
        // the peace every kingdom starts with, so that everyone can raise an economy before the first army marches
        int protectionTicksAtStart,
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
