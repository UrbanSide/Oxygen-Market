package austeretony.oxygen_trade.server;

import java.util.Set;
import java.util.UUID;

import austeretony.oxygen_core.server.sync.DataSyncHandlerServer;
import austeretony.oxygen_trade.common.config.TradeConfig;
import austeretony.oxygen_trade.common.main.TradeMain;

public class SalesHistorySyncHandlerServer implements DataSyncHandlerServer<SalesHistoryEntryServer> {

    @Override
    public int getDataId() {
        return TradeMain.SALES_HISTORY_DATA_ID;
    }

    @Override
    public boolean allowSync(UUID playerUUID) {
        return TradeConfig.ENABLE_SALES_HISTORY.getBooleanValue() && TradeConfig.ENABLE_SALES_HISTORY_SYNC.getBooleanValue();
    }

    @Override
    public Set<Long> getIds(UUID playerUUID) {
        return TradeManagerServer.instance().getSalesHistoryContainer().getEntriesIds();
    }

    @Override
    public SalesHistoryEntryServer getEntry(UUID playerUUID, long entryId) {
        return TradeManagerServer.instance().getSalesHistoryContainer().getEntry(entryId);
    }
}