package com.knightsofdarkness.web.round;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.knightsofdarkness.web.bots.IBotRepository;
import com.knightsofdarkness.web.common.game.RankingsDto;
import com.knightsofdarkness.web.common.game.RoundDto;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.model.KingdomRepository;
import com.knightsofdarkness.web.market.model.IMarketOfferRepository;
import com.knightsofdarkness.web.messaging.INotificationSystem;
import com.knightsofdarkness.web.round.model.GameRoundEntity;
import com.knightsofdarkness.web.round.model.GameRoundJpaRepository;
import com.knightsofdarkness.web.utils.Utils;
import com.knightsofdarkness.web.war.IWarReportRepository;

@Service
public class RoundService {
    private static final Logger log = LoggerFactory.getLogger(RoundService.class);
    private final GameRoundJpaRepository roundRepository;
    private final KingdomRepository kingdomRepository;
    private final IMarketOfferRepository marketOfferRepository;
    private final IWarReportRepository warReportRepository;
    private final IBotRepository botRepository;
    private final INotificationSystem notificationSystem;
    private final GameConfig gameConfig;

    @SuppressWarnings("java:S107")
    public RoundService(GameRoundJpaRepository roundRepository, KingdomRepository kingdomRepository, IMarketOfferRepository marketOfferRepository, IWarReportRepository warReportRepository, IBotRepository botRepository,
            INotificationSystem notificationSystem, GameConfig gameConfig)
    {
        this.roundRepository = roundRepository;
        this.kingdomRepository = kingdomRepository;
        this.marketOfferRepository = marketOfferRepository;
        this.warReportRepository = warReportRepository;
        this.botRepository = botRepository;
        this.notificationSystem = notificationSystem;
        this.gameConfig = gameConfig;
    }

    @Transactional
    public RoundDto getCurrentRound()
    {
        return currentRound().toDto(gameConfig.common().newTurnPeriodicity());
    }

    public List<RoundDto> getFinishedRounds()
    {
        return roundRepository.findByEndedAtIsNotNullOrderByNumberDesc().stream().map(round -> round.toDto(gameConfig.common().newTurnPeriodicity())).toList();
    }

    @Transactional(readOnly = true)
    public RankingsDto getRankings()
    {
        return RankingCalculator.calculate(kingdomRepository.getAllKingdoms(), botNames());
    }

    @Transactional
    public void tick()
    {
        var round = currentRound();
        if (round.tick())
        {
            endRound(round);
        }
        roundRepository.save(round);
    }

    @Transactional
    public void endCurrentRound()
    {
        var round = currentRound();
        endRound(round);
        roundRepository.save(round);
    }

    private void endRound(GameRoundEntity round)
    {
        var kingdoms = kingdomRepository.getAllKingdoms();
        var botNames = botNames();
        var rankings = RankingCalculator.calculate(kingdoms, botNames);
        var now = Instant.now();
        round.end(now, rankings);
        log.info("Round {} ended", round.getNumber());

        marketOfferRepository.deleteAll();
        warReportRepository.deleteAll();
        RoundReset.reset(kingdoms, botNames, gameConfig);
        kingdoms.forEach(kingdomRepository::update);

        var nextRound = new GameRoundEntity(round.getNumber() + 1, now, gameConfig.round().lengthInTicks());
        roundRepository.save(nextRound);

        var winner = rankings.kingdoms().isEmpty() ? "nobody" : rankings.kingdoms().get(0).kingdomName();
        var message = Utils.format("Round {} has ended, {} ruled the most land. Round {} begins, every kingdom starts anew", round.getNumber(), winner, nextRound.getNumber());
        kingdoms.forEach(kingdom -> notificationSystem.create(kingdom.getName(), message));
    }

    private Set<String> botNames()
    {
        return botRepository.findAllWithKingdoms().stream().map(bot -> bot.getKingdom().getName()).collect(Collectors.toSet());
    }

    private GameRoundEntity currentRound()
    {
        return roundRepository.findFirstByEndedAtIsNullOrderByNumberDesc()
                .orElseGet(() -> roundRepository.save(new GameRoundEntity(1, Instant.now(), gameConfig.round().lengthInTicks())));
    }
}
