package austeretony.oxygen_market.client.network.operation;

import austeretony.oxygen_core.common.network.operation.NetworkOperationsHandler;
import austeretony.oxygen_market.client.MarketManagerClient;
import austeretony.oxygen_market.common.main.MarketMain;
import austeretony.oxygen_market.common.market.Deal;
import austeretony.oxygen_market.common.network.operation.MarketOperation;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

import java.util.HashSet;
import java.util.Set;

public class MarketNetworkOperationsHandlerClient implements NetworkOperationsHandler {

    @Override
    public int getId() {
        return MarketMain.MARKET_OPERATIONS_HANDLER_ID;
    }

    @Override
    public void process(EntityPlayer player, int operationIndex, ByteBuf buffer) {
        MarketOperation operation = getEnum(MarketOperation.values(), operationIndex);
        if (operation == null) return;

        if (operation == MarketOperation.DEAL_CREATED) {
            Set<Long> createdDealsIds = new HashSet<>();
            int amount = buffer.readShort();
            for (int i = 0; i < amount; i++) {
                createdDealsIds.add(buffer.readLong());
            }
            Deal deal = new Deal();
            deal.read(buffer);
            long balance = buffer.readLong();

            MarketManagerClient.instance().dealsCreated(createdDealsIds, deal, balance);
        } else if (operation == MarketOperation.DEAL_CANCELED) {
            int amount = buffer.readShort();
            Set<Long> dealsIds = new HashSet<>();
            for (int i = 0; i < amount; i++) {
                dealsIds.add(buffer.readLong());
            }

            MarketManagerClient.instance().dealsCanceled(dealsIds);
        } else if (operation == MarketOperation.PURCHASED) {
            int amount = buffer.readShort();
            Set<Long> dealsIds = new HashSet<>();
            for (int i = 0; i < amount; i++) {
                dealsIds.add(buffer.readLong());
            }
            long balance = buffer.readLong();

            MarketManagerClient.instance().purchased(dealsIds, balance);
        } else if (operation == MarketOperation.PURCHASE_FAILED) {
            int amount = buffer.readShort();
            Set<Long> dealsIds = new HashSet<>();
            for (int i = 0; i < amount; i++) {
                dealsIds.add(buffer.readLong());
            }

            MarketManagerClient.instance().purchaseFailed(dealsIds);
        }
    }
}
