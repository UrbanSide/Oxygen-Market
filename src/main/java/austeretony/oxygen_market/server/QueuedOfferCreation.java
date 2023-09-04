package austeretony.oxygen_market.server;

import austeretony.oxygen_core.common.item.ItemStackWrapper;
import net.minecraft.entity.player.EntityPlayerMP;

public class QueuedOfferCreation {

    final EntityPlayerMP playerMP;

    final ItemStackWrapper stackWrapper;

    final int amount;

    final long price;

    protected QueuedOfferCreation(EntityPlayerMP playerMP, ItemStackWrapper stackWrapper, int amount, long price) {
        this.playerMP = playerMP;
        this.stackWrapper = stackWrapper;
        this.amount = amount;
        this.price = price;
    }
}
