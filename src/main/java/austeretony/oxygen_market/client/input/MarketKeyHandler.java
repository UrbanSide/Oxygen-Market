package austeretony.oxygen_market.client.input;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenGUIHelper;
import austeretony.oxygen_market.client.MenuManagerClient;
import austeretony.oxygen_market.common.config.MarketConfig;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;

public class MarketKeyHandler {

    private KeyBinding tradeMenuKeybinding;

    public MarketKeyHandler() {
        if (MarketConfig.ENABLE_MARKET_MENU_KEY.asBoolean() && !OxygenGUIHelper.isOxygenMenuEnabled())
            ClientReference.registerKeyBinding(this.tradeMenuKeybinding = new KeyBinding("key.oxygen_market.marketMenu", MarketConfig.MARKET_MENU_KEY.asInt(), "Oxygen"));
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        if (this.tradeMenuKeybinding != null && this.tradeMenuKeybinding.isPressed())
            if (MarketConfig.ENABLE_MARKET_MENU_ACCESS_CLIENTSIDE.asBoolean())
                MenuManagerClient.openMarketMenu();
    }

    public KeyBinding getTradeMenuKeybinding() {
        return this.tradeMenuKeybinding;
    }
}
