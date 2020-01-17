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
                .sorted((e1, e2)->e2.getId() < e1.getId() ? - 1 : e2.getId() > e1.getId() ? 1 : 0)
                .collect(Collectors.toList());
    }

    public List<SalesHistoryEntryClient> getPlayerPurchaseEntries() {
        String username = OxygenHelperClient.getPlayerUsername();
        return this.manager.getSalesHistoryContainer().getEntries()
                .stream()
                .filter((entry)->entry.getBuyerUsername().equals(username))
                .sorted((e1, e2)->e2.getId() < e1.getId() ? - 1 : e2.getId() > e1.getId() ? 1 : 0)
                .collect(Collectors.toList());
    }
}
