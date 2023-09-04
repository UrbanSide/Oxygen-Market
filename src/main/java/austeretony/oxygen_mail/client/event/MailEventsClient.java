package austeretony.oxygen_mail.client.event;

import austeretony.oxygen_core.client.api.event.OxygenClientInitEvent;
import austeretony.oxygen_mail.client.MailManagerClient;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MailEventsClient {

    @SubscribeEvent
    public void onClientInit(OxygenClientInitEvent event) {
        MailManagerClient.instance().worldLoaded();
    }
}