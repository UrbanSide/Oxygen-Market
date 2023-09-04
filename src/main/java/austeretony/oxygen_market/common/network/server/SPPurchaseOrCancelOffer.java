package austeretony.oxygen_market.common.network.server;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.main.EnumOxygenStatusMessage;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_market.common.config.MarketConfig;
import austeretony.oxygen_market.common.main.EnumOfferAction;
import austeretony.oxygen_market.common.main.MarketMain;
import austeretony.oxygen_market.server.MarketManagerServer;
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
        if (OxygenHelperServer.isNetworkRequestAvailable(CommonReference.getPersistentUUID(playerMP), MarketMain.OFFER_OPERATION_REQUEST_ID)) {
            if (MarketConfig.ENABLE_MARKET_MENU_ACCESS_CLIENTSIDE.asBoolean()
                    || OxygenHelperServer.checkTimeOut(CommonReference.getPersistentUUID(playerMP), MarketMain.MARKET_MENU_TIMEOUT_ID) 
                    || CommonReference.isPlayerOpped(playerMP)) {
                final int ordinal = buffer.readByte();
                final long offerId = buffer.readLong();
                if (ordinal >= 0 && ordinal < EnumOfferAction.values().length)
                    switch (EnumOfferAction.values()[ordinal]) {
                    case PURCHASE:
                        OxygenHelperServer.addRoutineTask(()->MarketManagerServer.instance().getOffersManager().purchaseItem(playerMP, offerId));
                        break;
                    case CANCEL:
                        OxygenHelperServer.addRoutineTask(()->MarketManagerServer.instance().getOffersManager().cancelOffer(playerMP, offerId));
                        break;
                    default:
                        break;        
                    }
            } else
                OxygenHelperServer.sendStatusMessage(playerMP, OxygenMain.OXYGEN_CORE_MOD_INDEX, EnumOxygenStatusMessage.ACTION_TIMEOUT.ordinal()); 
        }
    }
}
