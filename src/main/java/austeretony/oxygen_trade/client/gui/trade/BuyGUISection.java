package austeretony.oxygen_trade.client.gui.trade;

import java.util.List;
import java.util.stream.Collectors;

import austeretony.alternateui.screen.button.GUIButton;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenGUIHelper;
import austeretony.oxygen_core.client.api.WatcherHelperClient;
import austeretony.oxygen_core.client.gui.OxygenGUITextures;
import austeretony.oxygen_core.client.gui.elements.CurrencyValueGUIElement;
import austeretony.oxygen_core.client.gui.elements.InventoryLoadGUIElement;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIButton;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIButtonPanel;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIContextMenu;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIDDList;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIDDListElement;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIText;
import austeretony.oxygen_core.client.gui.elements.OxygenGUITextField;
import austeretony.oxygen_core.client.gui.elements.OxygenTexturedGUIButton;
import austeretony.oxygen_core.client.gui.elements.SectionsGUIDDList;
import austeretony.oxygen_core.client.gui.settings.GUISettings;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_core.server.OxygenPlayerData;
import austeretony.oxygen_trade.client.PlayerOfferClient;
import austeretony.oxygen_trade.client.TradeManagerClient;
import austeretony.oxygen_trade.client.categories.ItemCategoriesPresetClient;
import austeretony.oxygen_trade.client.categories.OfferCategoryClient;
import austeretony.oxygen_trade.client.categories.OfferSubCategoryClient;
import austeretony.oxygen_trade.client.gui.trade.buy.BuySectionGUIFiller;
import austeretony.oxygen_trade.client.gui.trade.buy.EnumOffersSorter;
import austeretony.oxygen_trade.client.gui.trade.buy.OfferGUIButton;
import austeretony.oxygen_trade.client.gui.trade.buy.context.PurchaseItemContextAction;
import austeretony.oxygen_trade.client.input.TradeKeyHandler;
import net.minecraft.item.EnumRarity;

public class BuyGUISection extends AbstractGUISection {

    private final TradeMenuGUIScreen screen;

    private OxygenGUIButton searchButton;

    private OxygenGUIText offersAmountLabel, offersEmptyLabel;

    private OxygenTexturedGUIButton resetFiltersButton;

    private OxygenGUIButtonPanel offersPanel;

    private OxygenGUIDDList categoryDDList, subCategoryDDList, sorterDDList, rarityDDList;

    private OxygenGUITextField minPriceField, maxPriceField, searchField;

    private InventoryLoadGUIElement inventoryLoadElement;

    private CurrencyValueGUIElement balanceElement;

    //cache

    private OfferCategoryClient currentCategory = ItemCategoriesPresetClient.COMMON_CATEGORY;

    private OfferSubCategoryClient currentSubCategory;

    private EnumOffersSorter currentOffersSorter = EnumOffersSorter.PURCHASE_PRICE;

    private int currentRarityIndex = - 1;

    public BuyGUISection(TradeMenuGUIScreen screen) {
        super(screen);
        this.screen = screen;
    }

