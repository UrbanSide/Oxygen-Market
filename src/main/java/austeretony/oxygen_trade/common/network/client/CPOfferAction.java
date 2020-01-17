package austeretony.oxygen_trade.common.network.client;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_trade.client.OfferClient;
import austeretony.oxygen_trade.client.TradeManagerClient;
import austeretony.oxygen_trade.common.main.EnumOfferAction;
import austeretony.oxygen_trade.server.OfferServer;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;

public class CPOfferAction extends Packet {

    private int ordinal;

    private OfferServer offer;
    
    private long balance;

    public CPOfferAction() {}

    public CPOfferAction(EnumOfferAction action, OfferServer offer, long balance) {
        this.ordinal = action.ordinal();
        this.offer = offer;
        this.balance = balance;
    }

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {
        buffer.writeByte(this.ordinal);
        this.offer.write(buffer);
        buffer.writeLong(this.balance);
    }

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        final int ordinal = buffer.readByte();
        final OfferClient offer = new OfferClient(); 
        offer.read(buffer);
        final long balance = buffer.readLong();
        OxygenHelperClient.addRoutineTask(()->TradeManagerClient.instance().getOffersManager().performedOfferAction(EnumOfferAction.values()[ordinal], offer, balance));
    }
}
