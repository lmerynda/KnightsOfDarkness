package com.knightsofdarkness.web.kingdom.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knightsofdarkness.web.common.kingdom.UnitName;
import com.knightsofdarkness.web.common.market.MarketResource;
import com.knightsofdarkness.web.kingdom.IKingdomInteractor;
import com.knightsofdarkness.web.kingdom.IKingdomRepository;

public class KingdomInteractor implements IKingdomInteractor {
    private static final Logger log = LoggerFactory.getLogger(KingdomInteractor.class);
    IKingdomRepository kingdomRepository;

    public KingdomInteractor(IKingdomRepository kingdomRepository)
    {
        this.kingdomRepository = kingdomRepository;
    }

    @Override
    public void transferResources(KingdomEntity from, String to, MarketResource resource, int amount)
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
    public void resolveAttack(KingdomEntity attackerKingdom, KingdomOngoingAttackEntity attack)
    {
        log.info("[KingdomInteractor] Resolving attack from {} on {}", attackerKingdom.getName(), attack.targetKingdomName);

        var maybeDefendantKingdom = kingdomRepository.getKingdomByName(attack.targetKingdomName);
        if (maybeDefendantKingdom.isEmpty())
        {
            log.warn("[KingdomInteractor] Kingdom with name {} not found", attack.targetKingdomName);
            return;
        }

        var defendantKingdom = maybeDefendantKingdom.get();

        processDefenseSalvo(attackerKingdom, defendantKingdom, attack);
        processAttackSalvo(attackerKingdom, defendantKingdom, attack);
        processMelee(attackerKingdom, defendantKingdom, attack);

        for (UnitName unitName : UnitName.getMilitaryUnits())
        {
            attackerKingdom.getUnits().moveMobileToAvailable(unitName, attack.units.getCount(unitName));
        }

        kingdomRepository.update(attackerKingdom);
        kingdomRepository.update(defendantKingdom);
    }

    void processDefenseSalvo(KingdomEntity attackerKingdom, KingdomEntity defenderKingdom, KingdomOngoingAttackEntity attack)
    {
        var defenderBowmenCount = defenderKingdom.getUnits().getAvailableCount(UnitName.bowman);
        var attackerUnits = attack.units;
        var attackerUnitsRatios = attack.units.getMilitaryUnitsRatios();

        var bowmenHittingBowmen = (int) Math.ceil(attackerUnitsRatios.get(UnitName.bowman) * defenderBowmenCount);
        var bowmenHittingInfantry = (int) Math.ceil(attackerUnitsRatios.get(UnitName.infantry) * defenderBowmenCount);
        var bowmenHittingCavarly = (int) Math.ceil(attackerUnitsRatios.get(UnitName.cavalry) * defenderBowmenCount);

        // TODO move consts to configuration
        var killedAttackingBowmen = Math.min(bowmenHittingBowmen / 3, attackerUnits.getCount(UnitName.bowman));
        var killedAttackingInfantry = Math.min(bowmenHittingInfantry / 5, attackerUnits.getCount(UnitName.infantry));
        var killedAttackingCavalry = Math.min(bowmenHittingCavarly / 7, attackerUnits.getCount(UnitName.cavalry));

        attackerUnits.subtractCount(UnitName.bowman, killedAttackingBowmen);
        attackerUnits.subtractCount(UnitName.infantry, killedAttackingInfantry);
        attackerUnits.subtractCount(UnitName.cavalry, killedAttackingCavalry);

        attackerKingdom.getUnits().subtractMobileCount(UnitName.bowman, killedAttackingBowmen);
        attackerKingdom.getUnits().subtractMobileCount(UnitName.infantry, killedAttackingInfantry);
        attackerKingdom.getUnits().subtractMobileCount(UnitName.cavalry, killedAttackingCavalry);

        log.info("[KingdomInteractor] Defender bowmen salvo killed {} bowmen, {} infantry, {} cavalry from attacking kingdom {}", killedAttackingBowmen, killedAttackingInfantry, killedAttackingCavalry, attackerKingdom.getName());
    }

