package austeretony.oxygen_trade.common.config;

import java.util.List;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.api.config.AbstractConfig;
import austeretony.oxygen_core.common.config.ConfigValue;
import austeretony.oxygen_core.common.config.ConfigValueUtils;
import austeretony.oxygen_trade.common.main.TradeMain;

public class TradeConfig extends AbstractConfig {

    public static final ConfigValue
    ENABLE_TRADE_MENU_KEY = ConfigValueUtils.getValue("client", "enable_trade_menu_key", true),

    TRADE_MENU_OPERATIONS_TIMEOUT_MILLIS = ConfigValueUtils.getValue("server", "trade_menu_operations_timeout_millis", 240000),
    ENABLE_TRADE_MENU_ACCESS_CLIENTSIDE = ConfigValueUtils.getValue("server", "enable_trade_menu_access_clientside", true, true),
    ENABLE_SELF_PURCHASE = ConfigValueUtils.getValue("server", "enable_self_purchase", true),
    MAX_OFFERS_PER_PLAYER = ConfigValueUtils.getValue("server", "max_offers_per_player", 30, true),
    ITEMS_PER_OFFER_MAX_AMOUNT = ConfigValueUtils.getValue("server", "items_per_offer_max_amount", - 1, true),
    PRICE_MAX_VALUE = ConfigValueUtils.getValue("server", "price_max_value", 100000L, true),   
    OFFER_EXPIRE_TIME_HOURS = ConfigValueUtils.getValue("server", "offer_expire_time_hours", 240, true),
    ENABLE_SALES_HISTORY = ConfigValueUtils.getValue("server", "enable_sales_history", true),
    ENABLE_SALES_HISTORY_SYNC = ConfigValueUtils.getValue("server", "enable_sales_history_sync", true, true),
    SALES_HISTORY_EXPIRE_TIME_HOURS = ConfigValueUtils.getValue("server", "sales_history_expire_time_hours", 240),
    OFFER_CREATION_FEE_PERCENT = ConfigValueUtils.getValue("server", "offer_creation_fee_percent", 1, true),
    OFFER_SALE_FEE_PERCENT = ConfigValueUtils.getValue("server", "offer_sale_fee_percent", 5, true);

    @Override
    public String getDomain() {
        return TradeMain.MODID;
    }

    @Override
    public String getExternalPath() {
        return CommonReference.getGameFolder() + "/config/oxygen/trade.json";
    }

    @Override
    public void getValues(List<ConfigValue> values) {
        values.add(ENABLE_TRADE_MENU_KEY);

        values.add(TRADE_MENU_OPERATIONS_TIMEOUT_MILLIS);
        values.add(ENABLE_TRADE_MENU_ACCESS_CLIENTSIDE);
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
    }
}
