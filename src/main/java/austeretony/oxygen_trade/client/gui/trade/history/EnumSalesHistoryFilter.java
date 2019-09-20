package austeretony.oxygen_trade.client.gui.trade.history;

import austeretony.oxygen_core.client.api.ClientReference;

public enum EnumSalesHistoryFilter {

    PURCHASES("oxygen_trade.salesHistorySorter.purchases"),
    SALES("oxygen_trade.salesHistorySorter.sales");

    public final String name;

    EnumSalesHistoryFilter(String name) {
        this.name = name;
    }

    public String localizedName() {
        return ClientReference.localize(this.name);
    }
}
