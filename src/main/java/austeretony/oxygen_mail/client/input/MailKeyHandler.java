package austeretony.oxygen_mail.client.input;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenGUIHelper;
import austeretony.oxygen_mail.client.MailMenuManager;
import austeretony.oxygen_mail.common.config.MailConfig;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;

public class MailKeyHandler {

    private KeyBinding mailMenuKeybinding;

    public MailKeyHandler() {        
        if (MailConfig.ENABLE_MAIL_MENU_KEY.asBoolean() && !OxygenGUIHelper.isOxygenMenuEnabled())
            ClientReference.registerKeyBinding(this.mailMenuKeybinding = new KeyBinding("key.oxygen_mail.mailMenu", MailConfig.MAIL_MENU_KEY.asInt(), "Oxygen"));
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {        
        if (this.mailMenuKeybinding != null && this.mailMenuKeybinding.isPressed())
            if (MailConfig.ENABLE_MAIL_ACCESS_CLIENTSIDE.asBoolean())
                MailMenuManager.openMailMenu();
    }

    public KeyBinding getMailMenuKeybinding() {
        return this.mailMenuKeybinding;
    }
}
