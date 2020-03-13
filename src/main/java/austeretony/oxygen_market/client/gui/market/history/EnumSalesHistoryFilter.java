package austeretony.oxygen_market.client.gui.market.history;

import austeretony.oxygen_core.client.api.ClientReference;

public enum EnumSalesHistoryFilter {

    PURCHASES("oxygen_market.salesHistorySorter.purchases"),
    SALES("oxygen_market.salesHistorySorter.sales");

    public final String name;

    EnumSalesHistoryFilter(String name) {
        this.name = name;
    }

    public String localizedName() {
        return ClientReference.localize(this.name);
    }
}
