package austeretony.oxygen_mail.client.gui.menu;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.gui.menu.OxygenMenuEntry;
import austeretony.oxygen_mail.client.MailMenuManager;
import austeretony.oxygen_mail.client.settings.EnumMailClientSetting;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.main.MailMain;

public class MailMenuEntry implements OxygenMenuEntry {

    @Override
    public int getId() {
        return MailMain.MAIL_MENU_SCREEN_ID;
    }

    @Override
    public String getLocalizedName() {
        return ClientReference.localize("oxygen_mail.gui.mail.title");
    }

    @Override
    public int getKeyCode() {
        return MailConfig.MAIL_MENU_KEY.asInt();
    }

    @Override
    public boolean isValid() {
        return MailConfig.ENABLE_MAIL_ACCESS_CLIENTSIDE.asBoolean() 
                && EnumMailClientSetting.ADD_MAIL_MENU.get().asBoolean();
    }

    @Override
    public void open() {
        MailMenuManager.openMailMenu();
    }
}
