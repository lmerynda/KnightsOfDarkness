package com.knightsofdarkness.web.common.kingdom;

/**
 * an enemy army marching on a kingdom, as its scouts see it: who sent it, how many of the sender's turns until it lands,
 * and roughly how many soldiers march in it
 */
public record IncomingArmyDto(String from, String to, AttackType attackType, int turnsLeft, int soldiers) {
}
