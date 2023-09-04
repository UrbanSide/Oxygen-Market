package austeretony.oxygen_menu.common.config;

import austeretony.oxygen_core.common.config.AbstractConfig;
import austeretony.oxygen_core.common.config.ConfigValue;
import austeretony.oxygen_core.common.config.ConfigValueUtils;
import austeretony.oxygen_menu.common.main.MenuMain;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class MenuConfig extends AbstractConfig {

    public static final ConfigValue
            ENABLE_OXYGEN_MENU_SCREEN_KEY = ConfigValueUtils.getBoolean("client", "enable_oxygen_menu_screen_key", true),
            OXYGEN_MENU_SCREEN_KEY_ID = ConfigValueUtils.getInt("client", "oxygen_menu_screen_key_id", Keyboard.KEY_GRAVE);

    @Override
    public String getDomain() {
        return MenuMain.MOD_ID;
    }

    @Override
    public String getVersion() {
        return MenuMain.VERSION_CUSTOM;
    }

    @Override
    public String getFileName() {
        return "oxygen_menu.json";
    }

    @Override
    public void getValues(List<ConfigValue> values) {
        values.add(ENABLE_OXYGEN_MENU_SCREEN_KEY);
        values.add(OXYGEN_MENU_SCREEN_KEY_ID);
    }
}
