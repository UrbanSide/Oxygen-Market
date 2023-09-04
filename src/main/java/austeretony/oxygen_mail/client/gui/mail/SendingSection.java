package austeretony.oxygen_mail.client.gui.mail;

import javax.annotation.Nullable;

import org.lwjgl.input.Keyboard;

import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.api.OxygenGUIHelper;
import austeretony.oxygen_core.client.api.PrivilegesProviderClient;
import austeretony.oxygen_core.client.gui.elements.OxygenCheckBoxButton;
import austeretony.oxygen_core.client.gui.elements.OxygenCurrencyValue;
import austeretony.oxygen_core.client.gui.elements.OxygenDefaultBackgroundWithButtonsUnderlinedFiller;
import austeretony.oxygen_core.client.gui.elements.OxygenInventoryLoad;
import austeretony.oxygen_core.client.gui.elements.OxygenKeyButton;
import austeretony.oxygen_core.client.gui.elements.OxygenNumberField;
import austeretony.oxygen_core.client.gui.elements.OxygenSectionSwitcher;
import austeretony.oxygen_core.client.gui.elements.OxygenTextBoxField;
import austeretony.oxygen_core.client.gui.elements.OxygenTextField;
import austeretony.oxygen_core.client.gui.elements.OxygenTextLabel;
import austeretony.oxygen_core.client.gui.elements.OxygenUsernameField;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_mail.client.MailManagerClient;
import austeretony.oxygen_mail.client.gui.mail.sending.InventoryItemPanelEntry;
import austeretony.oxygen_mail.client.gui.mail.sending.SelectedItem;
import austeretony.oxygen_mail.client.gui.mail.sending.callback.SelectItemCallback;
import austeretony.oxygen_mail.client.gui.mail.sending.callback.SendMessageCallback;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.mail.Attachment;
import austeretony.oxygen_mail.common.mail.EnumMail;
import austeretony.oxygen_mail.common.mail.Mail;
import austeretony.oxygen_mail.common.main.EnumMailPrivilege;
import net.minecraft.client.gui.ScaledResolution;

public class SendingSection extends AbstractGUISection {

    private final MailMenuScreen screen;

    private OxygenKeyButton selectItemButton, sendMailButton;

    private OxygenUsernameField addresseeUsernameField;

    private OxygenTextField subjectTextField;

    private OxygenNumberField currencyValueField, itemAmountField;

    private OxygenTextBoxField messageTextBoxField;

    private OxygenCheckBoxButton enableRemittanceButton, enableCODButton, enableParcelButton;

    private OxygenInventoryLoad inventoryLoad;

    private OxygenCurrencyValue balanceValue;

    private SelectedItem selectedItem; 

    private AbstractGUICallback selectItemCallback, sendMessageCallback;

    //cache

    @Nullable
    private ItemStackWrapper selectedItemWrapper;

    public SendingSection(MailMenuScreen screen) {
        super(screen);
        this.screen = screen;
        this.setDisplayText(ClientReference.localize("oxygen_mail.gui.mail.sending"));
    }

