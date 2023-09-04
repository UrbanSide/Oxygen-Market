package austeretony.oxygen_mail.server.event;

import austeretony.oxygen_core.server.event.OxygenServerEvent;
import austeretony.oxygen_mail.server.MailManagerServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MailEventsServer {

    @SubscribeEvent
    public void onServerStarting(OxygenServerEvent.Starting event) {
        MailManagerServer.instance().serverStarting();
    }
}
