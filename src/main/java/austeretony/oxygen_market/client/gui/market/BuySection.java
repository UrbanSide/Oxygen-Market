package austeretony.oxygen_market.client.gui.market;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.gui.base.Fills;
import austeretony.oxygen_core.client.gui.base.Texts;
import austeretony.oxygen_core.client.gui.base.background.Background;
import austeretony.oxygen_core.client.gui.base.block.Text;
import austeretony.oxygen_core.client.gui.base.button.ImageButton;
import austeretony.oxygen_core.client.gui.base.button.VerticalSlider;
import austeretony.oxygen_core.client.gui.base.common.ListEntry;
import austeretony.oxygen_core.client.gui.base.core.Section;
import austeretony.oxygen_core.client.gui.base.core.Widget;
import austeretony.oxygen_core.client.gui.base.list.DropDownList;
import austeretony.oxygen_core.client.gui.base.list.ScrollableList;
import austeretony.oxygen_core.client.gui.base.special.CurrencyValue;
import austeretony.oxygen_core.client.gui.base.special.InventoryLoad;
import austeretony.oxygen_core.client.gui.base.special.SectionSwitcher;
import austeretony.oxygen_core.client.gui.base.text.NumberField;
import austeretony.oxygen_core.client.gui.base.text.TextField;
import austeretony.oxygen_core.client.gui.base.text.TextLabel;
import austeretony.oxygen_core.client.gui.util.OxygenGUIUtils;
import austeretony.oxygen_core.client.preset.ItemCategoriesPresetClient;
import austeretony.oxygen_core.client.preset.ItemsSubCategory;
import austeretony.oxygen_core.client.settings.CoreSettings;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_market.client.gui.DealsSorter;
import austeretony.oxygen_market.client.gui.market.buy.CombinedDealsEntry;
import austeretony.oxygen_market.client.gui.market.buy.DealProfitability;
import austeretony.oxygen_market.client.gui.market.buy.MarketDealListEntry;
import austeretony.oxygen_market.client.gui.market.buy.context.MarketBuySelectItemQuantityContextAction;
import austeretony.oxygen_market.client.market.Profitability;
import austeretony.oxygen_market.client.settings.MarketSettings;
import austeretony.oxygen_market.common.market.Deal;
import net.minecraft.item.EnumRarity;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BuySection extends Section {

    private final MarketScreen screen;

    private TextLabel dealsAmountLabel, noDealsLabel;
    private ScrollableList<CombinedDealsEntry> dealsList;
    private TextField textFiled;
    private NumberField minPriceField, maxPriceField;
    private ImageButton resetFiltersButton, applyLatestFiltersButton;
    private DropDownList<String> itemCategoryDDList, itemSubCategoryDDList;
    private DropDownList<Integer> sorterDDList, rarityDDList, profitabilityDDList;
    private InventoryLoad inventoryLoad;
    private CurrencyValue balanceValue;

    private String currItemCategory = ItemCategoriesPresetClient.COMMON_CATEGORY_NAME;
    private String currItemSubCategory = ItemCategoriesPresetClient.COMMON_SUB_CATEGORY_NAME;
    private DealsSorter currDealsSorter = DealsSorter.PURCHASE_PRICE_MIN_TO_MAX;
    private long currMinPrice = 0L, currMaxPrice = Long.MAX_VALUE;
    private String currTextSearch = "";
    private int currRarityOrdinal = -1, currProfitabilityOrdinal = -1;

    private static String cachedItemCategory = ItemCategoriesPresetClient.COMMON_CATEGORY_NAME;
    private static String cachedItemSubCategory = ItemCategoriesPresetClient.COMMON_SUB_CATEGORY_NAME;
    private static DealsSorter cachedDealsSorter = DealsSorter.PURCHASE_PRICE_MIN_TO_MAX;
    private long cachedMinPrice = 0L, cachedMaxPrice = Long.MAX_VALUE;
    private String cachedTextSearch = "";
    private static int cachedRarityOrdinal = -1, cachedProfitabilityOrdinal = -1;

    public BuySection(@Nonnull MarketScreen screen) {
        super(screen, localize("oxygen_market.gui.market.section.buy"), true);
        this.screen = screen;
    }

    @Override
    public void init() {
        addWidget(new Background.UnderlinedTitleBottom(this));
        addWidget(new TextLabel(4, 12, Texts.title("oxygen_market.gui.market.title")));
        addWidget(new SectionSwitcher(this));

        addWidget(applyLatestFiltersButton = new ImageButton(61, 18, MarketScreen.BTN_SIZE, MarketScreen.BTN_SIZE,
                MarketScreen.CLOCK_ICONS_TEXTURE, localize("oxygen_shop.gui.shop.image_button.apply_latest_filters"))
                .setMouseClickListener((mouseX, mouseY, button) -> applyLatestFilters()));
        addWidget(resetFiltersButton = new ImageButton(68, 18, MarketScreen.BTN_SIZE, MarketScreen.BTN_SIZE,
                MarketScreen.CROSS_ICONS_TEXTURE, localize("oxygen_shop.gui.shop.image_button.reset_filters"))
                .setMouseClickListener((mouseX, mouseY, button) -> resetFilters()));

        //categories filter

        addWidget(new TextLabel(6, 24, Texts.additionalDark("oxygen_core.gui.label.item_category")));
        String commonItemsCategory = ItemCategoriesPresetClient.COMMON_CATEGORY_NAME;
        addWidget(itemCategoryDDList = new DropDownList<>(6, 25, 67, commonItemsCategory)
                .<String>setEntryMouseClickListener((previous, current, x, y, button) -> {
                    cachedItemCategory = currItemCategory = current.getEntry();
                    updateItemSubCategoriesFilter(current.getEntry());
                    filterDeals();
                }));
        itemCategoryDDList.addElement(ListEntry.of(commonItemsCategory, commonItemsCategory));
        for (String categoryName : screen.getItemCategoriesPreset().getSortedCategories()) {
            itemCategoryDDList.addElement(ListEntry.of(localize(categoryName), categoryName));
        }

        addWidget(itemSubCategoryDDList = new DropDownList<>(6, 36, 67, ItemCategoriesPresetClient.COMMON_SUB_CATEGORY_NAME)
                .<String>setEntryMouseClickListener((previous, current, x, y, button) -> {
                    cachedItemSubCategory = currItemSubCategory = current.getEntry();
                    filterDeals();
                }));
        updateItemSubCategoriesFilter(commonItemsCategory);

        //sorters filter

        addWidget(new TextLabel(6, 54, Texts.additionalDark("oxygen_market.gui.market.buy.sort_by")));
        addWidget(sorterDDList = new DropDownList<>(6, 56, 67, DealsSorter.PURCHASE_PRICE_MIN_TO_MAX.getDisplayName())
                .<Integer>setEntryMouseClickListener((previous, current, x, y, button) -> {
                    currDealsSorter = cachedDealsSorter = DealsSorter.values()[current.getEntry()];
                    filterDeals();
                }));

        for (DealsSorter sorter : DealsSorter.values()) {
            sorterDDList.addElement(ListEntry.of(sorter.getDisplayName(), sorter.ordinal()));
        }

        //price filter

        addWidget(new TextLabel(6, 74, Texts.additionalDark("oxygen_market.gui.market.buy.price_range")));
        addWidget(minPriceField = new NumberField(6, 76, 30, 0L, Long.MAX_VALUE)
                .setKeyPressListener((keyCode, keyChar) -> {
                    currMinPrice = cachedMinPrice = minPriceField.getTypedNumberAsLong();
                    filterDeals();
                }));
        addWidget(maxPriceField = new NumberField(43, 76, 30, 0L, Long.MAX_VALUE)
                .setKeyPressListener((keyCode, keyChar) -> {
                    long typedMaxPrice = maxPriceField.getTypedNumberAsLong();
                    if (typedMaxPrice == 0L) {
                        typedMaxPrice = Long.MAX_VALUE;
                    }
                    currMaxPrice = cachedMaxPrice = typedMaxPrice;
                    filterDeals();
                }));

        //text search

        addWidget(new TextLabel(6, 93, Texts.additionalDark("oxygen_market.gui.market.buy.text_search")));
        addWidget(textFiled = new TextField(6, 95, 67, 24)
                .setKeyPressListener((keyCode, keyChar) -> {
                    currTextSearch = cachedTextSearch = textFiled.getTypedText();
                    filterDeals();
                }));

        //rarity filter

        addWidget(new TextLabel(6, 112, Texts.additionalDark("oxygen_market.gui.market.buy.rarity")));
        String anyRarityStr = localize("oxygen_market.gui.market.buy.rarity.any");
        addWidget(rarityDDList = new DropDownList<>(6, 114, 67, anyRarityStr)
                .<Integer>setEntryMouseClickListener((previous, current, x, y, button) -> {
                    currRarityOrdinal = cachedRarityOrdinal = current.getEntry();
                    filterDeals();
                }));

        rarityDDList.addElement(ListEntry.of(anyRarityStr, -1));
        for (EnumRarity rarity : EnumRarity.values()) {
            rarityDDList.addElement(ListEntry.of(getRarityName(rarity), rarity.ordinal()));
        }

        //profitability filter

        boolean profitabilityEnabled = MarketSettings.ENABLE_DEALS_PROFITABILITY_DISPLAY.asBoolean();
        addWidget(new TextLabel(6, 132, Texts.additionalDark("oxygen_market.gui.market.buy.profitability"))
                .setVisible(profitabilityEnabled));

        String anyProfitabilityStr = localize("oxygen_market.gui.market.buy.profitability.any");
        addWidget(profitabilityDDList = new DropDownList<>(6, 134, 67, anyProfitabilityStr)
                .<Integer>setEntryMouseClickListener((previous, current, x, y, button) -> {
                    currProfitabilityOrdinal = cachedProfitabilityOrdinal = current.getEntry();
                    profitabilityDDList.getText().setColorEnabled(current.getText().getColorEnabled());
                    profitabilityDDList.getText().setColorMouseOver(current.getText().getColorEnabled());
                    filterDeals();
                }));
        profitabilityDDList.setVisible(profitabilityEnabled);

        profitabilityDDList.addElement(ListEntry.of(anyProfitabilityStr, -1));
        for (Profitability profitability : Profitability.values()) {
            if (profitability == Profitability.NO_DATA) continue;
            ListEntry<Integer> entry = ListEntry.of("", profitability.ordinal());
            Text text = entry.getText();
            text.setText(profitability.getDisplayName());
            text.setColorEnabled(profitability.getColorHex());
            text.setColorMouseOver(profitability.getColorHex());
            profitabilityDDList.addElement(entry);
        }

        //deals list

        addWidget(dealsList = new ScrollableList<>(76, 16, Fills.def(), 9, getWidth() - 85, 16, 1, true)
                .<CombinedDealsEntry>setEntryMouseClickListener((previous, current, x, y, button) -> {
                    MarketDealListEntry entry = (MarketDealListEntry) current;
                    if (!entry.isAvailable()) return;
                    entry.purchase();
                }));
        VerticalSlider slider = new VerticalSlider(6 + getWidth() - 6 * 2 - 3 + 1, 16, 2, 16 * 9 + 8);
        addWidget(slider);
        dealsList.setSlider(slider);
        dealsList.createContextMenu(Collections.singletonList(new MarketBuySelectItemQuantityContextAction()));

        addWidget(dealsAmountLabel = new TextLabel(6, getHeight() - 15, Texts.additionalDark("")));
        addWidget(noDealsLabel = new TextLabel(76, 23, Texts.additionalDark("oxygen_market.gui.market.label.buy.no_deals"))
                .setVisible(false));
        if (!screen.hasAccessToMarket()) {
            addWidget(new TextLabel(76, 23, Texts.additionalDark("oxygen_market.gui.market.label.no_access")));
        }

        addWidget(inventoryLoad = new InventoryLoad(6, getHeight() - 10).updateLoad());
        addWidget(balanceValue = new CurrencyValue(getWidth() - 14, getHeight() - 10)
                .setCurrency(OxygenMain.CURRENCY_COINS, OxygenClient.getWatcherValue(OxygenMain.CURRENCY_COINS, 0L)));
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (!textFiled.isFocused()) {
            OxygenGUIUtils.closeScreenOnKeyPress(getScreen(), keyCode);
        }
        super.keyTyped(typedChar, keyCode);
    }

    private void updateItemSubCategoriesFilter(String categoryName) {
        itemSubCategoryDDList.clear();

        String commonSubCategoryName = ItemCategoriesPresetClient.COMMON_SUB_CATEGORY_NAME;
        itemSubCategoryDDList.getText().setText(commonSubCategoryName);
        itemSubCategoryDDList.addElement(ListEntry.of(commonSubCategoryName, commonSubCategoryName));
        if (categoryName.equals(ItemCategoriesPresetClient.COMMON_CATEGORY_NAME)) return;

        List<ItemsSubCategory> subCategoriesList = screen.getItemCategoriesPreset()
                .getSortedSubCategories(categoryName);
        for (ItemsSubCategory subCategory : subCategoriesList) {
            itemSubCategoryDDList.addElement(ListEntry.of(subCategory.getLocalizedName(), subCategory.getName()));
        }
        cachedItemSubCategory = currItemSubCategory = ItemCategoriesPresetClient.COMMON_SUB_CATEGORY_NAME;
    }

    public static String getRarityName(EnumRarity rarity) {
        return rarity.rarityColor + localize(rarity.rarityName);
    }

    private void applyLatestFilters() {
        currItemCategory = cachedItemCategory;
        itemCategoryDDList.getText().setText(localize(currItemCategory));

        currItemSubCategory = cachedItemSubCategory;
        updateItemSubCategoriesFilter(currItemCategory);

        currDealsSorter = cachedDealsSorter;
        sorterDDList.getText().setText(currDealsSorter.getDisplayName());

        currMinPrice = cachedMinPrice;
        currMaxPrice = cachedMaxPrice;
        minPriceField.setText(currMinPrice > 0L ? String.valueOf(currMinPrice) : "");
        maxPriceField.setText(currMaxPrice < Long.MAX_VALUE ? String.valueOf(currMaxPrice) : "");

        currTextSearch = cachedTextSearch;
        textFiled.setText(currTextSearch);

        currRarityOrdinal = cachedRarityOrdinal;
        if (currRarityOrdinal >= 0) {
            rarityDDList.getText().setText(getRarityName(EnumRarity.values()[currRarityOrdinal]));
        } else {
            rarityDDList.getText().setText(localize("oxygen_market.gui.market.buy.rarity.any"));
        }

        currProfitabilityOrdinal = cachedProfitabilityOrdinal;
        if (currProfitabilityOrdinal >= 0) {
            Profitability profitability = Profitability.values()[currProfitabilityOrdinal];
            profitabilityDDList.getText().setText(profitability.getDisplayName());
            profitabilityDDList.getText().setColorEnabled(profitability.getColorHex());
        } else {
            profitabilityDDList.getText().setText(localize("oxygen_market.gui.market.buy.profitability.any"));
            profitabilityDDList.getText().setColorEnabled(CoreSettings.COLOR_TEXT_BASE_ENABLED.asInt());
        }

        filterDeals();
    }

    private void resetFilters() {
        currItemCategory = ItemCategoriesPresetClient.COMMON_CATEGORY_NAME;
        itemCategoryDDList.getText().setText(localize(currItemCategory));

        currItemSubCategory = ItemCategoriesPresetClient.COMMON_SUB_CATEGORY_NAME;
        updateItemSubCategoriesFilter(currItemCategory);

        currDealsSorter = DealsSorter.PURCHASE_PRICE_MIN_TO_MAX;
        sorterDDList.getText().setText(currDealsSorter.getDisplayName());

        currMinPrice = 0L;
        currMaxPrice = Long.MAX_VALUE;
        minPriceField.setText("");
        maxPriceField.setText("");

        textFiled.setText("");

        currRarityOrdinal = -1;
        rarityDDList.getText().setText(localize("oxygen_market.gui.market.buy.rarity.any"));

        currProfitabilityOrdinal = -1;
        profitabilityDDList.getText().setText(localize("oxygen_market.gui.market.buy.profitability.any"));
        profitabilityDDList.getText().setColorEnabled(CoreSettings.COLOR_TEXT_BASE_ENABLED.asInt());

        filterDeals();
    }

    private boolean canPlayerAfford(Deal deal) {
        return deal.getPrice() <= balanceValue.getValue();
    }

    private void filterDeals() {
        if (!screen.hasAccessToMarket()) return;

        int displayLimit = MarketSettings.MARKET_SCREEN_MAX_DISPLAYED_DEALS.asInt();
        List<CombinedDealsEntry> deals = getDeals();
        dealsList.clear();
        int index = 0;
        for (CombinedDealsEntry entry : deals) {
            if (++index > displayLimit) break;
            dealsList.addElement(new MarketDealListEntry(entry, screen.getPlayerItemStock(entry.getDeal().getStackWrapper()),
                    canPlayerAfford(entry.getDeal()), screen.getDealProfitability(entry.getDeal()), screen.getCurrencyProperties()));
        }

        int dealsTotal = screen.getCombinedDealsList().size();
        dealsAmountLabel.getText().setText(Math.min(deals.size(), displayLimit) + "/" + dealsTotal);
        noDealsLabel.setVisible(screen.hasAccessToMarket() && deals.isEmpty());
    }

    private List<CombinedDealsEntry> getDeals() {
        return screen.getCombinedDealsList()
                .stream()
                .filter(this::isValidCategory)
                .filter(this::isValidPrice)
                .filter(this::isValidItemName)
                .filter(this::isValidRarity)
                .filter(this::isValidProfitability)
                .sorted(Comparator.comparingInt(e -> e.getDeal().getStackWrapper().getItemId()))
                .sorted(currDealsSorter.comparator)
                .collect(Collectors.toList());
    }

    private boolean isValidCategory(CombinedDealsEntry entry) {
        return screen.getItemCategoriesPreset().isValidForCategory(currItemCategory, currItemSubCategory,
                entry.getDeal().getStackWrapper().getItemStackCached());
    }

    private boolean isValidPrice(CombinedDealsEntry entry) {
        return entry.getDeal().getPrice() >= currMinPrice && entry.getDeal().getPrice() <= currMaxPrice;
    }

    private boolean isValidItemName(CombinedDealsEntry entry) {
        return currTextSearch.isEmpty() || entry.getDeal().getStackWrapper().getItemStackCached().getDisplayName().contains(currTextSearch);
    }

    private boolean isValidRarity(CombinedDealsEntry entry) {
        return currRarityOrdinal < 0 || entry.getDeal().getStackWrapper().getItemStackCached().getRarity().ordinal() == currRarityOrdinal;
    }

    private boolean isValidProfitability(CombinedDealsEntry entry) {
        if (currProfitabilityOrdinal < 0) return true;
        DealProfitability dealProfitability = screen.getDealProfitability(entry.getDeal());
        if (currProfitabilityOrdinal == Profitability.OVERPRICE.ordinal()) {
            return dealProfitability.getProfitability() == Profitability.OVERPRICE;
        }
        return dealProfitability.getProfitability().ordinal() >= currProfitabilityOrdinal;
    }

    protected void dataSynchronized() {
        filterDeals();
    }

    protected void dealsCreated(int dealsQuantity, Deal deal, long balance) {
        balanceValue.setValue(balance);
        screen.incrementPlayerStock(deal.getStackWrapper(), -deal.getQuantity() * dealsQuantity);
        inventoryLoad.updateLoad(screen.getInventoryContentMap());
        checkDealsOverpriced(balance);
    }

    private void checkDealsOverpriced(long balance) {
        for (Widget widget : dealsList.getWidgets()) {
            ((MarketDealListEntry) widget).checkIsOverpriced(balance);
        }
    }

    protected void dealCanceled(Set<Long> dealsIds) {
        dealProcessed(dealsIds, MarketDealListEntry.State.FAILED);
    }

    private void dealProcessed(Set<Long> dealsIds, MarketDealListEntry.State state) {
        for (Widget widget : dealsList.getWidgets()) {
            MarketDealListEntry entry = (MarketDealListEntry) widget;
            boolean success = false;
            for (long dealId : dealsIds) {
                if (entry.isDealExist(dealId)) {
                    entry.dealProcessed(dealId, state);
                    success = true;
                }
            }
            if (success) {
                break;
            }
        }
    }

    protected void purchased(Set<Long> dealsIds, long balance) {
        balanceValue.setValue(balance);
        dealProcessed(dealsIds, MarketDealListEntry.State.PURCHASED);
        checkDealsOverpriced(balance);
    }

    protected void purchaseFailed(Set<Long> dealsIds) {
        dealProcessed(dealsIds, MarketDealListEntry.State.FAILED);
    }
}
