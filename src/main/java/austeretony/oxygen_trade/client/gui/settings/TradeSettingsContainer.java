package austeretony.oxygen_trade.client.gui.settings;

import austeretony.alternateui.screen.framework.GUIElementsFramework;
import austeretony.oxygen_core.client.OxygenManagerClient;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.gui.elements.OxygenCheckBoxButton;
import austeretony.oxygen_core.client.gui.elements.OxygenDropDownList;
import austeretony.oxygen_core.client.gui.elements.OxygenDropDownList.OxygenDropDownListEntry;
import austeretony.oxygen_core.client.gui.elements.OxygenTextLabel;
import austeretony.oxygen_core.client.gui.settings.ElementsContainer;
import austeretony.oxygen_core.client.gui.settings.gui.callback.SetColorCallback;
import austeretony.oxygen_core.client.gui.settings.gui.callback.SetKeyCallback;
import austeretony.oxygen_core.client.gui.settings.gui.callback.SetOffsetCallback;
import austeretony.oxygen_core.client.gui.settings.gui.callback.SetScaleCallback;
import austeretony.oxygen_trade.client.settings.EnumTradeClientSetting;
import austeretony.oxygen_trade.client.settings.gui.EnumTradeGUISetting;

public class TradeSettingsContainer implements ElementsContainer {

    //common

    private OxygenCheckBoxButton addTradeMenuButton, profitabilityCalculationButton;

    //interface

    private OxygenDropDownList alignmentTradeMenu;


    @Override
    public String getLocalizedName() {
        return ClientReference.localize("oxygen_trade.gui.settings.module.trade");
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

        //add trade menu to menu
        framework.addElement(new OxygenTextLabel(78, 34, ClientReference.localize("oxygen_trade.gui.settings.option.addTradeMenu"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.1F, EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));

        framework.addElement(this.addTradeMenuButton = new OxygenCheckBoxButton(68, 29));
        this.addTradeMenuButton.setToggled(EnumTradeClientSetting.ADD_TRADE_MENU.get().asBoolean());
        this.addTradeMenuButton.setClickListener((mouseX, mouseY, mouseButton)->{
            EnumTradeClientSetting.ADD_TRADE_MENU.get().setValue(String.valueOf(this.addTradeMenuButton.isToggled()));
            OxygenManagerClient.instance().getClientSettingManager().changed();
        });

        framework.addElement(new OxygenTextLabel(68, 45, ClientReference.localize("oxygen_core.gui.settings.option.misc"), EnumBaseGUISetting.TEXT_SCALE.get().asFloat() - 0.05F, EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        //profitability calculation
        framework.addElement(new OxygenTextLabel(78, 54, ClientReference.localize("oxygen_trade.gui.settings.option.profitabilityCalculation"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.1F, EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));

        framework.addElement(this.profitabilityCalculationButton = new OxygenCheckBoxButton(68, 49));
        this.profitabilityCalculationButton.setToggled(EnumTradeClientSetting.ENABLE_PROFITABILITY_CALCULATION.get().asBoolean());
        this.profitabilityCalculationButton.setClickListener((mouseX, mouseY, mouseButton)->{
            EnumTradeClientSetting.ENABLE_PROFITABILITY_CALCULATION.get().setValue(String.valueOf(this.profitabilityCalculationButton.isToggled()));
            OxygenManagerClient.instance().getClientSettingManager().changed();
        });
    }

    @Override
    public void addGUI(GUIElementsFramework framework) {
        framework.addElement(new OxygenTextLabel(68, 25, ClientReference.localize("oxygen_core.gui.settings.option.alignment"), EnumBaseGUISetting.TEXT_SCALE.get().asFloat() - 0.05F, EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        //trade menu alignment

        String currAlignmentStr;
        switch (EnumTradeGUISetting.TRADE_MENU_ALIGNMENT.get().asInt()) {
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
        framework.addElement(this.alignmentTradeMenu = new OxygenDropDownList(68, 35, 55, currAlignmentStr));
        this.alignmentTradeMenu.addElement(new OxygenDropDownListEntry<Integer>(- 1, ClientReference.localize("oxygen_core.alignment.left")));
        this.alignmentTradeMenu.addElement(new OxygenDropDownListEntry<Integer>(0, ClientReference.localize("oxygen_core.alignment.center")));
        this.alignmentTradeMenu.addElement(new OxygenDropDownListEntry<Integer>(1, ClientReference.localize("oxygen_core.alignment.right")));

        this.alignmentTradeMenu.<OxygenDropDownListEntry<Integer>>setClickListener((element)->{
            EnumTradeGUISetting.TRADE_MENU_ALIGNMENT.get().setValue(String.valueOf(element.index));
            OxygenManagerClient.instance().getClientSettingManager().changed();
        });

        framework.addElement(new OxygenTextLabel(68, 33, ClientReference.localize("oxygen_trade.gui.settings.option.alignmentTradeMenu"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.1F, EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
    }

    @Override
    public void resetCommon() {
        //add trade menu to menu
        this.addTradeMenuButton.setToggled(false);
        EnumTradeClientSetting.ADD_TRADE_MENU.get().reset();    

        //profitability calculation
        this.profitabilityCalculationButton.setToggled(false);
        EnumTradeClientSetting.ENABLE_PROFITABILITY_CALCULATION.get().reset();    

        OxygenManagerClient.instance().getClientSettingManager().changed();
    }

    @Override
    public void resetGUI() {
        //trade menu alignment
        this.alignmentTradeMenu.setDisplayText(ClientReference.localize("oxygen_core.alignment.center"));
        EnumTradeGUISetting.TRADE_MENU_ALIGNMENT.get().reset();

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

