package austeretony.oxygen_trade.server;

import java.util.UUID;

import austeretony.oxygen_core.common.item.ItemStackWrapper;

public class QueuedOfferCreation {

    final UUID playerUUID;

    final ItemStackWrapper stackWrapper;

    final int amount;

    final long price;

    protected QueuedOfferCreation(UUID playerUUID, ItemStackWrapper stackWrapper, int amount, long price) {
        this.playerUUID = playerUUID;
        this.stackWrapper = stackWrapper;
        this.amount = amount;
        this.price = price;
    }
}
