package austeretony.oxygen_trade.client.gui.history;

import java.util.List;
import java.util.stream.Collectors;

import org.lwjgl.input.Keyboard;

import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.OxygenManagerClient;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.gui.OxygenGUITextures;
import austeretony.oxygen_core.client.gui.elements.OxygenButton;
import austeretony.oxygen_core.client.gui.elements.OxygenDropDownList;
import austeretony.oxygen_core.client.gui.elements.OxygenDropDownList.OxygenDropDownListEntry;
import austeretony.oxygen_core.client.gui.elements.OxygenNumberField;
import austeretony.oxygen_core.client.gui.elements.OxygenScrollablePanel;
import austeretony.oxygen_core.client.gui.elements.OxygenTextField;
import austeretony.oxygen_core.client.gui.elements.OxygenTextLabel;
import austeretony.oxygen_core.client.gui.elements.OxygenTexturedButton;
import austeretony.oxygen_core.client.preset.ItemCategoriesPresetClient;
import austeretony.oxygen_core.client.preset.ItemCategoriesPresetClient.ItemCategory;
import austeretony.oxygen_core.client.preset.ItemCategoriesPresetClient.ItemSubCategory;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_trade.client.SalesHistoryEntryClient;
import austeretony.oxygen_trade.client.TradeManagerClient;
import austeretony.oxygen_trade.client.gui.trade.BuySection;
import austeretony.oxygen_trade.client.gui.trade.buy.BuySectionBackgroundFiller;
import austeretony.oxygen_trade.client.gui.trade.history.HistoryPanelEntry;
import net.minecraft.item.EnumRarity;

public class SalesHistorySection extends AbstractGUISection {

    private OxygenButton filterButton;

    private OxygenTextLabel entriesAmountLabel, historyEmptyLabel;

    private OxygenTexturedButton resetFiltersButton;

