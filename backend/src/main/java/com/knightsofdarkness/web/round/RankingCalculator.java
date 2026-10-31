package com.knightsofdarkness.web.round;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.knightsofdarkness.web.common.game.AllianceRankDto;
import com.knightsofdarkness.web.common.game.KingdomRankDto;
import com.knightsofdarkness.web.common.game.RankingsDto;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;

/**
 * Players are ranked by land, alliances by the land of their player members. Ties are broken by name. Bots are listed
 * after the players, unranked, and do not count towards their alliance.
 */
public final class RankingCalculator {
    private RankingCalculator()
    {
    }

    public static RankingsDto calculate(List<KingdomEntity> kingdoms, Set<String> botNames)
    {
        var sortedKingdoms = kingdoms.stream()
                .sorted(Comparator.comparingInt((KingdomEntity kingdom) -> kingdom.getResources().getCount(ResourceName.land)).reversed().thenComparing(KingdomEntity::getName))
                .toList();

        var kingdomRanks = new ArrayList<KingdomRankDto>();
        var rank = 0;
        for (var kingdom : sortedKingdoms.stream().filter(kingdom -> !botNames.contains(kingdom.getName())).toList())
        {
            kingdomRanks.add(toRank(++rank, kingdom, false));
        }
        for (var kingdom : sortedKingdoms.stream().filter(kingdom -> botNames.contains(kingdom.getName())).toList())
        {
            kingdomRanks.add(toRank(0, kingdom, true));
        }

        Map<String, int[]> allianceTotals = new HashMap<>();
        for (var kingdomRank : kingdomRanks)
        {
            if (kingdomRank.allianceName() != null)
            {
                var totals = allianceTotals.computeIfAbsent(kingdomRank.allianceName(), name -> new int[2]);
                totals[0]++;
                totals[1] += kingdomRank.bot() ? 0 : kingdomRank.land();
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

    private static KingdomRankDto toRank(int rank, KingdomEntity kingdom, boolean bot)
    {
        var allianceName = kingdom.getAlliance().map(alliance -> alliance.getName()).orElse(null);
        return new KingdomRankDto(rank, kingdom.getName(), allianceName, kingdom.getResources().getCount(ResourceName.land), kingdom.getBuildings().countAll(), bot);
    }
}
