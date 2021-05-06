package austeretony.oxygen_market.client.gui.market;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.api.PrivilegesClient;
import austeretony.oxygen_core.client.gui.base.Alignment;
import austeretony.oxygen_core.client.gui.base.Textures;
import austeretony.oxygen_core.client.gui.base.block.Texture;
import austeretony.oxygen_core.client.gui.base.core.OxygenScreen;
import austeretony.oxygen_core.client.gui.base.core.Section;
import austeretony.oxygen_core.client.gui.base.core.Workspace;
import austeretony.oxygen_core.client.gui.menu.OxygenMenuEntry;
import austeretony.oxygen_core.client.player.inventory.InventoryHelperClient;
import austeretony.oxygen_core.client.preset.CurrencyProperties;
import austeretony.oxygen_core.client.preset.ItemCategoriesPresetClient;
import austeretony.oxygen_core.client.util.MinecraftClient;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.util.objects.Triple;
import austeretony.oxygen_market.client.MarketManagerClient;
import austeretony.oxygen_market.client.gui.market.buy.CombinedDealsEntry;
import austeretony.oxygen_market.client.gui.market.buy.DealProfitability;
import austeretony.oxygen_market.client.gui.menu.MarketScreenMenuEntry;
import austeretony.oxygen_market.client.market.ItemMarketData;
import austeretony.oxygen_market.client.market.Profitability;
import austeretony.oxygen_market.client.settings.MarketSettings;
import austeretony.oxygen_market.common.config.MarketConfig;
import austeretony.oxygen_market.common.main.MarketMain;
import austeretony.oxygen_market.common.main.MarketPrivileges;
import austeretony.oxygen_market.common.market.Deal;
import austeretony.oxygen_market.common.market.SalesHistoryEntry;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.gui.GuiScreen;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class MarketScreen extends OxygenScreen {

    public static final OxygenMenuEntry MARKET_SCREEN_MENU_ENTRY = new MarketScreenMenuEntry();

    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

    public static final int BTN_SIZE = 5;
    public static final Texture CLOCK_ICONS_TEXTURE = Texture.builder()
            .texture(Textures.CLOCK_ICONS)
            .size(BTN_SIZE, BTN_SIZE)
            .imageSize(BTN_SIZE * 3, BTN_SIZE)
            .build();
    public static final Texture CROSS_ICONS_TEXTURE = Texture.builder()
            .texture(Textures.CROSS_ICONS)
            .size(BTN_SIZE, BTN_SIZE)
            .imageSize(BTN_SIZE * 3, BTN_SIZE)
            .build();

    private final ItemCategoriesPresetClient itemCategoriesPreset;
    private final CurrencyProperties currencyProperties;
    private final Map<ItemStackWrapper, Integer> inventoryContentMap;
    private final boolean marketAccess;

    private final Map<ItemStackWrapper, ItemMarketData> marketDataMap = new HashMap<>();
    private final Map<Long, DealProfitability> profitabilityMap = new HashMap<>();

    private BuySection buySection;
    private MyDealsSection myDealsSection;

    private int syncCounter;

    private final List<CombinedDealsEntry> combinedDealsList = new ArrayList<>();

    private MarketScreen() {
        itemCategoriesPreset = (ItemCategoriesPresetClient) OxygenClient.getPreset(OxygenMain.PRESET_ITEM_CATEGORIES);
        currencyProperties = OxygenClient.getCurrencyProperties(OxygenMain.CURRENCY_COINS);
        inventoryContentMap = InventoryHelperClient.getInventoryContent();
        marketAccess = PrivilegesClient.getBoolean(MarketPrivileges.MARKET_ACCESS.getId(), MarketConfig.ENABLE_MARKET_ACCESS.asBoolean());
    }

    @Override
    public void initGui() {
        super.initGui();
        OxygenClient.requestDataSync(MarketMain.DATA_ID_MARKET_DEALS);
        if (PrivilegesClient.getBoolean(MarketPrivileges.SALES_HISTORY_ACCESS.getId(), MarketConfig.ENABLE_SALES_HISTORY_SYNC.asBoolean())) {
            OxygenClient.requestDataSync(MarketMain.DATA_ID_MARKET_HISTORY);
        }
    }

    @Override
    public int getScreenId() {
        return MarketMain.SCREEN_ID_MARKET;
    }

    @Override
    public Workspace createWorkspace() {
        Workspace workspace = new Workspace(this, 320, 183);
        workspace.setAlignment(Alignment.valueOf(MarketSettings.MARKET_SCREEN_ALIGNMENT.asString()), 0, 0);
        return workspace;
    }

    @Override
    public void addSections() {
        getWorkspace().addSection(buySection = new BuySection(this));
        getWorkspace().addSection(myDealsSection = new MyDealsSection(this));
    }

    @Override
    public Section getDefaultSection() {
        return buySection;
    }

    public ItemCategoriesPresetClient getItemCategoriesPreset() {
        return itemCategoriesPreset;
    }

    public CurrencyProperties getCurrencyProperties() {
        return currencyProperties;
    }

    public Map<ItemStackWrapper, Integer> getInventoryContentMap() {
        return inventoryContentMap;
    }

    public int getPlayerItemStock(ItemStackWrapper itemStackWrapper) {
        return inventoryContentMap.getOrDefault(itemStackWrapper, 0);
    }

    public void incrementPlayerStock(ItemStackWrapper itemStackWrapper, int amount) {
        int stock = inventoryContentMap.getOrDefault(itemStackWrapper, 0);
        stock += amount;
        if (stock <= 0) {
            inventoryContentMap.remove(itemStackWrapper);
            return;
        }
        inventoryContentMap.put(itemStackWrapper, stock);
    }

    public boolean hasAccessToMarket() {
        return marketAccess;
    }

    public List<CombinedDealsEntry> getCombinedDealsList() {
        return combinedDealsList;
    }

    public static void open() {
        MinecraftClient.displayGuiScreen(new MarketScreen());
    }

    public static void dealsDataSynchronized() {
        GuiScreen screen = MinecraftClient.getCurrentScreen();
        if (screen instanceof MarketScreen) {
            ((MarketScreen) screen).dataSynchronized();
        }
    }

    public static void salesHistoryDataSynchronized() {
        GuiScreen screen = MinecraftClient.getCurrentScreen();
        if (screen instanceof MarketScreen) {
            ((MarketScreen) screen).dataSynchronized();
        }
    }

    private void dataSynchronized() {
        syncCounter++;
        if (syncCounter == 2) {
            combineEqualDeals();
            if (MarketSettings.ENABLE_DEALS_PROFITABILITY_DISPLAY.asBoolean()) {
                calculateItemsMarketData();
            }

            buySection.dataSynchronized();
            myDealsSection.dataSynchronized();
        }
    }

    private void combineEqualDeals() {
        combinedDealsList.clear();
        List<Deal> sortedDeals = MarketManagerClient.instance().getDealsMap().values()
                .stream()
                .sorted(Comparator.comparingLong(Deal::getId))
                .collect(Collectors.toList());

        for (Deal deal : sortedDeals) {
            CombinedDealsEntry container = null;
            for (CombinedDealsEntry entry : combinedDealsList) {
                if (deal.isEqual(entry.getDeal())) {
                    container = entry;
                }
            }
            if (container == null) {
                container = new CombinedDealsEntry(deal);
                combinedDealsList.add(container);
            }
            container.addDeal(deal);
        }
    }

    private void calculateItemsMarketData() {
        MarketManagerClient manager = MarketManagerClient.instance();

        Collection<SalesHistoryEntry> salesHistory = manager.getSalesHistoryMap().values();
        Multimap<ItemStackWrapper, Triple<Integer, Long, Long>> tempTransactionsDataMap = HashMultimap.create();

        for (SalesHistoryEntry entry : salesHistory) {
            Collection<Triple<Integer, Long, Long>> collection = tempTransactionsDataMap.get(entry.getStackWrapper());
            collection.add(Triple.of(entry.getQuantity(), entry.getPrice(), entry.getId()));
        }

        for (Map.Entry<ItemStackWrapper, Collection<Triple<Integer, Long, Long>>> stackWrapperEntry : tempTransactionsDataMap
                .asMap().entrySet()) {
            ItemStackWrapper stackWrapper = stackWrapperEntry.getKey();
            Collection<Triple<Integer, Long, Long>> transactionsCollection = stackWrapperEntry.getValue();

            long summaryPrice = 0L;
            double unitPrice;
            double minUnitPrice = Double.MAX_VALUE;
            double maxUnitPrice = Double.MIN_VALUE;
            int totalSoldAmount = 0;
            Map<Long, Double> transactionIdToUnitPriceMap = new HashMap<>();

            for (Triple<Integer, Long, Long> triple : transactionsCollection) {
                summaryPrice += triple.getSecond();

                unitPrice = (double) triple.getSecond() / triple.getFirst();
                if (unitPrice < minUnitPrice) {
                    minUnitPrice = unitPrice;
                }
                if (unitPrice > maxUnitPrice) {
                    maxUnitPrice = unitPrice;
                }

                totalSoldAmount += triple.getFirst();
                transactionIdToUnitPriceMap.put(triple.getThird(), unitPrice);
            }

            marketDataMap.put(stackWrapper, new ItemMarketData(stackWrapper, (double) summaryPrice / totalSoldAmount,
                    minUnitPrice, maxUnitPrice, totalSoldAmount, transactionIdToUnitPriceMap));
        }
    }

    @Nonnull
    public DealProfitability getDealProfitability(Deal deal) {
        DealProfitability dealProfitability = profitabilityMap.get(deal.getId());
        if (dealProfitability != null) {
            return dealProfitability;
        }

        ItemMarketData itemMarketData = marketDataMap.get(deal.getStackWrapper());
        if (itemMarketData != null) {
            Profitability profitability = Profitability.OVERPRICE;

            double unitPrice = (double) deal.getPrice() / deal.getQuantity();
            double delta = unitPrice - itemMarketData.getAveragePrice();
            double deltaPercents = delta / itemMarketData.getAveragePrice();
            if (deltaPercents < 0.0) {
                deltaPercents *= -1.0;
            }

            if (delta <= 0.0) {
                if (deltaPercents >= 0.4) {
                    profitability = Profitability.VERY_GOOD;
                } else if (deltaPercents >= 0.2) {
                    profitability = Profitability.GOOD;
                } else {
                    profitability = Profitability.NORMAL;
                }
            }

            String profitabilityPercentStr = (delta > 0.0 ? "+" : "-") + DECIMAL_FORMAT.format(deltaPercents * 100.0) + "%";
            dealProfitability = new DealProfitability(deal.getId(), profitability, profitabilityPercentStr, itemMarketData);
        } else {
            dealProfitability = new DealProfitability(deal.getId(), Profitability.NO_DATA, "", null);
        }
        profitabilityMap.put(deal.getId(), dealProfitability);

        return dealProfitability;
    }

    public static void dealsCreated(int dealsQuantity, Deal deal, long balance) {
        GuiScreen screen = MinecraftClient.getCurrentScreen();
        if (screen instanceof MarketScreen) {
            MarketScreen marketScreen = (MarketScreen) screen;
            marketScreen.combineEqualDeals();
            marketScreen.buySection.dealsCreated(dealsQuantity, deal, balance);
            marketScreen.myDealsSection.dealsCreated(dealsQuantity, deal, balance);
        }
    }

    public static void dealsCanceled(Set<Long> dealsIds) {
        GuiScreen screen = MinecraftClient.getCurrentScreen();
        if (screen instanceof MarketScreen) {
            MarketScreen marketScreen = (MarketScreen) screen;
            marketScreen.combineEqualDeals();
            marketScreen.buySection.dealCanceled(dealsIds);
            marketScreen.myDealsSection.dealCanceled(dealsIds);
        }
    }

    public static void purchased(Set<Long> dealsIds, long balance) {
        GuiScreen screen = MinecraftClient.getCurrentScreen();
        if (screen instanceof MarketScreen) {
            MarketScreen marketScreen = (MarketScreen) screen;
            marketScreen.combineEqualDeals();
            marketScreen.buySection.purchased(dealsIds, balance);
            marketScreen.myDealsSection.purchased(dealsIds, balance);
        }
    }

    public static void purchaseFailed(Set<Long> dealsIds) {
        GuiScreen screen = MinecraftClient.getCurrentScreen();
        if (screen instanceof MarketScreen) {
            MarketScreen marketScreen = (MarketScreen) screen;
            marketScreen.buySection.purchaseFailed(dealsIds);
            marketScreen.myDealsSection.purchaseFailed(dealsIds);
        }
    }
}
