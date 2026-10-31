package com.knightsofdarkness.web.common.alliance;

import java.util.List;

import com.knightsofdarkness.web.common.kingdom.OngoingAttackDto;
import com.knightsofdarkness.web.common.kingdom.ReinforcementDto;
import com.knightsofdarkness.web.common.war.BattleReportDto;
import com.knightsofdarkness.web.common.war.SpyReportDto;

/**
 * what an alliance knows together: its members' armies on the march, the soldiers they keep standing guard with each
 * other, the freshest intel any member's spies brought back on each enemy, and the members' recent battles
 */
public record AllianceWarRoomDto(List<OngoingAttackDto> marching, List<ReinforcementDto> guards, List<SpyReportDto> intel, List<BattleReportDto> battles) {
}
