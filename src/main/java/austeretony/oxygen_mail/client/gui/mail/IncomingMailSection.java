package austeretony.oxygen_mail.client.gui.mail;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.api.PrivilegesClient;
import austeretony.oxygen_core.client.gui.base.*;
import austeretony.oxygen_core.client.gui.base.background.Background;
import austeretony.oxygen_core.client.gui.base.block.Text;
import austeretony.oxygen_core.client.gui.base.button.VerticalSlider;
import austeretony.oxygen_core.client.gui.base.core.Callback;
import austeretony.oxygen_core.client.gui.base.core.Section;
import austeretony.oxygen_core.client.gui.base.list.ScrollableList;
import austeretony.oxygen_core.client.gui.base.special.*;
import austeretony.oxygen_core.client.gui.base.special.callback.YesNoCallback;
import austeretony.oxygen_core.client.gui.base.text.TextLabel;
import austeretony.oxygen_core.client.gui.util.OxygenGUIUtils;
import austeretony.oxygen_core.client.settings.CoreSettings;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_mail.client.MailManagerClient;
import austeretony.oxygen_mail.client.gui.mail.incoming.context.ReceiveAttachmentContextAction;
import austeretony.oxygen_mail.client.gui.mail.incoming.context.RemoveMailContextAction;
import austeretony.oxygen_mail.client.gui.mail.incoming.context.ReturnMailContextAction;
import austeretony.oxygen_mail.client.gui.mail.incoming.MailListEntry;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.mail.MailEntry;
import austeretony.oxygen_mail.common.main.MailPrivileges;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class IncomingMailSection extends Section {

    private final MailScreen screen;

    private ScrollableList<MailEntry> mailList;
    private Sorter receiveTimeSorter, subjectSorter;
    private CurrencyValue balanceValue;
    private InventoryLoad inventoryLoad;
    private KeyButton receiveAttachmentButton, removeMailButton;

    private TextLabel mailEntriesAmountLabel, senderNameLabel, receiveTimeLabel, expireTimeLabel, subjectLabel,
            attachmentDescLabel;
    private List<TextLabel> messageLabelsList = new ArrayList<>();
    private AttachmentWidget attachmentWidget;

    @Nullable
    private MailEntry selectedMailEntry;

    public IncomingMailSection(@Nonnull MailScreen screen) {
        super(screen, localize("oxygen_mail.gui.mail.section.incoming_mail"), true);
        this.screen = screen;
    }

    @Override
    public void init() {
        addWidget(new Background.UnderlinedTitleBottomButtons(this));
        addWidget(new TextLabel(4, 12, Texts.title("oxygen_mail.gui.mail.title")));
        addWidget(new SectionSwitcher(this));

        addWidget(receiveTimeSorter = new Sorter(6, 18, Sorter.State.DOWN, localize("oxygen_mail.gui.mail.incoming.sorter.by_receive_time"))
                .setStateChangeListener((previous, current) -> {
                    subjectSorter.setState(Sorter.State.INACTIVE);
                    sortMailEntries();
                }));
        addWidget(subjectSorter = new Sorter(12, 18, Sorter.State.INACTIVE, localize("oxygen_mail.gui.mail.incoming.sorter.by_subject"))
                .setStateChangeListener((previous, current) -> {
                    receiveTimeSorter.setState(Sorter.State.INACTIVE);
                    sortMailEntries();
                }));

        addWidget(mailList = new ScrollableList<>(6, 27, 14, 77, 10)
                .<MailEntry>setEntryMouseClickListener((previous, current, x, y, button) -> {
                    if (current == previous) return;
                    if (previous != null) {
                        previous.setSelected(false);
                    }
                    current.setSelected(true);

                    displayMailEntry(current.getEntry());
                }));
        VerticalSlider slider = new VerticalSlider(6 + 77 + 1, 27, 2, 10 * 14 + 13);
        addWidget(slider);
        mailList.setSlider(slider);
        mailList.createContextMenu(Arrays.asList(
                        new ReceiveAttachmentContextAction(),
                        new ReturnMailContextAction(),
                        new RemoveMailContextAction()));

        addWidget(mailEntriesAmountLabel = new TextLabel(6 + 77, 23, Texts.additional("")
                .setAlignment(Alignment.LEFT)));

        // mail entry start

        addWidget(senderNameLabel = new TextLabel(88, 23, Texts.additional("")).setVisible(false));
        addWidget(receiveTimeLabel = new TextLabel(88, 30, Texts.additionalDark("")).setVisible(false));
        addWidget(expireTimeLabel = new TextLabel(88, 37, Texts.additionalDark("")).setVisible(false));

        addWidget(subjectLabel = new TextLabel(88, 48, Texts.common("")).setVisible(false));

        addWidget(attachmentDescLabel = new TextLabel(88, 162, Texts.additionalDark("").decrementScale(.05F))
                .setVisible(false));
        addWidget(attachmentWidget = new AttachmentWidget(88, 164).setVisible(false));

        // mail entry end

        addWidget(inventoryLoad = new InventoryLoad(6, getHeight() - 10).updateLoad());
        addWidget(balanceValue = new CurrencyValue(getWidth() - 14, getHeight() - 10)
                .setCurrency(OxygenMain.CURRENCY_COINS, OxygenClient.getWatcherValue(OxygenMain.CURRENCY_COINS, 0L)));

        String keyButtonText = localize("oxygen_mail.gui.mail.incoming.button.receive_attachment");
        addWidget(receiveAttachmentButton = new KeyButton(0, 0, Keys.ACTION_KEY, keyButtonText)
                .setLayer(Layer.FRONT)
                .setPressListener(() -> {
                    if (selectedMailEntry == null) return;
                    Callback callback = new YesNoCallback(
                            "oxygen_mail.gui.mail.incoming.callback.receive_attachment",
                            "oxygen_mail.gui.mail.incoming.callback.receive_attachment.message",
                            () -> receiveAttachment(selectedMailEntry.getId()));
                    openCallback(callback);
                })
                .setEnabled(false));
        OxygenGUIUtils.calculateBottomCenteredOffscreenButtonPosition(receiveAttachmentButton, 3, 7);

        keyButtonText = localize("oxygen_mail.gui.mail.incoming.button.remove_mail");
        addWidget(removeMailButton = new KeyButton(0, 0, Keys.CANCEL_KEY, keyButtonText)
                .setLayer(Layer.FRONT)
                .setPressListener(() -> {
                    if (selectedMailEntry == null) return;
                    Callback callback = new YesNoCallback(
                            "oxygen_mail.gui.mail.incoming.callback.remove_mail",
                            "oxygen_mail.gui.mail.incoming.callback.remove_mail.message",
                            () -> removeMail(selectedMailEntry.getId()));
                    openCallback(callback);
                })
                .setEnabled(false));
        OxygenGUIUtils.calculateBottomCenteredOffscreenButtonPosition(removeMailButton, 5, 7);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        OxygenGUIUtils.closeScreenOnKeyPress(getScreen(), keyCode);
        super.keyTyped(typedChar, keyCode);
    }

    public void sharedDataSynchronized() {}

    public void dataSynchronized() {
        sortMailEntries();
    }

    private void sortMailEntries() {
        List<MailEntry> sortedEntries = getSortedMailEntries();
        mailList.clear();
        for (MailEntry mailEntry : sortedEntries) {
            mailList.addElement(new MailListEntry(mailEntry));
        }

        clearDisplayedMailData();
        if (!sortedEntries.isEmpty()) {
            mailList.setFirstElementSelected();
            displayMailEntry(sortedEntries.get(0));
        }

        int mailboxCapacity = PrivilegesClient.getInt(MailPrivileges.MAILBOX_SIZE.getId(), MailConfig.MAILBOX_SIZE.asInt());
        mailEntriesAmountLabel.getText().setText(sortedEntries.size() + "/" + mailboxCapacity);
    }

    private List<MailEntry> getSortedMailEntries() {
        return MailManagerClient.instance().getMailMap().values()
                .stream()
                .sorted(getSortersComparator())
                .collect(Collectors.toList());
    }

    private Comparator<MailEntry> getSortersComparator() {
        if (receiveTimeSorter.getState() != Sorter.State.INACTIVE) {
            if (receiveTimeSorter.getState() == Sorter.State.DOWN) {
                return Comparator.comparingLong(MailEntry::getId).reversed();
            } else {
                return Comparator.comparingLong(MailEntry::getId);
            }
        } else {
            if (subjectSorter.getState() == Sorter.State.DOWN) {
                return Comparator.comparing(MailEntry::getSubject);
            } else {
                return Comparator.comparing(MailEntry::getSubject).reversed();
            }
        }
    }

    private void displayMailEntry(MailEntry mailEntry) {
        selectedMailEntry = mailEntry;
        receiveAttachmentButton.setEnabled(mailEntry.isPending());
        removeMailButton.setEnabled(!mailEntry.isPending());

        if (mailEntry.isSentByPlayer()) {
            senderNameLabel.getText().setColorEnabled(CoreSettings.COLOR_TEXT_BASE_ENABLED.asInt());
        } else {
            senderNameLabel.getText().setColorEnabled(0xFFFFE460);
        }
        senderNameLabel.getText().setText(localize(mailEntry.getSenderName()));
        senderNameLabel.setVisible(true);
        ZonedDateTime receiveTime = OxygenClient.getZonedDateTime(mailEntry.getId());
        receiveTimeLabel.getText().setText(OxygenClient.getDateTimeFormatter().format(receiveTime));
        receiveTimeLabel.setVisible(true);
        expireTimeLabel.getText().setText(OxygenGUIUtils.getExpirationTimeLocalizedString(mailEntry.getId(),
                mailEntry.getAttachment().getExpireTimeMillis(mailEntry)));
        expireTimeLabel.setVisible(true);

        subjectLabel.getText().setText(localize(mailEntry.getSubject()));
        subjectLabel.setVisible(true);

        for (TextLabel textLabel : messageLabelsList) {
            getWidgets().remove(textLabel);
        }

        String[] args = mailEntry.getMessageArguments();
        for (int i = 0; i < args.length; i++) {
            String localized = localize(args[i]);
            args[i] = localized;
        }
        String messageStr = localize(mailEntry.getMessage(), args);
        List<String> lines = GUIUtils.splitTextToLines(messageStr, Texts.additionalDark("").getScale(), 124);
        int index = 0;
        for (String line : lines) {
            Text text = Texts.additionalDark(line);
            TextLabel textLabel = new TextLabel(90, 56 + (int) (text.getHeight() + 3F) * index++, text);
            addWidget(textLabel);
            messageLabelsList.add(textLabel);
        }

        attachmentDescLabel.getText().setText(mailEntry.getAttachmentType().getDisplayName());
        attachmentDescLabel.setVisible(true);
        attachmentWidget.setAttachment(mailEntry.getAttachment());
        attachmentWidget.setVisible(mailEntry.isPending());

        MailManagerClient.instance().markMailEntryAsRead(mailEntry.getId());
    }

    private void clearDisplayedMailData() {
        senderNameLabel.setVisible(false);
        receiveTimeLabel.setVisible(false);
        expireTimeLabel.setVisible(false);

        subjectLabel.setVisible(false);
        for (TextLabel textLabel : messageLabelsList) {
            getWidgets().remove(textLabel);
        }

        attachmentDescLabel.setVisible(false);
        attachmentWidget.setVisible(false);
    }

    public void mailSent(int currencyIndex, long balance) {
        if (balanceValue.getCurrencyIndex() == currencyIndex) {
            balanceValue.setValue(balance);
        }
        inventoryLoad.updateLoad();
    }

    private void removeMail(long entryId) {
        MailManagerClient.instance().removeMail(entryId);
    }

    public void mailRemoved(long entryId) {
        sortMailEntries();
    }

    private void receiveAttachment(long entryId) {
        MailManagerClient.instance().receiveAttachment(entryId);
    }

    public void attachmentReceived(long oldEntryId, long newEntryId, int currencyIndex, long balance) {
        int position = mailList.getScrollPosition();
        sortMailEntries();
        mailList.setScrollPosition(position);
        if (balanceValue.getCurrencyIndex() == currencyIndex) {
            balanceValue.setValue(balance);
        }
        inventoryLoad.updateLoad();

        for (int i = 0; i < mailList.getWidgets().size(); i++) {
            MailListEntry mailListEntry = (MailListEntry) mailList.getWidgets().get(i);
            mailListEntry.setSelected(false);
            if (mailListEntry.getEntry().getId() == newEntryId) {
                mailListEntry.setSelected(true);
            }
        }

        MailEntry mailEntry = MailManagerClient.instance().getMailEntry(newEntryId);
        if (mailEntry != null) {
            displayMailEntry(mailEntry);
        }
    }
}
