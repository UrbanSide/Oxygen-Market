package austeretony.oxygen_trade.client.gui.trade.buy;

import java.util.Comparator;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_trade.client.PlayerOfferClient;

public enum EnumOffersSorter {

    PURCHASE_PRICE("oxygen_trade.offersSorter.purchasePrice", (o1, o2)->((int) (o1.getPrice() - o2.getPrice()))),
    UNIT_PRICE("oxygen_trade.offersSorter.unitPrice", (o1, o2)->((int) (o1.getPrice() / o1.getAmount())) - ((int) (o2.getPrice() / o2.getAmount()))),
    SELLER_NAME("oxygen_trade.offersSorter.sellerName", (o1, o2)->(o1.getUsername().compareTo(o2.getUsername()))),
    TIME_LEFT("oxygen_trade.offersSorter.timeLeft", (o1, o2)->((int) ((o1.getId() - o2.getId()) / 1_000L)));

    public final String name;

    public final Comparator<PlayerOfferClient> comparator;

    EnumOffersSorter(String name, Comparator<PlayerOfferClient> comparator) {
        this.name = name;
        this.comparator = comparator;
    }

    public String localizedName() {
        return ClientReference.localize(this.name);
    }
}
