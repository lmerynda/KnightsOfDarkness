package com.knightsofdarkness.common.kingdom;

public record SendAttackDto(String destinationKingdomName, AttackType attackType, UnitsMapDto units) {

}
