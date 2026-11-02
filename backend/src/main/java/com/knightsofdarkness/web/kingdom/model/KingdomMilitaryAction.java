package com.knightsofdarkness.web.kingdom.model;

import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.kingdom.SendAttackDto;
import com.knightsofdarkness.web.common.kingdom.SendAttackResult;
import com.knightsofdarkness.web.common.kingdom.SendReinforcementsDto;
import com.knightsofdarkness.web.common.kingdom.SendReinforcementsResult;
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
        return sendAttack(sendAttackDto, target, null);
    }

    /**
     * lead is the ally's army on the march that ours joins, or null to march alone
     */
    public SendAttackResult sendAttack(SendAttackDto sendAttackDto, KingdomEntity target, KingdomOngoingAttackEntity lead)
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

        var joinError = lead == null ? null : validateJoin(lead, target);
        if (joinError != null)
        {
            return SendAttackResult.failure(joinError);
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
        if (lead != null)
        {
            newAttack.join(lead);
        }
        kingdom.getOngoingAttacks().add(newAttack);
        // a kingdom that marches gives up its own protection, it cannot strike and hide
        kingdom.setProtectionTicks(0);

        return SendAttackResult.success("Attack sent", sendAttackDto);
    }

    private String validateJoin(KingdomOngoingAttackEntity lead, KingdomEntity target)
    {
        if (!lead.getTargetKingdomName().equals(target.getName()))
        {
            return Utils.format("That army marches on {}, not {}", lead.getTargetKingdomName(), target.getName());
        }
        var leader = lead.getKingdom();
        if (leader.getName().equals(kingdom.getName()))
        {
            return "Join an ally's strike, not your own";
        }
        var ownAlliance = kingdom.getAlliance();
        var leaderAlliance = leader.getAlliance();
        if (ownAlliance.isEmpty() || leaderAlliance.isEmpty() || !ownAlliance.get().getName().equals(leaderAlliance.get().getName()))
        {
            return "Only allies' strikes can be joined";
        }
        return null;
    }

    private String validateTarget(KingdomEntity target)
    {
        if (target.getName().equals(kingdom.getName()))
        {
            return "You cannot attack your own kingdom";
        }

        if (target.getProtectionTicks() > 0)
        {
            return Utils.format("{} is protected after a recent defeat, for {} more turns", target.getName(), target.getProtectionTicks());
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

    /**
     * soldiers leave home to stand guard in an ally's kingdom; they arrive after a few of our turns and stay ours
     */
    public SendReinforcementsResult sendReinforcements(SendReinforcementsDto order, KingdomEntity host)
    {
        if (host.getName().equals(kingdom.getName()))
        {
            return SendReinforcementsResult.failure("Your soldiers already stand guard at home");
        }
        var ownAlliance = kingdom.getAlliance();
        var hostAlliance = host.getAlliance();
        if (ownAlliance.isEmpty() || hostAlliance.isEmpty() || !ownAlliance.get().getName().equals(hostAlliance.get().getName()))
        {
            return SendReinforcementsResult.failure("Soldiers stand guard only with allies");
        }

        var army = new UnitsMapDto();
        for (var unit : UnitName.getMilitaryUnits())
        {
            var count = order.units().getCount(unit);
            if (count < 0)
            {
                return SendReinforcementsResult.failure(Utils.format("Cannot send a negative number of {}", unit));
            }
            if (count > kingdom.getUnits().getAvailableCount(unit))
            {
                return SendReinforcementsResult.failure(Utils.format("Not enough {} at home", unit));
            }
            army.setCount(unit, count);
        }
        if (army.countMilitary() <= 0)
        {
            return SendReinforcementsResult.failure("Send at least one soldier");
        }

        for (var unit : UnitName.getMilitaryUnits())
        {
            kingdom.getUnits().moveAvailableToMobile(unit, army.getCount(unit));
        }
        var reinforcement = new KingdomReinforcementEntity(Id.generate(), kingdom, host, gameConfig.common().turnsToReinforce(), army);
        kingdom.getReinforcements().add(reinforcement);
        host.getGarrisons().add(reinforcement);
        return SendReinforcementsResult.success(Utils.format("{} soldiers march to stand guard in {}", army.countMilitary(), host.getName()));
    }

    /**
     * the soldiers come home at once
     */
    public void recallReinforcements(KingdomReinforcementEntity reinforcement)
    {
        reinforcement.goHome();
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
