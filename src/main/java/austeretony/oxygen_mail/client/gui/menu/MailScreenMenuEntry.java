package austeretony.oxygen_mail.client.gui.menu;

import austeretony.oxygen_core.client.gui.menu.OxygenMenuEntry;
import austeretony.oxygen_core.client.util.MinecraftClient;
import austeretony.oxygen_mail.client.settings.MailSettings;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.main.MailMain;
import net.minecraft.util.ResourceLocation;

public class MailScreenMenuEntry implements OxygenMenuEntry {

    private static final ResourceLocation ICON = new ResourceLocation(MailMain.MOD_ID,
            "textures/gui/menu/mail.png");

    @Override
    public int getScreenId() {
        return MailMain.SCREEN_ID_MAIL_SCREEN;
    }

    @Override
    public String getDisplayName() {
        return MinecraftClient.localize("oxygen_mail.gui.mail.title");
    }

    @Override
    public int getPriority() {
        return 800;
    }

    @Override
    public ResourceLocation getIconTexture() {
        return ICON;
    }

    @Override
    public int getKeyCode() {
        return MailConfig.MAIL_SCREEN_KEY.asInt();
    }

    @Override
    public boolean isValid() {
        return MailSettings.ADD_MAIL_SCREEN_TO_OXYGEN_MENU.asBoolean() && MailConfig.ENABLE_MAIL_ACCESS_CLIENT_SIDE.asBoolean();
    }
}
