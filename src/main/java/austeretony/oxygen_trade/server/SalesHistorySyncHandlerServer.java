package austeretony.oxygen_trade.server;

import java.util.Set;
import java.util.UUID;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_core.server.api.PrivilegesProviderServer;
import austeretony.oxygen_core.server.sync.DataSyncHandlerServer;
import austeretony.oxygen_trade.common.config.TradeConfig;
import austeretony.oxygen_trade.common.main.EnumTradePrivilege;
import austeretony.oxygen_trade.common.main.TradeMain;

public class SalesHistorySyncHandlerServer implements DataSyncHandlerServer<SalesHistoryEntryServer> {

    @Override
    public int getDataId() {
        return TradeMain.SALES_HISTORY_DATA_ID;
    }

    @Override
    public boolean allowSync(UUID playerUUID) {
        return (TradeConfig.ENABLE_TRADE_MENU_ACCESS_CLIENTSIDE.asBoolean() || OxygenHelperServer.checkTimeOut(playerUUID, TradeMain.TRADE_MENU_TIMEOUT_ID) || CommonReference.isPlayerOpped(CommonReference.playerByUUID(playerUUID))) 
                && PrivilegesProviderServer.getAsBoolean(playerUUID, EnumTradePrivilege.SALES_HISTORY_ACCESS.id(), TradeConfig.ENABLE_SALES_HISTORY_SYNC.asBoolean());
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
