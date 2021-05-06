package austeretony.oxygen_market.server.sync;

import austeretony.oxygen_core.server.api.PrivilegesServer;
import austeretony.oxygen_core.server.sync.DataSyncHandlerServer;
import austeretony.oxygen_market.common.config.MarketConfig;
import austeretony.oxygen_market.common.main.MarketMain;
import austeretony.oxygen_market.common.main.MarketPrivileges;
import austeretony.oxygen_market.common.market.Deal;
import austeretony.oxygen_market.server.MarketManagerServer;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;

public class MarketDealsSyncHandlerServer implements DataSyncHandlerServer<Deal> {

    @Override
    public int getDataId() {
        return MarketMain.DATA_ID_MARKET_DEALS;
    }

    @Override
    public boolean allowSync(UUID playerUUID) {
        return PrivilegesServer.getBoolean(playerUUID, MarketPrivileges.MARKET_ACCESS.getId(),
                MarketConfig.ENABLE_MARKET_ACCESS.asBoolean());
    }

    @Nonnull
    @Override
    public Map<Long, Deal> getDataMap(UUID playerUUID) {
        return MarketManagerServer.instance().getDealsMap();
    }
}
