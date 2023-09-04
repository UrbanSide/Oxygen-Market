package austeretony.oxygen_mail.client.gui.mail;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.api.PrivilegesClient;
import austeretony.oxygen_core.client.gui.base.Keys;
import austeretony.oxygen_core.client.gui.base.Layer;
import austeretony.oxygen_core.client.gui.base.Texts;
import austeretony.oxygen_core.client.gui.base.background.Background;
import austeretony.oxygen_core.client.gui.base.common.ListEntry;
import austeretony.oxygen_core.client.gui.base.common.WidgetGroup;
import austeretony.oxygen_core.client.gui.base.core.Callback;
import austeretony.oxygen_core.client.gui.base.core.Section;
import austeretony.oxygen_core.client.gui.base.list.DropDownList;
import austeretony.oxygen_core.client.gui.base.special.*;
import austeretony.oxygen_core.client.gui.base.text.TextBoxField;
import austeretony.oxygen_core.client.gui.base.text.TextField;
import austeretony.oxygen_core.client.gui.base.text.TextLabel;
import austeretony.oxygen_core.client.gui.util.OxygenGUIUtils;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.player.shared.PlayerSharedData;
import austeretony.oxygen_mail.client.gui.mail.sending.callback.SendMailCallback;
import austeretony.oxygen_mail.client.mail.SelectionWidgetSupplier;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.mail.AttachmentType;
import austeretony.oxygen_mail.common.mail.MailEntry;
import austeretony.oxygen_mail.common.main.MailPrivileges;

import javax.annotation.Nonnull;

public class SendMailSection extends Section {

    private final MailScreen screen;

    private UsernameField addresseeField;
    private TextField subjectField;
    private TextBoxField messageField;
    private CurrencyValue balanceValue;
    private InventoryLoad inventoryLoad;
    private KeyButton sendMailButton;

    private AttachmentType attachmentType;
    private SelectionWidgetSupplier selectionWidgetSupplier;
    private WidgetGroup attachmentGroup;

    public SendMailSection(@Nonnull MailScreen screen) {
        super(screen);
        name = localize("oxygen_mail.gui.mail.section.send_mail");
        enabled = PrivilegesClient.getBoolean(MailPrivileges.ALLOW_MAIL_SENDING.getId(),
                MailConfig.ENABLE_MAIL_SENDING.asBoolean());
        this.screen = screen;
    }

    @Override
    public void init() {
        addWidget(new Background.UnderlinedTitleBottomButtons(this));
        addWidget(new TextLabel(4, 12, Texts.title("oxygen_mail.gui.mail.title")));
        addWidget(new SectionSwitcher(this));

        addWidget(new TextLabel(6, 22, Texts.additionalDark("oxygen_mail.gui.mail.sending.label.addressee")));
        addWidget(addresseeField = new UsernameField(6, 23, 124)
                .setUsernameSelectionListener((previous, currency, x, y, button) -> updateSendButtonState())
                .setKeyPressListener((keyChar, keyCode) -> updateSendButtonState()));
        addresseeField.ignoreClientPlayer(true);
        addresseeField.setEnabled(false);

        addWidget(new TextLabel(6, 40, Texts.additionalDark("oxygen_mail.gui.mail.sending.label.subject")));
        addWidget(subjectField = new TextField(6, 41, 124, MailEntry.MESSAGE_SUBJECT_MAX_LENGTH)
                .setKeyPressListener((keyCode, keyChar) -> updateSendButtonState()));
        addWidget(messageField = new TextBoxField(6, 50, 124, 102, MailEntry.MESSAGE_MAX_LENGTH));

        addWidget(new TextLabel(6 + 124 + 4, 22,
                Texts.additional("oxygen_mail.gui.mail.sending.label.attachment")));
        AttachmentType firstType = AttachmentType.values()[0];
        DropDownList attachmentTypeList = new DropDownList<>(6 + 124 + 4, 23, 70, firstType.getDisplayName())
                .<Integer>setEntryMouseClickListener((previous, current, x, y, button) -> {
                    if (current == previous) return;
                    setupAttachmentSelectionWidgets(AttachmentType.values()[current.getEntry()]);
                });
        addWidget(attachmentTypeList);

        for (AttachmentType attachmentType : AttachmentType.values()) {
            attachmentTypeList.addElement(ListEntry.of(attachmentType.getDisplayName(),
                    attachmentType.ordinal()));
        }
        setupAttachmentSelectionWidgets(firstType);

        addWidget(inventoryLoad = new InventoryLoad(6, getHeight() - 10).updateLoad());
        addWidget(balanceValue = new CurrencyValue(getWidth() - 14, getHeight() - 10)
                .setCurrency(OxygenMain.CURRENCY_COINS, OxygenClient.getWatcherValue(OxygenMain.CURRENCY_COINS, 0L)));

        String keyButtonText = localize("oxygen_mail.gui.mail.sending.button.send_mail");
        addWidget(sendMailButton = new KeyButton(0, 0, Keys.ACTION_KEY, keyButtonText)
                .setLayer(Layer.FRONT)
                .setPressListener(() -> {
                    if (addresseeField.isFocused() || subjectField.isFocused() || messageField.isFocused()) return;
                    PlayerSharedData sharedData = addresseeField.getSelectedPlayer();
                    if (sharedData == null) return;

                    Callback callback = new SendMailCallback(
                            sharedData.getPlayerUUID(),
                            sharedData.getUsername(),
                            subjectField.getTypedText(),
                            messageField.getTypedText(),
                            attachmentType,
                            selectionWidgetSupplier.createAttachment());
                    openCallback(callback);
                })
                .setEnabled(false));
        OxygenGUIUtils.calculateBottomCenteredOffscreenButtonPosition(sendMailButton, 1, 1);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        OxygenGUIUtils.closeScreenOnKeyPress(getScreen(), keyCode);
        super.keyTyped(typedChar, keyCode);
    }

    private void setupAttachmentSelectionWidgets(AttachmentType type) {
        attachmentType = type;
        if (attachmentGroup != null) {
            getWidgets().remove(attachmentGroup);
        }
        selectionWidgetSupplier = type.getSelectionWidgetSupplier();
        attachmentGroup = selectionWidgetSupplier.getWidgetGroup();
        attachmentGroup.setPosition(6 + 124 + 4, 36);
        addWidget(attachmentGroup);
    }

    private void updateSendButtonState() {
        sendMailButton.setEnabled(addresseeField.getSelectedPlayer() != null && !subjectField.getTypedText().isEmpty());
    }

    public void sharedDataSynchronized() {
        addresseeField.updatePlayers();
        addresseeField.setEnabled(true);
    }

    public void dataSynchronized() {}

    public void mailSent(int currencyIndex, long balance) {
        if (balanceValue.getCurrencyIndex() == currencyIndex) {
            balanceValue.setValue(balance);
        }
        inventoryLoad.updateLoad();

        addresseeField.reset();
        subjectField.reset();
        messageField.reset();
        setupAttachmentSelectionWidgets(AttachmentType.values()[0]);
    }

    public void mailRemoved(long entryId) {}

    public void attachmentReceived(long oldEntryId, long newEntryId, int currencyIndex, long balance) {
        if (balanceValue.getCurrencyIndex() == currencyIndex) {
            balanceValue.setValue(balance);
        }
        inventoryLoad.updateLoad();
    }
}
