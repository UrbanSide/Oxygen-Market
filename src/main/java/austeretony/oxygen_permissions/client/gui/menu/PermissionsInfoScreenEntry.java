package austeretony.oxygen_permissions.client.gui.menu;

import austeretony.oxygen_core.client.gui.menu.OxygenMenuEntry;
import austeretony.oxygen_core.client.util.MinecraftClient;
import austeretony.oxygen_permissions.client.settings.PermissionsSettings;
import austeretony.oxygen_permissions.common.config.PermissionsConfig;
import austeretony.oxygen_permissions.common.main.PermissionsMain;
import net.minecraft.util.ResourceLocation;

public class PermissionsInfoScreenEntry implements OxygenMenuEntry {

    private static final ResourceLocation ICON = new ResourceLocation(PermissionsMain.MOD_ID,
            "textures/gui/menu/permissions.png");

    @Override
    public int getScreenId() {
        return PermissionsMain.SCREEN_ID_PERMISSIONS_INFO;
    }

    @Override
    public String getDisplayName() {
        return MinecraftClient.localize("oxygen_permissions.gui.permissions_info.title");
    }

    @Override
    public int getPriority() {
        return 10000;
    }

    @Override
    public ResourceLocation getIconTexture() {
        return ICON;
    }

    @Override
    public int getKeyCode() {
        return PermissionsConfig.PERMISSIONS_INFO_SCREEN_KEY_ID.asInt();
    }

    @Override
    public boolean isValid() {
        return PermissionsSettings.ADD_PERMISSIONS_SCREEN_TO_OXYGEN_MENU.asBoolean();
    }
}
