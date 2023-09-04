package austeretony.oxygen_market.client.gui.history;

import java.util.Comparator;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_market.client.market.SalesHistoryEntryClient;

public enum EnumHistorySorter {

    TIME("oxygen_market.historySorter.time", (e1, e2)->e2.getId() < e1.getId() ? - 1 : e2.getId() > e1.getId() ? 1 : 0),
    PURCHASE_PRICE("oxygen_market.offersSorter.purchasePrice", (e1, e2)->(int) (e1.getPrice() - e2.getPrice())),
    UNIT_PRICE("oxygen_market.offersSorter.unitPrice", (e1, e2)->(int) ((float) e1.getPrice() / (float) e1.getAmount() - (float) e2.getPrice() / (float) e2.getAmount())),
    BUYER_NAME("oxygen_market.historySorter.buyerName", (e1, e2)->(e1.getBuyerUsername().compareTo(e2.getBuyerUsername()))),
    SELLER_NAME("oxygen_market.offersSorter.sellerName", (e1, e2)->(e1.getSellerUsername().compareTo(e2.getSellerUsername())));

    public final String name;

    public final Comparator<SalesHistoryEntryClient> comparator;

    EnumHistorySorter(String name, Comparator<SalesHistoryEntryClient> comparator) {
        this.name = name;
        this.comparator = comparator;
    }

    public String localizedName() {
        return ClientReference.localize(this.name);
    }
}
