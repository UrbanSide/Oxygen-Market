package austeretony.oxygen_trade.common.network.client;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_trade.client.TradeManagerClient;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;

public class CPOpenTradeMenu extends Packet {

    public CPOpenTradeMenu() {}

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {}

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        ClientReference.delegateToClientThread(()->TradeManagerClient.instance().getTradeMenuManager().openTradeMenu());
    }
}
