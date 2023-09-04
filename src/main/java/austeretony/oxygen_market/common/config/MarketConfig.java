package austeretony.oxygen_market.common.config;

import java.util.List;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.api.config.AbstractConfig;
import austeretony.oxygen_core.common.config.ConfigValue;
import austeretony.oxygen_core.common.config.ConfigValueUtils;
import austeretony.oxygen_market.common.main.MarketMain;

public class MarketConfig extends AbstractConfig {

    public static final ConfigValue
    ENABLE_MARKET_MENU_KEY = ConfigValueUtils.getValue("client", "enable_market_menu_key", true),
    MARKET_MENU_KEY = ConfigValueUtils.getValue("client", "market_menu_key", 27),

    MARKET_MENU_OPERATIONS_TIMEOUT_MILLIS = ConfigValueUtils.getValue("server", "market_menu_operations_timeout_millis", 240000),
    DISCORD_WEBHOOK_URL = ConfigValueUtils.getValue("server", "DiscordWebHookUrl", "null"),
    ENABLE_MARKET_MENU_ACCESS_CLIENTSIDE = ConfigValueUtils.getValue("server", "enable_market_menu_access_clientside", true, true),
    ENABLE_SELF_PURCHASE = ConfigValueUtils.getValue("server", "enable_self_purchase", true),
    MAX_OFFERS_PER_PLAYER = ConfigValueUtils.getValue("server", "max_offers_per_player", 30, true),
    ITEMS_PER_OFFER_MAX_AMOUNT = ConfigValueUtils.getValue("server", "items_per_offer_max_amount", - 1, true),
    PRICE_MAX_VALUE = ConfigValueUtils.getValue("server", "price_max_value", 100000L, true),   
    OFFER_EXPIRE_TIME_HOURS = ConfigValueUtils.getValue("server", "offer_expire_time_hours", 240, true),
    ENABLE_SALES_HISTORY = ConfigValueUtils.getValue("server", "enable_sales_history", true),
    ENABLE_SALES_HISTORY_SYNC = ConfigValueUtils.getValue("server", "enable_sales_history_sync", true, true),
    SALES_HISTORY_EXPIRE_TIME_HOURS = ConfigValueUtils.getValue("server", "sales_history_expire_time_hours", 240),
    OFFER_CREATION_FEE_PERCENT = ConfigValueUtils.getValue("server", "offer_creation_fee_percent", 1, true),
    OFFER_SALE_FEE_PERCENT = ConfigValueUtils.getValue("server", "offer_sale_fee_percent", 5, true),
    ADVANCED_LOGGING = ConfigValueUtils.getValue("server", "advanced_logging", false);

    @Override
    public String getDomain() {
        return MarketMain.MODID;
    }

    @Override
    public String getVersion() {
        return MarketMain.VERSION_CUSTOM;
    }

    @Override
    public String getExternalPath() {
        return CommonReference.getGameFolder() + "/config/oxygen/market.json";
    }

    @Override
    public void getValues(List<ConfigValue> values) {
        values.add(ENABLE_MARKET_MENU_KEY);
        values.add(MARKET_MENU_KEY);

        values.add(MARKET_MENU_OPERATIONS_TIMEOUT_MILLIS);
        values.add(ENABLE_MARKET_MENU_ACCESS_CLIENTSIDE);
        values.add(ENABLE_SELF_PURCHASE);
        values.add(MAX_OFFERS_PER_PLAYER);
        values.add(ITEMS_PER_OFFER_MAX_AMOUNT);
        values.add(PRICE_MAX_VALUE);
        values.add(OFFER_EXPIRE_TIME_HOURS);
        values.add(ENABLE_SALES_HISTORY);
        values.add(ENABLE_SALES_HISTORY_SYNC);
        values.add(SALES_HISTORY_EXPIRE_TIME_HOURS);
        values.add(OFFER_CREATION_FEE_PERCENT);
        values.add(OFFER_SALE_FEE_PERCENT);
        values.add(DISCORD_WEBHOOK_URL);
        values.add(ADVANCED_LOGGING);
    }
}
