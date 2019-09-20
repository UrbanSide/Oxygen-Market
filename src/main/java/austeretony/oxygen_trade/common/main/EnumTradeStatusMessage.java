package austeretony.oxygen_trade.common.main;

import austeretony.oxygen_core.client.api.ClientReference;

public enum EnumTradeStatusMessage {

    ITEM_PURCHASED("itemPurchased"),
    ITEM_PURCHASE_FAILED("itemPurchaseFailed"),
    OFFER_CREATED("offerCreated"),
    OFFER_CREATION_FAILED("offerCreationFailed"),
    ITEM_DAMAGED("itemDamaged"),
    ITEM_BLACKLISTED("itemBlacklisted"),
    OFFER_CANCELED("offerCancelled"),
    OFFER_CANCELLATION_FAILED("offerCancellationFailed");

    private final String status;

    EnumTradeStatusMessage(String status) {
        this.status = "oxygen_trade.status." + status;
    }

    public String localizedName() {
        return ClientReference.localize(this.status);
    }
}
