package austeretony.oxygen_trade.client;

import java.util.Set;

import austeretony.oxygen_core.client.sync.DataSyncHandlerClient;
import austeretony.oxygen_core.client.sync.DataSyncListener;
import austeretony.oxygen_trade.common.main.TradeMain;

public class OffersSyncHandlerClient implements DataSyncHandlerClient<PlayerOfferClient> {

    @Override
    public int getDataId() {
        return TradeMain.OFFERS_DATA_ID;
    }

    @Override
    public Class<PlayerOfferClient> getDataContainerClass() {
        return PlayerOfferClient.class;
    }

    @Override
    public Set<Long> getIds() {
        return TradeManagerClient.instance().getOffersContainer().getOfferIds();
    }

    @Override
    public void clearData() {
        TradeManagerClient.instance().getOffersContainer().reset();
    }

    @Override
    public PlayerOfferClient getEntry(long entryId) {
        return TradeManagerClient.instance().getOffersContainer().getOffer(entryId);
    }

    @Override
    public void addEntry(PlayerOfferClient entry) {
        TradeManagerClient.instance().getOffersContainer().addOffer(entry);
    }

    @Override
    public void save() {
        TradeManagerClient.instance().getOffersContainer().setChanged(true);
    }

    @Override
    public DataSyncListener getSyncListener() {
        return (updated)->TradeManagerClient.instance().getTradeMenuManager().offersSynchronized();
    }
}
