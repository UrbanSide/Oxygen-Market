package austeretony.oxygen_market.client.gui.market.my_deals.callback;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.api.PrivilegesClient;
import austeretony.oxygen_core.client.gui.base.Keys;
import austeretony.oxygen_core.client.gui.base.Texts;
import austeretony.oxygen_core.client.gui.base.Textures;
import austeretony.oxygen_core.client.gui.base.background.Background;
import austeretony.oxygen_core.client.gui.base.block.Texture;
import austeretony.oxygen_core.client.gui.base.button.ImageButton;
import austeretony.oxygen_core.client.gui.base.core.Callback;
import austeretony.oxygen_core.client.gui.base.special.*;
import austeretony.oxygen_core.client.gui.base.text.NumberField;
import austeretony.oxygen_core.client.gui.base.text.TextLabel;
import austeretony.oxygen_core.client.player.inventory.InventoryHelperClient;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_market.client.MarketManagerClient;
import austeretony.oxygen_market.client.gui.market.MarketScreen;
import austeretony.oxygen_market.common.config.MarketConfig;
import austeretony.oxygen_market.common.main.MarketPrivileges;

public class CreateDealCallback extends Callback {

    public static final Texture CROSS_ICONS_TEXTURE = Texture.builder()
            .texture(Textures.CROSS_ICONS)
            .size(5, 5)
            .imageSize(5 * 3, 5)
            .build();

    private ItemStackSelectionWidget itemSelectionWidget;
    private NumberField unitPriceField, totalPriceField;
    private QuantitySelectionWidget dealsQuantityWidget;
    private CurrencyValue dealPlaceFeeValue, saleFeeValue, incomeValue;

    private KeyButton confirmButton;

    public CreateDealCallback() {
        super(120, 148);
    }

    @Override
    public void init() {
        addWidget(new Background.UnderlinedTitleBottom(this));
        addWidget(new TextLabel(4, 12, Texts.title("oxygen_market.gui.market.my_deals.callback.create_deal")));

        addWidget(new TextLabel(6, 23, Texts.additionalDark("oxygen_market.gui.market.my_deals.label.item")));
        int maxQuantity = PrivilegesClient.getInt(MarketPrivileges.DEAL_MAX_STACK_SIZE.getId(),
                MarketConfig.DEAL_MAX_STACK_SIZE.asInt());
        addWidget(itemSelectionWidget = new ItemStackSelectionWidget(6, 26, maxQuantity)
                .setItemStackSelectionListener((stackWrapper, quantity) -> itemSelected()));

        long maxPrice = getMaxPrice();
        addWidget(new TextLabel(6, 54, Texts.additionalDark("oxygen_market.gui.market.my_deals.label.unit_price")));
        addWidget(unitPriceField = new NumberField(6, 56, 45, 1L, maxPrice, true, 2)
                .setKeyPressListener((keyCode, keyChar) -> updateFeeIncomeStats(ChangedValue.UNIT_PRICE)));
        addWidget(new ImageButton(6 + 45 + 1, 57, 6, 6, CROSS_ICONS_TEXTURE)
                .setMouseClickListener((x, y, mouseButton) -> {
                    unitPriceField.reset();
                    updateFeeIncomeStats(ChangedValue.UNIT_PRICE);
                }));
        addWidget(new TextLabel(6, 74, Texts.additionalDark("oxygen_market.gui.market.my_deals.label.total_price")));
        addWidget(totalPriceField = new NumberField(6, 76, 45, 1L, maxPrice)
                .setKeyPressListener((keyCode, keyChar) -> updateFeeIncomeStats(ChangedValue.TOTAL_PRICE)));

        addWidget(new TextLabel(6, 94, Texts.additionalDark("oxygen_market.gui.market.my_deals.label.deals_quantity")));
        addWidget(dealsQuantityWidget = new QuantitySelectionWidget(6, 95, true, 70, 1)
                .setQuantityChangeListener(quantity -> updateFeeIncomeStats(ChangedValue.DEALS_QUANTITY))
                .setEnabled(false));

        addWidget(new TextLabel(6, 94 + 18, Texts.additionalDark("oxygen_market.gui.market.my_deals.label.deal_place_fee")));
        addWidget(dealPlaceFeeValue = new CurrencyValue(6 + 100, 87 + 18)
                .setCurrency(OxygenMain.CURRENCY_COINS, 0L));
        addWidget(new TextLabel(6, 104 + 18, Texts.additionalDark("oxygen_market.gui.market.my_deals.label.sale_fee")));
        addWidget(saleFeeValue = new CurrencyValue(6 + 100, 97 + 18)
                .setCurrency(OxygenMain.CURRENCY_COINS, 0L));
        addWidget(new TextLabel(6, 114 + 18, Texts.additionalDark("oxygen_market.gui.market.my_deals.label.sale_income")));
        addWidget(incomeValue = new CurrencyValue(6 + 100, 107 + 18)
                .setCurrency(OxygenMain.CURRENCY_COINS, 0L));

        int buttonPosSegment = (int) (getWidth() / 2F);
        addWidget(confirmButton = new KeyButton(0, getHeight() - 10, Keys.CONFIRM_KEY, "oxygen_core.gui.button.confirm")
                .setPressListener(this::confirm)
                .setEnabled(false));
        confirmButton.setX(getX() + (int) ((buttonPosSegment - confirmButton.getText().getWidth()) / 2F));
        KeyButton cancelButton;
        addWidget(cancelButton = new KeyButton(0, getHeight() - 10, Keys.CANCEL_KEY, "oxygen_core.gui.button.cancel")
                .setPressListener(this::close));
        cancelButton.setX(getX() + buttonPosSegment + (int) ((buttonPosSegment - cancelButton.getText().getWidth()) / 2F));
    }

