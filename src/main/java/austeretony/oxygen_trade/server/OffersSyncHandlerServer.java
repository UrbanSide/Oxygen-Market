package austeretony.oxygen_trade.server;

import java.util.Set;
import java.util.UUID;

import austeretony.oxygen_core.server.sync.DataSyncHandlerServer;
import austeretony.oxygen_trade.common.main.TradeMain;

public class OffersSyncHandlerServer implements DataSyncHandlerServer<PlayerOfferServer> {

    @Override
    public int getDataId() {
        return TradeMain.OFFERS_DATA_ID;
    }

    @Override
    public boolean allowSync(UUID playerUUID) {
        return true;
    }

    @Override
    public Set<Long> getIds(UUID playerUUID) {
        return TradeManagerServer.instance().getOffersContainer().getOfferIds();
    }

    @Override
    public PlayerOfferServer getEntry(UUID playerUUID, long entryId) {
        return TradeManagerServer.instance().getOffersContainer().getOffer(entryId);
    }
}
