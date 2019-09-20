package austeretony.oxygen_trade.client.gui.trade;

import java.util.HashSet;
import java.util.Set;

import austeretony.alternateui.screen.button.GUIButton;
import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenGUIHelper;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.api.PrivilegeProviderClient;
import austeretony.oxygen_core.client.gui.elements.CurrencyValueGUIElement;
import austeretony.oxygen_core.client.gui.elements.InventoryLoadGUIElement;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIButton;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIButtonPanel;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIText;
import austeretony.oxygen_core.client.gui.elements.OxygenGUITextField;
import austeretony.oxygen_core.client.gui.elements.SectionsGUIDDList;
import austeretony.oxygen_core.client.gui.settings.GUISettings;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_trade.client.PlayerOfferClient;
import austeretony.oxygen_trade.client.gui.trade.buy.BuySectionGUIFiller;
import austeretony.oxygen_trade.client.gui.trade.selling.InventoryItemGUIButton;
import austeretony.oxygen_trade.client.gui.trade.selling.ItemGUIElement;
import austeretony.oxygen_trade.client.gui.trade.selling.OffersAmountGUIElement;
import austeretony.oxygen_trade.client.gui.trade.selling.callback.OfferCreationGUICallback;
import austeretony.oxygen_trade.client.input.TradeKeyHandler;
import austeretony.oxygen_trade.common.config.TradeConfig;
import austeretony.oxygen_trade.common.main.EnumTradePrivilege;
import net.minecraft.item.ItemStack;

public class SellingGUISection extends AbstractGUISection {

    private final TradeMenuGUIScreen screen;

    private OxygenGUIButton createOfferButton;

    private OxygenGUIButtonPanel inventoryContentPanel;

    private OxygenGUITextField amountField, unitPriceField, totalPriceField;

    private ItemGUIElement itemElement;

    private OffersAmountGUIElement offersAmountElement;

    private InventoryLoadGUIElement inventoryLoadElement;

    private CurrencyValueGUIElement offerCreationFeeElement, saleFeeElement, profitElement, balanceElement;

    private AbstractGUICallback offerCreationCallback;

    //cache

    private InventoryItemGUIButton currentItemButton;

    public SellingGUISection(TradeMenuGUIScreen screen) {
        super(screen);
        this.screen = screen;
    }

    @Override
    public void init() {
        this.addElement(new BuySectionGUIFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new OxygenGUIText(4, 5, ClientReference.localize("oxygen_trade.gui.trade.title"), GUISettings.get().getTitleScale(), GUISettings.get().getEnabledTextColor()));

        this.addElement(this.offersAmountElement = new OffersAmountGUIElement(6, 18));

        this.addElement(this.itemElement = new ItemGUIElement(6, 28));

        this.addElement(new OxygenGUIText(6, 48, ClientReference.localize("oxygen_trade.gui.trade.amount"), GUISettings.get().getSubTextScale(), GUISettings.get().getEnabledTextColorDark()));
        this.addElement(this.amountField = new OxygenGUITextField(6, 55, 40, 8, 10, "", 3, true, - 1));

        this.amountField.setInputListener((charCode, keyCode)->this.calculateFeesAndProfit(EnumField.AMOUNT));

        long maxPrice = PrivilegeProviderClient.getValue(EnumTradePrivilege.PRICE_MAX_VALUE.toString(), TradeConfig.PRICE_MAX_VALUE.getLongValue());

        this.addElement(new OxygenGUIText(6, 68, ClientReference.localize("oxygen_trade.gui.trade.unitPrice"), GUISettings.get().getSubTextScale(), GUISettings.get().getEnabledTextColorDark()));
        this.addElement(this.unitPriceField = new OxygenGUITextField(6, 75, 40, 8, 10, "", 3, true, maxPrice));

        this.unitPriceField.setInputListener((charCode, keyCode)->this.calculateFeesAndProfit(EnumField.UNIT_PRICE));

        this.addElement(new OxygenGUIText(6, 88, ClientReference.localize("oxygen_trade.gui.trade.totalPrice"), GUISettings.get().getSubTextScale(), GUISettings.get().getEnabledTextColorDark()));
        this.addElement(this.totalPriceField = new OxygenGUITextField(6, 95, 40, 8, 10, "", 3, true, maxPrice));

        this.totalPriceField.setInputListener((charCode, keyCode)->this.calculateFeesAndProfit(EnumField.TOTAL_PRICE));

        this.addElement(new OxygenGUIText(6, 112, ClientReference.localize("oxygen_trade.gui.trade.listingFee"), GUISettings.get().getSubTextScale(), GUISettings.get().getEnabledTextColorDark()));
        this.addElement(this.offerCreationFeeElement = new CurrencyValueGUIElement(68, 112));   

        this.addElement(new OxygenGUIText(6, 122, ClientReference.localize("oxygen_trade.gui.trade.saleFee"), GUISettings.get().getSubTextScale(), GUISettings.get().getEnabledTextColorDark()));
        this.addElement(this.saleFeeElement = new CurrencyValueGUIElement(68, 122));   

        this.addElement(new OxygenGUIText(6, 132, ClientReference.localize("oxygen_trade.gui.trade.profit"), GUISettings.get().getSubTextScale(), GUISettings.get().getEnabledTextColorDark()));
        this.addElement(this.profitElement = new CurrencyValueGUIElement(68, 132));   

        //list item button
        this.addElement(this.createOfferButton = new OxygenGUIButton(20, this.getHeight() - 14, 40, 10, ClientReference.localize("oxygen_trade.gui.trade.createOfferButton")).disable()); 

        //inventory content panel
        this.addElement(this.inventoryContentPanel = new OxygenGUIButtonPanel(this.screen, 76, 16, this.getWidth() - 85, 16, 1, 36, 9, GUISettings.get().getSubTextScale(), true));

        this.loadInventoryContent();

        this.inventoryContentPanel.<InventoryItemGUIButton>setClickListener((previous, clicked, mouseX, mouseY, mouseButton)->{
            if (mouseButton == 0) {
                if (previous != null)
                    previous.setToggled(false);
                clicked.toggle();
                this.currentItemButton = clicked;
                this.updateFields();
                this.itemElement.setItemStack(clicked.index, clicked.getPlayerStock());
            }
        });

        //sections switcher
        this.addElement(new SectionsGUIDDList(this.getWidth() - 4, 5, this, this.screen.getBuySection(), this.screen.getOffersSection(), this.screen.getSalesHistorySection()));

        //client data
        this.addElement(this.inventoryLoadElement = new InventoryLoadGUIElement(78, this.getHeight() - 9, EnumGUIAlignment.RIGHT));
        this.inventoryLoadElement.setLoad(this.screen.getBuySection().getInventoryLoadElement().getLoad());
        this.addElement(this.balanceElement = new CurrencyValueGUIElement(this.getWidth() - 19, this.getHeight() - 10));   
        this.balanceElement.setValue(this.screen.getBuySection().getBalanceElement().getValue());

        this.offerCreationCallback = new OfferCreationGUICallback(this.screen, this, 140, 50).enableDefaultBackground();
    }

