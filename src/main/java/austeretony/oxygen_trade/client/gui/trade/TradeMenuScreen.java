package austeretony.oxygen_trade.client.gui.trade;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import austeretony.alternateui.screen.core.AbstractGUIScreen;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.core.GUIWorkspace;
import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.oxygen_core.client.OxygenManagerClient;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.api.PrivilegesProviderClient;
import austeretony.oxygen_core.client.currency.CurrencyProperties;
import austeretony.oxygen_core.client.gui.menu.OxygenMenuEntry;
import austeretony.oxygen_core.common.EnumRarity;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_trade.client.MarketDataManagerClient.ItemStackMarketData;
import austeretony.oxygen_trade.client.OfferClient;
import austeretony.oxygen_trade.client.TradeManagerClient;
import austeretony.oxygen_trade.client.gui.menu.TradeMenuEntry;
import austeretony.oxygen_trade.client.settings.EnumTradeClientSetting;
import austeretony.oxygen_trade.client.settings.gui.EnumTradeGUISetting;
import austeretony.oxygen_trade.common.config.TradeConfig;
import austeretony.oxygen_trade.common.main.EnumOfferAction;
import austeretony.oxygen_trade.common.main.EnumTradePrivilege;
import austeretony.oxygen_trade.common.main.TradeMain;
import net.minecraft.item.ItemStack;

public class TradeMenuScreen extends AbstractGUIScreen {

    public static final OxygenMenuEntry TRADE_MENU_ENTRY = new TradeMenuEntry();

    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

    public final Map<ItemStackWrapper, Integer> inventoryContent = new LinkedHashMap<>();

    private final CurrencyProperties currencyProperties = OxygenManagerClient.instance().getCurrencyManager().getProperties(OxygenMain.COMMON_CURRENCY_INDEX);

    private final Map<Long, OfferProfitability> offersProfitability = new HashMap<>();

    private BuySection buySection;

    private SellingSection sellingSection;

    private OffersSection offersSection;

    private HistorySection salesHistorySection;

    public boolean enableProfitabilityCalculations, enableMarketAccess;

    public TradeMenuScreen() {
        OxygenHelperClient.syncData(TradeMain.OFFERS_DATA_ID);        
        OxygenHelperClient.syncData(TradeMain.SALES_HISTORY_DATA_ID);
        this.updateInventoryContent();

        this.enableMarketAccess = PrivilegesProviderClient.getAsBoolean(EnumTradePrivilege.MARKET_ACCESS.id(), true);
        this.enableProfitabilityCalculations = EnumTradeClientSetting.ENABLE_PROFITABILITY_CALCULATION.get().asBoolean()
                && PrivilegesProviderClient.getAsBoolean(EnumTradePrivilege.SALES_HISTORY_ACCESS.id(), TradeConfig.ENABLE_SALES_HISTORY_SYNC.asBoolean());
    }

    @Override
    protected GUIWorkspace initWorkspace() {
        EnumGUIAlignment alignment = EnumGUIAlignment.CENTER;
        switch (EnumTradeGUISetting.TRADE_MENU_ALIGNMENT.get().asInt()) {
        case - 1: 
            alignment = EnumGUIAlignment.LEFT;
            break;
        case 0:
            alignment = EnumGUIAlignment.CENTER;
            break;
        case 1:
            alignment = EnumGUIAlignment.RIGHT;
            break;    
        default:
            alignment = EnumGUIAlignment.CENTER;
            break;
        }
        return new GUIWorkspace(this, 320, 183).setAlignment(alignment, 0, 0);
    }

    @Override
    protected void initSections() {  
        this.getWorkspace().initSection(this.buySection = (BuySection) new BuySection(this).enable());
        this.getWorkspace().initSection(this.sellingSection = (SellingSection) new SellingSection(this).enable());
        this.getWorkspace().initSection(this.offersSection = (OffersSection) new OffersSection(this).enable());
        this.getWorkspace().initSection(this.salesHistorySection = (HistorySection) new HistorySection(this).enable());
    }

    @Override
    protected AbstractGUISection getDefaultSection() {
        return this.buySection;
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element) {}

    @Override
    protected boolean doesGUIPauseGame() {
        return false;
    }

    public void offersSynchronized() {
        this.buySection.offersSynchronized();
        this.sellingSection.offersSynchronized();
        this.offersSection.offersSynchronized();
    }

    public void salesHistorySynchronized() {
        this.salesHistorySection.salesHistorySynchronized();
    }

    public BuySection getBuySection() {
        return this.buySection;
    }

    public SellingSection getSellingSection() {
        return this.sellingSection;
    }

