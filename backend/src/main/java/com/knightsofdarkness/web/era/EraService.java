package com.knightsofdarkness.web.era;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.knightsofdarkness.web.bots.IBotRepository;
import com.knightsofdarkness.web.common.game.EraDto;
import com.knightsofdarkness.web.common.game.EraEnding;
import com.knightsofdarkness.web.common.game.RankingsDto;
import com.knightsofdarkness.web.common.messaging.NotificationKind;
import com.knightsofdarkness.web.era.model.GameEraEntity;
import com.knightsofdarkness.web.era.model.GameEraJpaRepository;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.model.KingdomRepository;
import com.knightsofdarkness.web.market.model.IMarketOfferRepository;
import com.knightsofdarkness.web.messaging.INotificationSystem;
import com.knightsofdarkness.web.monument.MonumentService;
import com.knightsofdarkness.web.utils.Utils;
import com.knightsofdarkness.web.war.IWarReportRepository;

@Service
public class EraService {
    private static final Logger log = LoggerFactory.getLogger(EraService.class);
    private final GameEraJpaRepository eraRepository;
    private final KingdomRepository kingdomRepository;
    private final IMarketOfferRepository marketOfferRepository;
    private final IWarReportRepository warReportRepository;
    private final IBotRepository botRepository;
    private final INotificationSystem notificationSystem;
    private final MonumentService monumentService;
    private final GameConfig gameConfig;

    @SuppressWarnings("java:S107")
    public EraService(GameEraJpaRepository eraRepository, KingdomRepository kingdomRepository, IMarketOfferRepository marketOfferRepository, IWarReportRepository warReportRepository, IBotRepository botRepository,
            INotificationSystem notificationSystem, MonumentService monumentService, GameConfig gameConfig)
    {
        this.eraRepository = eraRepository;
        this.kingdomRepository = kingdomRepository;
        this.marketOfferRepository = marketOfferRepository;
        this.warReportRepository = warReportRepository;
        this.botRepository = botRepository;
        this.notificationSystem = notificationSystem;
        this.monumentService = monumentService;
        this.gameConfig = gameConfig;
    }

    @Transactional
    public EraDto getCurrentEra()
    {
        return currentEra().toDto(gameConfig.common().newTurnPeriodicity());
    }

    @Transactional(readOnly = true)
    public List<EraDto> getFinishedEras()
    {
        return eraRepository.findByEndedAtIsNotNullOrderByNumberDesc().stream().map(era -> era.toDto(gameConfig.common().newTurnPeriodicity())).toList();
    }

    @Transactional(readOnly = true)
    public RankingsDto getRankings()
    {
        return RankingCalculator.calculate(kingdomRepository.getAllKingdoms(), botNames());
    }

    @Transactional
    public void tick()
    {
        monumentService.tick();
        var era = currentEra();
        if (era.tick())
        {
            endEra(era, EraEnding.clock);
        }
        eraRepository.save(era);
    }

    private void endEra(GameEraEntity era, EraEnding ending)
    {
        var kingdoms = kingdomRepository.getAllKingdoms();
        var botNames = botNames();
        var rankings = RankingCalculator.calculate(kingdoms, botNames);
        var now = Instant.now();
        era.end(now, ending, rankings);
        log.info("Era {} ended", era.getNumber());

        marketOfferRepository.deleteAll();
        warReportRepository.deleteAll();
        monumentService.clear();
        EraReset.reset(kingdoms, botNames, gameConfig);
        kingdoms.forEach(kingdomRepository::update);

        var nextEra = new GameEraEntity(era.getNumber() + 1, now, gameConfig.era().maxLengthInTicks());
        eraRepository.save(nextEra);

        var message = Utils.format("Era {} has ended as the clock ran out. {} Era {} begins, every kingdom starts anew and at peace for {} turns", era.getNumber(), standing(rankings), nextEra.getNumber(), gameConfig.attack().protectionTicksAtStart());
        kingdoms.forEach(kingdom -> notificationSystem.create(kingdom.getName(), NotificationKind.era, message));
    }

    private static String standing(RankingsDto rankings)
    {
        var alliance = rankings.alliances().isEmpty() ? "No alliance was sworn" : rankings.alliances().get(0).allianceName() + " held the most land";
        var kingdom = rankings.kingdoms().isEmpty() ? "no kingdom stood" : rankings.kingdoms().get(0).kingdomName() + " was the largest kingdom";
        return alliance + ", " + kingdom + ".";
    }

    private Set<String> botNames()
    {
        return botRepository.findAllWithKingdoms().stream().map(bot -> bot.getKingdom().getName()).collect(Collectors.toSet());
    }

    private GameEraEntity currentEra()
    {
        return eraRepository.findFirstByEndedAtIsNullOrderByNumberDesc()
                .orElseGet(() -> eraRepository.save(new GameEraEntity(1, Instant.now(), gameConfig.era().maxLengthInTicks())));
    }
}
