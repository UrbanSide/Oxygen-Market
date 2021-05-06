package austeretony.oxygen_market.common.config;

import austeretony.oxygen_core.common.config.AbstractConfig;
import austeretony.oxygen_core.common.config.ConfigValue;
import austeretony.oxygen_core.common.config.ConfigValueUtils;
import austeretony.oxygen_market.common.main.MarketMain;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class MarketConfig extends AbstractConfig {

    public static final ConfigValue
            ENABLE_MARKET_SCREEN_KEY = ConfigValueUtils.getBoolean("client", "enable_market_screen_key", true),
            MARKET_SCREEN_KEY = ConfigValueUtils.getInt("client", "market_screen_key", Keyboard.KEY_RBRACKET),

    MARKET_SCREEN_OPERATIONS_TIMEOUT_MILLIS = ConfigValueUtils.getInt("server", "market_screen_operations_timeout_millis", 240000),
            ENABLE_MARKET_ACCESS = ConfigValueUtils.getBoolean("server", "enable_market_access", true, true),
            ENABLE_MARKET_ACCESS_CLIENT_SIDE = ConfigValueUtils.getBoolean("server", "enable_market_access_client_side", true, true),
            ENABLE_SELF_PURCHASE = ConfigValueUtils.getBoolean("server", "enable_self_purchase", true),
            MAX_DEALS_PER_PLAYER = ConfigValueUtils.getInt("server", "max_deals_per_player", 150, true),
            DEAL_MAX_STACK_SIZE = ConfigValueUtils.getInt("server", "deal_max_stack_size", -1, true),
            PRICE_MAX_VALUE = ConfigValueUtils.getLong("server", "price_max_value", 100000L, true),
            DEAL_PLACEMENT_FEE_PERCENT = ConfigValueUtils.getFloat("server", "deal_placement_fee_percent", .01F, true),
            DEAL_SALE_FEE_PERCENT = ConfigValueUtils.getFloat("server", "deal_sale_fee_percent", .05F, true),
            DEAL_EXPIRE_TIME_HOURS = ConfigValueUtils.getInt("server", "deal_expire_time_hours", 240, true),
            ENABLE_SALES_HISTORY = ConfigValueUtils.getBoolean("server", "enable_sales_history", true),
            ENABLE_SALES_HISTORY_SYNC = ConfigValueUtils.getBoolean("server", "enable_sales_history_sync", true, true),
            SALES_HISTORY_ENTRY_EXPIRE_TIME_HOURS = ConfigValueUtils.getInt("server", "sales_history_entry_expire_time_hours", 240);

    @Override
    public String getDomain() {
        return MarketMain.MOD_ID;
    }

    @Override
    public String getVersion() {
        return MarketMain.VERSION_CUSTOM;
    }

    @Override
    public String getFileName() {
        return "market.json";
    }

    @Override
    public void getValues(List<ConfigValue> values) {
        values.add(ENABLE_MARKET_SCREEN_KEY);
        values.add(MARKET_SCREEN_KEY);

        values.add(MARKET_SCREEN_OPERATIONS_TIMEOUT_MILLIS);
        values.add(ENABLE_MARKET_ACCESS);
        values.add(ENABLE_MARKET_ACCESS_CLIENT_SIDE);
        values.add(ENABLE_SELF_PURCHASE);
        values.add(MAX_DEALS_PER_PLAYER);
        values.add(DEAL_MAX_STACK_SIZE);
        values.add(PRICE_MAX_VALUE);
        values.add(DEAL_PLACEMENT_FEE_PERCENT);
        values.add(DEAL_SALE_FEE_PERCENT);
        values.add(DEAL_EXPIRE_TIME_HOURS);
        values.add(ENABLE_SALES_HISTORY);
        values.add(ENABLE_SALES_HISTORY_SYNC);
        values.add(SALES_HISTORY_ENTRY_EXPIRE_TIME_HOURS);
    }
}
