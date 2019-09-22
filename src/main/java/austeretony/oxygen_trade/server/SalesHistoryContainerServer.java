package austeretony.oxygen_trade.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import austeretony.oxygen_core.common.persistent.AbstractPersistentData;
import austeretony.oxygen_core.common.util.StreamUtils;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_trade.common.config.TradeConfig;
import austeretony.oxygen_trade.common.main.TradeMain;

public class SalesHistoryContainerServer extends AbstractPersistentData {

    private final Map<Long, SalesHistoryEntryServer> history = new ConcurrentHashMap<>();

    public int getEntriesAmount() {
        return this.history.size();
    }

    public Set<Long> getEntriesIds() {
        return this.history.keySet();
    }

    public Collection<SalesHistoryEntryServer> getEntries() {
        return this.history.values();
    }

    public boolean isEntryExist(long entryId) {
        return this.history.containsKey(entryId);
    }

    public SalesHistoryEntryServer getEntry(long entryId) {
        return this.history.get(entryId);
    }

    public void addEntry(SalesHistoryEntryServer entry) {
        entry.updateSellerUsername();
        entry.updateBuyerUsername();
        this.history.put(entry.getId(), entry);
    }

    public void removeEntry(long entryId) {
        this.history.remove(entryId);
    }

    @Override
    public String getDisplayName() {
        return "sales_history";
    }

    @Override
    public String getPath() {
        return OxygenHelperServer.getDataFolder() + "/server/world/trade/sales_history.dat";
    }

    @Override
    public long getSaveDelayMinutes() {
        return TradeConfig.DATA_SAVE_DELAY_MINUTES.getIntValue();
    }

    @Override
    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write(this.history.size(), bos);
        for (SalesHistoryEntryServer offer : this.history.values())
            offer.write(bos);
    }

    @Override
    public void read(BufferedInputStream bis) throws IOException {
        int amount = StreamUtils.readInt(bis);
        SalesHistoryEntryServer entry;
        for (int i = 0; i < amount; i++) {
            entry = new SalesHistoryEntryServer();
            entry.read(bis);
            this.addEntry(entry);
        }
        TradeMain.LOGGER.info("Loaded {} sales history entries.", this.getEntriesAmount());
        TradeManagerServer.instance().getSalesHistoryManager().processExpiredEntries();
    }

    @Override
    public void reset() {
        this.history.clear();
    }
}
