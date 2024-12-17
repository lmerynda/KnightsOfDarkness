package com.knightsofdarkness.game.kingdom;

import com.knightsofdarkness.common.kingdom.SendAttackDto;
import com.knightsofdarkness.common.kingdom.SendAttackResult;
import com.knightsofdarkness.common.kingdom.UnitName;
import com.knightsofdarkness.game.Id;
import com.knightsofdarkness.game.Utils;

public class KingdomMilitaryAction {
    private final Kingdom kingdom;

    public KingdomMilitaryAction(Kingdom kingdom)
    {
        this.kingdom = kingdom;
    }

    public SendAttackResult sendAttack(SendAttackDto sendAttackDto)
    {
        if (sendAttackDto.units().countAll() <= 0)
        {
            return SendAttackResult.failure("Need to send at least one unit");
        }

        var kingdomUnits = kingdom.getUnits();
        for (var unit : UnitName.getMilitaryUnits())
        {
            var availableCount = kingdomUnits.getAvailableCount(unit);
            var neededCount = sendAttackDto.units().getCount(unit);
            if (availableCount < neededCount)
            {
                return SendAttackResult.failure(Utils.format("Not enough {} to send attack, available: {}, needed {}", unit, availableCount, neededCount));
            }
        }

        for (var unit : UnitName.getMilitaryUnits())
        {
            kingdomUnits.moveAvailableToMobile(unit, sendAttackDto.units().getCount(unit));
        }

        // TODO make number turns for attack to arrive a game config
        var newAttack = new KingdomOngoingAttack(Id.generate(), sendAttackDto.destinationKingdomName(), 4, sendAttackDto.attackType(), sendAttackDto.units());
        kingdom.getOngoingAttacks().add(newAttack);

        return SendAttackResult.success("Attack sent", sendAttackDto);
    }

    public void withdrawAttack(KingdomOngoingAttack ongoingAttack)
    {
        kingdom.getOngoingAttacks().remove(ongoingAttack);
        for (var unit : UnitName.getMilitaryUnits())
        {
            kingdom.getUnits().moveMobileToAvailable(unit, ongoingAttack.units.getCount(unit));
        }
    }
}
