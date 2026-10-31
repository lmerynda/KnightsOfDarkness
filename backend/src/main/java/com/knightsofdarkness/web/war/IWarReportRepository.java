package com.knightsofdarkness.web.war;

import java.util.List;

import com.knightsofdarkness.web.common.war.BattleReportDto;
import com.knightsofdarkness.web.common.war.SpyReportDto;

public interface IWarReportRepository {
    void addBattleReport(BattleReportDto report);

    List<BattleReportDto> getBattleReports(String kingdomName, int limit);

    void addSpyReport(SpyReportDto report);

    List<SpyReportDto> getSpyReports(String kingdomName, int limit);

    void deleteAll();
}
