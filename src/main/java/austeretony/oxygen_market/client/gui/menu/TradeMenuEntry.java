package austeretony.oxygen_market.client.gui.menu;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.gui.menu.OxygenMenuEntry;
import austeretony.oxygen_market.client.MenuManagerClient;
import austeretony.oxygen_market.client.settings.EnumMarketClientSetting;
import austeretony.oxygen_market.common.config.MarketConfig;
import austeretony.oxygen_market.common.main.MarketMain;

public class TradeMenuEntry implements OxygenMenuEntry {

    @Override
    public int getId() {
        return MarketMain.MARKET_MENU_SCREEN_ID;
    }

    @Override
    public String getLocalizedName() {
        return ClientReference.localize("oxygen_market.gui.market.title");
    }

    @Override
    public int getKeyCode() {
        return MarketConfig.MARKET_MENU_KEY.asInt();
    }

    @Override
    public boolean isValid() {
        return EnumMarketClientSetting.ADD_MARKET_MENU.get().asBoolean() && MarketConfig.ENABLE_MARKET_MENU_ACCESS_CLIENTSIDE.asBoolean();
    }

    @Override
    public void open() {
        MenuManagerClient.openMarketMenu();
    }
}
