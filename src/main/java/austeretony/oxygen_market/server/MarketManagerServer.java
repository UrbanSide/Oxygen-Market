package austeretony.oxygen_market.server;

import java.util.concurrent.TimeUnit;

import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_core.server.item.ItemsBlackList;
import austeretony.oxygen_market.common.main.EnumMarketStatusMessage;
import austeretony.oxygen_market.common.main.MarketMain;
import net.minecraft.entity.player.EntityPlayerMP;

public final class MarketManagerServer {

    private static MarketManagerServer instance;

    private final OffersContainerServer offersContainer = new OffersContainerServer();

    private final SalesHistoryContainerServer salesHistoryContainer = new SalesHistoryContainerServer();

    private final OffersManagerServer offersManager;

    private final SalesHistoryManagerServer salesHistoryManager;

    private final ItemsBlackList itemsBlackList = ItemsBlackList.create("market");

    private MarketManagerServer() {
        this.offersManager = new OffersManagerServer(this);
        this.salesHistoryManager = new SalesHistoryManagerServer(this);
    }

    private void registerPersistentData() {
        OxygenHelperServer.registerPersistentData(this.offersContainer);
        OxygenHelperServer.registerPersistentData(this.salesHistoryContainer);
    }

    private void scheduleRepeatableProcesses() {
        OxygenHelperServer.getSchedulerExecutorService().scheduleAtFixedRate(this.offersManager::process, 500L, 500L, TimeUnit.MILLISECONDS);
    }

    public static void create() {
        if (instance == null) {
            instance = new MarketManagerServer();
            instance.registerPersistentData();
            instance.scheduleRepeatableProcesses();
        }
    }

    public static MarketManagerServer instance() {
        return instance;
    }

    public OffersContainerServer getOffersContainer() {
        return this.offersContainer;
    }

    public SalesHistoryContainerServer getSalesHistoryContainer() {
        return this.salesHistoryContainer;
    }

    public OffersManagerServer getOffersManager() {
        return this.offersManager;
    }

    public SalesHistoryManagerServer getSalesHistoryManager() {
        return this.salesHistoryManager;
    }

    public ItemsBlackList getItemsBlackList() {
        return this.itemsBlackList;
    }

    public void worldLoaded() {
        OxygenHelperServer.loadPersistentDataAsync(this.offersContainer);
        OxygenHelperServer.loadPersistentDataAsync(this.salesHistoryContainer);
    }

    public void sendStatusMessage(EntityPlayerMP playerMP, EnumMarketStatusMessage status) {
        OxygenHelperServer.sendStatusMessage(playerMP, MarketMain.MARKET_MOD_INDEX, status.ordinal());
    }
}
