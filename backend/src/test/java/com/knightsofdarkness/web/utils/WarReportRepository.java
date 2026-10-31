package com.knightsofdarkness.web.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.knightsofdarkness.web.common.war.BattleReportDto;
import com.knightsofdarkness.web.common.war.SpyReportDto;
import com.knightsofdarkness.web.war.IWarReportRepository;

public class WarReportRepository implements IWarReportRepository {
    private final List<BattleReportDto> battleReports = new ArrayList<>();
    private final List<SpyReportDto> spyReports = new ArrayList<>();

    @Override
    public void addBattleReport(BattleReportDto report)
    {
        battleReports.add(report);
    }

    @Override
    public List<BattleReportDto> getBattleReports(String kingdomName, int limit)
    {
        return battleReports.stream()
                .filter(report -> report.attacker().equals(kingdomName) || report.defender().equals(kingdomName))
                .sorted(Comparator.comparing(BattleReportDto::date).reversed())
                .limit(limit)
                .toList();
    }

    @Override
    public void addSpyReport(SpyReportDto report)
    {
        spyReports.add(report);
    }

    @Override
    public List<SpyReportDto> getSpyReports(String kingdomName, int limit)
    {
        return spyReports.stream()
                .filter(report -> report.kingdomName().equals(kingdomName))
                .sorted(Comparator.comparing(SpyReportDto::date).reversed())
                .limit(limit)
                .toList();
    }

    @Override
    public void deleteAll()
    {
        battleReports.clear();
        spyReports.clear();
    }
}
