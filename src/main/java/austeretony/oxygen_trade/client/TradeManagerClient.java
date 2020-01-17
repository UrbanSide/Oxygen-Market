package austeretony.oxygen_trade.client;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_trade.client.input.TradeKeyHandler;

public final class TradeManagerClient {

    private static TradeManagerClient instance;

    private final OffersContainerClient offersContainer = new OffersContainerClient();

    private final SalesHistoryContainerClient salesHistoryContainer = new SalesHistoryContainerClient();

    private final OffersManagerClient offersManager;

    private final SalesHistoryManagerClient salesHistoryManager;

    private final MarketDataManagerClient marketDataManager;

    private final TradeMenuManagerClient tradeMenuManager;    

    private final TradeKeyHandler keyHandler = new TradeKeyHandler();

    private TradeManagerClient() {
        this.offersManager = new OffersManagerClient(this);
        this.salesHistoryManager = new SalesHistoryManagerClient(this);
        this.marketDataManager = new MarketDataManagerClient(this);
        this.tradeMenuManager = new TradeMenuManagerClient(this);
        CommonReference.registerEvent(this.keyHandler);
    }

    private void registerPersistentData() {
        OxygenHelperClient.registerPersistentData(this.offersContainer);
        OxygenHelperClient.registerPersistentData(this.salesHistoryContainer);
    }

    public static void create() {
        if (instance == null) {
            instance = new TradeManagerClient();
            instance.registerPersistentData();
        }
    }

    public static TradeManagerClient instance() {
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

    public TradeMenuManagerClient getTradeMenuManager() {
        return this.tradeMenuManager;
    }

    public TradeKeyHandler getKeyHandler() {
        return this.keyHandler;
    }

    public void worldLoaded() {
        this.load(); 
    }

    public void load() {
        OxygenHelperClient.loadPersistentDataAsync(this.offersContainer);
    }
}
