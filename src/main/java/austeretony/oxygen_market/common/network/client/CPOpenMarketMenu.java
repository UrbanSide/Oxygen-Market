package austeretony.oxygen_market.common.network.client;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_market.client.MenuManagerClient;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;

public class CPOpenMarketMenu extends Packet {

    public CPOpenMarketMenu() {}

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {}

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        ClientReference.delegateToClientThread(MenuManagerClient::openMarketMenuDelegated);
    }
}
