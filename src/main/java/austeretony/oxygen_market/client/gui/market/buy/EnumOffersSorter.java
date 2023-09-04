package austeretony.oxygen_market.client.gui.market.buy;

import java.util.Comparator;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_market.client.market.OfferClient;

public enum EnumOffersSorter {

    PURCHASE_PRICE("oxygen_market.offersSorter.purchasePrice", (o1, o2)->(int) (o1.getPrice() - o2.getPrice())),
    UNIT_PRICE("oxygen_market.offersSorter.unitPrice", (o1, o2)->(int) ((float) o1.getPrice() / (float) o1.getAmount() - (float) o2.getPrice() / (float) o2.getAmount())),
    SELLER_NAME("oxygen_market.offersSorter.sellerName", (o1, o2)->(o1.getUsername().compareTo(o2.getUsername()))),
    TIME_LEFT("oxygen_market.offersSorter.timeLeft", (o1, o2)->o1.getId() < o2.getId() ? - 1 : o1.getId() > o2.getId() ? 1 : 0);

    public final String name;

    public final Comparator<OfferClient> comparator;

    EnumOffersSorter(String name, Comparator<OfferClient> comparator) {
        this.name = name;
        this.comparator = comparator;
    }

    public String localizedName() {
        return ClientReference.localize(this.name);
    }
}
