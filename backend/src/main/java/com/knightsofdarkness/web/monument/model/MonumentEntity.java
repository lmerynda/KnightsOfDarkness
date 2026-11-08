package com.knightsofdarkness.web.monument.model;

import java.util.ArrayList;
import java.util.List;

import com.knightsofdarkness.web.common.alliance.MonumentContributionDto;
import com.knightsofdarkness.web.common.alliance.MonumentDto;
import com.knightsofdarkness.web.common.alliance.MonumentGoodsDto;
import com.knightsofdarkness.web.common.alliance.MonumentLedgerDto;
import com.knightsofdarkness.web.game.config.Monument;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * An alliance's monument. Its stages rise as the store covers them and crumble in fractions when the store cannot pay the
 * upkeep; repairing a crumbled stage costs only the fraction that is gone.
 */
@Entity
public class MonumentEntity {
    private static final double whole = 1e-9;

    @Id
    String allianceName;
    String seat;
    double stagesBuilt;
    long gold;
    long iron;
    long tools;
    long food;
    int announcedStage;
    boolean upkeepUnpaid;

    public MonumentEntity()
    {
    }

    public MonumentEntity(String allianceName, String seat)
    {
        this.allianceName = allianceName;
        this.seat = seat;
    }

    public void pay(MonumentContributionDto goods)
    {
        gold += goods.gold();
        iron += goods.iron();
        tools += goods.tools();
        food += goods.food();
    }

    /**
     * @return the stages raised now, in the order they rose
     */
    public List<Integer> raiseStages(Monument rules)
    {
        var raised = new ArrayList<Integer>();
        while (!isComplete(rules))
        {
            var cost = nextStageCost(rules);
            if (gold < cost.gold() || iron < cost.iron() || tools < cost.tools())
            {
                return raised;
            }
            gold -= cost.gold();
            iron -= cost.iron();
            tools -= cost.tools();
            stagesBuilt = nextStage();
            raised.add(nextStage() - 1);
        }
        return raised;
    }

    /**
     * the first time a stage rises it is news; a stage repaired after crumbling a little is not
     *
     * @return whether this stage is news
     */
    public boolean announces(int stage)
    {
        if (stage <= announcedStage)
        {
            return false;
        }
        announcedStage = stage;
        return true;
    }

    /**
     * every stage eats its share of gold and food; a store that cannot pay it all pays nothing, and the monument crumbles
     *
     * @return whether the monument has just begun to crumble, which its members should hear about once
     */
    public boolean payUpkeep(Monument rules)
    {
        var upkeep = upkeep(rules);
        if (gold >= upkeep.gold() && food >= upkeep.food())
        {
            gold -= upkeep.gold();
            food -= upkeep.food();
            upkeepUnpaid = false;
            return false;
        }
        stagesBuilt = Math.max(0, stagesBuilt - rules.decayRatio());
        announcedStage = Math.min(announcedStage, (int) Math.ceil(stagesBuilt - whole));
        var begun = !upkeepUnpaid;
        upkeepUnpaid = true;
        return begun;
    }

    public MonumentDto toDto(Monument rules)
    {
        return new MonumentDto(allianceName, seat, Math.round(stagesBuilt * 100) / 100.0, rules.stages(), isComplete(rules));
    }

    public MonumentLedgerDto toLedgerDto(Monument rules)
    {
        return new MonumentLedgerDto(toDto(rules), new MonumentGoodsDto(gold, iron, tools, food), isComplete(rules) ? null : nextStageCost(rules), upkeep(rules), upkeepUnpaid);
    }

    public boolean isComplete(Monument rules)
    {
        return stagesBuilt >= rules.stages();
    }

    public String getAllianceName()
    {
        return allianceName;
    }

    public String getSeat()
    {
        return seat;
    }

    public void moveSeat(String kingdomName)
    {
        seat = kingdomName;
    }

    private int nextStage()
    {
        return (int) Math.floor(stagesBuilt + whole) + 1;
    }

    private MonumentGoodsDto nextStageCost(Monument rules)
    {
        var missing = nextStage() - stagesBuilt;
        var cost = rules.stageCost();
        return new MonumentGoodsDto(share(cost.gold(), missing), share(cost.iron(), missing), share(cost.tools(), missing), 0);
    }

    private MonumentGoodsDto upkeep(Monument rules)
    {
        var upkeep = rules.upkeepPerStage();
        return new MonumentGoodsDto(share(upkeep.gold(), stagesBuilt), 0, 0, share(upkeep.food(), stagesBuilt));
    }

    private static long share(long count, double stages)
    {
        return (long) Math.ceil(count * stages - whole);
    }
}
