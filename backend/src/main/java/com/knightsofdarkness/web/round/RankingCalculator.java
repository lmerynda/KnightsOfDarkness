package com.knightsofdarkness.web.round;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.knightsofdarkness.web.common.game.AllianceRankDto;
import com.knightsofdarkness.web.common.game.KingdomRankDto;
import com.knightsofdarkness.web.common.game.RankingsDto;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;

/**
 * Kingdoms are ranked by land, alliances by the land of all their members. Ties are broken by name.
 */
public final class RankingCalculator {
    private RankingCalculator()
    {
    }

    public static RankingsDto calculate(List<KingdomEntity> kingdoms)
    {
        var sortedKingdoms = kingdoms.stream()
                .sorted(Comparator.comparingInt((KingdomEntity kingdom) -> kingdom.getResources().getCount(ResourceName.land)).reversed().thenComparing(KingdomEntity::getName))
                .toList();

        var kingdomRanks = new ArrayList<KingdomRankDto>();
        for (int i = 0; i < sortedKingdoms.size(); i++)
        {
            var kingdom = sortedKingdoms.get(i);
            var allianceName = kingdom.getAlliance().map(alliance -> alliance.getName()).orElse(null);
            kingdomRanks.add(new KingdomRankDto(i + 1, kingdom.getName(), allianceName, kingdom.getResources().getCount(ResourceName.land), kingdom.getBuildings().countAll()));
        }

        Map<String, int[]> allianceTotals = new HashMap<>();
        for (var rank : kingdomRanks)
        {
            if (rank.allianceName() != null)
            {
                var totals = allianceTotals.computeIfAbsent(rank.allianceName(), name -> new int[2]);
                totals[0]++;
                totals[1] += rank.land();
            }
        }

        var sortedAlliances = allianceTotals.entrySet().stream()
                .sorted(Comparator.comparingInt((Map.Entry<String, int[]> entry) -> entry.getValue()[1]).reversed().thenComparing(Map.Entry::getKey))
                .toList();
        var allianceRanks = new ArrayList<AllianceRankDto>();
        for (int i = 0; i < sortedAlliances.size(); i++)
        {
            var entry = sortedAlliances.get(i);
            allianceRanks.add(new AllianceRankDto(i + 1, entry.getKey(), entry.getValue()[0], entry.getValue()[1]));
        }

        return new RankingsDto(kingdomRanks, allianceRanks);
    }
}
