package austeretony.oxygen_market.server.network.operation;

import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.network.operation.NetworkOperationsHandler;
import austeretony.oxygen_market.common.main.MarketMain;
import austeretony.oxygen_market.common.network.operation.MarketOperation;
import austeretony.oxygen_market.server.MarketManagerServer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.HashSet;
import java.util.Set;

public class MarketNetworkOperationsHandlerServer implements NetworkOperationsHandler {

    @Override
    public int getId() {
        return MarketMain.MARKET_OPERATIONS_HANDLER_ID;
    }

    @Override
    public void process(EntityPlayer player, int operationIndex, ByteBuf buffer) {
        MarketOperation operation = getEnum(MarketOperation.values(), operationIndex);
        if (operation == null) return;

        if (operation == MarketOperation.CREATE_DEAL) {
            int dealsQuantity = buffer.readShort();
            ItemStackWrapper stackWrapper = ItemStackWrapper.read(buffer);
            int quantityPerDeal = buffer.readShort();
            long pricePerDeal = buffer.readLong();

            MarketManagerServer.instance().createDeal((EntityPlayerMP) player, dealsQuantity, stackWrapper, quantityPerDeal,
                    pricePerDeal);
        } else if (operation == MarketOperation.CANCEL_DEAL) {
            int amount = buffer.readShort();
            Set<Long> dealsIds = new HashSet<>();
            for (int i = 0; i < amount; i++) {
                dealsIds.add(buffer.readLong());
            }

            MarketManagerServer.instance().cancelDeal((EntityPlayerMP) player, dealsIds);
        } else if (operation == MarketOperation.PURCHASE) {
            int amount = buffer.readShort();
            Set<Long> dealsIds = new HashSet<>();
            for (int i = 0; i < amount; i++) {
                dealsIds.add(buffer.readLong());
            }

            MarketManagerServer.instance().purchase((EntityPlayerMP) player, dealsIds);
        }
    }
}
