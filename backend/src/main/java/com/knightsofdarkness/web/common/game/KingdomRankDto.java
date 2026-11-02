package com.knightsofdarkness.web.common.game;

import org.jspecify.annotations.Nullable;

/**
 * bots trade and can be attacked but do not compete, they are listed after the players with rank 0
 */
public record KingdomRankDto(int rank, String kingdomName, @Nullable String allianceName, int land, int buildings, boolean bot, int protectionTicks) {
}
