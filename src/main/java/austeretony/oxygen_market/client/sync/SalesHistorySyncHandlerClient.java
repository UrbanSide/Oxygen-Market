package austeretony.oxygen_market.client.sync;

import java.util.Set;

import austeretony.oxygen_core.client.sync.DataSyncHandlerClient;
import austeretony.oxygen_core.client.sync.DataSyncListener;
import austeretony.oxygen_market.client.MarketManagerClient;
import austeretony.oxygen_market.client.market.SalesHistoryEntryClient;
import austeretony.oxygen_market.common.main.MarketMain;

public class SalesHistorySyncHandlerClient implements DataSyncHandlerClient<SalesHistoryEntryClient> {

    @Override
    public int getDataId() {
        return MarketMain.SALES_HISTORY_DATA_ID;
    }

    @Override
    public Class<SalesHistoryEntryClient> getDataContainerClass() {
        return SalesHistoryEntryClient.class;
    }

    @Override
    public Set<Long> getIds() {
        return MarketManagerClient.instance().getSalesHistoryContainer().getEntriesIds();
    }

    @Override
    public void clearData() {
        MarketManagerClient.instance().getSalesHistoryContainer().reset();
    }

    @Override
    public SalesHistoryEntryClient getEntry(long entryId) {
        return MarketManagerClient.instance().getSalesHistoryContainer().getEntry(entryId);
    }

    @Override
    public void addEntry(SalesHistoryEntryClient entry) {
        MarketManagerClient.instance().getSalesHistoryContainer().addEntry(entry);
    }

    @Override
    public void save() {
        MarketManagerClient.instance().getSalesHistoryContainer().setChanged(true);
    }

    @Override
    public DataSyncListener getSyncListener() {
        return (updated)->MarketManagerClient.instance().getMenuManager().salesHistoryDataSynchronized();
    }
}
