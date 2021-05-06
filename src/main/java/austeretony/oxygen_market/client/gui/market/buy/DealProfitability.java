package austeretony.oxygen_market.client.gui.market.buy;

import austeretony.oxygen_market.client.market.ItemMarketData;
import austeretony.oxygen_market.client.market.Profitability;

import javax.annotation.Nullable;

public class DealProfitability {

    private final long dealId;
    private final Profitability profitability;
    private final String profitabilityPercent;
    @Nullable
    private final ItemMarketData itemMarketData;

    public DealProfitability(long dealId, Profitability profitability, String profitabilityPercentStr,
                             @Nullable ItemMarketData itemMarketData) {
        this.dealId = dealId;
        this.profitability = profitability;
        this.profitabilityPercent = profitabilityPercentStr;
        this.itemMarketData = itemMarketData;
    }

    public long getDealId() {
        return dealId;
    }

    public Profitability getProfitability() {
        return profitability;
    }

    public String getDisplayProfitabilityPercent() {
        return profitabilityPercent;
    }

    @Nullable
    public ItemMarketData getItemMarketData() {
        return itemMarketData;
    }
}
