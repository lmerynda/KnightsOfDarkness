package com.knightsofdarkness.web.monument;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.knightsofdarkness.web.common.ActionResult;
import com.knightsofdarkness.web.common.alliance.FoundMonumentDto;
import com.knightsofdarkness.web.common.alliance.MonumentContributionDto;
import com.knightsofdarkness.web.common.alliance.MonumentDto;
import com.knightsofdarkness.web.common.alliance.MonumentLedgerDto;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.messaging.NotificationKind;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.game.config.Monument;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
import com.knightsofdarkness.web.kingdom.model.KingdomRepository;
import com.knightsofdarkness.web.messaging.INotificationSystem;
import com.knightsofdarkness.web.monument.model.MonumentEntity;
import com.knightsofdarkness.web.monument.model.MonumentJpaRepository;
import com.knightsofdarkness.web.utils.Utils;

/**
 * An alliance may raise one monument an era. Everyone hears when it is founded, at every stage from the half-way mark and
 * when it stands complete, so that its rivals can answer in time.
 */
@Service
public class MonumentService {
    private final MonumentJpaRepository monumentRepository;
    private final KingdomRepository kingdomRepository;
    private final INotificationSystem notificationSystem;
    private final GameConfig gameConfig;

    public MonumentService(MonumentJpaRepository monumentRepository, KingdomRepository kingdomRepository, INotificationSystem notificationSystem, GameConfig gameConfig)
    {
        this.monumentRepository = monumentRepository;
        this.kingdomRepository = kingdomRepository;
        this.notificationSystem = notificationSystem;
        this.gameConfig = gameConfig;
    }

    @Transactional(readOnly = true)
    public List<MonumentDto> getMonuments()
    {
        return monumentRepository.findAll().stream().map(monument -> monument.toDto(rules()))
                .sorted(Comparator.comparingDouble(MonumentDto::stages).reversed().thenComparing(MonumentDto::allianceName)).toList();
    }

    @Transactional(readOnly = true)
    public Optional<MonumentLedgerDto> getLedger(String kingdomName)
    {
        return kingdomRepository.getKingdomByName(kingdomName).flatMap(KingdomEntity::getAlliance)
                .flatMap(alliance -> monumentRepository.findById(alliance.getName())).map(monument -> monument.toLedgerDto(rules()));
    }

    @Transactional
    public ActionResult<Void> found(String emperor, FoundMonumentDto foundDto)
    {
        var alliance = kingdomRepository.getKingdomByName(emperor).flatMap(KingdomEntity::getAlliance);
        if (alliance.isEmpty())
        {
            return ActionResult.failure("Only an alliance can found a monument");
        }
        if (!alliance.get().getEmperor().equals(emperor))
        {
            return ActionResult.failure("Only the emperor can found the monument");
        }
        var allianceName = alliance.get().getName();
        if (monumentRepository.existsById(allianceName))
        {
            return ActionResult.failure(allianceName + " already has its monument");
        }
        if (alliance.get().getKingdoms().stream().noneMatch(member -> member.getName().equals(foundDto.seat())))
        {
            return ActionResult.failure("The monument's seat must be a member's kingdom");
        }
        monumentRepository.save(new MonumentEntity(allianceName, foundDto.seat()));
        tellEveryone(Utils.format("{} has founded a monument at {}", allianceName, foundDto.seat()));
        return ActionResult.success("The monument is founded at " + foundDto.seat());
    }

