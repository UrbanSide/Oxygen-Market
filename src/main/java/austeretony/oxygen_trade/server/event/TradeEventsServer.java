package austeretony.oxygen_trade.server.event;

import austeretony.oxygen_core.server.api.event.OxygenPrivilegesLoadedEvent;
import austeretony.oxygen_core.server.api.event.OxygenWorldLoadedEvent;
import austeretony.oxygen_trade.common.main.TradeMain;
import austeretony.oxygen_trade.server.TradeManagerServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TradeEventsServer {

    @SubscribeEvent
    public void onPrivilegesLoaded(OxygenPrivilegesLoadedEvent event) {
        TradeMain.addDefaultPrivileges();
    }

    @SubscribeEvent
    public void onWorldLoaded(OxygenWorldLoadedEvent event) {
        TradeManagerServer.instance().worldLoaded();
    }
}
