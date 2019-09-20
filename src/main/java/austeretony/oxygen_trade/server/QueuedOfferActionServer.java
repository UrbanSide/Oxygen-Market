package austeretony.oxygen_trade.server;

import java.util.UUID;

import austeretony.oxygen_trade.common.main.EnumOfferAction;

public class QueuedOfferActionServer {

    final UUID playerUUID;

    final EnumOfferAction action;

    final long offerId;

    protected QueuedOfferActionServer(UUID playerUUID, EnumOfferAction action, long offerId) {
        this.playerUUID = playerUUID;
        this.action = action;
        this.offerId = offerId;
    }
}
