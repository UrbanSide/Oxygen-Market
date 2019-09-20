package austeretony.oxygen_trade.client.input;

import org.lwjgl.input.Keyboard;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_trade.client.TradeManagerClient;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;

public class TradeKeyHandler {

    public static final KeyBinding TRADE_MENU = new KeyBinding("key.oxygen_trade.openTradeMenu", Keyboard.KEY_LBRACKET, "Oxygen");

    public TradeKeyHandler() {
        ClientReference.registerKeyBinding(TRADE_MENU);
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        if (TRADE_MENU.isPressed())
            TradeManagerClient.instance().getTradeMenuManager().openTradeMenu();
    }
}