    @Override
    public void init() {                
        this.addElement(new BuySectionGUIFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new OxygenGUIText(4, 5, ClientReference.localize("oxygen_trade.gui.trade.title"), GUISettings.get().getTitleScale(), GUISettings.get().getEnabledTextColor()));

        this.addElement(this.resetFiltersButton = new OxygenTexturedGUIButton(69, 18, 5, 5, OxygenGUITextures.CROSS_ICONS, 5, 5, ClientReference.localize("oxygen_trade.gui.trade.tooltip.resetFilters")));         

        //offers panel
        this.addElement(this.offersPanel = new OxygenGUIButtonPanel(this.screen, 76, 16, this.getWidth() - 85, 16, 1, 9, 9, GUISettings.get().getSubTextScale(), true));

        this.offersPanel.initContextMenu(new OxygenGUIContextMenu(GUISettings.get().getContextMenuWidth(), 9, new PurchaseItemContextAction()));

        this.offersPanel.<OfferGUIButton>setClickListener((previous, clicked, mouseX, mouseY, mouseButton)->{
            if (mouseButton == 0 && !clicked.isOverpriced() && !clicked.isPurchased())
                TradeManagerClient.instance().getOffersManager().purchaseItemSynced(clicked.index);
        });

        //sections switcher
        this.addElement(new SectionsGUIDDList(this.getWidth() - 4, 5, this, this.screen.getSellingSection(), this.screen.getOffersSection(), this.screen.getSalesHistorySection()));

        //price filter
        this.addElement(new OxygenGUIText(6, 75, ClientReference.localize("oxygen_trade.gui.trade.priceRange"), GUISettings.get().getSubTextScale(), GUISettings.get().getEnabledTextColorDark()));
        this.addElement(this.minPriceField = new OxygenGUITextField(6, 82, 30, 8, 10, "", 3, true, - 1));
        this.addElement(new OxygenGUIText(38, 84, "-", GUISettings.get().getSubTextScale(), GUISettings.get().getEnabledTextColor()));
        this.addElement(this.maxPriceField = new OxygenGUITextField(43, 82, 30, 8, 10, "", 3, true, - 1));
        //search field
        this.addElement(new OxygenGUIText(6, 95, ClientReference.localize("oxygen_trade.gui.trade.search"), GUISettings.get().getSubTextScale(), GUISettings.get().getEnabledTextColorDark()));
        this.addElement(this.searchField = new OxygenGUITextField(6, 102, 67, 8, 20, "...", 3, false, - 1));

        this.addElement(new OxygenGUIText(6, 18, ClientReference.localize("oxygen_trade.gui.trade.category"), GUISettings.get().getSubTextScale(), GUISettings.get().getEnabledTextColorDark()));
        this.addElement(new OxygenGUIText(6, 37, ClientReference.localize("oxygen_trade.gui.trade.subCategory"), GUISettings.get().getSubTextScale(), GUISettings.get().getEnabledTextColorDark()));
        this.addElement(new OxygenGUIText(6, 56, ClientReference.localize("oxygen_trade.gui.trade.sortBy"), GUISettings.get().getSubTextScale(), GUISettings.get().getEnabledTextColorDark()));
        this.addElement(new OxygenGUIText(6, 115, ClientReference.localize("oxygen_trade.gui.trade.rarity"), GUISettings.get().getSubTextScale(), GUISettings.get().getEnabledTextColorDark()));

        //filter offers button
        this.addElement(this.searchButton = new OxygenGUIButton(20, this.getHeight() - 14, 40, 10, ClientReference.localize("oxygen_trade.gui.trade.searchButton")).disable());
        this.addElement(this.offersAmountLabel = new OxygenGUIText(0, this.getHeight() - 22, "", GUISettings.get().getSubTextScale() - 0.05F, GUISettings.get().getEnabledTextColor()).disableFull());

        //client data
        this.addElement(this.inventoryLoadElement = new InventoryLoadGUIElement(78, this.getHeight() - 9, EnumGUIAlignment.RIGHT));
        this.inventoryLoadElement.updateLoad();
        this.addElement(this.balanceElement = new CurrencyValueGUIElement(this.getWidth() - 19, this.getHeight() - 10));   
        this.balanceElement.setValue(WatcherHelperClient.getLong(OxygenPlayerData.CURRENCY_COINS_WATCHER_ID));

        //rarity filter
        this.rarityDDList = new OxygenGUIDDList(6, 123, 75, 9, ClientReference.localize("oxygen_trade.rarity.any"));
        this.rarityDDList.addElement(new OxygenGUIDDListElement<Integer>(- 1, ClientReference.localize("oxygen_trade.rarity.any")));
        for (EnumRarity rarity : EnumRarity.values())
            this.rarityDDList.addElement(new OxygenGUIDDListElement<Integer>(rarity.ordinal(), getRarityName(rarity)));
        this.addElement(this.rarityDDList);

        //rarity filter listener
        this.rarityDDList.<OxygenGUIDDListElement<Integer>>setClickListener((element)->{
            this.currentRarityIndex = element.index;
        });

        //base sorters filter
        this.sorterDDList = new OxygenGUIDDList(6, 63, 75, 9, EnumOffersSorter.PURCHASE_PRICE.localizedName());
        for (EnumOffersSorter sorter : EnumOffersSorter.values())
            this.sorterDDList.addElement(new OxygenGUIDDListElement<EnumOffersSorter>(sorter, sorter.localizedName()));
        this.addElement(this.sorterDDList);

        //base sorters listener
        this.sorterDDList.<OxygenGUIDDListElement<EnumOffersSorter>>setClickListener((element)->{
            this.currentOffersSorter = element.index;
        });

        //sub categories filter
        this.subCategoryDDList = new OxygenGUIDDList(6, 44, 75, 9, "");
        this.addElement(this.subCategoryDDList);

        //sub categories listener
        this.subCategoryDDList.<OxygenGUIDDListElement<OfferSubCategoryClient>>setClickListener((element)->{
            this.currentSubCategory = element.index;
        });

        this.loadSubCategories(ItemCategoriesPresetClient.COMMON_CATEGORY);

        //categories filter
        this.categoryDDList = new OxygenGUIDDList(6, 25, 75, 9, ItemCategoriesPresetClient.COMMON_CATEGORY.localizedName());
        for (OfferCategoryClient category : TradeManagerClient.instance().getItemCategoriesPreset().getCategories())
            this.categoryDDList.addElement(new OxygenGUIDDListElement<OfferCategoryClient>(category, category.localizedName()));
        this.addElement(this.categoryDDList);

        //categories listener
        this.categoryDDList.<OxygenGUIDDListElement<OfferCategoryClient>>setClickListener((element)->{
            this.loadSubCategories(this.currentCategory = element.index);
        });

        String offersEmpty = ClientReference.localize("oxygen_trade.gui.trade.noOffersFound");
        this.addElement(this.offersEmptyLabel = new OxygenGUIText(76 + ((this.offersPanel.getButtonWidth() - this.textWidth(offersEmpty, GUISettings.get().getSubTextScale() - 0.05F)) / 2), 
                20, offersEmpty, GUISettings.get().getSubTextScale() - 0.05F, GUISettings.get().getEnabledTextColorDark()).setVisible(false));
    }

