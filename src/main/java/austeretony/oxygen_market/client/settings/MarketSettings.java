package austeretony.oxygen_market.client.settings;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.gui.base.Alignment;
import austeretony.oxygen_core.client.settings.SettingType;
import austeretony.oxygen_core.client.settings.SettingValue;
import austeretony.oxygen_core.client.settings.gui.SettingWidgets;
import austeretony.oxygen_core.common.util.value.ValueType;
import austeretony.oxygen_market.common.main.MarketMain;

public final class MarketSettings {

    public static final SettingValue
    MARKET_SCREEN_ALIGNMENT = OxygenClient.registerSetting(MarketMain.MOD_ID, SettingType.INTERFACE, "Market", "alignment",
            ValueType.STRING, "market_screen_alignment", Alignment.CENTER.toString(), SettingWidgets.screenAlignmentList()),
    MARKET_SCREEN_MAX_DISPLAYED_DEALS = OxygenClient.registerSetting(MarketMain.MOD_ID, SettingType.INTERFACE, "Market", "misc",
            ValueType.INTEGER, "market_screen_max_displayed_deals", 500, SettingWidgets.integerValueSelector(100, 5000)),

    ADD_MARKET_SCREEN_TO_OXYGEN_MENU = OxygenClient.registerSetting(MarketMain.MOD_ID, SettingType.COMMON, "Market", "oxygen_menu",
            ValueType.BOOLEAN, "add_market_screen", true, SettingWidgets.checkBox()),

    ENABLE_DEALS_PROFITABILITY_DISPLAY = OxygenClient.registerSetting(MarketMain.MOD_ID, SettingType.COMMON, "Market", "misc",
            ValueType.BOOLEAN, "enable_deals_profitability_display", true, SettingWidgets.checkBox());

    private MarketSettings() {}

    public static void register() {}
}
