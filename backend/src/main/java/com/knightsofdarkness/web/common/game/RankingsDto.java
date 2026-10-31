package com.knightsofdarkness.web.common.game;

import java.util.List;

public record RankingsDto(List<KingdomRankDto> kingdoms, List<AllianceRankDto> alliances) {
}
