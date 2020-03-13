package austeretony.oxygen_market.server;

import java.util.Set;
import java.util.UUID;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_core.server.api.PrivilegesProviderServer;
import austeretony.oxygen_core.server.sync.DataSyncHandlerServer;
import austeretony.oxygen_market.common.config.MarketConfig;
import austeretony.oxygen_market.common.main.EnumMarketPrivilege;
import austeretony.oxygen_market.common.main.MarketMain;

public class SalesHistorySyncHandlerServer implements DataSyncHandlerServer<SalesHistoryEntryServer> {

    @Override
    public int getDataId() {
        return MarketMain.SALES_HISTORY_DATA_ID;
    }

    @Override
    public boolean allowSync(UUID playerUUID) {
        return (MarketConfig.ENABLE_MARKET_MENU_ACCESS_CLIENTSIDE.asBoolean() || OxygenHelperServer.checkTimeOut(playerUUID, MarketMain.MARKET_MENU_TIMEOUT_ID) || CommonReference.isPlayerOpped(CommonReference.playerByUUID(playerUUID))) 
                && PrivilegesProviderServer.getAsBoolean(playerUUID, EnumMarketPrivilege.SALES_HISTORY_ACCESS.id(), MarketConfig.ENABLE_SALES_HISTORY_SYNC.asBoolean());
    }

    @Override
    public Set<Long> getIds(UUID playerUUID) {
        return MarketManagerServer.instance().getSalesHistoryContainer().getEntriesIds();
    }

    @Override
    public SalesHistoryEntryServer getEntry(UUID playerUUID, long entryId) {
        return MarketManagerServer.instance().getSalesHistoryContainer().getEntry(entryId);
    }
}
