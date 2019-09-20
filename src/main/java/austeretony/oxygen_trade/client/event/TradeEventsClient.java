package austeretony.oxygen_trade.client.event;

import austeretony.oxygen_core.client.api.event.OxygenClientInitEvent;
import austeretony.oxygen_trade.client.TradeManagerClient;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TradeEventsClient {

    @SubscribeEvent
    public void onClientInit(OxygenClientInitEvent event) {
        TradeManagerClient.instance().worldLoaded();
    }
}
