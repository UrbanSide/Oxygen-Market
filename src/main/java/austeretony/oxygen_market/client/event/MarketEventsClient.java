package austeretony.oxygen_market.client.event;

import austeretony.oxygen_core.client.api.event.OxygenClientInitEvent;
import austeretony.oxygen_market.client.MarketManagerClient;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MarketEventsClient {

    @SubscribeEvent
    public void onClientInit(OxygenClientInitEvent event) {
        MarketManagerClient.instance().worldLoaded();
    }
}
