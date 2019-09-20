package austeretony.oxygen_trade.common.main;

import austeretony.oxygen_core.common.EnumValueType;
import austeretony.oxygen_core.common.privilege.PrivilegeRegistry;

public enum EnumTradePrivilege {

    MAX_OFFERS_PER_PLAYER("maxOffersPerPlayer", EnumValueType.INT),
    
    ITEMS_PER_OFFER_MAX_AMOUNT("itemsPerOfferMaxAmount", EnumValueType.INT),
    PRICE_MAX_VALUE("priceMaxValue", EnumValueType.LONG),

    OFFER_CREATION_FEE_PERCENT("offerCreationFeePercent", EnumValueType.INT),
    OFFER_SALE_FEE_PERCENT("offerSaleFeePercent", EnumValueType.INT);

    private final String name;

    private final EnumValueType type;

    EnumTradePrivilege(String name, EnumValueType type) {
        this.name = "trade:" + name;
        this.type = type;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static void register() {
        for (EnumTradePrivilege privilege : EnumTradePrivilege.values())
            PrivilegeRegistry.registerPrivilege(privilege.name, privilege.type);
    }
}
