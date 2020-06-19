package austeretony.oxygen_market.client.sync;

import java.util.Set;

import austeretony.oxygen_core.client.sync.DataSyncHandlerClient;
import austeretony.oxygen_core.client.sync.DataSyncListener;
import austeretony.oxygen_market.client.MarketManagerClient;
import austeretony.oxygen_market.client.market.OfferClient;
import austeretony.oxygen_market.common.main.MarketMain;

public class OffersSyncHandlerClient implements DataSyncHandlerClient<OfferClient> {

    @Override
    public int getDataId() {
        return MarketMain.OFFERS_DATA_ID;
    }

    @Override
    public Class<OfferClient> getDataContainerClass() {
        return OfferClient.class;
    }

    @Override
    public Set<Long> getIds() {
        return MarketManagerClient.instance().getOffersContainer().getOfferIds();
    }

    @Override
    public void clearData() {
        MarketManagerClient.instance().getOffersContainer().reset();
    }

    @Override
    public OfferClient getEntry(long entryId) {
        return MarketManagerClient.instance().getOffersContainer().getOffer(entryId);
    }

    @Override
    public void addEntry(OfferClient entry) {
        MarketManagerClient.instance().getOffersContainer().addOffer(entry);
    }

    @Override
    public void save() {
        MarketManagerClient.instance().getOffersContainer().setChanged(true);
    }

    @Override
    public DataSyncListener getSyncListener() {
        return (updated)->MarketManagerClient.instance().getMenuManager().offersSynchronized();
    }
}
