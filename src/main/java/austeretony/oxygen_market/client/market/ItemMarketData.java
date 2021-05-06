package austeretony.oxygen_market.client.market;

import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_market.client.gui.market.ItemProfitabilityTooltip;

import javax.annotation.Nonnull;
import java.util.Map;

public class ItemMarketData {

    private final ItemStackWrapper stackWrapper;
    private final double averagePrice, minPrice, maxPrice; // for single item
    private final int totalSoldAmount;
    private final Map<Long, Double> transactionIdToUnitPriceMap;

    private ItemProfitabilityTooltip tooltipData;

    public ItemMarketData(ItemStackWrapper stackWrapper, double averagePrice, double minPrice, double maxPrice,
                          int totalSoldAmount, Map<Long, Double> transactionIdToUnitPriceMap) {
        this.stackWrapper = stackWrapper;
        this.averagePrice = averagePrice;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.totalSoldAmount = totalSoldAmount;
        this.transactionIdToUnitPriceMap = transactionIdToUnitPriceMap;
    }

    public ItemStackWrapper getStackWrapper() {
        return stackWrapper;
    }

    public double getAveragePrice() {
        return averagePrice;
    }

    public double getMinPrice() {
        return minPrice;
    }

    public double getMaxPrice() {
        return maxPrice;
    }

    public int getTotalSoldAmount() {
        return totalSoldAmount;
    }

    public Map<Long, Double> getTransactionIdsToUnitPriceMap() {
        return transactionIdToUnitPriceMap;
    }

    public int getTransactionsAmount() {
        return transactionIdToUnitPriceMap.size();
    }

    @Nonnull
    public ItemProfitabilityTooltip getTooltip() {
        if (tooltipData == null) {
            tooltipData = ItemProfitabilityTooltip.create(this);
        }
        return tooltipData;
    }
}
