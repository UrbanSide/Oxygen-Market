package austeretony.oxygen_market.client.gui.market;

import java.util.HashSet;
import java.util.Set;

import org.lwjgl.input.Keyboard;

import austeretony.alternateui.screen.button.GUIButton;
import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.api.OxygenGUIHelper;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.api.PrivilegesProviderClient;
import austeretony.oxygen_core.client.gui.OxygenGUITextures;
import austeretony.oxygen_core.client.gui.elements.OxygenCurrencyValue;
import austeretony.oxygen_core.client.gui.elements.OxygenDefaultBackgroundWithButtonsUnderlinedFiller;
import austeretony.oxygen_core.client.gui.elements.OxygenInventoryLoad;
import austeretony.oxygen_core.client.gui.elements.OxygenKeyButton;
import austeretony.oxygen_core.client.gui.elements.OxygenNumberField;
import austeretony.oxygen_core.client.gui.elements.OxygenScrollablePanel;
import austeretony.oxygen_core.client.gui.elements.OxygenSectionSwitcher;
import austeretony.oxygen_core.client.gui.elements.OxygenTextLabel;
import austeretony.oxygen_core.client.gui.elements.OxygenTexturedButton;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_market.client.MarketManagerClient;
import austeretony.oxygen_market.client.OfferClient;
import austeretony.oxygen_market.client.gui.market.selling.InventoryItemPanelEntry;
import austeretony.oxygen_market.client.gui.market.selling.OffersAmount;
import austeretony.oxygen_market.client.gui.market.selling.SelectedItem;
import austeretony.oxygen_market.client.gui.market.selling.callback.OfferCreationCallback;
import austeretony.oxygen_market.common.config.MarketConfig;
import austeretony.oxygen_market.common.main.EnumMarketPrivilege;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemStack;

public class SellingSection extends AbstractGUISection {

    private final MarketMenuScreen screen;

    private OxygenKeyButton createOfferButton;

    private OxygenTexturedButton resetAmountButton, resetUnitPriceButton, resetTotalPriceButton, applyRecommendedUnitPriceButton;

    private OxygenScrollablePanel inventoryContentPanel;

    private OxygenNumberField amountField, unitPriceField, totalPriceField;

    private SelectedItem selectedItem;

    private OffersAmount offersAmount;

    private OxygenInventoryLoad inventoryLoad;

    private OxygenCurrencyValue offerCreationFeeValue, saleFeeValue, profitValue, balanceValue;

    private AbstractGUICallback offerCreationCallback;

    //cache

    private InventoryItemPanelEntry currentEntry;

    private long currentAmount, currentTotalPrice;

    private float currentUnitPrice;

    public SellingSection(MarketMenuScreen screen) {
        super(screen);
        this.screen = screen;
        this.setDisplayText(ClientReference.localize("oxygen_market.gui.market.selling"));
    }

