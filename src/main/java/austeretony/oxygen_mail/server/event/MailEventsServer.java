package austeretony.oxygen_mail.server.event;

import austeretony.oxygen_core.server.api.event.OxygenPrivilegesLoadedEvent;
import austeretony.oxygen_core.server.api.event.OxygenWorldLoadedEvent;
import austeretony.oxygen_mail.common.main.MailMain;
import austeretony.oxygen_mail.server.MailManagerServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MailEventsServer {

    @SubscribeEvent
    public void onPrivilegesLoaded(OxygenPrivilegesLoadedEvent event) {
        MailMain.addDefaultPrivileges();
    }

    @SubscribeEvent
    public void onWorldLoaded(OxygenWorldLoadedEvent event) {
        MailManagerServer.instance().worldLoaded();
    }
}
