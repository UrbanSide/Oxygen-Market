package austeretony.oxygen_market.common.main;

import austeretony.oxygen_core.common.privileges.PrivilegeRegistry;
import austeretony.oxygen_core.common.util.value.ValueType;

public final class MarketPrivileges {

    public static final PrivilegeRegistry.Entry
            MAX_DEALS_PER_PLAYER = PrivilegeRegistry.register(1200, "market:max_deals_per_player", ValueType.INTEGER),
            MARKET_ACCESS = PrivilegeRegistry.register(1201, "market:market_access", ValueType.BOOLEAN),
            SALES_HISTORY_ACCESS = PrivilegeRegistry.register(1202, "market:sales_history_access", ValueType.BOOLEAN),

    DEAL_MAX_STACK_SIZE = PrivilegeRegistry.register(1210, "market:deal_max_stack_size", ValueType.INTEGER),
            PRICE_MAX_VALUE = PrivilegeRegistry.register(1211, "market:price_max_value", ValueType.LONG),

    DEAL_PLACEMENT_FEE_PERCENT = PrivilegeRegistry.register(1220, "market:deal_placement_fee_percent", ValueType.FLOAT),
            DEAL_SALE_FEE_PERCENT = PrivilegeRegistry.register(1221, "market:deal_sale_fee_percent", ValueType.FLOAT);

    private MarketPrivileges() {}

    public static void register() {}
}
