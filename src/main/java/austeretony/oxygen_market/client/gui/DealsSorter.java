package austeretony.oxygen_market.client.gui;

import austeretony.oxygen_core.client.util.MinecraftClient;
import austeretony.oxygen_market.client.gui.market.buy.CombinedDealsEntry;

import java.util.Comparator;

public enum DealsSorter {

    PURCHASE_PRICE_MIN_TO_MAX("purchase_price_min_to_max", Comparator.comparingLong(e -> e.getDeal().getPrice())),
    PURCHASE_PRICE_MAX_TO_MIN("purchase_price_max_to_min", Comparator.<CombinedDealsEntry>comparingLong(e -> e.getDeal().getPrice()).reversed()),
    UNIT_PRICE_MIN_TO_MAX("unit_price_min_to_max", Comparator.comparingDouble(e -> e.getDeal().getUnitPrice())),
    UNIT_PRICE_MAX_TO_MIN("unit_price_max_to_min", Comparator.<CombinedDealsEntry>comparingDouble(e -> e.getDeal().getUnitPrice()).reversed()),
    SELLER_NAME("seller_name", Comparator.comparing(e -> e.getDeal().getSellerUsername())),
    TIME_LEFT_MIN_TO_MAX("time_left_min_to_max", Comparator.comparingLong(CombinedDealsEntry::getId)),
    TIME_LEFT_MAX_TO_MIN("time_left_max_to_min", Comparator.comparingLong(CombinedDealsEntry::getId).reversed());

    public final String name;
    public final Comparator<CombinedDealsEntry> comparator;

    DealsSorter(String name, Comparator<CombinedDealsEntry> comparator) {
        this.name = name;
        this.comparator = comparator;
    }

    public String getDisplayName() {
        return MinecraftClient.localize("oxygen_market.deals_sorter." + name);
    }
}
