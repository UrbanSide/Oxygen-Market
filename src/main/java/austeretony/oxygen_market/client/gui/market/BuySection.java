package austeretony.oxygen_market.client.gui.market;

import java.util.List;
import java.util.stream.Collectors;

import org.lwjgl.input.Keyboard;

import austeretony.alternateui.screen.button.GUIButton;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.OxygenManagerClient;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.api.OxygenGUIHelper;
import austeretony.oxygen_core.client.api.PrivilegesProviderClient;
import austeretony.oxygen_core.client.api.WatcherHelperClient;
import austeretony.oxygen_core.client.gui.OxygenGUITextures;
import austeretony.oxygen_core.client.gui.elements.OxygenContextMenu;
import austeretony.oxygen_core.client.gui.elements.OxygenCurrencyValue;
import austeretony.oxygen_core.client.gui.elements.OxygenDefaultBackgroundWithButtonsUnderlinedFiller;
import austeretony.oxygen_core.client.gui.elements.OxygenDropDownList;
import austeretony.oxygen_core.client.gui.elements.OxygenDropDownList.OxygenDropDownListWrapperEntry;
import austeretony.oxygen_core.client.gui.elements.OxygenInventoryLoad;
import austeretony.oxygen_core.client.gui.elements.OxygenKeyButton;
import austeretony.oxygen_core.client.gui.elements.OxygenNumberField;
import austeretony.oxygen_core.client.gui.elements.OxygenScrollablePanel;
import austeretony.oxygen_core.client.gui.elements.OxygenSectionSwitcher;
import austeretony.oxygen_core.client.gui.elements.OxygenTextField;
import austeretony.oxygen_core.client.gui.elements.OxygenTextLabel;
import austeretony.oxygen_core.client.gui.elements.OxygenTexturedButton;
import austeretony.oxygen_core.client.preset.ItemCategoriesPresetClient;
import austeretony.oxygen_core.client.preset.ItemCategoriesPresetClient.ItemCategory;
import austeretony.oxygen_core.client.preset.ItemCategoriesPresetClient.ItemSubCategory;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_market.client.MarketManagerClient;
import austeretony.oxygen_market.client.gui.market.buy.EnumOffersSorter;
import austeretony.oxygen_market.client.gui.market.buy.OfferPanelEntry;
import austeretony.oxygen_market.client.gui.market.buy.context.CancelOfferContextAction;
import austeretony.oxygen_market.client.gui.market.buy.context.RemoveOfferContextAction;
import austeretony.oxygen_market.client.market.OfferClient;
import austeretony.oxygen_market.client.settings.EnumMarketClientSetting;
import austeretony.oxygen_market.common.config.MarketConfig;
import austeretony.oxygen_market.common.main.EnumMarketPrivilege;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.EnumRarity;

public class BuySection extends AbstractGUISection {

    private final MarketMenuScreen screen;

    private OxygenKeyButton filterOffersButton;

    private OxygenTextLabel offersAmountLabel, offersEmptyLabel;

    private OxygenTexturedButton applyLatestFiltersButton, resetFiltersButton;

    private OxygenScrollablePanel offersPanel;

    private OxygenDropDownList categoriesList, subCategoriesList, sortersList, rarityList, profitabilityList;

    private OxygenTextField textField;

    private OxygenNumberField minPriceField, maxPriceField;

    private OxygenInventoryLoad inventoryLoad;

    private OxygenCurrencyValue balanceValue;

    //filters

    private ItemCategory currentCategory = ItemCategoriesPresetClient.COMMON_CATEGORY;

    private ItemSubCategory currentSubCategory;

    private EnumOffersSorter currentSorter = EnumOffersSorter.PURCHASE_PRICE;

    private int
    currentRarity = - 1,
    currentProfitability = - 1;

    private long
    currentMinPrice = 0L,
    currentMaxPrice = Long.MAX_VALUE;

    //cache

    private static ItemCategory categoryCached = ItemCategoriesPresetClient.COMMON_CATEGORY;

    private static ItemSubCategory subCategoryCached;

    private static EnumOffersSorter sorterCached = EnumOffersSorter.PURCHASE_PRICE;

    private static int
    rarityCached = - 1,
    profitabilityCached = - 1;

    private static long
    minPriceCached = 0L,
    maxPriceCached = Long.MAX_VALUE;

    private static String textSearchCached = "";

    public BuySection(MarketMenuScreen screen) {
        super(screen);
        this.screen = screen;
        this.setDisplayText(ClientReference.localize("oxygen_market.gui.market.buy"));
    }

