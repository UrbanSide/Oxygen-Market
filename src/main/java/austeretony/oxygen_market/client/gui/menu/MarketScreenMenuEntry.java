package austeretony.oxygen_market.client.gui.menu;

import austeretony.oxygen_core.client.gui.menu.OxygenMenuEntry;
import austeretony.oxygen_core.client.util.MinecraftClient;
import austeretony.oxygen_market.client.settings.MarketSettings;
import austeretony.oxygen_market.common.config.MarketConfig;
import austeretony.oxygen_market.common.main.MarketMain;
import net.minecraft.util.ResourceLocation;

public class MarketScreenMenuEntry implements OxygenMenuEntry {

    private static final ResourceLocation ICON = new ResourceLocation(MarketMain.MOD_ID,
            "textures/gui/menu/market.png");

    @Override
    public int getScreenId() {
        return MarketMain.SCREEN_ID_MARKET;
    }

    @Override
    public String getDisplayName() {
        return MinecraftClient.localize("oxygen_market.gui.market.title");
    }

    @Override
    public int getPriority() {
        return 1700;
    }

    @Override
    public ResourceLocation getIconTexture() {
        return ICON;
    }

    @Override
    public int getKeyCode() {
        return MarketConfig.MARKET_SCREEN_KEY.asInt();
    }

    @Override
    public boolean isValid() {
        return MarketSettings.ADD_MARKET_SCREEN_TO_OXYGEN_MENU.asBoolean()
                && MarketConfig.ENABLE_MARKET_ACCESS_CLIENT_SIDE.asBoolean();
    }
}
