package austeretony.oxygen_menu.client.settings;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.gui.base.Alignment;
import austeretony.oxygen_core.client.settings.SettingType;
import austeretony.oxygen_core.client.settings.SettingValue;
import austeretony.oxygen_core.client.settings.gui.SettingWidgets;
import austeretony.oxygen_core.common.util.value.ValueType;
import austeretony.oxygen_menu.common.main.MenuMain;

public final class MenuSettings {

    public static final SettingValue
            OXYGEN_MENU_SCREEN_ALIGNMENT = OxygenClient.registerSetting(MenuMain.MOD_ID, SettingType.INTERFACE, "Menu", "alignment",
            ValueType.STRING, "oxygen_menu_screen_alignment", Alignment.RIGHT.toString(), SettingWidgets.screenAlignmentList()),

    ENABLE_OXYGEN_MENU_GUI_OVERLAY = OxygenClient.registerSetting(MenuMain.MOD_ID, SettingType.COMMON, "Menu", "misc",
            ValueType.BOOLEAN, "enable_gui_oxygen_menu_overlay", true, SettingWidgets.checkBox());

    private MenuSettings() {}

    public static void register() {}
}
