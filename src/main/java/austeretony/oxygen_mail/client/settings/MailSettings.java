package austeretony.oxygen_mail.client.settings;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.gui.base.Alignment;
import austeretony.oxygen_core.client.settings.SettingType;
import austeretony.oxygen_core.client.settings.SettingValue;
import austeretony.oxygen_core.client.settings.gui.SettingWidgets;
import austeretony.oxygen_core.common.util.value.ValueType;
import austeretony.oxygen_mail.common.main.MailMain;

public final class MailSettings {

    public static final SettingValue
    MAIL_SCREEN_ALIGNMENT = OxygenClient.registerSetting(MailMain.MOD_ID, SettingType.INTERFACE, "Mail", "alignment",
            ValueType.STRING, "mail_screen_alignment", Alignment.CENTER.toString(), SettingWidgets.screenAlignmentList()),

    ADD_MAIL_SCREEN_TO_OXYGEN_MENU = OxygenClient.registerSetting(MailMain.MOD_ID, SettingType.COMMON, "Mail", "oxygen_menu",
            ValueType.BOOLEAN, "add_mail_screen", true, SettingWidgets.checkBox());

    private MailSettings() {}

    public static void register() {}
}
