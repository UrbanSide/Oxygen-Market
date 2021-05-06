package austeretony.oxygen_market.server.event;

import austeretony.oxygen_core.server.event.OxygenServerEvent;
import austeretony.oxygen_market.server.MarketManagerServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MarketEventsServer {

    @SubscribeEvent
    public void onServerStarting(OxygenServerEvent.Starting event) {
        MarketManagerServer.instance().serverStarting();
    }
}
