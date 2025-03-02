package com.knightsofdarkness.web.kingdom.model;

import com.knightsofdarkness.common.kingdom.SendAttackDto;
import com.knightsofdarkness.common.kingdom.SendAttackResult;
import com.knightsofdarkness.common.kingdom.UnitName;
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

        var turnsLeft = gameConfig.attack().turnsToArrive();
        var newAttack = new KingdomOngoingAttackEntity(Id.generate(), kingdom, sendAttackDto.destinationKingdomName(), turnsLeft, sendAttackDto.attackType(), sendAttackDto.units());
        kingdom.getOngoingAttacks().add(newAttack);

        return SendAttackResult.success("Attack sent", sendAttackDto);
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