    @Override
    public void init() {
        this.offerCreationCallback = new OfferCreationCallback(this.screen, this, 140, 56).enableDefaultBackground();

        this.addElement(new OxygenDefaultBackgroundWithButtonsUnderlinedFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new OxygenTextLabel(4, 12, ClientReference.localize("oxygen_market.gui.market.title"), EnumBaseGUISetting.TEXT_TITLE_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        this.addElement(this.offersAmount = new OffersAmount(6, 18));
        this.addElement(this.selectedItem = new SelectedItem(6, 26));

        this.addElement(new OxygenTextLabel(6, 53, ClientReference.localize("oxygen_market.gui.market.amount"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
        this.addElement(this.amountField = new OxygenNumberField(6, 55, 45, "", 0, false, 0, true));
        this.amountField.setInputListener((charCode, keyCode)->this.calculateFeesAndProfit(EnumField.AMOUNT));

        long maxPrice = PrivilegesProviderClient.getAsLong(EnumMarketPrivilege.PRICE_MAX_VALUE.id(), MarketConfig.PRICE_MAX_VALUE.asLong());

        this.addElement(new OxygenTextLabel(6, 73, ClientReference.localize("oxygen_market.gui.market.unitPrice"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
        this.addElement(this.unitPriceField = new OxygenNumberField(6, 75, 45, "", maxPrice, true, 2, true));
        this.unitPriceField.setInputListener((charCode, keyCode)->this.calculateFeesAndProfit(EnumField.UNIT_PRICE));

        this.addElement(new OxygenTextLabel(6, 93, ClientReference.localize("oxygen_market.gui.market.totalPrice"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
        this.addElement(this.totalPriceField = new OxygenNumberField(6, 95, 45, "", maxPrice, false, 0, true));
        this.totalPriceField.setInputListener((charCode, keyCode)->this.calculateFeesAndProfit(EnumField.TOTAL_PRICE));

        //buttons
        this.addElement(this.resetAmountButton = new OxygenTexturedButton(53, 56, 5, 5, OxygenGUITextures.CROSS_ICONS, 5, 5, ClientReference.localize("oxygen_core.gui.reset")));

        this.addElement(this.resetUnitPriceButton = new OxygenTexturedButton(53, 76, 5, 5, OxygenGUITextures.CROSS_ICONS, 5, 5, ClientReference.localize("oxygen_core.gui.reset")));
        this.addElement(this.applyRecommendedUnitPriceButton = new OxygenTexturedButton(60, 76, 5, 5, OxygenGUITextures.CHECK_ICONS, 5, 5, ClientReference.localize("oxygen_market.gui.market.tooltip.applyRecommended")).disable());

        this.addElement(this.resetTotalPriceButton = new OxygenTexturedButton(53, 96, 5, 5, OxygenGUITextures.CROSS_ICONS, 5, 5, ClientReference.localize("oxygen_core.gui.reset")));         

        this.addElement(new OxygenTextLabel(6, 113, ClientReference.localize("oxygen_market.gui.market.listingFee"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
        this.addElement(this.offerCreationFeeValue = new OxygenCurrencyValue(68, 106));   
        this.offerCreationFeeValue.setValue(OxygenMain.COMMON_CURRENCY_INDEX, 0L);

        this.addElement(new OxygenTextLabel(6, 123, ClientReference.localize("oxygen_market.gui.market.saleFee"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
        this.addElement(this.saleFeeValue = new OxygenCurrencyValue(68, 116));   
        this.saleFeeValue.setValue(OxygenMain.COMMON_CURRENCY_INDEX, 0L);

        this.addElement(new OxygenTextLabel(6, 133, ClientReference.localize("oxygen_market.gui.market.profit"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
        this.addElement(this.profitValue = new OxygenCurrencyValue(68, 126));   
        this.profitValue.setValue(OxygenMain.COMMON_CURRENCY_INDEX, 0L);

        //create offer button
        this.addElement(this.createOfferButton = new OxygenKeyButton(0, this.getY() + this.getHeight() + this.screen.guiTop - 8, ClientReference.localize("oxygen_market.gui.market.button.createOffer"), Keyboard.KEY_E, this::create).disable()); 

        //inventory content panel
        this.addElement(this.inventoryContentPanel = new OxygenScrollablePanel(this.screen, 76, 16, this.getWidth() - 85, 16, 1, 100, 9, EnumBaseGUISetting.TEXT_PANEL_SCALE.get().asFloat(), true));

        this.loadInventoryContent();

        this.inventoryContentPanel.<InventoryItemPanelEntry>setElementClickListener((previous, clicked, mouseX, mouseY, mouseButton)->{
            if (mouseButton == 0) {
                if (previous != null)
                    previous.setToggled(false);
                clicked.toggle();
                this.currentEntry = clicked;
                this.updateFields();
                this.selectedItem.setItemStack(clicked.getWrapped().getCachedItemStack(), clicked.getPlayerStock());
                this.applyRecommendedUnitPriceButton.setEnabled(clicked.getAverageMarketPrice() != 0.0F);
            }
        });

        //sections switcher
        this.addElement(new OxygenSectionSwitcher(this.getWidth() - 4, 5, this, this.screen.getBuySection(), this.screen.getOffersSection(), this.screen.getSalesHistorySection()));

        //client data
        this.addElement(this.inventoryLoad = new OxygenInventoryLoad(6, this.getHeight() - 8));
        this.inventoryLoad.setLoad(this.screen.getBuySection().getInventoryLoad().getLoad());
        this.addElement(this.balanceValue = new OxygenCurrencyValue(this.getWidth() - 14, this.getHeight() - 10));   
        this.balanceValue.setValue(OxygenMain.COMMON_CURRENCY_INDEX, this.screen.getBuySection().getBalanceValue().getValue());
    }

    private void calculateButtonsHorizontalPosition() {
        ScaledResolution sr = new ScaledResolution(this.mc);
        this.createOfferButton.setX((sr.getScaledWidth() - (12 + this.textWidth(this.createOfferButton.getDisplayText(), this.createOfferButton.getTextScale()))) / 2 - this.screen.guiLeft);
    }

    private void loadInventoryContent() {
        this.inventoryContentPanel.reset();
        Set<String> stacks = new HashSet<>();
        String key;
        for (ItemStackWrapper stackWrapper : this.screen.inventoryContent.keySet()) {
            key = stackWrapper.toString();
            if (!stacks.contains(key)) {
                this.inventoryContentPanel.addEntry(new InventoryItemPanelEntry(
                        stackWrapper, 
                        this.screen.getEqualStackAmount(stackWrapper),
                        this.screen.getCurrencyProperties()));
                stacks.add(key);
            }              
        }

        this.inventoryContentPanel.getScroller().reset();
        this.inventoryContentPanel.getScroller().updateRowsAmount(MathUtils.clamp(this.screen.inventoryContent.size(), 9, ClientReference.getClientPlayer().inventory.mainInventory.size()));
    }

    public void updateOffersAmount() {
        this.offersAmount.updateOffersAmount();
    }

    private void updateFields() {
        int maxAmount = PrivilegesProviderClient.getAsInt(EnumMarketPrivilege.ITEMS_PER_OFFER_MAX_AMOUNT.id(), MarketConfig.ITEMS_PER_OFFER_MAX_AMOUNT.asInt());
        if (maxAmount < 0)
            maxAmount = this.currentEntry.getWrapped().getCachedItemStack().getMaxStackSize();
        this.amountField.setMaxNumber(MathUtils.lesserOfTwo(
                this.currentEntry.getPlayerStock(), 
                maxAmount));
        this.amountField.setText("1");
        this.unitPriceField.reset();
        this.totalPriceField.reset();
        this.offerCreationFeeValue.updateValue(0L);
        this.offerCreationFeeValue.setRed(false);
        this.saleFeeValue.updateValue(0L);
        this.profitValue.updateValue(0L);
        this.createOfferButton.disable();
        this.applyRecommendedUnitPriceButton.disable();
    }

    private void calculateFeesAndProfit(EnumField changedField) {
        this.currentAmount = this.amountField.getTypedNumberAsLong();
        this.currentUnitPrice = this.unitPriceField.getTypedNumberAsFloat();
        this.currentTotalPrice = this.totalPriceField.getTypedNumberAsLong();
        long 
        result = 0L, 
        maxAllowedPrice = PrivilegesProviderClient.getAsLong(EnumMarketPrivilege.PRICE_MAX_VALUE.id(), MarketConfig.PRICE_MAX_VALUE.asLong()),
        offerCreationFee, saleFee;    
        switch (changedField) {
        case AMOUNT:
            if (this.currentUnitPrice > 0.0F) {
                result = (long) (this.currentUnitPrice * this.currentAmount);
                if (result > maxAllowedPrice) return;

                this.currentTotalPrice = result;
                this.totalPriceField.setText(String.valueOf(result));
            }
            break;
        case UNIT_PRICE:
            if (this.currentAmount > 0L) {
                result = (long) (this.currentUnitPrice * this.currentAmount);
                if (result > maxAllowedPrice) return;

                this.currentTotalPrice = result;
                this.totalPriceField.setText(String.valueOf(result));
            }
            break;
        case TOTAL_PRICE:
            if (this.currentAmount > 0L) {
                result = this.currentTotalPrice;

                this.currentUnitPrice = (float) this.currentTotalPrice / (float) this.currentAmount;
                this.unitPriceField.setText(MarketMenuScreen.DECIMAL_FORMAT.format(this.currentUnitPrice));
            }
            break;
        }
        offerCreationFee = MathUtils.percentValueOf(result, PrivilegesProviderClient.getAsInt(EnumMarketPrivilege.OFFER_CREATION_FEE_PERCENT.id(), MarketConfig.OFFER_CREATION_FEE_PERCENT.asInt()));
        this.offerCreationFeeValue.updateValue(offerCreationFee);
        this.offerCreationFeeValue.setRed(offerCreationFee > this.balanceValue.getValue());
        if (!this.offersAmount.reachedMaxAmount())
            this.createOfferButton.setEnabled(this.screen.enableMarketAccess && result > 0L && this.balanceValue.getValue() >= offerCreationFee);
        saleFee = MathUtils.percentValueOf(result, PrivilegesProviderClient.getAsInt(EnumMarketPrivilege.OFFER_SALE_FEE_PERCENT.id(), MarketConfig.OFFER_SALE_FEE_PERCENT.asInt()));
        this.saleFeeValue.updateValue(saleFee);
        this.profitValue.updateValue(result - saleFee);
    }

    private void create() {
        this.offerCreationCallback.open();
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {   
        if (!this.amountField.isDragged() 
                && !this.unitPriceField.isDragged() 
                && !this.totalPriceField.isDragged() 
                && !this.hasCurrentCallback())
            if (OxygenGUIHelper.isOxygenMenuEnabled()) {
                if (keyCode == MarketMenuScreen.MARKET_MENU_ENTRY.getKeyCode())
                    this.screen.close();
            } else if (MarketConfig.ENABLE_MARKET_MENU_KEY.asBoolean() 
                    && keyCode == MarketManagerClient.instance().getKeyHandler().getTradeMenuKeybinding().getKeyCode())
                this.screen.close();
        return super.keyTyped(typedChar, keyCode); 
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (mouseButton == 0) {
            if (element == this.createOfferButton)
                this.offerCreationCallback.open();
            else if (element == this.resetAmountButton) {
                this.amountField.reset();
                this.calculateFeesAndProfit(EnumField.AMOUNT);
            } else if (element == this.resetUnitPriceButton) {
                this.unitPriceField.reset();
                this.calculateFeesAndProfit(EnumField.UNIT_PRICE);
            } else if (element == this.resetTotalPriceButton) {
                this.totalPriceField.reset();
                this.calculateFeesAndProfit(EnumField.TOTAL_PRICE);
            } else if (element == this.applyRecommendedUnitPriceButton) {
                this.unitPriceField.setText(MarketMenuScreen.DECIMAL_FORMAT.format(this.currentEntry.getAverageMarketPrice()));
                this.calculateFeesAndProfit(EnumField.UNIT_PRICE);
            }
        }
    }

    public void offersSynchronized() {
        this.updateOffersAmount();

        this.calculateButtonsHorizontalPosition();
    }

    public void salesHistorySynchronized() {
        InventoryItemPanelEntry entry;
        for (GUIButton button : this.inventoryContentPanel.buttonsBuffer) {
            entry = (InventoryItemPanelEntry) button;
            entry.initMarketData(MarketManagerClient.instance().getMarketDataManager().getItemStackMarketData(entry.getWrapped()));
        }
    }

    public void setCreateOfferButtonState(boolean enabled) {
        this.createOfferButton.setEnabled(enabled);
    }

    public void itemPurchased(OfferClient offer, long balance) {
        this.balanceValue.updateValue(balance);
        if (offer.isOwner(OxygenHelperClient.getPlayerUsername()))
            this.offersAmount.decrementOffersAmount(1);
    }

    public void offerCreated(OfferClient offer, long balance) {
        this.balanceValue.updateValue(balance);
        this.screen.decrementEqualStackAmount(offer.getStackWrapper(), offer.getAmount());
        this.offersAmount.incrementOffersAmount(1);
        if (this.offersAmount.reachedMaxAmount())
            this.createOfferButton.disable();
        boolean reloadContent = false;
        ItemStack itemStack;
        for (GUIButton button : this.inventoryContentPanel.buttonsBuffer) {
            InventoryItemPanelEntry entry = (InventoryItemPanelEntry) button;
            if (entry.getWrapped().isEquals(offer.getStackWrapper())) {
                entry.decrementPlayerStock(offer.getAmount());
                if (entry.isToggled()) {
                    itemStack = offer.getStackWrapper().getCachedItemStack();
                    this.inventoryLoad.decrementLoad(offer.getAmount() / itemStack.getMaxStackSize());
                    if (entry.getPlayerStock() > 0) {
                        this.selectedItem.setPlayerStock(entry.getPlayerStock());
                        this.amountField.setMaxNumber(entry.getPlayerStock());
                        if (this.currentAmount > entry.getPlayerStock()) {
                            this.amountField.setText(String.valueOf(entry.getPlayerStock()));
                            this.calculateFeesAndProfit(EnumField.AMOUNT);
                        }
                    } else {
                        reloadContent = true;
                        this.selectedItem.setVisible(false);
                        this.updateFields();
                        this.createOfferButton.disable();
                        break;
                    }
                }
            }
        }
        if (reloadContent)
            this.loadInventoryContent();
    }

    public void offerCanceled(OfferClient offer, long balance) {
        this.offersAmount.decrementOffersAmount(1);
    }

    public OffersAmount getOffersAmountElement() {
        return this.offersAmount;
    }

    public OxygenInventoryLoad getInventoryLoad() {
        return this.inventoryLoad;
    }

    public OxygenCurrencyValue getBalanceValue() {
        return this.balanceValue;
    }

    public InventoryItemPanelEntry getCurrentItemButton() {
        return this.currentEntry;
    }

    public long getCurrentAmount() {
        return this.currentAmount;
    }

    public long getCurrentTotalPrice() {
        return this.currentTotalPrice;
    }

    private enum EnumField {

        AMOUNT,
        UNIT_PRICE,
        TOTAL_PRICE
    }
}
