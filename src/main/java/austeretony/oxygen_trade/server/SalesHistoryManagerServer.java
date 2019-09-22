package austeretony.oxygen_trade.server;

import java.util.Iterator;

import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_trade.common.config.TradeConfig;
import austeretony.oxygen_trade.common.main.TradeMain;

public class SalesHistoryManagerServer {

    private final TradeManagerServer manager;

    public SalesHistoryManagerServer(TradeManagerServer manager) {
        this.manager = manager;
    }

    public void processExpiredEntries() {
        OxygenHelperServer.addRoutineTask(()->{
            if (this.manager.getSalesHistoryContainer().getEntriesAmount() > 0) {
                Iterator<SalesHistoryEntryServer> iterator = this.manager.getSalesHistoryContainer().getEntries().iterator();
                SalesHistoryEntryServer entry;
                long 
                currTimeMillis = System.currentTimeMillis(),
                expireTimeMillis = TradeConfig.SALES_HISTORY_EXPIRE_TIME_HOURS.getIntValue() * 3_600_000L;
                int removed = 0;
                while (iterator.hasNext()) {
                    entry = iterator.next();
                    if (currTimeMillis - entry.getId() > expireTimeMillis) {
                        iterator.remove();
                        removed++;
                    }
                }
                if (removed > 0)
                    this.manager.getSalesHistoryContainer().setChanged(true);
                TradeMain.LOGGER.info("Removed {} expired sales entries.", removed);
            }
        });
    }
}
