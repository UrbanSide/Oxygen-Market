package austeretony.oxygen_permissions.client.settings;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.gui.base.Alignment;
import austeretony.oxygen_core.client.settings.SettingType;
import austeretony.oxygen_core.client.settings.SettingValue;
import austeretony.oxygen_core.client.settings.gui.SettingWidgets;
import austeretony.oxygen_core.common.util.value.ValueType;
import austeretony.oxygen_permissions.common.main.PermissionsMain;

public final class PermissionsSettings {

    public static final SettingValue
    PERMISSIONS_SCREEN_ALIGNMENT = OxygenClient.registerSetting(PermissionsMain.MOD_ID, SettingType.INTERFACE, "Permissions", "alignment",
            ValueType.STRING, "permissions_screen_alignment", Alignment.CENTER.toString(), SettingWidgets.screenAlignmentList()),

    ADD_PERMISSIONS_SCREEN_TO_OXYGEN_MENU = OxygenClient.registerSetting(PermissionsMain.MOD_ID, SettingType.COMMON, "Permissions", "oxygen_menu",
            ValueType.BOOLEAN, "add_permissions_screen", true, SettingWidgets.checkBox());

    private PermissionsSettings() {}

    public static void register() {}
}
