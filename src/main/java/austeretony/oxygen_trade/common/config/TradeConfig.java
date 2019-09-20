package austeretony.oxygen_trade.common.config;

import java.util.List;

import austeretony.oxygen_core.common.EnumValueType;
import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.api.config.AbstractConfigHolder;
import austeretony.oxygen_core.common.api.config.ConfigValueImpl;
import austeretony.oxygen_core.common.config.ConfigValue;
import austeretony.oxygen_trade.common.main.TradeMain;

public class TradeConfig extends AbstractConfigHolder {

    public static final ConfigValue
    DATA_SAVE_DELAY_MINUTES = new ConfigValueImpl(EnumValueType.INT, "setup", "data_save_delay_minutes"),

    MAX_OFFERS_PER_PLAYER = new ConfigValueImpl(EnumValueType.INT, "main", "max_offers_per_player"),
    ITEMS_PER_OFFER_MAX_AMOUNT = new ConfigValueImpl(EnumValueType.INT, "main", "items_per_offer_max_amount"),
    PRICE_MAX_VALUE = new ConfigValueImpl(EnumValueType.LONG, "main", "price_max_value"),   
    OFFER_EXPIRE_TIME_HOURS = new ConfigValueImpl(EnumValueType.INT, "main", "offer_expire_time_hours"),
    ENABLE_SALES_HISTORY = new ConfigValueImpl(EnumValueType.BOOLEAN, "main", "enable_sales_history"),
    ENABLE_SALES_HISTORY_SYNC = new ConfigValueImpl(EnumValueType.BOOLEAN, "main", "enable_sales_history_sync"),
    SALES_HISTORY_EXPIRE_TIME_HOURS = new ConfigValueImpl(EnumValueType.INT, "main", "sales_history_expire_time_hours"),
    OFFER_CREATION_FEE_PERCENT = new ConfigValueImpl(EnumValueType.INT, "main", "offer_creation_fee_percent"),
    OFFER_SALE_FEE_PERCENT = new ConfigValueImpl(EnumValueType.INT, "main", "offer_sale_fee_percent");

    @Override
    public String getDomain() {
        return TradeMain.MODID;
    }

    @Override
    public String getVersion() {
        return TradeMain.VERSION_CUSTOM;
    }

    @Override
    public String getExternalPath() {
        return CommonReference.getGameFolder() + "/config/oxygen/trade.json";
    }

    @Override
    public String getInternalPath() {
        return "assets/oxygen_trade/trade.json";
    }

    @Override
    public void getValues(List<ConfigValue> values) {
        values.add(DATA_SAVE_DELAY_MINUTES);

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

    @Override
    public boolean sync() {
        return true;
    }
}
