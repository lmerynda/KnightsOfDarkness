package com.knightsofdarkness.web.common.kingdom;

import java.util.UUID;

/**
 * soldiers sent to stand guard in an ally's kingdom; they defend it once turnsLeft reaches 0
 */
public record ReinforcementDto(UUID id, String from, String to, int turnsLeft, UnitsMapDto units) {
}
