package austeretony.oxygen_market.server;

import java.util.Iterator;

import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_market.common.config.MarketConfig;

public class SalesHistoryManagerServer {

    private final MarketManagerServer manager;

    public SalesHistoryManagerServer(MarketManagerServer manager) {
        this.manager = manager;
    }

    public void processExpiredEntries() {
        OxygenHelperServer.addRoutineTask(()->{
            if (this.manager.getSalesHistoryContainer().getEntriesAmount() > 0) {
                Iterator<SalesHistoryEntryServer> iterator = this.manager.getSalesHistoryContainer().getEntries().iterator();
                SalesHistoryEntryServer entry;
                long 
                currTimeMillis = System.currentTimeMillis(),
                expireTimeMillis = MarketConfig.SALES_HISTORY_EXPIRE_TIME_HOURS.asInt() * 3_600_000L;
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
                OxygenMain.LOGGER.info("[Market] Removed {} expired sales history entries.", removed);
            }
        });
    }

    public void clearSalesHistoryGlobal(int periodHours) {
        if (periodHours == - 1)
            this.manager.getSalesHistoryContainer().reset();
        else {
            Iterator<SalesHistoryEntryServer> iterator = this.manager.getSalesHistoryContainer().getEntries().iterator();
            SalesHistoryEntryServer entry;
            long 
            currTimeMillis = System.currentTimeMillis(),
            clearTimeMillis = periodHours * 3_600_000L;
            int removed = 0;
            while (iterator.hasNext()) {
                entry = iterator.next();
                if (currTimeMillis - entry.getId() < clearTimeMillis) {
                    iterator.remove();
                    removed++;
                }
            }
            if (removed > 0)
                this.manager.getSalesHistoryContainer().setChanged(true);
            OxygenMain.LOGGER.info("[Market] Cleared {} sales history entries.", removed);
        }
    }

    public void clearSalesHistoryForItem(int periodHours, ItemStackWrapper stackWrapper) {
        Iterator<SalesHistoryEntryServer> iterator = this.manager.getSalesHistoryContainer().getEntries().iterator();
        SalesHistoryEntryServer entry;
        long 
        currTimeMillis = System.currentTimeMillis(),
        clearTimeMillis = periodHours * 3_600_000L;
        int removed = 0;
        while (iterator.hasNext()) {
            entry = iterator.next();
            if ((periodHours == - 1 || currTimeMillis - entry.getId() < clearTimeMillis)
                    && entry.getStackWrapperStack().isEquals(stackWrapper)) {
                iterator.remove();
                removed++;
            }
        }
        if (removed > 0)
            this.manager.getSalesHistoryContainer().setChanged(true);
        OxygenMain.LOGGER.info("[Market] Cleared {} sales history entries.", removed);
    }
}
