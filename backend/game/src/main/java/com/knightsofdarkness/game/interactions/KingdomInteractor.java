package com.knightsofdarkness.game.interactions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knightsofdarkness.common.kingdom.UnitName;
import com.knightsofdarkness.common.market.MarketResource;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.kingdom.KingdomOngoingAttack;
import com.knightsofdarkness.game.storage.IKingdomRepository;

public class KingdomInteractor implements IKingdomInteractor {
    private static final Logger log = LoggerFactory.getLogger(KingdomInteractor.class);
    IKingdomRepository kingdomRepository;

    public KingdomInteractor(IKingdomRepository kingdomRepository)
    {
        this.kingdomRepository = kingdomRepository;
    }

    @Override
    public void transferResources(Kingdom from, String to, MarketResource resource, int amount)
    {
        var toKingdom = kingdomRepository.getKingdomByName(to);
        if (toKingdom.isEmpty())
        {
            log.warn("[KingdomInteractor] Kingdom with name {} not found", to);
            return;
        }

        toKingdom.get().receiveResourceTransfer(resource, amount);
        kingdomRepository.update(from);
        kingdomRepository.update(toKingdom.get());
    }

    @Override
    public void resolveAttack(Kingdom attackerKingdom, KingdomOngoingAttack attack)
    {
        log.info("[KingdomInteractor] Resolving attack from {} on {}", attackerKingdom.getName(), attack.getTargetKingdomName());

        var maybeDefendantKingdom = kingdomRepository.getKingdomByName(attack.getTargetKingdomName());
        if (maybeDefendantKingdom.isEmpty())
        {
            log.warn("[KingdomInteractor] Kingdom with name {} not found", attack.getTargetKingdomName());
            return;
        }

        var defendantKingdom = maybeDefendantKingdom.get();

        processDefenseSalvo(attackerKingdom, defendantKingdom, attack);
        processAttackSalvo(attackerKingdom, defendantKingdom, attack);
        processMelee(attackerKingdom, defendantKingdom, attack);
    }

    private void processDefenseSalvo(Kingdom attackerKingdom, Kingdom defenderKingdom, KingdomOngoingAttack attack)
    {
        var defenderBowmenCount = defenderKingdom.getUnits().getAvailableCount(UnitName.bowman);
        var attackerUnits = attack.getUnits();
        var attackerUnitsRatios = attack.getUnits().getMilitaryUnitsRatios();

        var bowmenHittingBowmen = (int) Math.ceil(attackerUnitsRatios.get(UnitName.bowman) * defenderBowmenCount);
        var bowmenHittingInfantry = (int) Math.ceil(attackerUnitsRatios.get(UnitName.infantry) * defenderBowmenCount);
        var bowmenHittingCavarly = (int) Math.ceil(attackerUnitsRatios.get(UnitName.cavalry) * defenderBowmenCount);

        var killedAttackingBowmen = Math.min(bowmenHittingBowmen / 2, attackerUnits.getCount(UnitName.bowman));
        var killedAttackingInfantry = Math.min(bowmenHittingInfantry / 3, attackerUnits.getCount(UnitName.infantry));
        var killedAttackingCavalry = Math.min(bowmenHittingCavarly / 4, attackerUnits.getCount(UnitName.cavalry));

        attackerUnits.subtractCount(UnitName.bowman, killedAttackingBowmen);
        attackerUnits.subtractCount(UnitName.infantry, killedAttackingInfantry);
        attackerUnits.subtractCount(UnitName.cavalry, killedAttackingCavalry);
    }

    private void processAttackSalvo(Kingdom attackerKingdom, Kingdom defendantKingdom, KingdomOngoingAttack attack)
    {
        // XXX Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'processAttackSalvo'");
    }

    private void processMelee(Kingdom attackerKingdom, Kingdom defendantKingdom, KingdomOngoingAttack attack)
    {
        // XXX Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'processMelee'");
    }
}
