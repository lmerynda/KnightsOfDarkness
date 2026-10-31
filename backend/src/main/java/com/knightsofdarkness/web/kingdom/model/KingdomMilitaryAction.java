package com.knightsofdarkness.web.kingdom.model;

import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.kingdom.SendAttackDto;
import com.knightsofdarkness.web.common.kingdom.SendAttackResult;
import com.knightsofdarkness.web.common.kingdom.UnitName;
import com.knightsofdarkness.web.common.kingdom.UnitsMapDto;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.utils.Id;
import com.knightsofdarkness.web.utils.Utils;

public class KingdomMilitaryAction {
    private final KingdomEntity kingdom;
    private final GameConfig gameConfig;

    public KingdomMilitaryAction(KingdomEntity kingdom, GameConfig gameConfig)
    {
        this.kingdom = kingdom;
        this.gameConfig = gameConfig;
    }

    public SendAttackResult sendAttack(SendAttackDto sendAttackDto, KingdomEntity target)
    {
        if (sendAttackDto.attackType() == null)
        {
            return SendAttackResult.failure("Choose an attack type");
        }

        var targetError = validateTarget(target);
        if (targetError != null)
        {
            return SendAttackResult.failure(targetError);
        }

        var kingdomUnits = kingdom.getUnits();
        for (var unit : UnitName.getMilitaryUnits())
        {
            var availableCount = kingdomUnits.getAvailableCount(unit);
            var neededCount = sendAttackDto.units().getCount(unit);
            if (neededCount < 0)
            {
                return SendAttackResult.failure(Utils.format("Cannot send a negative number of {}", unit));
            }
            if (availableCount < neededCount)
            {
                return SendAttackResult.failure(Utils.format("Not enough {} to send attack, available: {}, needed {}", unit, availableCount, neededCount));
            }
        }

        if (sendAttackDto.units().countMilitary() <= 0)
        {
            return SendAttackResult.failure("Need to send at least one unit");
        }

        var army = new UnitsMapDto();
        for (var unit : UnitName.getMilitaryUnits())
        {
            army.setCount(unit, sendAttackDto.units().getCount(unit));
            kingdomUnits.moveAvailableToMobile(unit, army.getCount(unit));
        }

        var turnsLeft = gameConfig.attack().turnsToArrive();
        var newAttack = new KingdomOngoingAttackEntity(Id.generate(), kingdom, target.getName(), turnsLeft, sendAttackDto.attackType(), army);
        kingdom.getOngoingAttacks().add(newAttack);

        return SendAttackResult.success("Attack sent", sendAttackDto);
    }

    private String validateTarget(KingdomEntity target)
    {
        if (target.getName().equals(kingdom.getName()))
        {
            return "You cannot attack your own kingdom";
        }

        var ownAlliance = kingdom.getAlliance();
        var targetAlliance = target.getAlliance();
        if (ownAlliance.isPresent() && targetAlliance.isPresent() && ownAlliance.get().getName().equals(targetAlliance.get().getName()))
        {
            return "You cannot attack a member of your own alliance";
        }

        var ownLand = kingdom.getResources().getCount(ResourceName.land);
        var targetLand = target.getResources().getCount(ResourceName.land);
        if (targetLand < ownLand * gameConfig.attack().minTargetLandRatio())
        {
            return Utils.format("{} is too small to attack, a target needs at least {}% of your land", target.getName(), Math.round(gameConfig.attack().minTargetLandRatio() * 100));
        }

        return null;
    }

    public void withdrawAttack(KingdomOngoingAttackEntity ongoingAttack)
    {
        kingdom.getOngoingAttacks().remove(ongoingAttack);
        for (var unit : UnitName.getMilitaryUnits())
        {
            kingdom.getUnits().moveMobileToAvailable(unit, ongoingAttack.units.getCount(unit));
        }
    }
}
