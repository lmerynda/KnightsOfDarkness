package com.knightsofdarkness.web.war;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.knightsofdarkness.web.common.messaging.NotificationKind;
import com.knightsofdarkness.web.common.war.BattleReportDto;
import com.knightsofdarkness.web.common.war.SendSpiesDto;
import com.knightsofdarkness.web.common.ActionResult;
import com.knightsofdarkness.web.common.war.SpyReportDto;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.model.KingdomRepository;
import com.knightsofdarkness.web.kingdom.model.KingdomSpyAction;
import com.knightsofdarkness.web.messaging.INotificationSystem;
import com.knightsofdarkness.web.utils.Utils;

@Service
public class WarService {
    private static final Logger log = LoggerFactory.getLogger(WarService.class);
    private static final int reportsLimit = 30;
    private final KingdomRepository kingdomRepository;
    private final IWarReportRepository warReportRepository;
    private final INotificationSystem notificationSystem;
    private final GameConfig gameConfig;

    public WarService(KingdomRepository kingdomRepository, IWarReportRepository warReportRepository, INotificationSystem notificationSystem, GameConfig gameConfig)
    {
        this.kingdomRepository = kingdomRepository;
        this.warReportRepository = warReportRepository;
        this.notificationSystem = notificationSystem;
        this.gameConfig = gameConfig;
    }

    @Transactional
    public Optional<ActionResult<SpyReportDto>> sendSpies(String kingdomName, SendSpiesDto sendSpiesDto)
    {
        log.info("[{}] sending spies {}", kingdomName, sendSpiesDto);
        var maybeKingdom = kingdomRepository.getKingdomByName(kingdomName);
        if (maybeKingdom.isEmpty())
        {
            return Optional.empty();
        }

        var maybeTarget = kingdomRepository.getKingdomByName(sendSpiesDto.targetKingdomName());
        if (maybeTarget.isEmpty())
        {
            return Optional.of(ActionResult.failure("Target kingdom does not exist"));
        }

        var kingdom = maybeKingdom.get();
        var result = new KingdomSpyAction(kingdom, gameConfig).sendSpies(maybeTarget.get(), sendSpiesDto.spies());
        result.data().ifPresent(report ->
        {
            warReportRepository.addSpyReport(report);
            if (!report.success())
            {
                notificationSystem.create(report.targetKingdomName(), NotificationKind.spies, Utils.format("Our guards caught {} spies sent by {}", report.spiesLost(), kingdomName));
            }
        });
        kingdomRepository.update(kingdom);

        return Optional.of(result);
    }

    public List<BattleReportDto> getBattleReports(String kingdomName)
    {
        return warReportRepository.getBattleReports(kingdomName, reportsLimit);
    }

    public List<SpyReportDto> getSpyReports(String kingdomName)
    {
        return warReportRepository.getSpyReports(kingdomName, reportsLimit);
    }
}