    @Transactional
    public ActionResult<Void> contribute(String kingdomName, MonumentContributionDto goods)
    {
        var maybeKingdom = kingdomRepository.getKingdomByName(kingdomName);
        var alliance = maybeKingdom.flatMap(KingdomEntity::getAlliance);
        if (alliance.isEmpty())
        {
            return ActionResult.failure("Only an alliance's members can pay into a monument");
        }
        var monument = monumentRepository.findByAllianceNameForUpdate(alliance.get().getName());
        if (monument.isEmpty())
        {
            return ActionResult.failure("Your alliance has no monument");
        }
        if (goods.gold() < 0 || goods.iron() < 0 || goods.tools() < 0 || goods.food() < 0)
        {
            return ActionResult.failure("A monument takes goods, it gives none back");
        }
        if ((long) goods.gold() + goods.iron() + goods.tools() + goods.food() == 0)
        {
            return ActionResult.failure("Choose what to pay into the monument");
        }
        var kingdom = maybeKingdom.get();
        var resources = kingdom.getResources();
        if (goods.gold() > resources.getCount(ResourceName.gold) || goods.iron() > resources.getCount(ResourceName.iron) || goods.tools() > resources.getCount(ResourceName.tools) || goods.food() > resources.getCount(ResourceName.food))
        {
            return ActionResult.failure("You don't hold that much");
        }

        resources.subtractCount(ResourceName.gold, goods.gold());
        resources.subtractCount(ResourceName.iron, goods.iron());
        resources.subtractCount(ResourceName.tools, goods.tools());
        resources.subtractCount(ResourceName.food, goods.food());
        kingdomRepository.update(kingdom);

        var paidInto = monument.get();
        paidInto.pay(goods);
        var raised = paidInto.raiseStages(rules());
        raised.forEach(stage -> announce(paidInto, stage));
        return ActionResult.success(raised.isEmpty() ? "Paid into the monument" : Utils.format("Paid into the monument, and it rose to stage {}", raised.getLast()));
    }

    /**
     * the clock takes the upkeep of every monument
     */
    @Transactional
    public void tick()
    {
        for (var monument : monumentRepository.findAllForUpdate())
        {
            if (monument.payUpkeep(rules()))
            {
                tellMembers(monument.getAllianceName(), Utils.format("The store of our monument at {} cannot pay its upkeep, and the monument is crumbling", monument.getSeat()));
            }
        }
    }

    /**
     * a member has left: a monument seated in its kingdom moves to the emperor, who leaves last
     */
    @Transactional
    public void memberLeft(String allianceName, String emperor, String kingdomName)
    {
        monumentRepository.findByAllianceNameForUpdate(allianceName).filter(monument -> monument.getSeat().equals(kingdomName)).ifPresent(monument -> {
            monument.moveSeat(emperor);
            tellEveryone(Utils.format("{} moved the seat of its monument from {} to {}", allianceName, kingdomName, emperor));
        });
    }

    @Transactional
    public void allianceDisbanded(String allianceName)
    {
        monumentRepository.findByAllianceNameForUpdate(allianceName).ifPresent(monument -> {
            monumentRepository.delete(monument);
            monumentRepository.flush();
            tellEveryone(Utils.format("The monument of {} fell with its alliance", allianceName));
        });
    }

    /**
     * an era's monuments end with it
     */
    @Transactional
    public void clear()
    {
        monumentRepository.deleteAll();
    }

    private void announce(MonumentEntity monument, int stage)
    {
        var total = rules().stages();
        if (!monument.announces(stage))
        {
            return;
        }
        if (stage == total)
        {
            tellEveryone(Utils.format("{} has completed its monument at {}", monument.getAllianceName(), monument.getSeat()));
        } else if (stage * 2 >= total)
        {
            tellEveryone(Utils.format("{} raised its monument at {} to stage {} of {}", monument.getAllianceName(), monument.getSeat(), stage, total));
        }
    }

    private void tellEveryone(String message)
    {
        kingdomRepository.getAllKingdoms().forEach(kingdom -> notificationSystem.create(kingdom.getName(), NotificationKind.monument, message));
    }

    private void tellMembers(String allianceName, String message)
    {
        kingdomRepository.getAllKingdoms().stream()
                .filter(kingdom -> kingdom.getAlliance().filter(alliance -> alliance.getName().equals(allianceName)).isPresent())
                .forEach(kingdom -> notificationSystem.create(kingdom.getName(), NotificationKind.monument, message));
    }

    private Monument rules()
    {
        return gameConfig.monument();
    }
}
