package austeretony.oxygen_market.client.sync;

import austeretony.oxygen_core.client.sync.DataSyncHandlerClient;
import austeretony.oxygen_core.client.sync.DataSyncListener;
import austeretony.oxygen_market.client.MarketManagerClient;
import austeretony.oxygen_market.client.gui.market.MarketScreen;
import austeretony.oxygen_market.common.main.MarketMain;
import austeretony.oxygen_market.common.market.Deal;

import javax.annotation.Nullable;
import java.util.Map;

public class MarketDealsSyncHandlerClient implements DataSyncHandlerClient<Deal> {

    @Override
    public int getDataId() {
        return MarketMain.DATA_ID_MARKET_DEALS;
    }

    @Override
    public Class<Deal> getSynchronousEntryClass() {
        return Deal.class;
    }

    @Override
    public Map<Long, Deal> getDataMap() {
        return MarketManagerClient.instance().getDealsMap();
    }

    @Override
    public void clear() {
        MarketManagerClient.instance().getDealsMap().clear();
    }

    @Override
    public void save() {
        MarketManagerClient.instance().markChanged();
    }

    @Nullable
    @Override
    public DataSyncListener getSyncListener() {
        return updated -> MarketScreen.dealsDataSynchronized();
    }
}
