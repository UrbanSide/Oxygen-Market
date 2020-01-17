package austeretony.oxygen_trade.common.network.server;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.main.EnumOxygenStatusMessage;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_trade.common.config.TradeConfig;
import austeretony.oxygen_trade.common.main.TradeMain;
import austeretony.oxygen_trade.server.TradeManagerServer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;

public class SPCreateOffer extends Packet {

    private ItemStackWrapper stackWrapper;

    private int amount;

    private long price;

    public SPCreateOffer() {}

    public SPCreateOffer(ItemStackWrapper stackWrapper, int amount, long price) {
        this.stackWrapper = stackWrapper;
        this.amount = amount;
        this.price = price;
    }

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {
        this.stackWrapper.write(buffer);
        buffer.writeShort(this.amount);
        buffer.writeLong(this.price);
    }

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {  
        final EntityPlayerMP playerMP = getEntityPlayerMP(netHandler);
        if (OxygenHelperServer.isNetworkRequestAvailable(CommonReference.getPersistentUUID(playerMP), TradeMain.OFFER_OPERATION_REQUEST_ID)) {
            if (TradeConfig.ENABLE_TRADE_MENU_ACCESS_CLIENTSIDE.asBoolean() 
                    || OxygenHelperServer.checkTimeOut(CommonReference.getPersistentUUID(playerMP), TradeMain.TRADE_MENU_TIMEOUT_ID) 
                    || CommonReference.isPlayerOpped(playerMP)) {
                final ItemStackWrapper stackWrapper = ItemStackWrapper.read(buffer);
                final int amount = buffer.readShort();
                final long price = buffer.readLong();
                OxygenHelperServer.addRoutineTask(()->TradeManagerServer.instance().getOffersManager().createOffer(playerMP, stackWrapper, amount, price));
            } else
                OxygenHelperServer.sendStatusMessage(playerMP, OxygenMain.OXYGEN_CORE_MOD_INDEX, EnumOxygenStatusMessage.ACTION_TIMEOUT.ordinal()); 
        }
    }
}