    private void loadInventoryContent() {
        this.inventoryContentPanel.reset();
        Set<String> stacks = new HashSet<>();
        String key;
        for (ItemStackWrapper stackWrapper : this.screen.inventoryContent.keySet()) {
            key = getKey(stackWrapper);
            if (!stacks.contains(key)) {
                this.inventoryContentPanel.addButton(new InventoryItemGUIButton(stackWrapper, this.screen.getEqualStackAmount(stackWrapper)));
                stacks.add(key);
            }              
        }

        this.inventoryContentPanel.getScroller().resetPosition();
        this.inventoryContentPanel.getScroller().getSlider().reset();

        this.inventoryContentPanel.getScroller().updateRowsAmount(MathUtils.clamp(this.screen.inventoryContent.size(), 9, 36));
    }

    public void updateOffersAmount() {
        this.offersAmountElement.updateOffersAmount();
    }

    private static String getKey(ItemStackWrapper stackWrapper) {
        return String.valueOf(stackWrapper.itemId) + "_" + String.valueOf(stackWrapper.damage) + "_" + stackWrapper.stackNBTStr + "_" + stackWrapper.capNBTStr;
    }

    private void updateFields() {
        int maxAmount = PrivilegeProviderClient.getValue(EnumTradePrivilege.ITEMS_PER_OFFER_MAX_AMOUNT.toString(), TradeConfig.ITEMS_PER_OFFER_MAX_AMOUNT.getIntValue());
        if (maxAmount < 0)
            maxAmount = this.currentItemButton.index.getMaxStackSize();
        this.amountField.enableNumberFieldMode(MathUtils.lesserOfTwo(
                this.currentItemButton.getPlayerStock(), 
                maxAmount));
        this.amountField.setText("1");
        this.unitPriceField.reset();
        this.totalPriceField.reset();
        this.offerCreationFeeElement.setValue(0);
        this.offerCreationFeeElement.setRed(false);
        this.saleFeeElement.setValue(0);
        this.profitElement.setValue(0);
        this.createOfferButton.disable();
    }

