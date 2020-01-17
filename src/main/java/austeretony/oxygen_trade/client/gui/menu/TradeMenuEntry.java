package austeretony.oxygen_trade.client.gui.menu;

import org.lwjgl.input.Keyboard;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.gui.menu.OxygenMenuEntry;
import austeretony.oxygen_trade.client.TradeManagerClient;
import austeretony.oxygen_trade.client.settings.EnumTradeClientSetting;
import austeretony.oxygen_trade.common.config.TradeConfig;

public class TradeMenuEntry implements OxygenMenuEntry {

    @Override
    public int getId() {
        return 100;
    }

    @Override
    public String getLocalizedName() {
        return ClientReference.localize("oxygen_trade.gui.trade.title");
    }

    @Override
    public int getKeyCode() {
        return Keyboard.KEY_S;
    }

    @Override
    public boolean isValid() {
        return EnumTradeClientSetting.ADD_TRADE_MENU.get().asBoolean() && TradeConfig.ENABLE_TRADE_MENU_ACCESS_CLIENTSIDE.asBoolean();
    }

    @Override
    public void open() {
        TradeManagerClient.instance().getTradeMenuManager().openTradeMenu();
    }
}
