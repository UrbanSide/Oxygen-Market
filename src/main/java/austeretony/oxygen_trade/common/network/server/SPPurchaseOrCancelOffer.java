package austeretony.oxygen_trade.common.network.server;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_core.server.api.RequestsFilterHelper;
import austeretony.oxygen_trade.common.main.EnumOfferAction;
import austeretony.oxygen_trade.common.main.TradeMain;
import austeretony.oxygen_trade.server.TradeManagerServer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;

public class SPPurchaseOrCancelOffer extends Packet {

    private int ordinal;

    private long offerId;

    public SPPurchaseOrCancelOffer() {}

    public SPPurchaseOrCancelOffer(EnumOfferAction action, long offerId) {
        this.ordinal = action.ordinal();
        this.offerId = offerId;
    }

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {
        buffer.writeByte(this.ordinal);
        buffer.writeLong(this.offerId);
    }

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        final EntityPlayerMP playerMP = getEntityPlayerMP(netHandler);
        if (RequestsFilterHelper.getLock(CommonReference.getPersistentUUID(playerMP), TradeMain.PURCHASE_OR_CANCEL_REQUEST_ID)) {
            final int ordinal = buffer.readByte();
            final long offerId = buffer.readLong();
            if (ordinal >= 0 && ordinal < EnumOfferAction.values().length)
                switch (EnumOfferAction.values()[ordinal]) {
                case PURCHASE:
                    OxygenHelperServer.addRoutineTask(()->TradeManagerServer.instance().getOffersManager().purchaseItem(playerMP, offerId));
                    break;
                case CANCEL:
                    OxygenHelperServer.addRoutineTask(()->TradeManagerServer.instance().getOffersManager().cancelOffer(playerMP, offerId));
                    break;
                default:
                    break;        
                }
        }
    }
}
