package austeretony.oxygen_trade.client;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_trade.client.categories.ItemCategoriesPresetClient;
import austeretony.oxygen_trade.client.gui.trade.TradeMenuGUIScreen;
import austeretony.oxygen_trade.common.main.EnumTradeStatusMessage;

public final class TradeManagerClient {

    private static TradeManagerClient instance;

    private final OffersContainerClient offersContainer = new OffersContainerClient();

    private final SalesHistoryContainerClient salesHistoryContainer = new SalesHistoryContainerClient();

    private final OffersManagerClient offersManager;

    private final SalesHistoryManagerClient salesHistoryManager;

    private final TradeMenuManagerClient tradeMenuManager = new TradeMenuManagerClient();

    private final ItemCategoriesPresetClient itemCategoriesPreset = new ItemCategoriesPresetClient();

    private TradeManagerClient() {
        this.offersManager = new OffersManagerClient(this);
        this.salesHistoryManager = new SalesHistoryManagerClient(this);
    }

    private void registerPersistentData() {
        OxygenHelperClient.registerPersistentData(this.offersContainer);
        OxygenHelperClient.registerPersistentData(this.salesHistoryContainer);
    }

    private void registerPresets() {
        OxygenHelperClient.registerPreset(this.itemCategoriesPreset);
    }

    public static void create() {
        if (instance == null) {
            instance = new TradeManagerClient();
            instance.registerPersistentData();
            instance.registerPresets();
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

    public TradeMenuManagerClient getTradeMenuManager() {
        return this.tradeMenuManager;
    }

    public ItemCategoriesPresetClient getItemCategoriesPreset() {
        return this.itemCategoriesPreset;
    }

    public void worldLoaded() {
        this.load(); 
    }

    public void load() {
        OxygenHelperClient.loadPersistentDataAsync(this.offersContainer);
    }
}
