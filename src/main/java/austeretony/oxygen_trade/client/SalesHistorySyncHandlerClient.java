package austeretony.oxygen_trade.client;

import java.util.Set;

import austeretony.oxygen_core.client.sync.DataSyncHandlerClient;
import austeretony.oxygen_core.client.sync.DataSyncListener;
import austeretony.oxygen_trade.common.main.TradeMain;

public class SalesHistorySyncHandlerClient implements DataSyncHandlerClient<SalesHistoryEntryClient> {

    @Override
    public int getDataId() {
        return TradeMain.SALES_HISTORY_DATA_ID;
    }

    @Override
    public Class<SalesHistoryEntryClient> getDataContainerClass() {
        return SalesHistoryEntryClient.class;
    }

    @Override
    public Set<Long> getIds() {
        return TradeManagerClient.instance().getSalesHistoryContainer().getEntriesIds();
    }

    @Override
    public void clearData() {
        TradeManagerClient.instance().getSalesHistoryContainer().reset();
    }

    @Override
    public SalesHistoryEntryClient getEntry(long entryId) {
        return TradeManagerClient.instance().getSalesHistoryContainer().getEntry(entryId);
    }

    @Override
    public void addEntry(SalesHistoryEntryClient entry) {
        TradeManagerClient.instance().getSalesHistoryContainer().addEntry(entry);
    }

    @Override
    public void save() {
        TradeManagerClient.instance().getSalesHistoryContainer().setChanged(true);
    }

    @Override
    public DataSyncListener getSyncListener() {
        return (updated)->TradeManagerClient.instance().getTradeMenuManager().salesHistoryDataSynchronized();
    }
}
