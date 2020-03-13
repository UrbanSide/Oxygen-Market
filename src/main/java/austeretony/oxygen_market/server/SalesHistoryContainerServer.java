package austeretony.oxygen_market.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.persistent.AbstractPersistentData;
import austeretony.oxygen_core.common.util.StreamUtils;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_core.server.api.TimeHelperServer;

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

    @Nullable
    public SalesHistoryEntryServer getEntry(long entryId) {
        return this.history.get(entryId);
    }

    public void addEntry(SalesHistoryEntryServer entry) {
        entry.setSellerUsername(OxygenHelperServer.getPlayerSharedData(entry.getSellerUUID()).getUsername());
        entry.setBuyerUsername(OxygenHelperServer.getPlayerSharedData(entry.getBuyerUUID()).getUsername());
        this.history.put(entry.getId(), entry);
    }

    public void removeEntry(long entryId) {
        this.history.remove(entryId);
    }

    public long createId() {
        long id = TimeHelperServer.getCurrentMillis();
        while (this.history.containsKey(id))
            id++;
        return id;
    }

    @Override
    public String getDisplayName() {
        return "market:sales_history_server";
    }

    @Override
    public String getPath() {
        return OxygenHelperServer.getDataFolder() + "/server/world/market/sales_history.dat";
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
        OxygenMain.LOGGER.info("[Market] Loaded {} sales history entries.", this.getEntriesAmount());
        MarketManagerServer.instance().getSalesHistoryManager().processExpiredEntries();
    }

    @Override
    public void reset() {
        this.history.clear();
    }
}
