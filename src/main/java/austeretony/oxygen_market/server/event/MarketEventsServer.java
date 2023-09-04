package austeretony.oxygen_market.server.event;

import austeretony.oxygen_core.server.api.event.OxygenPrivilegesLoadedEvent;
import austeretony.oxygen_core.server.api.event.OxygenWorldLoadedEvent;
import austeretony.oxygen_market.common.main.MarketMain;
import austeretony.oxygen_market.server.MarketManagerServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MarketEventsServer {

    @SubscribeEvent
    public void onPrivilegesLoaded(OxygenPrivilegesLoadedEvent event) {
        MarketMain.addDefaultPrivileges();
    }

    @SubscribeEvent
    public void onWorldLoaded(OxygenWorldLoadedEvent event) {
        MarketManagerServer.instance().worldLoaded();
    }
}
