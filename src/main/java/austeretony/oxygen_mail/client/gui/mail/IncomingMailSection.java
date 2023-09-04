package austeretony.oxygen_mail.client.gui.mail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.lwjgl.input.Keyboard;

import austeretony.alternateui.screen.button.GUIButton;
import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.text.GUITextBoxLabel;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.api.OxygenGUIHelper;
import austeretony.oxygen_core.client.api.PrivilegesProviderClient;
import austeretony.oxygen_core.client.api.TimeHelperClient;
import austeretony.oxygen_core.client.api.WatcherHelperClient;
import austeretony.oxygen_core.client.gui.elements.OxygenContextMenu;
import austeretony.oxygen_core.client.gui.elements.OxygenCurrencyValue;
import austeretony.oxygen_core.client.gui.elements.OxygenDefaultBackgroundWithButtonsUnderlinedFiller;
import austeretony.oxygen_core.client.gui.elements.OxygenInventoryLoad;
import austeretony.oxygen_core.client.gui.elements.OxygenKeyButton;
import austeretony.oxygen_core.client.gui.elements.OxygenScrollablePanel;
import austeretony.oxygen_core.client.gui.elements.OxygenSectionSwitcher;
import austeretony.oxygen_core.client.gui.elements.OxygenSorter;
import austeretony.oxygen_core.client.gui.elements.OxygenSorter.EnumSorting;
import austeretony.oxygen_core.client.gui.elements.OxygenTextLabel;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_core.common.util.OxygenUtils;
import austeretony.oxygen_mail.client.MailManagerClient;
import austeretony.oxygen_mail.client.gui.mail.incoming.MessagePanelEntry;
import austeretony.oxygen_mail.client.gui.mail.incoming.callback.RemoveMessageCallback;
import austeretony.oxygen_mail.client.gui.mail.incoming.callback.ReturnAttachmentCallback;
import austeretony.oxygen_mail.client.gui.mail.incoming.callback.TakeAttachmentCallback;
import austeretony.oxygen_mail.client.gui.mail.incoming.context.RemoveMessageContextAction;
import austeretony.oxygen_mail.client.gui.mail.incoming.context.ReturnAttachmentContextAction;
import austeretony.oxygen_mail.client.gui.mail.incoming.context.TakeAttachmentContextAction;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.mail.Attachment;
import austeretony.oxygen_mail.common.mail.EnumMail;
import austeretony.oxygen_mail.common.mail.Mail;
import austeretony.oxygen_mail.common.main.EnumMailPrivilege;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.text.TextFormatting;

public class IncomingMailSection extends AbstractGUISection {

    private final MailMenuScreen screen;

    private OxygenKeyButton takeAttachmentButton, removeMessageButton;

    private OxygenTextLabel messagesAmountLabel;

    private OxygenSorter timeSorterElement, subjectSorterElement;

    private OxygenScrollablePanel messagesPanel;

    private OxygenInventoryLoad inventoryLoad;

    private OxygenCurrencyValue balanceValue;

    private AbstractGUICallback takeAttachmentCallback, returnAttachmentCallback, removeMessageCallback;

    //message content

    private OxygenTextLabel senderTextLabel, receiveTimeTextLabel, expireTimeTextLabel, messageSubjectTextLabel, 
    attachmentTitleTextLabel, codCostTextLabel;

    private GUITextBoxLabel messageTextBoxLabel;

    private MessageAttachment attachment;

    //cache

    @Nullable
    private MessagePanelEntry currentMessageEntry;

    public IncomingMailSection(MailMenuScreen screen) {
        super(screen);
        this.screen = screen;
        this.setDisplayText(ClientReference.localize("oxygen_mail.gui.mail.incoming"));
    }

