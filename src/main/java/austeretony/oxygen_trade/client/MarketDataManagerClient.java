package austeretony.oxygen_trade.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_trade.client.settings.EnumTradeClientSetting;

public class MarketDataManagerClient {

    private final TradeManagerClient manager;

    private final Map<ItemStackWrapper, ItemStackMarketData> marketData = new ConcurrentHashMap<>();

    protected MarketDataManagerClient(TradeManagerClient manager) {
        this.manager = manager;
    }

    public void updateMarketData() {
        this.marketData.clear();
        if (EnumTradeClientSetting.ENABLE_PROFITABILITY_CALCULATION.get().asBoolean()) {
            Map<ItemStackWrapper, Set<HistoryEntryData>> historyData = new HashMap<>();

            for (SalesHistoryEntryClient entry : this.manager.getSalesHistoryContainer().getEntries()) {
                if (!historyData.containsKey(entry.getOfferedStack())) {
                    Set<HistoryEntryData> dataSet = new HashSet<>();
                    dataSet.add(new HistoryEntryData(entry.getAmount(), entry.getPrice()));
                    historyData.put(entry.getOfferedStack(), dataSet);
                } else
                    historyData.get(entry.getOfferedStack()).add(new HistoryEntryData(entry.getAmount(), entry.getPrice()));
            }

            ItemStackWrapper stackWrapper;
            Set<HistoryEntryData> dataSet;
            long summaryPrice = 0L;
            float 
            unitPrice,
            minPrice = - 1.0F, 
            maxPrice = - 1.0F;
            int totalItemsSoldAmount = 0;

            for (Map.Entry<ItemStackWrapper, Set<HistoryEntryData>> entry : historyData.entrySet()) {
                stackWrapper = entry.getKey();
                dataSet = entry.getValue();

                for (HistoryEntryData data : dataSet) {
                    summaryPrice += data.price;

                    unitPrice = (float) data.price / (float) data.amount;
                    if (minPrice == - 1.0F || unitPrice < minPrice)
                        minPrice = unitPrice;
                    if (maxPrice == - 1.0F || unitPrice > maxPrice)
                        maxPrice = unitPrice;

                    totalItemsSoldAmount += data.amount;
                }

                this.marketData.put(stackWrapper, new ItemStackMarketData(
                        (float) summaryPrice / (float) totalItemsSoldAmount, 
                        minPrice,
                        maxPrice,
                        dataSet.size(),
                        totalItemsSoldAmount));

                summaryPrice = 0L;
                minPrice = - 1.0F;
                maxPrice = - 1.0F;
                totalItemsSoldAmount = 0;
            }
        }
    }

    public ItemStackMarketData getItemStackMarketData(ItemStackWrapper stackWrapper) {
        return this.marketData.get(stackWrapper);
    }

    public static class HistoryEntryData {

        final int amount;

        final long price;

        HistoryEntryData(int amount, long price) {
            this.amount = amount;
            this.price = price;
        }
    }

    public static class ItemStackMarketData {

        private final float averagePrice, minPrice, maxPrice;

        private final int completedTransactions, totalAmount;

        public ItemStackMarketData(float averagePrice, float minPrice, float maxPrice, int completedTransactions, int totalAmount) {
            this.averagePrice = averagePrice;
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
            this.completedTransactions = completedTransactions;
            this.totalAmount = totalAmount;
        }

        public float getAveragePrice() {
            return this.averagePrice;
        }

        public float getMinPrice() {
            return this.minPrice;
        }

        public float getMaxPrice() {
            return this.maxPrice;
        }

        public int getCompletedTransactionsAmount() {
            return this.completedTransactions;
        }

        public int getTotalItemsSoldAmount() {
            return this.totalAmount;
        }
    }
}
