package austeretony.oxygen_market.server;

import java.util.UUID;

import austeretony.oxygen_market.common.main.EnumOfferAction;
import net.minecraft.entity.player.EntityPlayerMP;

public class QueuedOfferAction {

    final EntityPlayerMP playerMP;

    final EnumOfferAction action;

    final long offerId;

    protected QueuedOfferAction(EntityPlayerMP playerMP, EnumOfferAction action, long offerId) {
        this.playerMP = playerMP;
        this.action = action;
        this.offerId = offerId;
    }
}
