package com.knightsofdarkness.web.war.model;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.knightsofdarkness.web.common.war.BattleReportDto;
import com.knightsofdarkness.web.common.war.SpyReportDto;
import com.knightsofdarkness.web.war.IWarReportRepository;

@Repository
public class WarReportRepository implements IWarReportRepository {
    private final BattleReportJpaRepository battleReports;
    private final SpyReportJpaRepository spyReports;

    public WarReportRepository(BattleReportJpaRepository battleReports, SpyReportJpaRepository spyReports)
    {
        this.battleReports = battleReports;
        this.spyReports = spyReports;
    }

    @Override
    public void addBattleReport(BattleReportDto report)
    {
        battleReports.save(new BattleReportEntity(report));
    }

    @Override
    public List<BattleReportDto> getBattleReports(String kingdomName, int limit)
    {
        return battleReports.findByKingdom(kingdomName, PageRequest.of(0, limit)).stream().map(BattleReportEntity::toDto).toList();
    }

    @Override
    public void addSpyReport(SpyReportDto report)
    {
        spyReports.save(new SpyReportEntity(report));
    }

    @Override
    public List<SpyReportDto> getSpyReports(String kingdomName, int limit)
    {
        return spyReports.findByKingdomNameOrderByDateDesc(kingdomName, PageRequest.of(0, limit)).stream().map(SpyReportEntity::toDto).toList();
    }

    @Override
    public void deleteAll()
    {
        battleReports.deleteAll();
        spyReports.deleteAll();
    }
}