    private void loadSubCategories(OfferCategoryClient category) {
        this.currentSubCategory = category.getSubCategories().get(0);
        this.subCategoryDDList.reset();
        this.subCategoryDDList.setDisplayText(this.currentSubCategory.localizedName());
        for (OfferSubCategoryClient subCategory : category.getSubCategories())
            this.subCategoryDDList.addElement(new OxygenGUIDDListElement<OfferSubCategoryClient>(subCategory, subCategory.localizedName()));
    }

    private static String getRarityName(EnumRarity rarity) {
        return rarity.rarityColor + ClientReference.localize(rarity.getName());
    }

    public void filterOffers() {
        List<PlayerOfferClient> offers = this.getOffersAndApplyFilters();

        this.offersEmptyLabel.setVisible(offers.isEmpty());

        this.offersPanel.reset();
        for (PlayerOfferClient offer : offers)
            this.offersPanel.addButton(new OfferGUIButton(offer, this.screen.getEqualStackAmount(offer.getOfferedStack()), offer.getPrice() > this.balanceElement.getValue()));

        this.offersPanel.getScroller().resetPosition();
        this.offersPanel.getScroller().getSlider().reset();

        this.offersPanel.getScroller().updateRowsAmount(MathUtils.clamp(offers.size(), 9, 3000));

        this.offersAmountLabel.setDisplayText(String.valueOf(offers.size()) + "/" + String.valueOf(TradeManagerClient.instance().getOffersContainer().getOffersAmount()));
        this.offersAmountLabel.setX((80 - this.textWidth(this.offersAmountLabel.getDisplayText(), this.offersAmountLabel.getTextScale())) / 2);
    }

    private List<PlayerOfferClient> getOffersAndApplyFilters() {
        return TradeManagerClient.instance().getOffersContainer().getOffers()
                .stream()
                .filter((offer)->(this.filterCategory(offer)))
                .filter((offer)->(this.filterPrice(offer)))
                .filter((offer)->(this.filterName(offer)))
                .filter((offer)->(this.filterRarity(offer)))
                .sorted((o1, o2)->(o1.getOfferedStack().itemId - o2.getOfferedStack().itemId))
                .sorted(this.currentOffersSorter.comparator)
                .collect(Collectors.toList());
    }

