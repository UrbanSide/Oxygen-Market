package austeretony.oxygen_market.client.event;

import austeretony.oxygen_core.client.event.OxygenClientInitializedEvent;
import austeretony.oxygen_market.client.MarketManagerClient;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MarketEventsClient {

    @SubscribeEvent
    public void onClientInitialized(OxygenClientInitializedEvent event) {
        MarketManagerClient.instance().clientInitialized();
    }
}
