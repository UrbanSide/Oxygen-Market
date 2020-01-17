package austeretony.oxygen_trade.common.main;

import austeretony.oxygen_core.common.EnumValueType;
import austeretony.oxygen_core.common.privilege.PrivilegeRegistry;

public enum EnumTradePrivilege {

    MAX_OFFERS_PER_PLAYER("trade:maxOffersPerPlayer", 1000, EnumValueType.INT),
    MARKET_ACCESS("trade:marketAccess", 1001, EnumValueType.BOOLEAN),
    SALES_HISTORY_ACCESS("trade:salesHistoryAccess", 1002, EnumValueType.BOOLEAN),
    TRADE_MENU_OPERATOR_OPTIONS("trade:tradeMenuOperatorOptions", 1003, EnumValueType.BOOLEAN),

    ITEMS_PER_OFFER_MAX_AMOUNT("trade:itemsPerOfferMaxAmount", 1010, EnumValueType.INT),
    PRICE_MAX_VALUE("trade:priceMaxValue", 1011, EnumValueType.LONG),

    OFFER_CREATION_FEE_PERCENT("trade:offerCreationFeePercent", 1020, EnumValueType.INT),
    OFFER_SALE_FEE_PERCENT("trade:offerSaleFeePercent", 1021, EnumValueType.INT);

    private final String name;

    private final int id;

    private final EnumValueType type;

    EnumTradePrivilege(String name, int id, EnumValueType type) {
        this.name = name;
        this.id = id;
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public int id() {
        return id;
    }

    public static void register() {
        for (EnumTradePrivilege privilege : EnumTradePrivilege.values())
            PrivilegeRegistry.registerPrivilege(privilege.name, privilege.id, privilege.type);
    }
}
