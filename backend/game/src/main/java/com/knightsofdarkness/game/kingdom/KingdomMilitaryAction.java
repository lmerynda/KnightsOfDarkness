package com.knightsofdarkness.game.kingdom;

import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.common.kingdom.SendAttackDto;
import com.knightsofdarkness.common.kingdom.SendAttackResult;
import com.knightsofdarkness.common.kingdom.UnitName;
import com.knightsofdarkness.game.Utils;

public class KingdomMilitaryAction {
    private final Kingdom kingdom;

    public KingdomMilitaryAction(Kingdom kingdom)
    {
        this.kingdom = kingdom;
    }

    public SendAttackResult sendAttack(SendAttackDto sendAttackDto)
    {
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

        // TODO save progressing attack with number of turns
        return SendAttackResult.success("Attack sent", sendAttackDto);
    }

    public void withdrawCarriers(KingdomCarriersOnTheMove carriersOnTheMove)
    {
        kingdom.getCarriersOnTheMove().remove(carriersOnTheMove);
        kingdom.getResources().addCount(ResourceName.from(carriersOnTheMove.getResource()),
                carriersOnTheMove.getResourceCount());
        kingdom.getUnits().moveMobileToAvailable(UnitName.carrier, carriersOnTheMove.getCarriersCount());
    }
}
