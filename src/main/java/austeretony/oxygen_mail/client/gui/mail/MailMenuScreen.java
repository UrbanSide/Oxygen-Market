package austeretony.oxygen_mail.client.gui.mail;

import java.util.Map;

import austeretony.alternateui.screen.core.AbstractGUIScreen;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.core.GUIWorkspace;
import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.InventoryProviderClient;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.api.PrivilegesProviderClient;
import austeretony.oxygen_core.client.gui.menu.OxygenMenuEntry;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_mail.client.gui.menu.MailMenuEntry;
import austeretony.oxygen_mail.client.settings.gui.EnumMailGUISetting;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.mail.Attachment;
import austeretony.oxygen_mail.common.mail.EnumMail;
import austeretony.oxygen_mail.common.mail.Mail;
import austeretony.oxygen_mail.common.main.EnumMailPrivilege;
import austeretony.oxygen_mail.common.main.MailMain;

public class MailMenuScreen extends AbstractGUIScreen {

    public static final OxygenMenuEntry MAIL_MENU_ENTRY = new MailMenuEntry();

    private final Map<ItemStackWrapper, Integer> inventoryContent;

    protected IncomingMailSection incomingSection;

    protected SendingSection sendingSection;

    public final boolean allowMailSending;

    public MailMenuScreen() {
        OxygenHelperClient.syncSharedData(MailMain.MAIL_MENU_SCREEN_ID);
        OxygenHelperClient.syncData(MailMain.MAIL_DATA_ID);
        this.inventoryContent = InventoryProviderClient.getPlayerInventory().getInventoryContent(ClientReference.getClientPlayer());

        this.allowMailSending = PrivilegesProviderClient.getAsBoolean(EnumMailPrivilege.ALLOW_MAIL_SENDING.id(), MailConfig.ALLOW_MAIL_SENDING.asBoolean());      
    }

    @Override
    protected GUIWorkspace initWorkspace() {
        EnumGUIAlignment alignment = EnumGUIAlignment.CENTER;
        switch (EnumMailGUISetting.MAIL_MENU_ALIGNMENT.get().asInt()) {
        case - 1: 
            alignment = EnumGUIAlignment.LEFT;
            break;
        case 0:
            alignment = EnumGUIAlignment.CENTER;
            break;
        case 1:
            alignment = EnumGUIAlignment.RIGHT;
            break;    
        default:
            alignment = EnumGUIAlignment.CENTER;
            break;
        }
        return new GUIWorkspace(this, 213, 170).setAlignment(alignment, 0, 0);
    }

    @Override
    protected void initSections() {
        this.getWorkspace().initSection(this.incomingSection = (IncomingMailSection) new IncomingMailSection(this).enable());    
        this.getWorkspace().initSection(this.sendingSection = (SendingSection) new SendingSection(this).enable());        
    }

    @Override
    protected AbstractGUISection getDefaultSection() {
        return this.incomingSection;
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element) {}

    @Override
    protected boolean doesGUIPauseGame() {
        return false;
    }

    public Map<ItemStackWrapper, Integer> getInventoryContent() {
        return this.inventoryContent;
    }

    public int getEqualStackAmount(ItemStackWrapper stackWrapper) {
        Integer amount = this.inventoryContent.get(stackWrapper);
        return amount == null ? 0 : amount.intValue();
    }

    public void addItemStack(ItemStackWrapper stackWrapper, int amount) {
        Integer stored = this.inventoryContent.get(stackWrapper);
        this.inventoryContent.put(stackWrapper, stored != null ? stored + amount : amount);
    }

    public void removeItemStack(ItemStackWrapper stackWrapper, int amount) {
        Integer stored = this.inventoryContent.get(stackWrapper);
        if (stored != null) {
            if (stored > amount)
                this.inventoryContent.put(stackWrapper, stored - amount);
            else
                this.inventoryContent.remove(stackWrapper);
        }
    }

    public void sharedDataSynchronized() {
        this.incomingSection.sharedDataSynchronized();
        this.sendingSection.sharedDataSynchronized();
    }

    public void mailSynchronized() {
        this.incomingSection.mailSynchronized();
        this.sendingSection.mailSynchronized();
    }

    public void mailSent(EnumMail type, Attachment attachment, long balance) {
        attachment.sent();
        this.incomingSection.mailSent(type, attachment, balance);
        this.sendingSection.mailSent(type, attachment, balance);
    }

    public void messageRemoved(long messageId) {
        this.incomingSection.messageRemoved(messageId);
        this.sendingSection.messageRemoved(messageId);
    }

    public void attachmentReceived(long oldMessageId, Mail mail, long balance) {
        mail.getAttachment().received();
        this.incomingSection.attachmentReceived(oldMessageId, mail, balance);
        this.sendingSection.attachmentReceived(oldMessageId, mail, balance);
    }

    public IncomingMailSection getIncomingSection() {
        return this.incomingSection;
    }

    public SendingSection getSendingSection() {
        return this.sendingSection;
    }
}
