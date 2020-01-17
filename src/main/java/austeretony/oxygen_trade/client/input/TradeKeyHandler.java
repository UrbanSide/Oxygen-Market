package austeretony.oxygen_trade.client.input;

import org.lwjgl.input.Keyboard;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenGUIHelper;
import austeretony.oxygen_trade.client.TradeManagerClient;
import austeretony.oxygen_trade.common.config.TradeConfig;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;

public class TradeKeyHandler {

    private KeyBinding tradeMenuKeybinding;

    public TradeKeyHandler() {
        if (TradeConfig.ENABLE_TRADE_MENU_KEY.asBoolean() && !OxygenGUIHelper.isOxygenMenuEnabled())
            ClientReference.registerKeyBinding(this.tradeMenuKeybinding = new KeyBinding("key.oxygen_trade.tradeMenu", Keyboard.KEY_RBRACKET, "Oxygen"));
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        if (this.tradeMenuKeybinding != null && this.tradeMenuKeybinding.isPressed())
            if (TradeConfig.ENABLE_TRADE_MENU_ACCESS_CLIENTSIDE.asBoolean())
                TradeManagerClient.instance().getTradeMenuManager().openTradeMenu();
    }

    public KeyBinding getTradeMenuKeybinding() {
        return this.tradeMenuKeybinding;
    }
}
