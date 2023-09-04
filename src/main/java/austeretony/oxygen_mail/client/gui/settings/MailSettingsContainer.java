package austeretony.oxygen_mail.client.gui.settings;

import austeretony.alternateui.screen.framework.GUIElementsFramework;
import austeretony.oxygen_core.client.OxygenManagerClient;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.gui.elements.OxygenCheckBoxButton;
import austeretony.oxygen_core.client.gui.elements.OxygenDropDownList;
import austeretony.oxygen_core.client.gui.elements.OxygenDropDownList.OxygenDropDownListWrapperEntry;
import austeretony.oxygen_core.client.gui.elements.OxygenTextLabel;
import austeretony.oxygen_core.client.gui.settings.ElementsContainer;
import austeretony.oxygen_core.client.gui.settings.gui.callback.SetColorCallback;
import austeretony.oxygen_core.client.gui.settings.gui.callback.SetKeyCallback;
import austeretony.oxygen_core.client.gui.settings.gui.callback.SetOffsetCallback;
import austeretony.oxygen_core.client.gui.settings.gui.callback.SetScaleCallback;
import austeretony.oxygen_mail.client.settings.EnumMailClientSetting;
import austeretony.oxygen_mail.client.settings.gui.EnumMailGUISetting;

public class MailSettingsContainer implements ElementsContainer {

    //common

    private OxygenCheckBoxButton addMailMenuButton;

    //interface

    private OxygenDropDownList alignmentMailMenu;


    @Override
    public String getLocalizedName() {
        return ClientReference.localize("oxygen_mail.gui.settings.module.mail");
    }

    @Override
    public boolean hasCommonSettings() {
        return true;
    }

    @Override
    public boolean hasGUISettings() {
        return true;
    }

    @Override
    public void addCommon(GUIElementsFramework framework) {
        framework.addElement(new OxygenTextLabel(68, 25, ClientReference.localize("oxygen_core.gui.settings.option.oxygenMenu"), EnumBaseGUISetting.TEXT_SCALE.get().asFloat() - 0.05F, EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        //add mail menu to menu
        framework.addElement(new OxygenTextLabel(78, 34, ClientReference.localize("oxygen_mail.gui.settings.option.addMailMenu"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.1F, EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));

        framework.addElement(this.addMailMenuButton = new OxygenCheckBoxButton(68, 29));
        this.addMailMenuButton.setToggled(EnumMailClientSetting.ADD_MAIL_MENU.get().asBoolean());
        this.addMailMenuButton.setClickListener((mouseX, mouseY, mouseButton)->{
            EnumMailClientSetting.ADD_MAIL_MENU.get().setValue(String.valueOf(this.addMailMenuButton.isToggled()));
            OxygenManagerClient.instance().getClientSettingManager().changed();
        });
    }

    @Override
    public void addGUI(GUIElementsFramework framework) {
        framework.addElement(new OxygenTextLabel(68, 25, ClientReference.localize("oxygen_core.gui.settings.option.alignment"), EnumBaseGUISetting.TEXT_SCALE.get().asFloat() - 0.05F, EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        //mail menu alignment

        String currAlignmentStr;
        switch (EnumMailGUISetting.MAIL_MENU_ALIGNMENT.get().asInt()) {
        case - 1: 
            currAlignmentStr = ClientReference.localize("oxygen_core.alignment.left");
            break;
        case 0:
            currAlignmentStr = ClientReference.localize("oxygen_core.alignment.center");
            break;
        case 1:
            currAlignmentStr = ClientReference.localize("oxygen_core.alignment.right");
            break;    
        default:
            currAlignmentStr = ClientReference.localize("oxygen_core.alignment.center");
            break;
        }
        framework.addElement(this.alignmentMailMenu = new OxygenDropDownList(68, 35, 55, currAlignmentStr));
        this.alignmentMailMenu.addElement(new OxygenDropDownListWrapperEntry(- 1, ClientReference.localize("oxygen_core.alignment.left")));
        this.alignmentMailMenu.addElement(new OxygenDropDownListWrapperEntry<Integer>(0, ClientReference.localize("oxygen_core.alignment.center")));
        this.alignmentMailMenu.addElement(new OxygenDropDownListWrapperEntry<Integer>(1, ClientReference.localize("oxygen_core.alignment.right")));

        this.alignmentMailMenu.<OxygenDropDownListWrapperEntry<Integer>>setElementClickListener((element)->{
            EnumMailGUISetting.MAIL_MENU_ALIGNMENT.get().setValue(String.valueOf(element.getWrapped()));
            OxygenManagerClient.instance().getClientSettingManager().changed();
        });

        framework.addElement(new OxygenTextLabel(68, 33, ClientReference.localize("oxygen_mail.gui.settings.option.alignmentMailMenu"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.1F, EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
    }

    @Override
    public void resetCommon() {
        //add mail menu to menu
        this.addMailMenuButton.setToggled(true);
        EnumMailClientSetting.ADD_MAIL_MENU.get().reset();    

        OxygenManagerClient.instance().getClientSettingManager().changed();
    }

    @Override
    public void resetGUI() {
        //mail menu alignment
        this.alignmentMailMenu.setDisplayText(ClientReference.localize("oxygen_core.alignment.center"));
        EnumMailGUISetting.MAIL_MENU_ALIGNMENT.get().reset();

        OxygenManagerClient.instance().getClientSettingManager().changed();
    }

    @Override
    public void initSetColorCallback(SetColorCallback callback) {}

    @Override
    public void initSetScaleCallback(SetScaleCallback callback) {}

    @Override
    public void initSetOffsetCallback(SetOffsetCallback callback) {}

    @Override
    public void initSetKeyCallback(SetKeyCallback callback) {}
}
