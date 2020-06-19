package austeretony.oxygen_market.server.sync;

import java.util.Set;
import java.util.UUID;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_core.server.api.PrivilegesProviderServer;
import austeretony.oxygen_core.server.sync.DataSyncHandlerServer;
import austeretony.oxygen_market.common.config.MarketConfig;
import austeretony.oxygen_market.common.main.EnumMarketPrivilege;
import austeretony.oxygen_market.common.main.MarketMain;
import austeretony.oxygen_market.server.MarketManagerServer;
import austeretony.oxygen_market.server.market.OfferServer;

public class OffersSyncHandlerServer implements DataSyncHandlerServer<OfferServer> {

    @Override
    public int getDataId() {
        return MarketMain.OFFERS_DATA_ID;
    }

    @Override
    public boolean allowSync(UUID playerUUID) {
        return (MarketConfig.ENABLE_MARKET_MENU_ACCESS_CLIENTSIDE.asBoolean() || OxygenHelperServer.checkTimeOut(playerUUID, MarketMain.MARKET_MENU_TIMEOUT_ID) || CommonReference.isPlayerOpped(CommonReference.playerByUUID(playerUUID))) 
                && PrivilegesProviderServer.getAsBoolean(playerUUID, EnumMarketPrivilege.MARKET_ACCESS.id(), true);
    }

    @Override
    public Set<Long> getIds(UUID playerUUID) {
        return MarketManagerServer.instance().getOffersContainer().getOfferIds();
    }

    @Override
    public OfferServer getEntry(UUID playerUUID, long entryId) {
        return MarketManagerServer.instance().getOffersContainer().getOffer(entryId);
    }
}