    private long getMaxPrice() {
        return PrivilegesClient.getLong(MarketPrivileges.PRICE_MAX_VALUE.getId(), MarketConfig.PRICE_MAX_VALUE.asLong());
    }

    private void itemSelected() {
        updateConfirmButtonState();
        updateFeeIncomeStats(ChangedValue.ITEM_QUANTITY);
    }

    private void updateConfirmButtonState() {
        boolean enabled = true;
        if (itemSelectionWidget.getStackWrapper() == null) {
            enabled = false;
        }
        long totalPrice = totalPriceField.getTypedNumberAsLong();
        if (totalPrice == 0L || totalPrice > getMaxPrice()) {
            enabled = false;
        }
        if (dealPlaceFeeValue.getValue() > getPlayerBalance()) {
            enabled = false;
        }
        confirmButton.setEnabled(enabled);
    }

    private long getPlayerBalance() {
        return OxygenClient.getWatcherValue(OxygenMain.CURRENCY_COINS, 0L);
    }

    private void updateFeeIncomeStats(ChangedValue changedValue) {
        float placementFeePercent = PrivilegesClient.getFloat(MarketPrivileges.DEAL_PLACEMENT_FEE_PERCENT.getId(),
                MarketConfig.DEAL_PLACEMENT_FEE_PERCENT.asFloat());
        float saleFeePercent = PrivilegesClient.getFloat(MarketPrivileges.DEAL_SALE_FEE_PERCENT.getId(),
                MarketConfig.DEAL_SALE_FEE_PERCENT.asFloat());

        int inputQuantity = itemSelectionWidget.getQuantity();
        double inputUnitPrice = unitPriceField.getTypedNumberAsDouble();
        long inputTotalPrice = totalPriceField.getTypedNumberAsLong();

        long maxPrice = getMaxPrice();
        long totalPrice = totalPriceField.getTypedNumberAsLong();
        if (changedValue == ChangedValue.ITEM_QUANTITY) {
            if (inputUnitPrice <= 0.0) return;
            totalPrice = (long) (inputQuantity * inputUnitPrice);
            if (totalPrice > maxPrice) return;

            totalPriceField.setText(String.valueOf(totalPrice));
            updateDealsQuantitySelectionWidgetState();
        } else if (changedValue == ChangedValue.UNIT_PRICE) {
            if (inputQuantity <= 0) return;
            totalPrice = (long) (inputQuantity * inputUnitPrice);
            if (totalPrice > maxPrice) return;

            totalPriceField.setText(String.valueOf(totalPrice));
            updateDealsQuantitySelectionWidgetState();
        } else if (changedValue == ChangedValue.TOTAL_PRICE) {
            if (inputQuantity <= 0) return;
            totalPrice = inputTotalPrice;
            inputUnitPrice = (double) inputTotalPrice / inputQuantity;

            unitPriceField.setText(MarketScreen.DECIMAL_FORMAT.format(inputUnitPrice));
            updateDealsQuantitySelectionWidgetState();
        }

        long placementFee = (long) (totalPrice * MathUtils.clamp(placementFeePercent, 0F, 1F));
        if (changedValue == ChangedValue.DEALS_QUANTITY) {
            placementFee *= dealsQuantityWidget.getQuantity();
        }
        dealPlaceFeeValue.setValue(placementFee);
        if (placementFee > getPlayerBalance()) {
            dealPlaceFeeValue.setState(SpecialState.INACTIVE);
        } else {
            dealPlaceFeeValue.setState(SpecialState.NORMAL);
        }

        if (changedValue == ChangedValue.DEALS_QUANTITY) {
            totalPrice *= dealsQuantityWidget.getQuantity();
        }
        saleFeeValue.setValue((long) (totalPrice * MathUtils.clamp(saleFeePercent, 0F, 1F)));
        incomeValue.setValue(totalPrice - saleFeeValue.getValue());

        updateConfirmButtonState();
    }

