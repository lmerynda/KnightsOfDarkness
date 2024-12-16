package com.knightsofdarkness.game.kingdom;

import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.common.kingdom.SendCarriersDto;
import com.knightsofdarkness.common.kingdom.SendCarriersResult;
import com.knightsofdarkness.common.kingdom.UnitName;
import com.knightsofdarkness.game.Id;

public class KingdomCarriersAction {
    private final Kingdom kingdom;

    public KingdomCarriersAction(Kingdom kingdom)
    {
        this.kingdom = kingdom;
    }

    public SendCarriersResult sendCarriers(SendCarriersDto sendCarriersDto)
    {
        if (sendCarriersDto.amount() <= 0)
        {
            return SendCarriersResult.failure("Amount to send must be greater than 0");
        }

        var resource = sendCarriersDto.resource();
        int singleCarrierCapacity = kingdom.getConfig().carrierCapacity().get(resource);
        int carriersCapacity = kingdom.getUnits().getAvailableCount(UnitName.carrier) * singleCarrierCapacity;
        if (carriersCapacity <= 0)
        {
            return SendCarriersResult.failure("You have not enough carriers to send");
        }

        int resourceCount = kingdom.getResources().getCount(ResourceName.from(resource));
        int amountPossibleToSend = Math.min(resourceCount, sendCarriersDto.amount());
        int amountToSend = Math.min(amountPossibleToSend, carriersCapacity);

        if (amountToSend <= 0)
        {
            return SendCarriersResult.failure("You have not enough resources to send");
        }

        int carriersToSend = (int) Math.ceil((double) amountToSend / singleCarrierCapacity);

        int turnsLeft = kingdom.getConfig().common().turnsToDeliverResources();
        KingdomCarriersOnTheMove carriersOnTheMove = new KingdomCarriersOnTheMove(Id.generate(),
                sendCarriersDto.destinationKingdomName(), turnsLeft, carriersToSend, sendCarriersDto.resource(),
                amountToSend);
        kingdom.getCarriersOnTheMove().add(carriersOnTheMove);
        kingdom.getResources().subtractCount(ResourceName.from(resource), amountToSend);
        kingdom.getUnits().moveAvailableToMobile(UnitName.carrier, carriersToSend);

        var message = amountToSend == sendCarriersDto.amount() ? "Carriers sent and should arrive in a few turns"
                : "Partial transfer sent, not enough carriers to carry everything requested";
        return SendCarriersResult.success(message,
                new SendCarriersDto(sendCarriersDto.destinationKingdomName(), resource, amountToSend));
    }

    public void withdrawCarriers(KingdomCarriersOnTheMove carriersOnTheMove)
    {
        kingdom.getCarriersOnTheMove().remove(carriersOnTheMove);
        kingdom.getResources().addCount(ResourceName.from(carriersOnTheMove.getResource()),
                carriersOnTheMove.getResourceCount());
        kingdom.getUnits().moveMobileToAvailable(UnitName.carrier, carriersOnTheMove.getCarriersCount());
    }
}
