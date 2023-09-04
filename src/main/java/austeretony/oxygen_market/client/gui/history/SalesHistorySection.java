package austeretony.oxygen_market.client.gui.history;

import java.util.List;
import java.util.stream.Collectors;

import org.lwjgl.input.Keyboard;

import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.OxygenManagerClient;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.gui.OxygenGUITextures;
import austeretony.oxygen_core.client.gui.elements.OxygenDefaultBackgroundWithButtonsUnderlinedFiller;
import austeretony.oxygen_core.client.gui.elements.OxygenDropDownList;
import austeretony.oxygen_core.client.gui.elements.OxygenDropDownList.OxygenDropDownListWrapperEntry;
import austeretony.oxygen_core.client.gui.elements.OxygenKeyButton;
import austeretony.oxygen_core.client.gui.elements.OxygenNumberField;
import austeretony.oxygen_core.client.gui.elements.OxygenScrollablePanel;
import austeretony.oxygen_core.client.gui.elements.OxygenTextField;
import austeretony.oxygen_core.client.gui.elements.OxygenTextLabel;
import austeretony.oxygen_core.client.gui.elements.OxygenTexturedButton;
import austeretony.oxygen_core.client.preset.ItemCategoriesPresetClient;
import austeretony.oxygen_core.client.preset.ItemCategoriesPresetClient.ItemCategory;
import austeretony.oxygen_core.client.preset.ItemCategoriesPresetClient.ItemSubCategory;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_market.client.MarketManagerClient;
import austeretony.oxygen_market.client.gui.market.BuySection;
import austeretony.oxygen_market.client.gui.market.history.HistoryPanelEntry;
import austeretony.oxygen_market.client.market.SalesHistoryEntryClient;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.EnumRarity;

public class SalesHistorySection extends AbstractGUISection {

    private OxygenKeyButton filterHistoryButton;

    private OxygenTextLabel entriesAmountLabel, historyEmptyLabel;

    private OxygenTexturedButton resetFiltersButton;

    private OxygenScrollablePanel historyEntriesPanel;

    private OxygenDropDownList categoriesList, subCategoriesList, sortersList, rarityList;

    private OxygenTextField textField, buyerUsernameField, sellerUsernameField;

    private OxygenNumberField minPriceField, maxPriceField;

    //filters

    private ItemCategory currentCategory = ItemCategoriesPresetClient.COMMON_CATEGORY;

    private ItemSubCategory currentSubCategory;

    private EnumHistorySorter currentSorter = EnumHistorySorter.TIME;

    private int currentRarity = - 1;

    private long 
    currentMinPrice = 0L, 
    currentMaxPrice = Long.MAX_VALUE;