    @Override
    public void init() {
        this.selectItemCallback = new SelectItemCallback(this.screen, this, 140, 94).enableDefaultBackground();
        this.sendMessageCallback = new SendMessageCallback(this.screen, this, 140, 83).enableDefaultBackground();

        this.addElement(new OxygenDefaultBackgroundWithButtonsUnderlinedFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new OxygenTextLabel(4, 12, ClientReference.localize("oxygen_mail.gui.mail.title"), EnumBaseGUISetting.TEXT_TITLE_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        this.addElement(new OxygenTextLabel(6, 46, ClientReference.localize("oxygen_mail.gui.mail.subject"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
        this.addElement(this.subjectTextField = new OxygenTextField(6, 47, 120, Mail.MESSAGE_SUBJECT_MAX_LENGTH, ""));
        this.subjectTextField.setInputListener((keyChar, keyCode)->this.sendMailButton.setEnabled(
                this.screen.allowMailSending
                && !this.addresseeUsernameField.getTypedText().isEmpty() 
                && !this.subjectTextField.getTypedText().isEmpty()));

        this.addElement(this.messageTextBoxField = new OxygenTextBoxField(6, 58, 120, 96, Mail.MESSAGE_MAX_LENGTH));

        this.addElement(new OxygenTextLabel(6, 24, ClientReference.localize("oxygen_mail.gui.mail.sendTo"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
        this.addElement(this.addresseeUsernameField = new OxygenUsernameField(6, 25, 120));
        this.addresseeUsernameField.setInputListener((keyChar, keyCode)->this.sendMailButton.setEnabled(
                this.screen.allowMailSending
                && !this.addresseeUsernameField.getTypedText().isEmpty() 
                && !this.subjectTextField.getTypedText().isEmpty()));
        this.addresseeUsernameField.disable();

        this.addElement(new OxygenTextLabel(130, 24, ClientReference.localize("oxygen_mail.gui.mail.attachment"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        this.addElement(this.enableRemittanceButton = new OxygenCheckBoxButton(130, 27));    
        this.addElement(new OxygenTextLabel(139, 33, ClientReference.localize("oxygen_mail.gui.mail.remittance"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));

        this.addElement(this.enableParcelButton = new OxygenCheckBoxButton(130, 37));        
        this.addElement(new OxygenTextLabel(139, 43, ClientReference.localize("oxygen_mail.gui.mail.parcel"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));     

        this.addElement(this.enableCODButton = new OxygenCheckBoxButton(130, 47));
        this.addElement(new OxygenTextLabel(139, 53, ClientReference.localize("oxygen_mail.gui.mail.cod"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));

        this.addElement(new OxygenTextLabel(130, 63, ClientReference.localize("oxygen_mail.gui.mail.label.currencyAmount"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
        this.addElement(this.currencyValueField = new OxygenNumberField(130, 64, 45, "", 0L, false, 0, true));
        this.currencyValueField.disable();

        this.addElement(new OxygenTextLabel(130, 100, ClientReference.localize("oxygen_mail.gui.mail.label.itemAmount"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
        this.addElement(this.itemAmountField = new OxygenNumberField(130, 101, 45, "", 0L, false, 0, true));
        this.itemAmountField.disable();

        this.addElement(this.sendMailButton = new OxygenKeyButton(0, this.getY() + this.getHeight() + this.screen.guiTop - 8, ClientReference.localize("oxygen_mail.gui.mail.button.sendMessage"), Keyboard.KEY_E, this::openSendMessageCallback).disable());     
        this.addElement(this.selectItemButton = new OxygenKeyButton(0, this.getY() + this.getHeight() + this.screen.guiTop - 8, ClientReference.localize("oxygen_mail.gui.mail.button.selectItem"), Keyboard.KEY_I, this::openSelectItemCallback).disable());   

        this.addElement(this.inventoryLoad = new OxygenInventoryLoad(6, this.getHeight() - 8));
        this.inventoryLoad.setLoad(this.screen.getIncomingSection().getInventoryLoad().getLoad());
        this.addElement(this.balanceValue = new OxygenCurrencyValue(this.getWidth() - 14, this.getHeight() - 10));   
        this.balanceValue.setValue(OxygenMain.COMMON_CURRENCY_INDEX, this.screen.getIncomingSection().getBalanceValue().getValue());

        this.addElement(this.selectedItem = new SelectedItem(130, 76).disableFull());

        this.addElement(new OxygenSectionSwitcher(this.getWidth() - 4, 5, this, this.screen.getIncomingSection()));
    }

    private void calculateButtonsHorizontalPosition() {
        ScaledResolution sr = new ScaledResolution(this.mc);
        this.sendMailButton.setX((sr.getScaledWidth() - (12 + this.textWidth(this.sendMailButton.getDisplayText(), this.sendMailButton.getTextScale()))) / 2 - this.screen.guiLeft);
        this.selectItemButton.setX(sr.getScaledWidth() / 2 + 50 - this.screen.guiLeft);
    }

    private void openSelectItemCallback() {
        if (!this.addresseeUsernameField.isDragged()
                && !this.subjectTextField.isDragged()
                && !this.messageTextBoxField.isDragged())          
            this.selectItemCallback.open();   
    }

    private void openSendMessageCallback() {
        if (!this.addresseeUsernameField.isDragged()
                && !this.subjectTextField.isDragged()
                && !this.messageTextBoxField.isDragged())          
            this.sendMessageCallback.open();  
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (mouseButton == 0) {
            if (element == this.selectItemButton)
                this.openSelectItemCallback();
            else if (element == this.sendMailButton)
                this.openSendMessageCallback();
            else if (element == this.enableRemittanceButton) {
                this.enableCODButton.setToggled(false);
                this.enableParcelButton.setToggled(false);
                this.selectItemButton.disable();
                this.itemAmountField.disable();
                this.selectedItemWrapper = null;
                this.selectedItem.disableFull();

                this.currencyValueField.setMaxNumber(MathUtils.lesserOfTwo(
                        this.balanceValue.getValue(), 
                        PrivilegesProviderClient.getAsLong(EnumMailPrivilege.REMITTANCE_MAX_VALUE.id(), MailConfig.REMITTANCE_MAX_VALUE.asLong())));

                this.currencyValueField.reset();
                this.currencyValueField.enable();

                this.sendMailButton.setEnabled(
                        this.screen.allowMailSending
                        && !this.addresseeUsernameField.getTypedText().isEmpty() 
                        && !this.subjectTextField.getTypedText().isEmpty());
            } else if (element == this.enableCODButton) {
                this.enableRemittanceButton.setToggled(false);
                this.enableParcelButton.setToggled(false);
                this.selectedItemWrapper = null;
                this.selectedItem.disableFull();

                this.currencyValueField.setMaxNumber(PrivilegesProviderClient.getAsLong(EnumMailPrivilege.COD_MAX_VALUE.id(), MailConfig.COD_MAX_VALUE.asLong()));

                this.currencyValueField.reset();
                this.currencyValueField.enable();
                this.selectItemButton.enable();
                this.itemAmountField.reset();

                this.sendMailButton.disable();
            } else if (element == this.enableParcelButton) {
                this.enableRemittanceButton.setToggled(false);
                this.enableCODButton.setToggled(false);
                this.currencyValueField.reset();
                this.currencyValueField.disable();
                this.selectedItemWrapper = null;
                this.selectedItem.disableFull();

                this.selectItemButton.enable();
                this.itemAmountField.reset();

                this.sendMailButton.disable();
            }
        }
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {   
        if (!this.hasCurrentCallback()
                && !this.addresseeUsernameField.isDragged()
                && !this.subjectTextField.isDragged()
                && !this.messageTextBoxField.isDragged())
            if (OxygenGUIHelper.isOxygenMenuEnabled()) {
                if (keyCode == MailMenuScreen.MAIL_MENU_ENTRY.getKeyCode())
                    this.screen.close();
            } else if (MailConfig.ENABLE_MAIL_MENU_KEY.asBoolean() 
                    && keyCode == MailManagerClient.instance().getKeyHandler().getMailMenuKeybinding().getKeyCode())
                this.screen.close();
        return super.keyTyped(typedChar, keyCode); 
    }

    public void itemSelected(InventoryItemPanelEntry clicked) {
        clicked.toggle();                    
        this.selectedItemWrapper = clicked.getWrapped();
        int maxAmount = PrivilegesProviderClient.getAsInt(EnumMailPrivilege.PARCEL_MAX_AMOUNT.id(), MailConfig.PARCEL_MAX_AMOUNT.asInt());
        if (maxAmount < 0) 
            maxAmount = clicked.getWrapped().getCachedItemStack().getMaxStackSize();
        this.itemAmountField.setMaxNumber(MathUtils.lesserOfTwo(this.screen.getEqualStackAmount(clicked.getWrapped()), maxAmount));
        this.itemAmountField.reset();
        this.itemAmountField.enable();

        this.selectedItem.setItemStack(
                clicked.getWrapped().getCachedItemStack(), 
                this.screen.getEqualStackAmount(clicked.getWrapped()));
        this.selectedItem.enableFull();

        this.sendMailButton.setEnabled(
                this.screen.allowMailSending
                && !this.addresseeUsernameField.getTypedText().isEmpty() 
                && !this.subjectTextField.getTypedText().isEmpty());
    }

    public EnumMail getMessageType() {
        EnumMail type = EnumMail.LETTER;
        if (this.enableRemittanceButton.isToggled())
            type = EnumMail.REMITTANCE;
        else if (this.enableParcelButton.isToggled())
            type = EnumMail.PARCEL;
        else if (this.enableCODButton.isToggled())
            type = EnumMail.COD;
        return type;
    }

    public void sharedDataSynchronized() {
        this.addresseeUsernameField.load();
        this.addresseeUsernameField.enable();
    }

    public void mailSynchronized() {
        this.calculateButtonsHorizontalPosition();
    }

    public void mailSent(EnumMail type, Attachment attachment, long balance) {
        this.balanceValue.updateValue(this.screen.getIncomingSection().getBalanceValue().getValue());
        if (type == EnumMail.PARCEL || type == EnumMail.COD) {
            this.inventoryLoad.setLoad(this.screen.getIncomingSection().getInventoryLoad().getLoad());
            ((SelectItemCallback) this.selectItemCallback).loadInventoryContent();
        }

        this.addresseeUsernameField.reset();
        this.subjectTextField.reset();
        this.messageTextBoxField.reset();

        this.enableRemittanceButton.setToggled(false);
        this.enableCODButton.setToggled(false);
        this.enableParcelButton.setToggled(false);

        this.currencyValueField.disable();
        this.currencyValueField.reset();
        this.itemAmountField.disable();
        this.itemAmountField.reset();

        this.selectedItemWrapper = null;
        this.selectedItem.disableFull();

        this.selectItemButton.disable();
        this.sendMailButton.disable();
    }

    public void messageRemoved(long messageId) {}

    public void attachmentReceived(long oldMessageId, Mail mail, long balance) {
        this.balanceValue.updateValue(this.screen.getIncomingSection().getBalanceValue().getValue());
        this.inventoryLoad.setLoad(this.screen.getIncomingSection().getInventoryLoad().getLoad());
        ((SelectItemCallback) this.selectItemCallback).loadInventoryContent();
    }

    public OxygenUsernameField getAddresseeUsernameField() {
        return this.addresseeUsernameField;
    }

    public OxygenTextField getSubjectField() {
        return this.subjectTextField;
    }

    public OxygenTextBoxField getMessageBox() {
        return this.messageTextBoxField;
    }

    public OxygenNumberField getCurrencyValueField() {
        return this.currencyValueField;
    }

    public OxygenNumberField getItemAmountField() {
        return this.itemAmountField;
    }

    @Nullable
    public ItemStackWrapper getSelecteItemWrapper() {
        return this.selectedItemWrapper;
    }

    public OxygenInventoryLoad getInventoryLoad() {
        return this.inventoryLoad;
    }

    public OxygenCurrencyValue getBalanceValue() {
        return this.balanceValue;
    }
}
