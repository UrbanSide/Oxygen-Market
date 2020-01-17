package austeretony.oxygen_trade.server;

import java.util.concurrent.TimeUnit;

import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_core.server.item.ItemsBlackList;

public final class TradeManagerServer {

    private static TradeManagerServer instance;

    private final OffersContainerServer offersContainer = new OffersContainerServer();

    private final SalesHistoryContainerServer salesHistoryContainer = new SalesHistoryContainerServer();

    private final OffersManagerServer offersManager;

    private final SalesHistoryManagerServer salesHistoryManager;

    private final ItemsBlackList itemsBlackList = ItemsBlackList.create("trade");

    private TradeManagerServer() {
        this.offersManager = new OffersManagerServer(this);
        this.salesHistoryManager = new SalesHistoryManagerServer(this);
    }

    private void registerPersistentData() {
        OxygenHelperServer.registerPersistentData(this.offersContainer);
        OxygenHelperServer.registerPersistentData(this.salesHistoryContainer);
    }

    private void scheduleRepeatableProcesses() {
        OxygenHelperServer.getSchedulerExecutorService().scheduleAtFixedRate(
                ()->{
                    this.offersManager.processOfferCreationQueue();
                    this.offersManager.processOfferActionsQueue();
                }, 500L, 500L, TimeUnit.MILLISECONDS);
    }

    public static void create() {
        if (instance == null) {
            instance = new TradeManagerServer();
            instance.registerPersistentData();
            instance.scheduleRepeatableProcesses();
        }
    }

    public static TradeManagerServer instance() {
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
}
