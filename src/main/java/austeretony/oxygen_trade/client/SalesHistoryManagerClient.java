package austeretony.oxygen_trade.client;

import java.util.List;
import java.util.stream.Collectors;

import austeretony.oxygen_core.client.api.OxygenHelperClient;

public class SalesHistoryManagerClient {

    private final TradeManagerClient manager;

    public SalesHistoryManagerClient(TradeManagerClient manager) {
        this.manager = manager;
    }

    public int getHistoryEntriesAmountForPlayer() {
        int amount = 0;
        String username = OxygenHelperClient.getPlayerUsername();
        for (SalesHistoryEntryClient entry : this.manager.getSalesHistoryContainer().getEntries())
            if (entry.getBuyerUsername().equals(username) || entry.getSellerUsername().equals(username))
                amount++;
        return amount;
    }

    public List<SalesHistoryEntryClient> getPlayerSaleEntries() {
        String username = OxygenHelperClient.getPlayerUsername();
        return this.manager.getSalesHistoryContainer().getEntries()
                .stream()
                .filter((entry)->entry.getSellerUsername().equals(username))
                .collect(Collectors.toList());
    }

    public List<SalesHistoryEntryClient> getPlayerPurchaseEntries() {
        String username = OxygenHelperClient.getPlayerUsername();
        return this.manager.getSalesHistoryContainer().getEntries()
                .stream()
                .filter((entry)->entry.getBuyerUsername().equals(username))
                .collect(Collectors.toList());
    }
}
