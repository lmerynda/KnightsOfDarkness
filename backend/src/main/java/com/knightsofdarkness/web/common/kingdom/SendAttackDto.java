package com.knightsofdarkness.web.common.kingdom;

public record SendAttackDto(String destinationKingdomName, AttackType attackType, UnitsMapDto units) {

}
