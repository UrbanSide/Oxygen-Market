package austeretony.oxygen_trade.client.settings;

import austeretony.oxygen_core.client.OxygenManagerClient;
import austeretony.oxygen_core.common.EnumValueType;
import austeretony.oxygen_core.common.settings.SettingValue;
import austeretony.oxygen_core.common.settings.SettingValueUtils;

public enum EnumTradeClientSetting {

    //Oxygen Menu

    ADD_TRADE_MENU("menu_add_trade_menu", EnumValueType.BOOLEAN, String.valueOf(true)),

    //Misc

    ENABLE_PROFITABILITY_CALCULATION("enable_profitability_calculation", EnumValueType.BOOLEAN, String.valueOf(true));

    private final String key, baseValue;

    private final EnumValueType type;

    private SettingValue value;

    EnumTradeClientSetting(String key, EnumValueType type, String baseValue) {
        this.key = key;
        this.type = type;
        this.baseValue = baseValue;
    }

    public SettingValue get() {
        if (this.value == null)
            this.value = OxygenManagerClient.instance().getClientSettingManager().getSettingValue(this.key);
        return this.value;
    }

    public static void register() {
        for (EnumTradeClientSetting setting : EnumTradeClientSetting.values())
            OxygenManagerClient.instance().getClientSettingManager().register(SettingValueUtils.getValue(setting.type, setting.key, setting.baseValue));
    }
}
