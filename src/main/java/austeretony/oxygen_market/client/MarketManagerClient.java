package austeretony.oxygen_market.client;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_market.client.input.MarketKeyHandler;

public final class MarketManagerClient {

    private static MarketManagerClient instance;

    private final OffersContainerClient offersContainer = new OffersContainerClient();

    private final SalesHistoryContainerClient salesHistoryContainer = new SalesHistoryContainerClient();

    private final OffersManagerClient offersManager;

    private final SalesHistoryManagerClient salesHistoryManager;

    private final MarketDataManagerClient marketDataManager;

    private final MenuManagerClient menuManager;    

    private final MarketKeyHandler keyHandler = new MarketKeyHandler();

    private MarketManagerClient() {
        this.offersManager = new OffersManagerClient(this);
        this.salesHistoryManager = new SalesHistoryManagerClient(this);
        this.marketDataManager = new MarketDataManagerClient(this);
        this.menuManager = new MenuManagerClient(this);
        CommonReference.registerEvent(this.keyHandler);
    }

    private void registerPersistentData() {
        OxygenHelperClient.registerPersistentData(this.offersContainer);
        OxygenHelperClient.registerPersistentData(this.salesHistoryContainer);
    }

    public static void create() {
        if (instance == null) {
            instance = new MarketManagerClient();
            instance.registerPersistentData();
        }
    }

    public static MarketManagerClient instance() {
        return instance;
    }

    public OffersContainerClient getOffersContainer() {
        return this.offersContainer;
    }

    public SalesHistoryContainerClient getSalesHistoryContainer() {
        return this.salesHistoryContainer;
    }

    public OffersManagerClient getOffersManager() {
        return this.offersManager;
    }

    public SalesHistoryManagerClient getSalesHistoryManager() {
        return this.salesHistoryManager;
    }

    public MarketDataManagerClient getMarketDataManager() {
        return this.marketDataManager;
    }

    public MenuManagerClient getMenuManager() {
        return this.menuManager;
    }

    public MarketKeyHandler getKeyHandler() {
        return this.keyHandler;
    }

    public void worldLoaded() {
        OxygenHelperClient.loadPersistentDataAsync(this.offersContainer);
    }
}