    void processAttackSalvo(KingdomEntity attackerKingdom, KingdomEntity defendantKingdom, KingdomOngoingAttackEntity attack)
    {
        var attackerBowmenCount = attack.units.getCount(UnitName.bowman);
        var defenderUnits = defendantKingdom.units;
        var defenderUnitsRatios = defenderUnits.getAvailableMilitaryUnitsRatios();

        var bowmenHittingBowmen = (int) Math.ceil(defenderUnitsRatios.get(UnitName.bowman) * attackerBowmenCount);
        var bowmenHittingInfantry = (int) Math.ceil(defenderUnitsRatios.get(UnitName.infantry) * attackerBowmenCount);
        var bowmenHittingCavarly = (int) Math.ceil(defenderUnitsRatios.get(UnitName.cavalry) * attackerBowmenCount);

        var killedDefendingBowmen = Math.min(bowmenHittingBowmen / 3, defenderUnits.getAvailableCount(UnitName.bowman));
        var killedDefendingInfantry = Math.min(bowmenHittingInfantry / 5, defenderUnits.getAvailableCount(UnitName.infantry));
        var killedDefendingCavalry = Math.min(bowmenHittingCavarly / 7, defenderUnits.getAvailableCount(UnitName.cavalry));

        defenderUnits.subtractAvailableCount(UnitName.bowman, killedDefendingBowmen);
        defenderUnits.subtractAvailableCount(UnitName.infantry, killedDefendingInfantry);
        defenderUnits.subtractAvailableCount(UnitName.cavalry, killedDefendingCavalry);

        defendantKingdom.getUnits().subtractAvailableCount(UnitName.bowman, killedDefendingBowmen);
        defendantKingdom.getUnits().subtractAvailableCount(UnitName.infantry, killedDefendingInfantry);
        defendantKingdom.getUnits().subtractAvailableCount(UnitName.cavalry, killedDefendingCavalry);

        log.info("[KingdomInteractor] Attacker bowmen salvo killed {} bowmen, {} infantry, {} cavalry from defending kingdom {}", killedDefendingBowmen, killedDefendingInfantry, killedDefendingCavalry, defendantKingdom.getName());
    }

    void processMelee(KingdomEntity attackerKingdom, KingdomEntity defendantKingdom, KingdomOngoingAttackEntity attack)
    {
        var attackingUnits = attack.units;
        var attackingUnitsRatios = attackingUnits.getMilitaryUnitsRatios();
        var defenderUnits = defendantKingdom.units;
        var defenderUnitsRatios = defenderUnits.getAvailableMilitaryUnitsRatios();

        var defenderInfantryHittingBowman = (int) Math.ceil(defenderUnits.getAvailableCount(UnitName.bowman) * attackingUnitsRatios.get(UnitName.bowman));
        var defenderInfantryHittingInfantry = (int) Math.ceil(defenderUnits.getAvailableCount(UnitName.infantry) * attackingUnitsRatios.get(UnitName.infantry));
        var defenderInfantryHittingCavalry = (int) Math.ceil(defenderUnits.getAvailableCount(UnitName.cavalry) * attackingUnitsRatios.get(UnitName.cavalry));

        var attackerInfantryHittingBowman = (int) Math.ceil(attackingUnits.getCount(UnitName.bowman) * defenderUnitsRatios.get(UnitName.bowman));
        var attackerInfantryHittingInfantry = (int) Math.ceil(attackingUnits.getCount(UnitName.infantry) * defenderUnitsRatios.get(UnitName.infantry));
        var attackerInfantryHittingCavalry = (int) Math.ceil(attackingUnits.getCount(UnitName.cavalry) * defenderUnitsRatios.get(UnitName.cavalry));

        var killedAttackingBowmen = Math.min(defenderInfantryHittingBowman / 2, attackingUnits.getCount(UnitName.bowman));
        var killedAttackingInfantry = Math.min(defenderInfantryHittingInfantry / 3, attackingUnits.getCount(UnitName.infantry));
        var killedAttackingCavalry = Math.min(defenderInfantryHittingCavalry / 4, attackingUnits.getCount(UnitName.cavalry));

        var killedDefendingBowmen = Math.min(attackerInfantryHittingBowman / 2, defenderUnits.getAvailableCount(UnitName.bowman));
        var killedDefendingInfantry = Math.min(attackerInfantryHittingInfantry / 3, defenderUnits.getAvailableCount(UnitName.infantry));
        var killedDefendingCavalry = Math.min(attackerInfantryHittingCavalry / 4, defenderUnits.getAvailableCount(UnitName.cavalry));

        attackingUnits.subtractCount(UnitName.bowman, killedAttackingBowmen);
        attackingUnits.subtractCount(UnitName.infantry, killedAttackingInfantry);
        attackingUnits.subtractCount(UnitName.cavalry, killedAttackingCavalry);

        attackerKingdom.getUnits().subtractMobileCount(UnitName.bowman, killedAttackingBowmen);
        attackerKingdom.getUnits().subtractMobileCount(UnitName.infantry, killedAttackingInfantry);
        attackerKingdom.getUnits().subtractMobileCount(UnitName.cavalry, killedAttackingCavalry);

        defenderUnits.subtractAvailableCount(UnitName.bowman, killedDefendingBowmen);
        defenderUnits.subtractAvailableCount(UnitName.infantry, killedDefendingInfantry);
        defenderUnits.subtractAvailableCount(UnitName.cavalry, killedDefendingCavalry);
    }
}
