package austeretony.oxygen_market.client.gui.market.buy;

import austeretony.oxygen_market.common.market.Deal;

import java.util.LinkedHashMap;
import java.util.Map;

public class CombinedDealsEntry {

    private final Deal deal;
    private final Map<Long, Deal> dealsMap = new LinkedHashMap<>();

    public CombinedDealsEntry(Deal deal) {
        this.deal = deal;
    }

    public long getId() {
        return deal.getId();
    }

    public Deal getDeal() {
        return deal;
    }

    public Map<Long, Deal> getDealsMap() {
        return dealsMap;
    }

    public void addDeal(Deal deal) {
        dealsMap.put(deal.getId(), deal);
    }
}
