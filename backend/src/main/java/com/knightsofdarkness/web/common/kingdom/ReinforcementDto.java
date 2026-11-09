package com.knightsofdarkness.web.common.kingdom;

import java.util.UUID;

/**
 * soldiers sent to stand guard in an ally's kingdom or at their alliance's monument, whose seat is then to; they defend once turnsLeft reaches 0
 */
public record ReinforcementDto(UUID id, String from, String to, int turnsLeft, UnitsMapDto units, boolean atMonument) {
}