    @Override
    public void init() {
        this.takeAttachmentCallback = new TakeAttachmentCallback(this.screen, this, 140, 36).enableDefaultBackground();
        this.returnAttachmentCallback = new ReturnAttachmentCallback(this.screen, this, 140, 36).enableDefaultBackground();
        this.removeMessageCallback = new RemoveMessageCallback(this.screen, this, 140, 36).enableDefaultBackground();

        this.addElement(new OxygenDefaultBackgroundWithButtonsUnderlinedFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new OxygenTextLabel(4, 12, ClientReference.localize("oxygen_mail.gui.mail.title"), EnumBaseGUISetting.TEXT_TITLE_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        this.addElement(new OxygenSectionSwitcher(this.getWidth() - 4, 5, this, this.screen.getSendingSection()));

        this.addElement(this.messagesAmountLabel = new OxygenTextLabel(0, 22, "", EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.05F, EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        this.addElement(this.timeSorterElement = new OxygenSorter(6, 18, EnumSorting.DOWN, ClientReference.localize("oxygen_mail.sorting.receiveTime")));   

        this.timeSorterElement.setSortingListener((sorting)->{
            this.subjectSorterElement.reset();
            if (sorting == EnumSorting.DOWN)
                this.sortMail(0);
            else
                this.sortMail(1);
        });

        this.addElement(this.subjectSorterElement = new OxygenSorter(12, 18, EnumSorting.INACTIVE, ClientReference.localize("oxygen_mail.sorting.subject")));  

        this.subjectSorterElement.setSortingListener((sorting)->{
            this.timeSorterElement.reset();
            if (sorting == EnumSorting.DOWN)
                this.sortMail(2);
            else
                this.sortMail(3);
        });

        this.addElement(this.messagesPanel = new OxygenScrollablePanel(this.screen, 6, 24, 75, 10, 1, 120, 12, EnumBaseGUISetting.TEXT_PANEL_SCALE.get().asFloat(), true));

        this.messagesPanel.<MessagePanelEntry>setElementClickListener((previous, clicked, mouseX, mouseY, mouseButton)->{
            if (this.currentMessageEntry != clicked) {       
                if (this.currentMessageEntry != null)
                    this.currentMessageEntry.setToggled(false);
                clicked.toggle();    
                this.currentMessageEntry = clicked;
                this.resetMessageContent();
                this.loadMessageContent(clicked.getWrapped());
                if (!MailManagerClient.instance().getMailboxContainer().isMarkedAsRead(clicked.getWrapped().getId())) {
                    MailManagerClient.instance().getMailboxContainer().markAsRead(clicked.getWrapped().getId());
                    MailManagerClient.instance().getMailboxContainer().setChanged(true);
                    clicked.read();
                }
            }
        });

        this.messagesPanel.initContextMenu(new OxygenContextMenu(
                new TakeAttachmentContextAction(this),
                new ReturnAttachmentContextAction(this),
                new RemoveMessageContextAction(this)));

        this.addElement(this.inventoryLoad = new OxygenInventoryLoad(6, this.getHeight() - 8));
        this.inventoryLoad.updateLoad();
        this.addElement(this.balanceValue = new OxygenCurrencyValue(this.getWidth() - 14, this.getHeight() - 10));   
        this.balanceValue.setValue(OxygenMain.COMMON_CURRENCY_INDEX, WatcherHelperClient.getLong(OxygenMain.COMMON_CURRENCY_INDEX));

        this.initMessageElements();
    }

    private void initMessageElements() {
        this.addElement(this.senderTextLabel = new OxygenTextLabel(88, 23, "", EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()).disableFull());
        this.addElement(this.receiveTimeTextLabel = new OxygenTextLabel(88, 31, "", EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.05F, EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()).disableFull());
        this.addElement(this.expireTimeTextLabel = new OxygenTextLabel(88, 39, "", EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.05F, EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()).disableFull()); 

        this.addElement(this.messageSubjectTextLabel = new OxygenTextLabel(88, 51, "", EnumBaseGUISetting.TEXT_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()).disableFull());
        this.addElement(this.messageTextBoxLabel = new GUITextBoxLabel(90, 54, 120, 84).setEnabledTextColor(EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt())
                .setTextScale(EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat()).setLineOffset(2).disableFull());

        this.addElement(this.takeAttachmentButton = new OxygenKeyButton(0, this.getY() + this.getHeight() + this.screen.guiTop - 8, ClientReference.localize("oxygen_mail.gui.mail.button.takeAttachment"), Keyboard.KEY_E, this::openTakeAttachmentCallback).disable());  
        this.addElement(this.removeMessageButton = new OxygenKeyButton(0, this.getY() + this.getHeight() + this.screen.guiTop - 8, ClientReference.localize("oxygen_mail.gui.mail.button.removeMessage"), Keyboard.KEY_X, this::openRemoveMessageCallback).disable());  

        this.addElement(this.attachment = new MessageAttachment(88, 139).disableFull()); 
    }

    private void calculateButtonsHorizontalPosition() {
        ScaledResolution sr = new ScaledResolution(this.mc);
        this.takeAttachmentButton.setX((sr.getScaledWidth() - (12 + this.textWidth(this.takeAttachmentButton.getDisplayText(), this.takeAttachmentButton.getTextScale()))) / 2 - this.screen.guiLeft);
        this.removeMessageButton.setX(sr.getScaledWidth() / 2 + 50 - this.screen.guiLeft);
    }

    private void sortMail(int mode) {
        this.resetMessageContent();

        List<Mail> mail = new ArrayList<>(MailManagerClient.instance().getMailboxContainer().getMessages());

        if (mode == 0)
            Collections.sort(mail, (m1, m2)->m2.getId() < m1.getId() ? - 1 : m2.getId() > m1.getId() ? 1 : 0);
        else if (mode == 1)
            Collections.sort(mail, (m1, m2)->m1.getId() < m2.getId() ? - 1 : m1.getId() > m2.getId() ? 1 : 0);
        else if (mode == 2)
            Collections.sort(mail, (m1, m2)->localize(m1.getSubject()).compareTo(localize(m2.getSubject())));
        else if (mode == 3)
            Collections.sort(mail, (m1, m2)->localize(m2.getSubject()).compareTo(localize(m1.getSubject())));

        this.messagesPanel.reset();
        for (Mail msg : mail)
            this.messagesPanel.addEntry(new MessagePanelEntry(msg));

        int maxAmount = PrivilegesProviderClient.getAsInt(EnumMailPrivilege.MAILBOX_SIZE.id(), MailConfig.MAILBOX_SIZE.asInt());
        this.messagesAmountLabel.setDisplayText(String.format("%d/%d", mail.size(), maxAmount));     
        this.messagesAmountLabel.setX(84 - this.textWidth(this.messagesAmountLabel.getDisplayText(), this.messagesAmountLabel.getTextScale()));

        this.messagesPanel.getScroller().reset();
        this.messagesPanel.getScroller().updateRowsAmount(MathUtils.clamp(mail.size(), 12, MathUtils.greaterOfTwo(mail.size(), maxAmount)));
    }

    private static String localize(String value) {
        return ClientReference.localize(value);
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (mouseButton == 0) {
            if (element == this.takeAttachmentButton)
                this.openTakeAttachmentCallback();
            else if (element == this.removeMessageButton)
                this.openRemoveMessageCallback();
        }
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {   
        if (!this.hasCurrentCallback())
            if (OxygenGUIHelper.isOxygenMenuEnabled()) {
                if (keyCode == MailMenuScreen.MAIL_MENU_ENTRY.getKeyCode())
                    this.screen.close();
            } else if (MailConfig.ENABLE_MAIL_MENU_KEY.asBoolean() 
                    && keyCode == MailManagerClient.instance().getKeyHandler().getMailMenuKeybinding().getKeyCode())
                this.screen.close();
        return super.keyTyped(typedChar, keyCode); 
    }

    private void loadMessageContent(Mail mail) {
        this.senderTextLabel.setDisplayText(ClientReference.localize("oxygen_mail.gui.mail.msg.sender", 
                mail.isSystemMessage() ? TextFormatting.YELLOW + localize(mail.getSenderName()) : localize(mail.getSenderName())));
        this.senderTextLabel.enableFull(); 

        this.receiveTimeTextLabel.setDisplayText(TimeHelperClient.getDateTimeFormatter().format(TimeHelperClient.getZonedDateTime(mail.getId())));
        this.receiveTimeTextLabel.initTooltip(ClientReference.localize("oxygen_mail.gui.mail.msg.received", OxygenUtils.getTimePassedLocalizedString(mail.getId())));
        this.receiveTimeTextLabel.enableFull(); 

        this.expireTimeTextLabel.setDisplayText(ClientReference.localize("oxygen_mail.gui.mail.msg.expires", getExpirationTimeLocalizedString(mail)));
        this.expireTimeTextLabel.enableFull();   

        this.messageSubjectTextLabel.setDisplayText(ClientReference.localize(mail.getSubject()));
        this.messageSubjectTextLabel.enableFull();
        this.messageTextBoxLabel.setDisplayText(ClientReference.localize(mail.getMessage(), (Object[]) mail.getMessageArguments()));
        this.messageTextBoxLabel.enableFull();

        if (mail.isPending()) {
            this.attachment.load(mail.getType(), mail.getAttachment());
            this.attachment.enableFull();
            if (!mail.isPending())
                this.attachment.disableFull();

            this.takeAttachmentButton.enable();
            if (mail.getType() == EnumMail.COD)
                this.takeAttachmentButton.setDisplayText(ClientReference.localize("oxygen_mail.gui.mail.button.payForAttachment"));
            else
                this.takeAttachmentButton.setDisplayText(ClientReference.localize("oxygen_mail.gui.mail.button.takeAttachment"));
            if (this.inventoryLoad.isOverloaded())
                this.takeAttachmentButton.disable();

            if (!mail.getAttachment().canReceive()) {
                this.attachment.disable();
                this.takeAttachmentButton.disable();
            }
        } else
            this.removeMessageButton.enable();
    }

    private void resetMessageContent() {
        this.senderTextLabel.disableFull(); 
        this.expireTimeTextLabel.disableFull();   
        this.receiveTimeTextLabel.disableFull();   

        this.messageSubjectTextLabel.disableFull();
        this.messageTextBoxLabel.disableFull();

        this.attachment.disableFull();

        this.takeAttachmentButton.disable();  
        this.removeMessageButton.disable();
    }

    private static String getExpirationTimeLocalizedString(Mail mail) {
        int expiresInHours = - 1;
        switch (mail.getType()) {
        case LETTER:
            expiresInHours = mail.isSystemMessage() ? MailConfig.SYSTEM_LETTER_EXPIRE_TIME_HOURS.asInt() : MailConfig.LETTER_EXPIRE_TIME_HOURS.asInt();
            break;
        case REMITTANCE:
            expiresInHours = mail.isSystemMessage() ? MailConfig.SYSTEM_REMITTANCE_EXPIRE_TIME_HOURS.asInt() : MailConfig.REMITTANCE_EXPIRE_TIME_HOURS.asInt();
            break;
        case PARCEL:
            expiresInHours = mail.isSystemMessage() ? MailConfig.SYSTEM_PARCEL_EXPIRE_TIME_HOURS.asInt() : MailConfig.PARCEL_EXPIRE_TIME_HOURS.asInt();
            break;
        case COD:
            expiresInHours = MailConfig.COD_EXPIRE_TIME_HOURS.asInt();
            break;  
        }
        if (expiresInHours < 0)
            return ClientReference.localize("oxygen_mail.gui.neverExpires");
        return OxygenUtils.getExpirationTimeLocalizedString(TimeUnit.HOURS.toMillis(expiresInHours), mail.getId());
    }

    public void sharedDataSynchronized() {}

    public void mailSynchronized() {
        this.sortMail(0);
        this.initLatestMessageOnMenuOpen();

        this.calculateButtonsHorizontalPosition();
    }

    private void initLatestMessageOnMenuOpen() {
        if (!this.messagesPanel.visibleButtons.isEmpty()) {
            GUIButton button = this.messagesPanel.visibleButtons.get(0).toggle();
            this.messagesPanel.setPreviousClickedButton(button);
            this.currentMessageEntry = (MessagePanelEntry) button;
            this.loadMessageContent(this.currentMessageEntry.getWrapped());

            if (!MailManagerClient.instance().getMailboxContainer().isMarkedAsRead(this.currentMessageEntry.getWrapped().getId())) {
                MailManagerClient.instance().getMailboxContainer().markAsRead(this.currentMessageEntry.getWrapped().getId());
                MailManagerClient.instance().getMailboxContainer().setChanged(true);
                this.currentMessageEntry.read();
            }
        }
    }

    public void mailSent(EnumMail type, Attachment attachment, long balance) {
        this.balanceValue.updateValue(balance);
        if (type == EnumMail.PARCEL || type == EnumMail.COD)
            this.inventoryLoad.updateLoad();
    }

    public void messageRemoved(long messageId) {
        this.timeSorterElement.setSorting(EnumSorting.DOWN);
        this.subjectSorterElement.reset();
        this.sortMail(0);
        this.initLatestMessageOnMenuOpen();
    }

    public void attachmentReceived(long oldMessageId, Mail mail, long balance) {
        this.balanceValue.updateValue(balance);

        if (mail.getType() == EnumMail.PARCEL || mail.getType() == EnumMail.COD) 
            this.inventoryLoad.updateLoad();

        this.sortMail(0);
        this.initLatestMessageOnMenuOpen();
    }

    public OxygenInventoryLoad getInventoryLoad() {
        return this.inventoryLoad;
    }

    public OxygenCurrencyValue getBalanceValue() {
        return this.balanceValue;
    }

    @Nullable
    public MessagePanelEntry getCurrentMessageEntry() {
        return this.currentMessageEntry;
    }

    public OxygenKeyButton getTakeAttachmentButton() {
        return this.takeAttachmentButton;
    }

    public OxygenKeyButton getRemoveMessageButton() {
        return this.removeMessageButton;
    }

    public void openTakeAttachmentCallback() {
        this.takeAttachmentCallback.open();
    }

    public void openReturnAttachmentCallback() {
        this.returnAttachmentCallback.open();
    }

    public void openRemoveMessageCallback() {
        this.removeMessageCallback.open();
    }
}
