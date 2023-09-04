package austeretony.oxygen_permissions.common.config;

import austeretony.oxygen_core.common.config.AbstractConfig;
import austeretony.oxygen_core.common.config.ConfigValue;
import austeretony.oxygen_core.common.config.ConfigValueUtils;
import austeretony.oxygen_permissions.common.main.PermissionsMain;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class PermissionsConfig extends AbstractConfig {

    public static final ConfigValue
            ENABLE_PERMISSIONS_INFO_SCREEN_KEY = ConfigValueUtils.getBoolean("client", "enable_permissions_info_screen_key", true),
            PERMISSIONS_INFO_SCREEN_KEY_ID = ConfigValueUtils.getInt("client", "permissions_info_screen_key_id", Keyboard.KEY_V),

            ENABLE_FORMATTED_CHAT = ConfigValueUtils.getBoolean("server", "enable_formatted_chat", true, true),
            ENABLE_CHAT_FORMATTING_ROLE_SELECTION = ConfigValueUtils.getBoolean("server", "enable_chat_formatting_role_selection", true, true),
            DEFAULT_FORMATTED_CHAT_COLOR = ConfigValueUtils.getString("server", "default_formatted_chat_color", TextFormatting.WHITE.name(), true);

    @Override
    public String getDomain() {
        return PermissionsMain.MOD_ID;
    }

    @Override
    public String getVersion() {
        return PermissionsMain.VERSION_CUSTOM;
    }

    @Override
    public String getFileName() {
        return "permissions.json";
    }

    @Override
    public void getValues(List<ConfigValue> values) {
        values.add(ENABLE_PERMISSIONS_INFO_SCREEN_KEY);
        values.add(PERMISSIONS_INFO_SCREEN_KEY_ID);

        values.add(ENABLE_FORMATTED_CHAT);
        values.add(ENABLE_CHAT_FORMATTING_ROLE_SELECTION);
        values.add(DEFAULT_FORMATTED_CHAT_COLOR);
    }
}