    public OffersSection getOffersSection() {
        return this.offersSection;
    }

    public HistorySection getSalesHistorySection() {
        return this.salesHistorySection;
    }

    public void updateInventoryContent() {
        this.inventoryContent.clear();
        ItemStackWrapper wrapper;
        int amount;
        for (ItemStack itemStack : ClientReference.getClientPlayer().inventory.mainInventory) {
            if (!itemStack.isEmpty()) {
                wrapper = ItemStackWrapper.getFromStack(itemStack);
                if (!this.inventoryContent.containsKey(wrapper))
                    this.inventoryContent.put(wrapper, itemStack.getCount());
                else {
                    amount = this.inventoryContent.get(wrapper);
                    amount += itemStack.getCount();
                    this.inventoryContent.put(wrapper, amount);
                }
            }
        }
    }

    public int getEqualStackAmount(ItemStackWrapper stackWrapper) {
        int amount = 0;
        for (ItemStackWrapper wrapper : this.inventoryContent.keySet())
            if (wrapper.isEquals(stackWrapper))
                amount += this.inventoryContent.get(wrapper);
        return amount;
    }

    public void decrementEqualStackAmount(ItemStackWrapper stackWrapper, int value) {
        int newAmount = this.inventoryContent.get(stackWrapper) - value;
        if (newAmount == 0)
            this.inventoryContent.remove(stackWrapper);
        else
            this.inventoryContent.put(stackWrapper, newAmount);
    }

    public void performedOfferAction(EnumOfferAction action, OfferClient offer, long balance) {
        switch (action) {
        case PURCHASE:
            this.buySection.itemPurchased(offer, balance);
            this.sellingSection.itemPurchased(offer, balance);
            this.offersSection.itemPurchased(offer, balance);
            break;
        case CREATION:
            this.sellingSection.offerCreated(offer, balance);
            this.buySection.offerCreated(offer, balance);
            this.offersSection.offerCreated(offer, balance);
            break;
        case CANCEL:
            this.offersSection.offerCanceled(offer, balance);
            this.buySection.offerCanceled(offer, balance);
            this.sellingSection.offerCanceled(offer, balance);
            break;
        }
    }

    public CurrencyProperties getCurrencyProperties() {
        return this.currencyProperties;
    }

    public int calculateOfferProfitability(OfferClient offer) {
        if (!this.offersProfitability.containsKey(offer.getId())) {
            ItemStackMarketData marketData = TradeManagerClient.instance().getMarketDataManager().getItemStackMarketData(offer.getOfferedStack());
            if (this.enableProfitabilityCalculations 
                    && marketData != null) {
                int profitabilityIndex = - 1;
                EnumRarity rarity = EnumRarity.NORMAL;
                float 
                offerUnitPrice = (float) offer.getPrice() / (float) offer.getAmount(),
                delta = offerUnitPrice - marketData.getAveragePrice(),
                deltaPercents = delta / (marketData.getAveragePrice() / 100.0F);
                if (deltaPercents < 0.0F)
                    deltaPercents *= - 1.0F;
                if (delta <= 0.0F) {
                    profitabilityIndex = 0;
                    if (deltaPercents >= 90.0F)
                        rarity = EnumRarity.LEGENDARY;
                    else if (deltaPercents >= 70.0F)
                        rarity = EnumRarity.EPIC;
                    else if (deltaPercents >= 45.0F)
                        rarity = EnumRarity.SUPERIOR;
                    else if (deltaPercents >= 20.0F)
                        rarity = EnumRarity.FINE;
                    else
                        rarity = EnumRarity.NORMAL;
                } else
                    profitabilityIndex = - 2;

                if (profitabilityIndex == 0)
                    profitabilityIndex = rarity.ordinal();

                this.offersProfitability.put(offer.getId(), new OfferProfitability(
                        profitabilityIndex,
                        profitabilityIndex == - 2 ? EnumBaseGUISetting.INACTIVE_TEXT_COLOR.get().asInt() : rarity.getColor(),
                                (delta > 0.0F ? "-" : "") + DECIMAL_FORMAT.format(deltaPercents) + "%",
                                "(" + String.valueOf(marketData.getCompletedTransactionsAmount()) + 
                                "/" + String.valueOf(marketData.getTotalItemsSoldAmount()) + 
                                "): " + String.valueOf(DECIMAL_FORMAT.format(marketData.getAveragePrice()))
                        ));

                return profitabilityIndex;
            }
            return - 1;
        } else
            return this.offersProfitability.get(offer.getId()).profitabilityIndex;
    }

    public OfferProfitability getOfferProfitability(OfferClient offer) {
        return this.offersProfitability.get(offer.getId());
    }
}