    private void updateDealsQuantitySelectionWidgetState() {
        dealsQuantityWidget.setEnabled(false);
        if (itemSelectionWidget.isItemSelected()
                && unitPriceField.getTypedNumberAsDouble() > 0.0) {
            dealsQuantityWidget.setMaxQuantity(getMaxSelectableDealsQuantity());
            dealsQuantityWidget.setEnabled(true);
        }
    }

    private int getMaxSelectableDealsQuantity() {
        if (!itemSelectionWidget.isItemSelected()) {
            return 1;
        }

        int selectedQuantity = itemSelectionWidget.getQuantity();
        int playerStock = InventoryHelperClient.getItemQuantity(itemSelectionWidget.getStackWrapper());

        int maxDeals = PrivilegesClient.getInt(MarketPrivileges.MAX_DEALS_PER_PLAYER.getId(),
                MarketConfig.MAX_DEALS_PER_PLAYER.asInt());
        int existingDeals = MarketManagerClient.instance().getClientPlayerDeals().size();

        return Math.min(playerStock / selectedQuantity, maxDeals - existingDeals);
    }

    private void confirm() {
        int itemQuantity = itemSelectionWidget.getQuantity();
        MarketManagerClient.instance().createDeal(dealsQuantityWidget.getQuantity(), itemSelectionWidget.getStackWrapper(),
                itemQuantity, totalPriceField.getTypedNumberAsLong());

        int stock = ((MarketScreen) screen).getPlayerItemStock(itemSelectionWidget.getStackWrapper());
        stock -= itemQuantity;
        if (stock < itemQuantity) {
            itemSelectionWidget.setQuantity(stock);
            updateFeeIncomeStats(ChangedValue.ITEM_QUANTITY);
        }

        int maxDeals = PrivilegesClient.getInt(MarketPrivileges.MAX_DEALS_PER_PLAYER.getId(), MarketConfig.MAX_DEALS_PER_PLAYER.asInt());
        if (stock <= 0 || MarketManagerClient.instance().getClientPlayerDeals().size() >= maxDeals) {
            close();
        }
    }

    private enum ChangedValue {

        ITEM_QUANTITY,
        UNIT_PRICE,
        TOTAL_PRICE,
        DEALS_QUANTITY
    }
}