    private boolean filterCategory(PlayerOfferClient offer) {
        return this.currentCategory.isValid(this.currentSubCategory, offer.getOfferedStack().getCachedItemStack().getItem().getRegistryName());
    }

    private boolean filterPrice(PlayerOfferClient offer) {
        long 
        minPrice = this.minPriceField.getTypedNumber(),
        maxPrice = this.maxPriceField.getTypedNumber();
        if (maxPrice != 0L && minPrice <= maxPrice) {
            return offer.getPrice() >= minPrice && offer.getPrice() <= maxPrice;
        }
        return true;
    }

    private boolean filterName(PlayerOfferClient offer) {
        return this.searchField.getTypedText().isEmpty() || offer.getOfferedStack().getCachedItemStack().getDisplayName().contains(this.searchField.getTypedText());
    }

    private boolean filterRarity(PlayerOfferClient offer) {
        return this.currentRarityIndex == - 1 || offer.getOfferedStack().getCachedItemStack().getRarity().ordinal() == this.currentRarityIndex;
    }

    public void setSearchButtonState(boolean flag) {
        this.searchButton.setEnabled(flag);
        if (flag) {
            this.offersAmountLabel.enableFull();
            this.offersAmountLabel.setDisplayText("0/" + String.valueOf(TradeManagerClient.instance().getOffersContainer().getOffersAmount()));
            this.offersAmountLabel.setX((80 - this.textWidth(this.offersAmountLabel.getDisplayText(), this.offersAmountLabel.getTextScale())) / 2);
        }
    }

    private void resetFilters() {
        this.currentCategory = ItemCategoriesPresetClient.COMMON_CATEGORY;
        this.categoryDDList.setDisplayText(ItemCategoriesPresetClient.COMMON_CATEGORY.localizedName());
        this.loadSubCategories(this.currentCategory);
        this.currentOffersSorter = EnumOffersSorter.PURCHASE_PRICE;
        this.sorterDDList.setDisplayText(EnumOffersSorter.PURCHASE_PRICE.localizedName());
        this.currentRarityIndex = - 1;
        this.rarityDDList.setDisplayText(ClientReference.localize("oxygen_trade.rarity.any"));
        this.minPriceField.reset();
        this.maxPriceField.reset();
        this.searchField.reset();
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {   
        if (!this.searchField.isDragged() 
                && !this.minPriceField.isDragged() 
                && !this.maxPriceField.isDragged() 
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
        if (mouseButton == 0) {
            if (element == this.searchButton)
                this.filterOffers();
            else if (element == this.resetFiltersButton)
                this.resetFilters();
        }
    }

    public void itemPurchased(PlayerOfferClient offer, long balance) {
        this.balanceElement.setValue(balance);
        for (GUIButton b : this.offersPanel.buttonsBuffer) {
            OfferGUIButton button = (OfferGUIButton) b;
            if (button.price > balance)
                button.setOverpriced();
            if (button.index == offer.getId())
                button.setPurchased();
        }
        this.offersAmountLabel.setDisplayText(String.valueOf(this.offersPanel.buttonsBuffer.size() - 1) + "/" + String.valueOf(TradeManagerClient.instance().getOffersContainer().getOffersAmount()));
    }

    public void offerCreated(PlayerOfferClient offer, long balance) {
        this.balanceElement.setValue(balance);
        this.inventoryLoadElement.setLoad(this.screen.getSellingSection().getInventoryLoadElement().getLoad());
        for (GUIButton b : this.offersPanel.buttonsBuffer) {
            OfferGUIButton button = (OfferGUIButton) b;
            if (button.price > balance)
                button.setOverpriced();
        }
        this.offersAmountLabel.setDisplayText(String.valueOf(this.offersPanel.buttonsBuffer.size()) + "/" + String.valueOf(TradeManagerClient.instance().getOffersContainer().getOffersAmount()));
    }

    public void offerCanceled(PlayerOfferClient offer, long balance) {
        if (!this.offersPanel.buttonsBuffer.isEmpty())
            this.filterOffers();
    }

    public InventoryLoadGUIElement getInventoryLoadElement() {
        return this.inventoryLoadElement;
    }

    public CurrencyValueGUIElement getBalanceElement() {
        return this.balanceElement;
    }
}
