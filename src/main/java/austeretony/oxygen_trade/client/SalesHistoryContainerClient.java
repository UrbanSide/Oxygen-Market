package austeretony.oxygen_trade.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.persistent.AbstractPersistentData;
import austeretony.oxygen_core.common.util.StreamUtils;
import austeretony.oxygen_trade.common.main.TradeMain;

public class SalesHistoryContainerClient extends AbstractPersistentData {

    private final Map<Long, SalesHistoryEntryClient> history = new ConcurrentHashMap<>();

    public int getEntriesAmount() {
        return this.history.size();
    }

    public Set<Long> getEntriesIds() {
        return this.history.keySet();
    }

    public Collection<SalesHistoryEntryClient> getEntries() {
        return this.history.values();
    }

    public boolean isEntryExist(long entryId) {
        return this.history.containsKey(entryId);
    }

    public SalesHistoryEntryClient getEntry(long entryId) {
        return this.history.get(entryId);
    }

    public void addEntry(SalesHistoryEntryClient entry) {
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
        return OxygenHelperClient.getDataFolder() + "/client/world/trade/sales_history.dat";
    }

    @Override
    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write(this.history.size(), bos);
        for (SalesHistoryEntryClient offer : this.history.values())
            offer.write(bos);
    }

    @Override
    public void read(BufferedInputStream bis) throws IOException {
        int amount = StreamUtils.readInt(bis);
        SalesHistoryEntryClient entry;
        for (int i = 0; i < amount; i++) {
            entry = new SalesHistoryEntryClient();
            entry.read(bis);
            this.addEntry(entry);
        }
        TradeMain.LOGGER.info("Loaded <{}> sales history entries.", this.getEntriesAmount());
    }

    @Override
    public void reset() {
        this.history.clear();
    }
}
