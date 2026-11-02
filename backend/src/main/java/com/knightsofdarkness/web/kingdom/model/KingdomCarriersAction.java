package com.knightsofdarkness.web.kingdom.model;

import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.kingdom.SendCarriersDto;
import com.knightsofdarkness.web.common.ActionResult;
import com.knightsofdarkness.web.common.kingdom.UnitName;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.utils.Id;

public class KingdomCarriersAction {
    private final KingdomEntity kingdom;
    private final GameConfig gameConfig;

    public KingdomCarriersAction(KingdomEntity kingdom, GameConfig gameConfig)
    {
        this.kingdom = kingdom;
        this.gameConfig = gameConfig;
    }

    public ActionResult<SendCarriersDto> sendCarriers(SendCarriersDto sendCarriersDto)
    {
        if (sendCarriersDto.count() <= 0)
        {
            return ActionResult.failure("Amount to send must be greater than 0");
        }

        var resource = sendCarriersDto.resource();
        int singleCarrierCapacity = gameConfig.carrierCapacity().get(resource);
        int carriersCapacity = kingdom.getUnits().getAvailableCount(UnitName.carrier) * singleCarrierCapacity;
        if (carriersCapacity <= 0)
        {
            return ActionResult.failure("You have not enough carriers to send");
        }

        int resourceCount = kingdom.getResources().getCount(ResourceName.from(resource));
        int countPossibleToSend = Math.min(resourceCount, sendCarriersDto.count());
        int countToSend = Math.min(countPossibleToSend, carriersCapacity);

        if (countToSend <= 0)
        {
            return ActionResult.failure("You have not enough resources to send");
        }

        int carriersToSend = (int) Math.ceil((double) countToSend / singleCarrierCapacity);

        int turnsLeft = gameConfig.common().turnsToDeliverResources();
        KingdomCarriersOnTheMoveEntity carriersOnTheMove = new KingdomCarriersOnTheMoveEntity(Id.generate(), kingdom,
                sendCarriersDto.destinationKingdomName(), turnsLeft, carriersToSend, sendCarriersDto.resource(),
                countToSend);
        kingdom.getCarriersOnTheMove().add(carriersOnTheMove);
        kingdom.getResources().subtractCount(ResourceName.from(resource), countToSend);
        kingdom.getUnits().moveAvailableToMobile(UnitName.carrier, carriersToSend);

        var message = countToSend == sendCarriersDto.count() ? "Carriers sent and should arrive in a few turns"
                : "Partial transfer sent, not enough carriers to carry everything requested";
        return ActionResult.success(message,
                new SendCarriersDto(sendCarriersDto.destinationKingdomName(), resource, countToSend));
    }

    public void withdrawCarriers(KingdomCarriersOnTheMoveEntity carriersOnTheMove)
    {
        kingdom.getCarriersOnTheMove().remove(carriersOnTheMove);
        kingdom.getResources().addCount(ResourceName.from(carriersOnTheMove.getResource()),
                carriersOnTheMove.getResourceCount());
        kingdom.getUnits().moveMobileToAvailable(UnitName.carrier, carriersOnTheMove.getCarriersCount());
    }
}
