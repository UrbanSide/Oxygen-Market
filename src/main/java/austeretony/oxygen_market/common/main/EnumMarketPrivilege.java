package austeretony.oxygen_market.common.main;

import austeretony.oxygen_core.common.EnumValueType;
import austeretony.oxygen_core.common.privilege.PrivilegeRegistry;

public enum EnumMarketPrivilege {

    MAX_OFFERS_PER_PLAYER("market:maxOffersPerPlayer", 1000, EnumValueType.INT),
    MARKET_ACCESS("market:marketAccess", 1001, EnumValueType.BOOLEAN),
    SALES_HISTORY_ACCESS("market:salesHistoryAccess", 1002, EnumValueType.BOOLEAN),
    MARKET_MENU_OPERATOR_OPTIONS("market:marketMenuOperatorOptions", 1003, EnumValueType.BOOLEAN),

    ITEMS_PER_OFFER_MAX_AMOUNT("market:itemsPerOfferMaxAmount", 1010, EnumValueType.INT),
    PRICE_MAX_VALUE("market:priceMaxValue", 1011, EnumValueType.LONG),

    OFFER_CREATION_FEE_PERCENT("market:offerCreationFeePercent", 1020, EnumValueType.INT),
    OFFER_SALE_FEE_PERCENT("market:offerSaleFeePercent", 1021, EnumValueType.INT);

    private final String name;

    private final int id;

    private final EnumValueType type;

    EnumMarketPrivilege(String name, int id, EnumValueType type) {
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
        for (EnumMarketPrivilege privilege : values())
            PrivilegeRegistry.registerPrivilege(privilege.name, privilege.id, privilege.type);
    }
}
