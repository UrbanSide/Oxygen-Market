package austeretony.oxygen_mail.client.mail;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.api.PrivilegesClient;
import austeretony.oxygen_core.client.gui.base.Textures;
import austeretony.oxygen_core.client.gui.base.block.Texture;
import austeretony.oxygen_core.client.gui.base.button.ImageButton;
import austeretony.oxygen_core.client.gui.base.common.Rectangle;
import austeretony.oxygen_core.client.gui.base.common.WidgetGroup;
import austeretony.oxygen_core.client.gui.base.core.Callback;
import austeretony.oxygen_core.client.gui.base.core.Section;
import austeretony.oxygen_core.client.gui.base.special.callback.ItemStackSelectionCallback;
import austeretony.oxygen_core.client.gui.base.text.TextLabel;
import austeretony.oxygen_core.client.settings.CoreSettings;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.util.CommonUtils;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.mail.Attachment;
import austeretony.oxygen_mail.common.mail.AttachmentParcel;
import austeretony.oxygen_mail.common.mail.Attachments;
import austeretony.oxygen_mail.common.main.MailPrivileges;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ParcelWidgetSupplier implements SelectionWidgetSupplier {

    public static final int BTN_SIZE = 6;
    public static final Texture PLUS_ICONS_TEXTURE = Texture.builder()
            .texture(Textures.PLUS_ICONS)
            .size(BTN_SIZE, BTN_SIZE)
            .imageSize(BTN_SIZE * 3, BTN_SIZE)
            .build();

    private WidgetGroup widgetGroup;
    private TextLabel postageLabel;

    private ImageButton selectItemButton;
    private final List<SelectedItemStackWidget> selectedItemsWidgetsList = new ArrayList<>();

    @Override
    public WidgetGroup getWidgetGroup() {
        widgetGroup = new WidgetGroup();
        selectItemButton = new ImageButton(5, 5, 6, 6, PLUS_ICONS_TEXTURE)
                .setMouseClickListener((x, y, mouseButton) -> selectItem());
        widgetGroup.addWidget(selectItemButton);
        postageLabel = createPostageWidget(OxygenMain.CURRENCY_COINS, 0, 25, widgetGroup);
        return widgetGroup;
    }

    private void selectItem() {
        int maxQuantity = PrivilegesClient.getInt(MailPrivileges.PARCEL_MAX_STACK_SIZE.getId(),
                MailConfig.PARCEL_MAX_STACK_SIZE.asInt());
        Callback callback = new ItemStackSelectionCallback(
                "oxygen_mail.gui.mail.sending.callback.item_selection",
                "oxygen_mail.gui.mail.sending.callback.item_selection.parcel.message",
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
        if (size == AttachmentParcel.MAX_ITEMS_PER_PARCEL) {
            selectItemButton.setEnabled(false);
            selectItemButton.setVisible(false);
        }
    }

    private void updatePostage() {
        long postagePerEntry = PrivilegesClient.getLong(MailPrivileges.PARCEL_POSTAGE_VALUE.getId(),
                MailConfig.PARCEL_POSTAGE_VALUE.asLong());
        long postageValue = postagePerEntry * selectedItemsWidgetsList.size();

        postageLabel.getText().setColorEnabled(CoreSettings.COLOR_TEXT_BASE_ENABLED.asInt());
        long balance = OxygenClient.getWatcherValue(OxygenMain.CURRENCY_COINS, 0L);
        if (balance < postageValue) {
            postageLabel.getText().setColorEnabled(CoreSettings.COLOR_TEXT_INACTIVE.asInt());
        }

        postageLabel.getText().setText(CommonUtils.formatCurrencyValue(postageValue));
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
        return Attachments.parcel(itemsMap);
    }
}