    private OxygenScrollablePanel entriesPanel;

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
        this.addElement(new BuySectionBackgroundFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new OxygenTextLabel(4, 12, ClientReference.localize("oxygen_trade.gui.history.title"), EnumBaseGUISetting.TEXT_TITLE_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        this.addElement(this.resetFiltersButton = new OxygenTexturedButton(68, 18, 5, 5, OxygenGUITextures.CROSS_ICONS, 5, 5, ClientReference.localize("oxygen_trade.gui.trade.tooltip.resetFilters")));         

        //offers panel
        this.addElement(this.entriesPanel = new OxygenScrollablePanel(this.screen, 76, 16, this.getWidth() - 85, 16, 1, 100, 9, EnumBaseGUISetting.TEXT_PANEL_SCALE.get().asFloat(), true));

        //price filter
        this.addElement(new OxygenTextLabel(6, 74, ClientReference.localize("oxygen_trade.gui.trade.priceRange"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
        this.addElement(this.minPriceField = new OxygenNumberField(6, 76, 30, "", Long.MAX_VALUE, false, 0, true));
        this.minPriceField.setInputListener((keyChar, keyCode)->this.currentMinPrice = this.minPriceField.getTypedNumberAsLong());

        this.addElement(this.maxPriceField = new OxygenNumberField(43, 76, 30, "", Long.MAX_VALUE, false, 0, true));
        this.maxPriceField.setInputListener((keyChar, keyCode)->this.currentMaxPrice = this.maxPriceField.getTypedNumberAsLong());

        //search field
        this.addElement(new OxygenTextLabel(6, 93, ClientReference.localize("oxygen_trade.gui.trade.search"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
        this.addElement(this.textField = new OxygenTextField(6, 95, 67, 20, ""));

        //buyer username field
        this.addElement(new OxygenTextLabel(6, 112, ClientReference.localize("oxygen_trade.gui.history.buyer"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
        this.addElement(this.buyerUsernameField = new OxygenTextField(6, 114, 67, 20, ""));

        //seller username field
        this.addElement(new OxygenTextLabel(6, 131, ClientReference.localize("oxygen_trade.gui.history.seller"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
        this.addElement(this.sellerUsernameField = new OxygenTextField(6, 133, 67, 20, ""));

        this.addElement(new OxygenTextLabel(6, 23, ClientReference.localize("oxygen_trade.gui.trade.category"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
        this.addElement(new OxygenTextLabel(6, 54, ClientReference.localize("oxygen_trade.gui.trade.sortBy"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
        this.addElement(new OxygenTextLabel(6, 150, ClientReference.localize("oxygen_trade.gui.trade.rarity"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));

        //filter offers button
        this.addElement(this.filterButton = new OxygenButton(6, this.getHeight() - 12, 40, 10, ClientReference.localize("oxygen_trade.gui.trade.filterButton")));
        this.filterButton.setKeyPressListener(Keyboard.KEY_E, ()->this.filter());
        this.addElement(this.entriesAmountLabel = new OxygenTextLabel(6, this.getHeight() - 14, "", EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.05F, EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()).disableFull());

        //rarity filter
        this.rarityList = new OxygenDropDownList(6, 152, 67, ClientReference.localize("oxygen_trade.rarity.any"));
        this.rarityList.addElement(new OxygenDropDownListEntry<Integer>(- 1, ClientReference.localize("oxygen_trade.rarity.any")));
        for (EnumRarity rarity : EnumRarity.values())
            this.rarityList.addElement(new OxygenDropDownListEntry<Integer>(rarity.ordinal(), BuySection.getRarityName(rarity)));
        this.addElement(this.rarityList);

        //rarity filter listener
        this.rarityList.<OxygenDropDownListEntry<Integer>>setClickListener((element)->{
            this.currentRarity = element.index;
        });

        //base sorters filter
        this.sortersList = new OxygenDropDownList(6, 56, 67, this.currentSorter.localizedName());
        for (EnumHistorySorter sorter : EnumHistorySorter.values())
            this.sortersList.addElement(new OxygenDropDownListEntry<EnumHistorySorter>(sorter, sorter.localizedName()));
        this.addElement(this.sortersList);

        //base sorters listener
        this.sortersList.<OxygenDropDownListEntry<EnumHistorySorter>>setClickListener((element)->{
            this.currentSorter = element.index;
        });

        //sub categories filter
        this.subCategoriesList = new OxygenDropDownList(6, 36, 67, "");
        this.addElement(this.subCategoriesList);

        //sub categories listener
        this.subCategoriesList.<OxygenDropDownListEntry<ItemSubCategory>>setClickListener((element)->{
            this.currentSubCategory = element.index;
        });

        this.loadSubCategories(ItemCategoriesPresetClient.COMMON_CATEGORY);

        //categories filter
        this.categoriesList = new OxygenDropDownList(6, 25, 67, this.currentCategory.localizedName());
        for (ItemCategory category : OxygenManagerClient.instance().getItemCategoriesPreset().getCategories())
            this.categoriesList.addElement(new OxygenDropDownListEntry<ItemCategory>(category, category.localizedName()));
        this.addElement(this.categoriesList);

        //categories listener
        this.categoriesList.<OxygenDropDownListEntry<ItemCategory>>setClickListener((element)->{
            this.loadSubCategories(this.currentCategory = element.index);
        });

        String offersEmpty = ClientReference.localize("oxygen_trade.gui.trade.noDataFound");
        this.addElement(this.historyEmptyLabel = new OxygenTextLabel(76 + ((this.entriesPanel.getButtonWidth() - this.textWidth(offersEmpty, EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.05F)) / 2), 
                23, offersEmpty, EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.05F, EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()).setVisible(false));
    }

    private void loadSubCategories(ItemCategory category) {
        this.currentSubCategory = category.getSubCategories().get(0);
        this.subCategoriesList.reset();
        this.subCategoriesList.setDisplayText(this.currentSubCategory.localizedName());
        for (ItemSubCategory subCategory : category.getSubCategories())
            this.subCategoriesList.addElement(new OxygenDropDownListEntry<ItemSubCategory>(subCategory, subCategory.localizedName()));
    }

    private void filter() {
        if (!this.textField.isDragged())
            this.filterOffers();
    }

    public void filterOffers() {
        List<SalesHistoryEntryClient> entries = this.getSalesHistoryEntriesList();

        this.historyEmptyLabel.setVisible(entries.isEmpty());

        this.entriesPanel.reset();
        for (SalesHistoryEntryClient entry : entries)
            this.entriesPanel.addEntry(new HistoryPanelEntry(
                    entry, 
                    ClientReference.localize("oxygen_trade.gui.history.bought", entry.getBuyerUsername(), entry.getSellerUsername()), 
                    ((SalesHistoryScreen) this.screen).getCurrencyProperties()));

        this.entriesAmountLabel.setDisplayText(String.valueOf(entries.size()) + "/" + String.valueOf(TradeManagerClient.instance().getSalesHistoryContainer().getEntriesAmount()));

        this.entriesPanel.getScroller().reset();
        this.entriesPanel.getScroller().updateRowsAmount(MathUtils.clamp(entries.size(), 9, 900));
    }

    private List<SalesHistoryEntryClient> getSalesHistoryEntriesList() {
        return TradeManagerClient.instance().getSalesHistoryContainer().getEntries()
                .stream()
                .filter((entry)->(this.filterByCategory(entry)))
                .filter((entry)->(this.filterByPriceRange(entry)))
                .filter((entry)->(this.filterByName(entry)))
                .filter((entry)->(this.filterByBuyerUsername(entry)))
                .filter((entry)->(this.filterBySellerUsername(entry)))
                .filter((entry)->(this.filterByRarity(entry)))
                .sorted(this.currentSorter.comparator)
                .collect(Collectors.toList());
    }

    private boolean filterByCategory(SalesHistoryEntryClient entry) {
        return this.currentCategory.isValid(this.currentSubCategory, entry.getOfferedStack().getCachedItemStack().getItem().getRegistryName());
    }

    private boolean filterByPriceRange(SalesHistoryEntryClient entry) {
        return entry.getPrice() >= this.currentMinPrice && entry.getPrice() <= this.currentMaxPrice;
    }

    private boolean filterByName(SalesHistoryEntryClient entry) {
        return this.textField.getTypedText().isEmpty() || entry.getOfferedStack().getCachedItemStack().getDisplayName().contains(this.textField.getTypedText());
    }

    private boolean filterByBuyerUsername(SalesHistoryEntryClient entry) {
        return this.buyerUsernameField.getTypedText().isEmpty() || entry.getBuyerUsername().contains(this.buyerUsernameField.getTypedText());
    }

    private boolean filterBySellerUsername(SalesHistoryEntryClient entry) {
        return this.sellerUsernameField.getTypedText().isEmpty() || entry.getSellerUsername().contains(this.sellerUsernameField.getTypedText());
    }

    private boolean filterByRarity(SalesHistoryEntryClient entry) {
        return this.currentRarity == - 1 || entry.getOfferedStack().getCachedItemStack().getRarity().ordinal() == this.currentRarity;
    }

    private void resetFilters() {
        this.currentCategory = ItemCategoriesPresetClient.COMMON_CATEGORY;
        this.categoriesList.setDisplayText(this.currentCategory.localizedName());
        this.loadSubCategories(this.currentCategory);
        this.currentSorter = EnumHistorySorter.PURCHASE_PRICE;
        this.sortersList.setDisplayText(this.currentSorter.localizedName());
        this.currentRarity = - 1;
        this.rarityList.setDisplayText(ClientReference.localize("oxygen_trade.rarity.any"));
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
            if (element == this.filterButton)
                this.filterOffers();
            else if (element == this.resetFiltersButton)
                this.resetFilters();
        }
    }

    public void setFilterButtonState(boolean flag) {
        this.filterButton.setEnabled(flag);
        if (flag) {
            this.entriesAmountLabel.enableFull();
            this.entriesAmountLabel.setDisplayText("0/" + String.valueOf(TradeManagerClient.instance().getSalesHistoryContainer().getEntriesAmount()));
        }
    }

    public void salesHistorySynchronized() {
        this.setFilterButtonState(true);
    }
}
