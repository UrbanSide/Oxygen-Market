package austeretony.oxygen_market.common.main;

import austeretony.oxygen_core.client.api.ClientReference;

public enum EnumMarketStatusMessage {

    ITEM_PURCHASED("itemPurchased"),
    ITEM_PURCHASE_FAILED("itemPurchaseFailed"),
    OFFER_CREATED("offerCreated"),
    OFFER_CREATION_FAILED("offerCreationFailed"),
    ITEM_DAMAGED("itemDamaged"),
    ITEM_BLACKLISTED("itemBlacklisted"),
    OFFER_CANCELED("offerCancelled"),
    OFFER_CANCELLATION_FAILED("offerCancellationFailed");

    private final String status;

    EnumMarketStatusMessage(String status) {
        this.status = "oxygen_market.status.message." + status;
    }

    public String localizedName() {
        return ClientReference.localize(this.status);
    }
}
