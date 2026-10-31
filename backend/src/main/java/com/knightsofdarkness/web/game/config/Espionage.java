package com.knightsofdarkness.web.game.config;

public record Espionage(
        double counterIntelligencePerSpy,
        double counterIntelligencePerGuard,
        double failedMissionLossRatio) {
}
