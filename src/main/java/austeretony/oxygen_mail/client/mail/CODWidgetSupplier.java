package austeretony.oxygen_mail.client.mail;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.api.PrivilegesClient;
import austeretony.oxygen_core.client.gui.base.Texts;
import austeretony.oxygen_core.client.gui.base.block.Texture;
import austeretony.oxygen_core.client.gui.base.button.ImageButton;
import austeretony.oxygen_core.client.gui.base.common.ImageLabel;
import austeretony.oxygen_core.client.gui.base.common.WidgetGroup;
import austeretony.oxygen_core.client.gui.base.core.Callback;
import austeretony.oxygen_core.client.gui.base.core.Section;
import austeretony.oxygen_core.client.gui.base.special.callback.ItemStackSelectionCallback;
import austeretony.oxygen_core.client.gui.base.text.NumberField;
import austeretony.oxygen_core.client.gui.base.text.TextLabel;
import austeretony.oxygen_core.client.preset.CurrencyProperties;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.util.CommonUtils;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.mail.Attachment;
import austeretony.oxygen_mail.common.mail.AttachmentCOD;
import austeretony.oxygen_mail.common.mail.Attachments;
import austeretony.oxygen_mail.common.main.MailPrivileges;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CODWidgetSupplier implements SelectionWidgetSupplier {

    private WidgetGroup widgetGroup;
    private NumberField numberField;
    private TextLabel postageLabel;

    private ImageButton selectItemButton;
    private final List<SelectedItemStackWidget> selectedItemsWidgetsList = new ArrayList<>();

    @Override
    public WidgetGroup getWidgetGroup() {
        widgetGroup = new WidgetGroup();

        selectItemButton = new ImageButton(5, 5, 6, 6, ParcelWidgetSupplier.PLUS_ICONS_TEXTURE)
                .setMouseClickListener((x, y, mouseButton) -> selectItem());
        widgetGroup.addWidget(selectItemButton);

        widgetGroup.addWidget(new TextLabel(0, 26, Texts.additionalDark("oxygen_mail.gui.mail.label.price")));
        CurrencyProperties properties = OxygenClient.getCurrencyProperties(OxygenMain.CURRENCY_COINS);
        Texture texture = Texture.builder()
                .imageSize(properties.getIconWidth(), properties.getIconHeight())
                .size(properties.getIconWidth(), properties.getIconHeight())
                .texture(properties.getIconTexture())
                .build();
        widgetGroup.addWidget(new ImageLabel(0, 28 + properties.getIconYOffset(), texture));

        long maxValue = PrivilegesClient.getLong(MailPrivileges.COD_MAX_VALUE.getId(), MailConfig.COD_MAX_VALUE.asLong());
        widgetGroup.addWidget(numberField = new NumberField(properties.getIconWidth() + 2 * properties.getIconXOffset() + 2, 28, 30, 0L, maxValue)
                .setKeyPressListener((keyCode, keyChar) -> updatePostage()));
        numberField.setText(String.valueOf(0));

        postageLabel = createPostageWidget(OxygenMain.CURRENCY_COINS, 0, 46, widgetGroup);

        return widgetGroup;
    }

    private void selectItem() {
        int maxQuantity = PrivilegesClient.getInt(MailPrivileges.PARCEL_MAX_STACK_SIZE.getId(),
                MailConfig.PARCEL_MAX_STACK_SIZE.asInt());
        Callback callback = new ItemStackSelectionCallback(
                "oxygen_mail.gui.mail.sending.callback.item_selection",
                "oxygen_mail.gui.mail.sending.callback.item_selection.cod.message",
                maxQuantity,
                pair -> itemSelected(pair.getKey(), pair.getValue()));
        Section.tryOpenCallback(callback);
    }

    private void itemSelected(ItemStackWrapper stackWrapper, int amount) {
        int size = selectedItemsWidgetsList.size();
        SelectedItemStackWidget widget = new SelectedItemStackWidget(this::updateByWidgets, size * 20, 0, stackWrapper, amount);
        widgetGroup.addWidget(widget);
        selectedItemsWidgetsList.add(widget);

        updateSelectItemButtonState();
        updatePostage();
    }

    private void updateSelectItemButtonState() {
        int size = selectedItemsWidgetsList.size();
        selectItemButton.setPosition(size * 20 + 5, 5);
        selectItemButton.setEnabled(true);
        selectItemButton.setVisible(true);
        if (size == AttachmentCOD.MAX_ITEMS_PER_PARCEL) {
            selectItemButton.setEnabled(false);
            selectItemButton.setVisible(false);
        }
    }

    private void updatePostage() {
        long postagePerEntry = PrivilegesClient.getLong(MailPrivileges.PARCEL_POSTAGE_VALUE.getId(),
                MailConfig.PARCEL_POSTAGE_VALUE.asLong());
        long postageValue = postagePerEntry * selectedItemsWidgetsList.size();

        String postageValueStr = CommonUtils.formatCurrencyValue(postageValue);

        long balance = OxygenClient.getWatcherValue(OxygenMain.CURRENCY_COINS, 0L);
        if (balance < postageValue) {
            postageValueStr = TextFormatting.RED + postageValueStr + TextFormatting.RESET;
        }

        long codPrice = numberField.getTypedNumberAsLong();
        if (codPrice != 0) {
            float feePercent = PrivilegesClient.getFloat(MailPrivileges.COD_PRICE_FEE_PERCENT.getId(),
                    MailConfig.COD_PRICE_FEE_PERCENT.asFloat());
            long codFee = (long) (codPrice * feePercent);
            postageValueStr = postageValueStr + " + " + CommonUtils.formatCurrencyValue(codFee);
        }

        postageLabel.getText().setText(postageValueStr);
    }

    void updateByWidgets() {
        for (SelectedItemStackWidget widget : selectedItemsWidgetsList) {
            widgetGroup.getWidgets().remove(widget);
        }
        selectedItemsWidgetsList.removeIf(SelectedItemStackWidget::isRemoved);

        for (int i = 0; i < selectedItemsWidgetsList.size(); i++) {
            SelectedItemStackWidget widget = selectedItemsWidgetsList.get(i);
            widget.setPosition(i * 20, 0);
            widgetGroup.addWidget(widget);
        }

        updateSelectItemButtonState();
        updatePostage();
    }

    @Override
    public Attachment createAttachment() {
        Map<ItemStackWrapper, Integer> itemsMap = new LinkedHashMap<>();
        for (SelectedItemStackWidget widget : selectedItemsWidgetsList) {
            itemsMap.put(widget.getStackWrapper(), widget.getAmount());
        }
        return Attachments.cod(OxygenMain.CURRENCY_COINS, numberField.getTypedNumberAsLong(), itemsMap);
    }
}
