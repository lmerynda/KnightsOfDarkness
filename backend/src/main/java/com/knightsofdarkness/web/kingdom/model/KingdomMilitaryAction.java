package com.knightsofdarkness.web.kingdom.model;

import com.knightsofdarkness.web.common.kingdom.AttackType;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.kingdom.SendAttackDto;
import com.knightsofdarkness.web.common.ActionResult;
import com.knightsofdarkness.web.common.kingdom.SendReinforcementsDto;
import com.knightsofdarkness.web.common.kingdom.UnitName;
import com.knightsofdarkness.web.common.kingdom.UnitsMapDto;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.monument.model.MonumentEntity;
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

    public ActionResult<SendAttackDto> sendAttack(SendAttackDto sendAttackDto, KingdomEntity target)
    {
        return sendAttack(sendAttackDto, target, null);
    }

    /**
     * lead is the ally's army on the march that ours joins, or null to march alone
     */
    public ActionResult<SendAttackDto> sendAttack(SendAttackDto sendAttackDto, KingdomEntity target, KingdomOngoingAttackEntity lead)
    {
        if (sendAttackDto.attackType() == null)
        {
            return ActionResult.failure("Choose an attack type");
        }

        var targetError = sendAttackDto.attackType() == AttackType.monument ? validateMonumentSeat(target) : validateTarget(target);
        if (targetError != null)
        {
            return ActionResult.failure(targetError);
        }

        var joinError = lead == null ? null : validateJoin(lead, target, sendAttackDto.attackType());
        if (joinError != null)
        {
            return ActionResult.failure(joinError);
        }

        var kingdomUnits = kingdom.getUnits();
        for (var unit : UnitName.getMilitaryUnits())
        {
            var availableCount = kingdomUnits.getAvailableCount(unit);
            var neededCount = sendAttackDto.units().getCount(unit);
            if (neededCount < 0)
            {
                return ActionResult.failure(Utils.format("Cannot send a negative number of {}", unit));
            }
            if (availableCount < neededCount)
            {
                return ActionResult.failure(Utils.format("Not enough {} to send attack, available: {}, needed {}", unit, availableCount, neededCount));
            }
        }

        if (sendAttackDto.units().countMilitary() <= 0)
        {
            return ActionResult.failure("Need to send at least one unit");
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

        return ActionResult.success("Attack sent", sendAttackDto);
    }

    private String validateJoin(KingdomOngoingAttackEntity lead, KingdomEntity target, AttackType attackType)
    {
        if (!lead.getTargetKingdomName().equals(target.getName()))
        {
            return Utils.format("That army marches on {}, not {}", lead.getTargetKingdomName(), target.getName());
        }
        if ((lead.attackType == AttackType.monument) != (attackType == AttackType.monument))
        {
            return lead.attackType == AttackType.monument ? "That army marches on the monument, not the kingdom" : "That army marches on the kingdom, not the monument";
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

    /**
     * a monument has no protection and no size, the only rule is that it is an enemy's
     */
    private String validateMonumentSeat(KingdomEntity seat)
    {
        var ownAlliance = kingdom.getAlliance();
        var seatAlliance = seat.getAlliance();
        if (ownAlliance.isPresent() && seatAlliance.isPresent() && ownAlliance.get().getName().equals(seatAlliance.get().getName()))
        {
            return "You cannot assault your own alliance's monument";
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
            return Utils.format("{} is protected for {} more turns", target.getName(), target.getProtectionTicks());
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
    public ActionResult<Void> sendReinforcements(SendReinforcementsDto order, KingdomEntity host)
    {
        if (host.getName().equals(kingdom.getName()))
        {
            return ActionResult.failure("Your soldiers already stand guard at home");
        }
        var ownAlliance = kingdom.getAlliance();
        var hostAlliance = host.getAlliance();
        if (ownAlliance.isEmpty() || hostAlliance.isEmpty() || !ownAlliance.get().getName().equals(hostAlliance.get().getName()))
        {
            return ActionResult.failure("Soldiers stand guard only with allies");
        }

        var problem = guardProblem(order.units());
        if (problem != null)
        {
            return ActionResult.failure(problem);
        }

        var army = leaveHome(order.units());
        var reinforcement = new KingdomReinforcementEntity(Id.generate(), kingdom, host, gameConfig.common().turnsToReinforce(), army);
        kingdom.getReinforcements().add(reinforcement);
        host.getGarrisons().add(reinforcement);
        return ActionResult.success(Utils.format("{} soldiers march to stand guard in {}", army.countMilitary(), host.getName()));
    }

    /**
     * soldiers leave home to stand guard at our alliance's monument, the only defense it has
     */
    public ActionResult<Void> sendToMonument(UnitsMapDto units, MonumentEntity monument)
    {
        var problem = guardProblem(units);
        if (problem != null)
        {
            return ActionResult.failure(problem);
        }

        var army = leaveHome(units);
        var reinforcement = new KingdomReinforcementEntity(Id.generate(), kingdom, monument, gameConfig.common().turnsToReinforce(), army);
        kingdom.getReinforcements().add(reinforcement);
        monument.getGarrison().add(reinforcement);
        return ActionResult.success(Utils.format("{} soldiers march to stand guard at the monument at {}", army.countMilitary(), monument.getSeat()));
    }

    private String guardProblem(UnitsMapDto units)
    {
        for (var unit : UnitName.getMilitaryUnits())
        {
            var count = units.getCount(unit);
            if (count < 0)
            {
                return Utils.format("Cannot send a negative number of {}", unit);
            }
            if (count > kingdom.getUnits().getAvailableCount(unit))
            {
                return Utils.format("Not enough {} at home", unit);
            }
        }
        return units.countMilitary() <= 0 ? "Send at least one soldier" : null;
    }

    private UnitsMapDto leaveHome(UnitsMapDto units)
    {
        var army = new UnitsMapDto();
        for (var unit : UnitName.getMilitaryUnits())
        {
            army.setCount(unit, units.getCount(unit));
            kingdom.getUnits().moveAvailableToMobile(unit, army.getCount(unit));
        }
        return army;
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