    @Override
    public void init() {
        this.addElement(new OxygenDefaultBackgroundWithButtonsUnderlinedFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new OxygenTextLabel(4, 12, ClientReference.localize("oxygen_market.gui.market.title"), EnumBaseGUISetting.TEXT_TITLE_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        this.addElement(this.applyLatestFiltersButton = new OxygenTexturedButton(61, 18, 5, 5, OxygenGUITextures.CLOCK_ICONS, 5, 5, ClientReference.localize("oxygen_market.gui.market.tooltip.latestFilters")));
        this.addElement(this.resetFiltersButton = new OxygenTexturedButton(68, 18, 5, 5, OxygenGUITextures.CROSS_ICONS, 5, 5, ClientReference.localize("oxygen_market.gui.market.tooltip.resetFilters")));

        //offers panel
        this.addElement(this.offersPanel = new OxygenScrollablePanel(this.screen, 76, 16, this.getWidth() - 85, 16, 1, 100, 9, EnumBaseGUISetting.TEXT_PANEL_SCALE.get().asFloat(), true));

        this.offersPanel.<OfferPanelEntry>setElementClickListener((previous, clicked, mouseX, mouseY, mouseButton)->{
            if (mouseButton == 0 && !clicked.isOverpriced() && !clicked.isPurchased())
                MarketManagerClient.instance().getOffersManager().purchaseItemSynced(clicked.getWrapped().getId());
        });

        if (PrivilegesProviderClient.getAsBoolean(EnumMarketPrivilege.MARKET_MENU_OPERATOR_OPTIONS.id(), false))
            this.offersPanel.initContextMenu(new OxygenContextMenu(
                    new CancelOfferContextAction(),
                    new RemoveOfferContextAction()));

        //sections switcher
        this.addElement(new OxygenSectionSwitcher(this.getWidth() - 4, 5, this, this.screen.getSellingSection(), this.screen.getOffersSection(), this.screen.getSalesHistorySection()));

        //price filter
        this.addElement(new OxygenTextLabel(6, 74, ClientReference.localize("oxygen_market.gui.market.priceRange"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
        this.addElement(this.minPriceField = new OxygenNumberField(6, 76, 30, "", Long.MAX_VALUE, false, 0, true));
        this.minPriceField.setInputListener((keyChar, keyCode)->minPriceCached = this.currentMinPrice = this.minPriceField.getTypedNumberAsLong());

        this.addElement(this.maxPriceField = new OxygenNumberField(43, 76, 30, "", Long.MAX_VALUE, false, 0, true));
        this.maxPriceField.setInputListener((keyChar, keyCode)->maxPriceCached = this.currentMaxPrice = this.maxPriceField.getTypedNumberAsLong());

        //search field
        this.addElement(new OxygenTextLabel(6, 93, ClientReference.localize("oxygen_market.gui.market.search"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
        this.addElement(this.textField = new OxygenTextField(6, 95, 67, 20, ""));

        this.addElement(new OxygenTextLabel(6, 23, ClientReference.localize("oxygen_market.gui.market.category"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
        this.addElement(new OxygenTextLabel(6, 54, ClientReference.localize("oxygen_market.gui.market.sortBy"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
        this.addElement(new OxygenTextLabel(6, 112, ClientReference.localize("oxygen_market.gui.market.rarity"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
        this.addElement(new OxygenTextLabel(6, 132, ClientReference.localize("oxygen_market.gui.market.profitability"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));

        //filter offers button
        this.addElement(this.filterOffersButton = new OxygenKeyButton(0, this.getY() + this.getHeight() + this.screen.guiTop - 8, ClientReference.localize("oxygen_market.gui.market.button.filterOffers"), Keyboard.KEY_E, this::filter).disable());
        this.addElement(this.offersAmountLabel = new OxygenTextLabel(6, this.getHeight() - 15, "", EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.05F, EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()).disableFull());

        //client data
        this.addElement(this.inventoryLoad = new OxygenInventoryLoad(6, this.getHeight() - 8));
        this.inventoryLoad.updateLoad();
        this.addElement(this.balanceValue = new OxygenCurrencyValue(this.getWidth() - 14, this.getHeight() - 10));
        this.balanceValue.setValue(OxygenMain.COMMON_CURRENCY_INDEX, WatcherHelperClient.getLong(OxygenMain.COMMON_CURRENCY_INDEX));

        //profitability filter
        this.profitabilityList = new OxygenDropDownList(6, 134, 67, ClientReference.localize("oxygen_market.profitability.any"));
        this.profitabilityList.addElement(new OxygenDropDownListWrapperEntry<Integer>(- 1, ClientReference.localize("oxygen_market.profitability.any")));
        for (austeretony.oxygen_core.common.EnumRarity rarity : austeretony.oxygen_core.common.EnumRarity.values())
            this.profitabilityList.addElement(new OxygenDropDownListWrapperEntry<Integer>(rarity.ordinal(), rarity.localizedName())
                    .setTextDynamicColor(rarity.getColor(), rarity.getColor(), rarity.getColor()));
        this.addElement(this.profitabilityList);
        this.profitabilityList.setEnabled(EnumMarketClientSetting.ENABLE_PROFITABILITY_CALCULATION.get().asBoolean());

        //profitability filter listener
        this.profitabilityList.<OxygenDropDownListWrapperEntry<Integer>>setElementClickListener((element)->{
            if (element.getWrapped() == - 1) {
                this.profitabilityList.setTextDynamicColor(EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt(), EnumBaseGUISetting.TEXT_DISABLED_COLOR.get().asInt(), EnumBaseGUISetting.TEXT_HOVERED_COLOR.get().asInt());
            } else {
                int color = austeretony.oxygen_core.common.EnumRarity.values()[element.getWrapped()].getColor();
                this.profitabilityList.setTextDynamicColor(color, color, color);
            }

            profitabilityCached = this.currentProfitability = element.getWrapped();
        });

        //rarity filter
        this.rarityList = new OxygenDropDownList(6, 114, 67, ClientReference.localize("oxygen_market.rarity.any"));
        this.rarityList.addElement(new OxygenDropDownListWrapperEntry<Integer>(- 1, ClientReference.localize("oxygen_market.rarity.any")));
        for (EnumRarity rarity : EnumRarity.values())
            this.rarityList.addElement(new OxygenDropDownListWrapperEntry<Integer>(rarity.ordinal(), getRarityName(rarity)));
        this.addElement(this.rarityList);

        //rarity filter listener
        this.rarityList.<OxygenDropDownListWrapperEntry<Integer>>setElementClickListener((element)->{
            rarityCached = this.currentRarity = element.getWrapped();
        });

        //base sorters filter
        this.sortersList = new OxygenDropDownList(6, 56, 67, this.currentSorter.localizedName());
        for (EnumOffersSorter sorter : EnumOffersSorter.values())
            this.sortersList.addElement(new OxygenDropDownListWrapperEntry<EnumOffersSorter>(sorter, sorter.localizedName()));
        this.addElement(this.sortersList);

        //base sorters listener
        this.sortersList.<OxygenDropDownListWrapperEntry<EnumOffersSorter>>setElementClickListener((element)->{
            sorterCached = this.currentSorter = element.getWrapped();
        });

        //sub categories filter
        this.subCategoriesList = new OxygenDropDownList(6, 36, 67, "");
        this.addElement(this.subCategoriesList);

        //sub categories listener
        this.subCategoriesList.<OxygenDropDownListWrapperEntry<ItemSubCategory>>setElementClickListener((element)->{
            subCategoryCached = this.currentSubCategory = element.getWrapped();
        });

        this.loadSubCategories(ItemCategoriesPresetClient.COMMON_CATEGORY);

        //categories filter
        this.categoriesList = new OxygenDropDownList(6, 25, 67, this.currentCategory.localizedName());
        for (ItemCategory category : OxygenManagerClient.instance().getItemCategoriesPreset().getCategories())
            this.categoriesList.addElement(new OxygenDropDownListWrapperEntry<ItemCategory>(category, category.localizedName()));
        this.addElement(this.categoriesList);

        //categories listener
        this.categoriesList.<OxygenDropDownListWrapperEntry<ItemCategory>>setElementClickListener((element)->{
            this.loadSubCategories(categoryCached = this.currentCategory = element.getWrapped());
        });

        String offersEmpty = ClientReference.localize("oxygen_market.gui.market.noOffersFound");
        this.addElement(this.offersEmptyLabel = new OxygenTextLabel(76 + ((this.offersPanel.getButtonWidth() - this.textWidth(offersEmpty, EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.05F)) / 2),
                23, offersEmpty, EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.05F, EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()).setVisible(false));
    }

    private void calculateButtonsHorizontalPosition() {
        ScaledResolution sr = new ScaledResolution(this.mc);
        this.filterOffersButton.setX((sr.getScaledWidth() - (12 + this.textWidth(this.filterOffersButton.getDisplayText(), this.filterOffersButton.getTextScale()))) / 2 - this.screen.guiLeft);
    }

    private void loadSubCategories(ItemCategory category) {
        this.currentSubCategory = category.getSubCategories().get(0);
        this.subCategoriesList.reset();
        this.subCategoriesList.setDisplayText(this.currentSubCategory.localizedName());
        for (ItemSubCategory subCategory : category.getSubCategories())
            this.subCategoriesList.addElement(new OxygenDropDownListWrapperEntry<ItemSubCategory>(subCategory, subCategory.localizedName()));
    }

    public static String getRarityName(EnumRarity rarity) {
        return rarity.color + ClientReference.localize(rarity.rarityName);
    }

    private void filter() {
        if (!this.textField.isDragged())
            this.filterOffers();
    }

    public void filterOffers() {
        List<OfferClient> offers = this.getOffers();

        this.offersEmptyLabel.setVisible(offers.isEmpty());

        this.offersPanel.reset();
        OfferPanelEntry entry;
        for (OfferClient offer : offers) {
            this.offersPanel.addEntry(entry = new OfferPanelEntry(
                    offer,
                    this.screen.getCurrencyProperties(),
                    this.screen.getEqualStackAmount(offer.getStackWrapper()), offer.getPrice() > this.balanceValue.getValue()));
            if (this.screen.historySynchronized)
                entry.initProfitability(this.screen.getOfferProfitability(offer));
        }

        this.offersAmountLabel.setDisplayText(String.format("%d/%d", offers.size(), MarketManagerClient.instance().getOffersContainer().getOffersAmount()));

        this.offersPanel.getScroller().reset();
        this.offersPanel.getScroller().updateRowsAmount(MathUtils.clamp(offers.size(), 9, 900));
    }

    private List<OfferClient> getOffers() {
        return MarketManagerClient.instance().getOffersContainer().getOffers()
                .stream()
                .filter(this::filterByCategory)
                .filter(this::filterByPriceRange)
                .filter(this::filterByName)
                .filter(this::filterByRarity)
                .filter(this::filterByProfitability)
                .sorted((o1, o2)->(o1.getStackWrapper().getItemId() - o2.getStackWrapper().getItemId()))
                .sorted(this.currentSorter.comparator)
                .collect(Collectors.toList());
    }

    private boolean filterByCategory(OfferClient offer) {
        return this.currentCategory.isValid(this.currentSubCategory, offer.getStackWrapper().getCachedItemStack().getItem().getRegistryName());
    }

    private boolean filterByPriceRange(OfferClient offer) {
        return offer.getPrice() >= this.currentMinPrice && offer.getPrice() <= this.currentMaxPrice;
    }

    private boolean filterByName(OfferClient offer) {
        return this.textField.getTypedText().isEmpty() || offer.getStackWrapper().getCachedItemStack().getDisplayName().contains(textSearchCached = this.textField.getTypedText());
    }

    private boolean filterByRarity(OfferClient offer) {
        return this.currentRarity == - 1 || offer.getStackWrapper().getCachedItemStack().getRarity().ordinal() == this.currentRarity;
    }

    private boolean filterByProfitability(OfferClient offer) {
        int offerProfitability = this.screen.calculateOfferProfitability(offer);
        if (offerProfitability == - 2)
            offerProfitability = - 1;
        return this.currentProfitability == - 1 || offerProfitability >= this.currentProfitability;
    }

    public void setFilterButtonState(boolean flag) {
        this.filterOffersButton.setEnabled(flag);
        if (flag) {
            this.offersAmountLabel.enableFull();
            this.offersAmountLabel.setDisplayText(String.format("0/%d", MarketManagerClient.instance().getOffersContainer().getOffersAmount()));
        }
    }

    private void applyLatestFilters() {
        this.currentCategory = categoryCached;
        this.categoriesList.setDisplayText(this.currentCategory.localizedName());
        this.loadSubCategories(this.currentCategory);
        this.currentSubCategory = subCategoryCached;
        if (this.currentSubCategory != null)
            this.subCategoriesList.setDisplayText(this.currentSubCategory.localizedName());
        this.currentSorter = sorterCached;
        this.sortersList.setDisplayText(this.currentSorter.localizedName());
        this.currentRarity = rarityCached;
        this.rarityList.setDisplayText(this.currentRarity == - 1 ? ClientReference.localize("oxygen_market.rarity.any") : getRarityName(EnumRarity.values()[this.currentRarity]));
        this.currentProfitability = profitabilityCached;
        this.profitabilityList.setDisplayText(this.currentProfitability == - 1 ? ClientReference.localize("oxygen_market.profitability.any") : austeretony.oxygen_core.common.EnumRarity.values()[this.currentProfitability].localizedName());
        if (this.currentProfitability == - 1)
            this.profitabilityList.setTextDynamicColor(EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt(), EnumBaseGUISetting.TEXT_DISABLED_COLOR.get().asInt(), EnumBaseGUISetting.TEXT_HOVERED_COLOR.get().asInt());
        else {
            int color = austeretony.oxygen_core.common.EnumRarity.values()[this.currentProfitability].getColor();
            this.profitabilityList.setTextDynamicColor(color, color, color);
        }

        this.currentMinPrice = minPriceCached;
        if (this.currentMinPrice == 0L)
            this.minPriceField.reset();
        else
            this.minPriceField.setText(String.valueOf(this.currentMinPrice));
        this.currentMaxPrice = maxPriceCached;
        if (this.currentMaxPrice == Long.MAX_VALUE)
            this.maxPriceField.reset();
        else
            this.maxPriceField.setText(String.valueOf(this.currentMaxPrice));

        this.textField.setText(textSearchCached);
    }

    private void resetFilters() {
        this.currentCategory = ItemCategoriesPresetClient.COMMON_CATEGORY;
        this.categoriesList.setDisplayText(this.currentCategory.localizedName());
        this.loadSubCategories(this.currentCategory);
        this.currentSorter = EnumOffersSorter.PURCHASE_PRICE;
        this.sortersList.setDisplayText(this.currentSorter.localizedName());
        this.currentRarity = - 1;
        this.rarityList.setDisplayText(ClientReference.localize("oxygen_market.rarity.any"));
        this.rarityList.setTextDynamicColor(EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt(), EnumBaseGUISetting.TEXT_DISABLED_COLOR.get().asInt(), EnumBaseGUISetting.TEXT_HOVERED_COLOR.get().asInt());
        this.currentProfitability = - 1;
        this.profitabilityList.setDisplayText(ClientReference.localize("oxygen_market.profitability.any"));
        this.profitabilityList.setTextDynamicColor(EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt(), EnumBaseGUISetting.TEXT_DISABLED_COLOR.get().asInt(), EnumBaseGUISetting.TEXT_HOVERED_COLOR.get().asInt());

        this.currentMinPrice = 0L;
        this.minPriceField.reset();
        this.currentMaxPrice = Long.MAX_VALUE;
        this.maxPriceField.reset();

        this.textField.reset();
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {
        if (!this.textField.isDragged()
                && !this.minPriceField.isDragged()
                && !this.maxPriceField.isDragged()
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
            if (element == this.filterOffersButton)
                this.filterOffers();
            else if (element == this.applyLatestFiltersButton)
                this.applyLatestFilters();
            else if (element == this.resetFiltersButton)
                this.resetFilters();
        }
    }

    public void offersSynchronized() {
        this.setFilterButtonState(this.screen.enableMarketAccess);

        this.calculateButtonsHorizontalPosition();
    }

    public void salesHistorySynchronized() {
        OfferPanelEntry entry;
        for (GUIButton button : this.offersPanel.buttonsBuffer) {
            entry = (OfferPanelEntry) button;
            entry.initProfitability(this.screen.getOfferProfitability(entry.getWrapped()));
        }
    }

    public void itemPurchased(OfferClient offer, long balance) {
        this.balanceValue.updateValue(balance);

        OfferPanelEntry entry;
        for (GUIButton button : this.offersPanel.buttonsBuffer) {
            entry = (OfferPanelEntry) button;
            if (entry.getWrapped().getPrice() > balance)
                entry.setOverpriced();
            if (entry.getWrapped().getId() == offer.getId())
                entry.setPurchased();
        }

        this.offersAmountLabel.setDisplayText(String.format("%s/%s", this.offersPanel.buttonsBuffer.size() - 1, MarketManagerClient.instance().getOffersContainer().getOffersAmount()));
    }

    public void offerCreated(OfferClient offer, long balance) {
        this.balanceValue.updateValue(balance);
        this.inventoryLoad.setLoad(this.screen.getSellingSection().getInventoryLoad().getLoad());

        OfferPanelEntry entry;
        for (GUIButton button : this.offersPanel.buttonsBuffer) {
            entry = (OfferPanelEntry) button;
            if (entry.getWrapped().getPrice() > balance)
                entry.setOverpriced();
        }

        this.offersAmountLabel.setDisplayText(String.format("%s/%s", this.offersPanel.buttonsBuffer.size(), MarketManagerClient.instance().getOffersContainer().getOffersAmount()));
    }

    public void offerCanceled(OfferClient offer, long balance) {
        if (!this.offersPanel.buttonsBuffer.isEmpty())
            this.filterOffers();
    }

    public OxygenInventoryLoad getInventoryLoad() {
        return this.inventoryLoad;
    }

    public OxygenCurrencyValue getBalanceValue() {
        return this.balanceValue;
    }
}
