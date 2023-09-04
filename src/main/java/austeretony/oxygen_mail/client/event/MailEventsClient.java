package austeretony.oxygen_mail.client.event;

import austeretony.oxygen_core.client.event.OxygenClientInitializedEvent;
import austeretony.oxygen_mail.client.MailManagerClient;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MailEventsClient {

    @SubscribeEvent
    public void onClientInitialized(OxygenClientInitializedEvent event) {
        MailManagerClient.instance().clientInitialized();
    }
}