    @Override
    public void init() {
        this.addElement(new OxygenDefaultBackgroundWithButtonsUnderlinedFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new OxygenTextLabel(4, 12, ClientReference.localize("oxygen_market.gui.history.title"), EnumBaseGUISetting.TEXT_TITLE_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        this.addElement(this.resetFiltersButton = new OxygenTexturedButton(68, 18, 5, 5, OxygenGUITextures.CROSS_ICONS, 5, 5, ClientReference.localize("oxygen_market.gui.market.tooltip.resetFilters")));         

        //offers panel
        this.addElement(this.historyEntriesPanel = new OxygenScrollablePanel(this.screen, 76, 16, this.getWidth() - 85, 16, 1, 100, 9, EnumBaseGUISetting.TEXT_PANEL_SCALE.get().asFloat(), true));

        //price filter
        this.addElement(new OxygenTextLabel(6, 74, ClientReference.localize("oxygen_market.gui.market.priceRange"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
        this.addElement(this.minPriceField = new OxygenNumberField(6, 76, 30, "", Long.MAX_VALUE, false, 0, true));
        this.minPriceField.setInputListener((keyChar, keyCode)->this.currentMinPrice = this.minPriceField.getTypedNumberAsLong());

        this.addElement(this.maxPriceField = new OxygenNumberField(43, 76, 30, "", Long.MAX_VALUE, false, 0, true));
        this.maxPriceField.setInputListener((keyChar, keyCode)->this.currentMaxPrice = this.maxPriceField.getTypedNumberAsLong());

        //search field
        this.addElement(new OxygenTextLabel(6, 93, ClientReference.localize("oxygen_market.gui.market.search"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
        this.addElement(this.textField = new OxygenTextField(6, 95, 67, 20, ""));

        //buyer username field
        this.addElement(new OxygenTextLabel(6, 112, ClientReference.localize("oxygen_market.gui.history.buyer"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
        this.addElement(this.buyerUsernameField = new OxygenTextField(6, 114, 67, 20, ""));

        //seller username field
        this.addElement(new OxygenTextLabel(6, 131, ClientReference.localize("oxygen_market.gui.history.seller"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
        this.addElement(this.sellerUsernameField = new OxygenTextField(6, 133, 67, 20, ""));

        this.addElement(new OxygenTextLabel(6, 23, ClientReference.localize("oxygen_market.gui.market.category"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
        this.addElement(new OxygenTextLabel(6, 54, ClientReference.localize("oxygen_market.gui.market.sortBy"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
        this.addElement(new OxygenTextLabel(6, 150, ClientReference.localize("oxygen_market.gui.market.rarity"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));

        //filter offers button
        this.addElement(this.filterHistoryButton = new OxygenKeyButton(0, this.getY() + this.getHeight() + this.screen.guiTop - 8, ClientReference.localize("oxygen_market.gui.market.button.filterHistory"), Keyboard.KEY_E, this::filter));
        this.addElement(this.entriesAmountLabel = new OxygenTextLabel(6, this.getHeight() - 15, "", EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.05F, EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()).disableFull());

        //rarity filter
        this.rarityList = new OxygenDropDownList(6, 152, 67, ClientReference.localize("oxygen_market.rarity.any"));
        this.rarityList.addElement(new OxygenDropDownListWrapperEntry<Integer>(- 1, ClientReference.localize("oxygen_market.rarity.any")));
        for (EnumRarity rarity : EnumRarity.values())
            this.rarityList.addElement(new OxygenDropDownListWrapperEntry<Integer>(rarity.ordinal(), BuySection.getRarityName(rarity)));
        this.addElement(this.rarityList);

        //rarity filter listener
        this.rarityList.<OxygenDropDownListWrapperEntry<Integer>>setElementClickListener((element)->{
            this.currentRarity = element.getWrapped();
        });

        //base sorters filter
        this.sortersList = new OxygenDropDownList(6, 56, 67, this.currentSorter.localizedName());
        for (EnumHistorySorter sorter : EnumHistorySorter.values())
            this.sortersList.addElement(new OxygenDropDownListWrapperEntry<EnumHistorySorter>(sorter, sorter.localizedName()));
        this.addElement(this.sortersList);

        //base sorters listener
        this.sortersList.<OxygenDropDownListWrapperEntry<EnumHistorySorter>>setElementClickListener((element)->{
            this.currentSorter = element.getWrapped();
        });

        //sub categories filter
        this.subCategoriesList = new OxygenDropDownList(6, 36, 67, "");
        this.addElement(this.subCategoriesList);

        //sub categories listener
        this.subCategoriesList.<OxygenDropDownListWrapperEntry<ItemSubCategory>>setElementClickListener((element)->{
            this.currentSubCategory = element.getWrapped();
        });

        this.loadSubCategories(ItemCategoriesPresetClient.COMMON_CATEGORY);

        //categories filter
        this.categoriesList = new OxygenDropDownList(6, 25, 67, this.currentCategory.localizedName());
        for (ItemCategory category : OxygenManagerClient.instance().getItemCategoriesPreset().getCategories())
            this.categoriesList.addElement(new OxygenDropDownListWrapperEntry<ItemCategory>(category, category.localizedName()));
        this.addElement(this.categoriesList);

        //categories listener
        this.categoriesList.<OxygenDropDownListWrapperEntry<ItemCategory>>setElementClickListener((element)->{
            this.loadSubCategories(this.currentCategory = element.getWrapped());
        });

        String offersEmpty = ClientReference.localize("oxygen_market.gui.market.noDataFound");
        this.addElement(this.historyEmptyLabel = new OxygenTextLabel(76 + ((this.historyEntriesPanel.getButtonWidth() - this.textWidth(offersEmpty, EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.05F)) / 2), 
                23, offersEmpty, EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.05F, EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()).setVisible(false));
    }

    private void calculateButtonsHorizontalPosition() {
        ScaledResolution sr = new ScaledResolution(this.mc);
        this.filterHistoryButton.setX((sr.getScaledWidth() - (12 + this.textWidth(this.filterHistoryButton.getDisplayText(), this.filterHistoryButton.getTextScale()))) / 2 - this.screen.guiLeft);
    }

    private void loadSubCategories(ItemCategory category) {
        this.currentSubCategory = category.getSubCategories().get(0);
        this.subCategoriesList.reset();
        this.subCategoriesList.setDisplayText(this.currentSubCategory.localizedName());
        for (ItemSubCategory subCategory : category.getSubCategories())
            this.subCategoriesList.addElement(new OxygenDropDownListWrapperEntry<ItemSubCategory>(subCategory, subCategory.localizedName()));
    }

    private void filter() {
        if (!this.textField.isDragged())
            this.filterOffers();
    }

    public void filterOffers() {
        List<SalesHistoryEntryClient> entries = this.getSalesHistoryEntriesList();

        this.historyEmptyLabel.setVisible(entries.isEmpty());

        this.historyEntriesPanel.reset();
        for (SalesHistoryEntryClient entry : entries)
            this.historyEntriesPanel.addEntry(new HistoryPanelEntry(
                    entry, 
                    ClientReference.localize("oxygen_market.gui.history.bought", entry.getBuyerUsername(), entry.getSellerUsername()), 
                    ((SalesHistoryScreen) this.screen).getCurrencyProperties()));

        this.entriesAmountLabel.setDisplayText(String.format("%d/%d", entries.size(), MarketManagerClient.instance().getSalesHistoryContainer().getEntriesAmount()));

        this.historyEntriesPanel.getScroller().reset();
        this.historyEntriesPanel.getScroller().updateRowsAmount(MathUtils.clamp(entries.size(), 9, 900));
    }

    private List<SalesHistoryEntryClient> getSalesHistoryEntriesList() {
        return MarketManagerClient.instance().getSalesHistoryContainer().getEntries()
                .stream()
                .filter(this::filterByCategory)
                .filter(this::filterByPriceRange)
                .filter(this::filterByName)
                .filter(this::filterByBuyerUsername)
                .filter(this::filterBySellerUsername)
                .filter(this::filterByRarity)
                .sorted(this.currentSorter.comparator)
                .collect(Collectors.toList());
    }

    private boolean filterByCategory(SalesHistoryEntryClient entry) {
        return this.currentCategory.isValid(this.currentSubCategory, entry.getStackWrapper().getCachedItemStack().getItem().getRegistryName());
    }

    private boolean filterByPriceRange(SalesHistoryEntryClient entry) {
        return entry.getPrice() >= this.currentMinPrice && entry.getPrice() <= this.currentMaxPrice;
    }

    private boolean filterByName(SalesHistoryEntryClient entry) {
        return this.textField.getTypedText().isEmpty() || entry.getStackWrapper().getCachedItemStack().getDisplayName().contains(this.textField.getTypedText());
    }

    private boolean filterByBuyerUsername(SalesHistoryEntryClient entry) {
        return this.buyerUsernameField.getTypedText().isEmpty() || entry.getBuyerUsername().contains(this.buyerUsernameField.getTypedText());
    }

    private boolean filterBySellerUsername(SalesHistoryEntryClient entry) {
        return this.sellerUsernameField.getTypedText().isEmpty() || entry.getSellerUsername().contains(this.sellerUsernameField.getTypedText());
    }

    private boolean filterByRarity(SalesHistoryEntryClient entry) {
        return this.currentRarity == - 1 || entry.getStackWrapper().getCachedItemStack().getRarity().ordinal() == this.currentRarity;
    }

    private void resetFilters() {
        this.currentCategory = ItemCategoriesPresetClient.COMMON_CATEGORY;
        this.categoriesList.setDisplayText(this.currentCategory.localizedName());
        this.loadSubCategories(this.currentCategory);
        this.currentSorter = EnumHistorySorter.PURCHASE_PRICE;
        this.sortersList.setDisplayText(this.currentSorter.localizedName());
        this.currentRarity = - 1;
        this.rarityList.setDisplayText(ClientReference.localize("oxygen_market.rarity.any"));
        this.rarityList.setTextDynamicColor(EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt(), EnumBaseGUISetting.TEXT_DISABLED_COLOR.get().asInt(), EnumBaseGUISetting.TEXT_HOVERED_COLOR.get().asInt());

        this.currentMinPrice = 0L;
        this.minPriceField.reset();
        this.currentMaxPrice = Long.MAX_VALUE;
        this.maxPriceField.reset();

        this.textField.reset();
        this.buyerUsernameField.reset();
        this.sellerUsernameField.reset();
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (mouseButton == 0) {
            if (element == this.filterHistoryButton)
                this.filterOffers();
            else if (element == this.resetFiltersButton)
                this.resetFilters();
        }
    }

    public void setFilterButtonState(boolean flag) {
        this.filterHistoryButton.setEnabled(flag);
        if (flag) {
            this.entriesAmountLabel.enableFull();
            this.entriesAmountLabel.setDisplayText(String.format("0/%s", MarketManagerClient.instance().getSalesHistoryContainer().getEntriesAmount()));
        }
    }

    public void salesHistorySynchronized() {
        this.setFilterButtonState(true);

        this.calculateButtonsHorizontalPosition();
    }
}
