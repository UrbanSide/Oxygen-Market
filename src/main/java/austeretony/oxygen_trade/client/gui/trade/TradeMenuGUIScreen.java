package austeretony.oxygen_trade.client.gui.trade;

import java.util.LinkedHashMap;
import java.util.Map;

import austeretony.alternateui.screen.core.AbstractGUIScreen;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.core.GUIWorkspace;
import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.gui.menu.OxygenMenuEntry;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_trade.client.PlayerOfferClient;
import austeretony.oxygen_trade.common.main.EnumOfferAction;
import austeretony.oxygen_trade.common.main.TradeMain;
import net.minecraft.item.ItemStack;

public class TradeMenuGUIScreen extends AbstractGUIScreen {

    public static final OxygenMenuEntry TRADE_MENU_ENTRY = new TradeMenuEntry();

    private BuyGUISection buySection;

    private SellingGUISection sellingSection;

    private OffersGUISection offersSection;

    private SalesHistoryGUISection salesHistorySection;

    public final Map<ItemStackWrapper, Integer> inventoryContent;

    public TradeMenuGUIScreen() {
        OxygenHelperClient.syncData(TradeMain.OFFERS_DATA_ID);        
        OxygenHelperClient.syncData(TradeMain.SALES_HISTORY_DATA_ID);

        this.inventoryContent = new LinkedHashMap<>();
        this.updateInventoryContent();
    }

    @Override
    protected GUIWorkspace initWorkspace() {
        return new GUIWorkspace(this, 300, 184).setAlignment(EnumGUIAlignment.RIGHT, - 10, 0);
    }

    @Override
    protected void initSections() {  
        this.getWorkspace().initSection(this.buySection = (BuyGUISection) new BuyGUISection(this).setDisplayText(ClientReference.localize("oxygen_trade.gui.trade.buy")).enable());
        this.getWorkspace().initSection(this.sellingSection = (SellingGUISection) new SellingGUISection(this).setDisplayText(ClientReference.localize("oxygen_trade.gui.trade.selling")).enable());
        this.getWorkspace().initSection(this.offersSection = (OffersGUISection) new OffersGUISection(this).setDisplayText(ClientReference.localize("oxygen_trade.gui.trade.offers")).enable());
        this.getWorkspace().initSection(this.salesHistorySection = (SalesHistoryGUISection) new SalesHistoryGUISection(this).setDisplayText(ClientReference.localize("oxygen_trade.gui.trade.salesHistory")).enable());
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
        this.buySection.setSearchButtonState(true);
        this.sellingSection.updateOffersAmount();
        this.offersSection.updateOffersAmount();
        this.offersSection.loadPlayerOffers();
    }

    public void salesHistorySynchronized() {
        this.salesHistorySection.enableFilterButton();
    }

    public BuyGUISection getBuySection() {
        return this.buySection;
    }

    public SellingGUISection getSellingSection() {
        return this.sellingSection;
    }

    public OffersGUISection getOffersSection() {
        return this.offersSection;
    }

    public SalesHistoryGUISection getSalesHistorySection() {
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

    public void performedOfferAction(EnumOfferAction action, PlayerOfferClient offer, long balance) {
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
}