    private void calculateFeesAndProfit(EnumField changedField) {
        long 
        amount = this.amountField.getTypedNumber(),
        unitPrice = this.unitPriceField.getTypedNumber(),
        totalPrice = this.totalPriceField.getTypedNumber(),
        result = 0, 
        maxPrice = PrivilegeProviderClient.getValue(EnumTradePrivilege.PRICE_MAX_VALUE.toString(), TradeConfig.PRICE_MAX_VALUE.getLongValue()),
        offerCreationFee, saleFee;
        switch (changedField) {
        case AMOUNT:
            if (unitPrice > 0) {
                result = unitPrice * amount;
                if (result > maxPrice) return;
                this.totalPriceField.setText(String.valueOf(result));
            }
            break;
        case UNIT_PRICE:
            if (amount > 0) {
                result = unitPrice * amount;
                if (result > maxPrice) return;
                this.totalPriceField.setText(String.valueOf(result));
            }
            break;
        case TOTAL_PRICE:
            if (amount > 0) {
                result = totalPrice;
                this.unitPriceField.setText(String.valueOf(totalPrice / amount));
            }
            break;
        }
        offerCreationFee = MathUtils.percentValueOf(result, PrivilegeProviderClient.getValue(EnumTradePrivilege.OFFER_CREATION_FEE_PERCENT.toString(), TradeConfig.OFFER_CREATION_FEE_PERCENT.getIntValue()));
        this.offerCreationFeeElement.setValue(offerCreationFee);
        this.offerCreationFeeElement.setRed(offerCreationFee > this.balanceElement.getValue());
        if (!this.offersAmountElement.reachedMaxAmount())
            this.createOfferButton.setEnabled(result > 0 && this.balanceElement.getValue() >= offerCreationFee);
        saleFee = MathUtils.percentValueOf(result, PrivilegeProviderClient.getValue(EnumTradePrivilege.OFFER_SALE_FEE_PERCENT.toString(), TradeConfig.OFFER_SALE_FEE_PERCENT.getIntValue()));
        this.saleFeeElement.setValue(saleFee);
        this.profitElement.setValue(result - saleFee);
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {   
        if (!this.amountField.isDragged() 
                && !this.unitPriceField.isDragged() 
                && !this.totalPriceField.isDragged() 
                && !this.hasCurrentCallback())
            if (OxygenGUIHelper.isOxygenMenuEnabled()) {
                if (keyCode == TradeMenuGUIScreen.TRADE_MENU_ENTRY.getIndex() + 2)
                    this.screen.close();
            } else if (keyCode == TradeKeyHandler.TRADE_MENU.getKeyCode())
                this.screen.close();
        return super.keyTyped(typedChar, keyCode); 
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (mouseButton == 0)
            if (element == this.createOfferButton)
                this.offerCreationCallback.open();
    }

    public void setCreateOfferButtonState(boolean enabled) {
        this.createOfferButton.setEnabled(enabled);
    }

    public void itemPurchased(PlayerOfferClient offer, long balance) {
        this.balanceElement.setValue(balance);
        if (offer.isOwner(OxygenHelperClient.getPlayerUsername()))
            this.offersAmountElement.decrementOffersAmount(1);
    }

    public void offerCreated(PlayerOfferClient offer, long balance) {
        this.balanceElement.setValue(balance);
        this.screen.decrementEqualStackAmount(offer.getOfferedStack(), offer.getAmount());
        this.offersAmountElement.incrementOffersAmount(1);
        if (this.offersAmountElement.reachedMaxAmount())
            this.createOfferButton.disable();
        boolean reloadContent = false;
        ItemStack itemStack;
        for (GUIButton b : this.inventoryContentPanel.buttonsBuffer) {
            InventoryItemGUIButton button = (InventoryItemGUIButton) b;
            if (button.stackWrapper.isEquals(offer.getOfferedStack())) {
                button.decrementPlayerStock(offer.getAmount());
                if (button.isToggled()) {
                    itemStack = offer.getOfferedStack().getItemStack();
                    this.inventoryLoadElement.decrementLoad(offer.getAmount() / itemStack.getMaxStackSize());
                    if (button.getPlayerStock() > 0) {
                        this.itemElement.setPlayerStock(button.getPlayerStock());
                        this.amountField.enableNumberFieldMode(button.getPlayerStock());
                        if (this.amountField.getTypedNumber() > button.getPlayerStock()) {
                            this.amountField.setText(String.valueOf(button.getPlayerStock()));
                            this.calculateFeesAndProfit(EnumField.AMOUNT);
                        }
                    } else {
                        reloadContent = true;
                        this.itemElement.setVisible(false);
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

    public void offerCanceled(PlayerOfferClient offer, long balance) {
        this.offersAmountElement.decrementOffersAmount(1);
    }

    public OffersAmountGUIElement getOffersAmountElement() {
        return this.offersAmountElement;
    }

    public InventoryLoadGUIElement getInventoryLoadElement() {
        return this.inventoryLoadElement;
    }

    public CurrencyValueGUIElement getBalanceElement() {
        return this.balanceElement;
    }

    public InventoryItemGUIButton getCurrentItemButton() {
        return this.currentItemButton;
    }

    public OxygenGUITextField getAmountTextField() {
        return this.amountField;
    }

    public OxygenGUITextField getTotalPriceTextField() {
        return this.totalPriceField;
    }

    private enum EnumField {

        AMOUNT,
        UNIT_PRICE,
        TOTAL_PRICE
    }
}
