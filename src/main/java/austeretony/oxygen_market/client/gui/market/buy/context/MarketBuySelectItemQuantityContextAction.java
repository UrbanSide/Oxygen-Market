package austeretony.oxygen_market.client.gui.market.buy.context;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.gui.base.context.ContextAction;
import austeretony.oxygen_core.client.gui.base.core.Callback;
import austeretony.oxygen_core.client.gui.base.core.Section;
import austeretony.oxygen_core.client.gui.base.special.callback.SelectQuantityCallback;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_market.client.MarketManagerClient;
import austeretony.oxygen_market.client.gui.market.buy.CombinedDealsEntry;

import javax.annotation.Nonnull;
import java.util.stream.Collectors;

public class MarketBuySelectItemQuantityContextAction implements ContextAction<CombinedDealsEntry> {

    @Nonnull
    @Override
    public String getName(CombinedDealsEntry entry) {
        return "oxygen_market.gui.market.buy.context.select_item_quantity";
    }

    @Override
    public boolean isValid(CombinedDealsEntry entry) {
        return getPlayerBalance() >= entry.getDeal().getPrice();
    }

    @Override
    public void execute(CombinedDealsEntry entry) {
        int maxAmount = (int) Math.floor((double) getPlayerBalance() / entry.getDeal().getPrice());
        Callback callback = new SelectQuantityCallback(
                "oxygen_market.gui.market.buy.callback.select_purchase_quantity",
                "oxygen_market.gui.market.buy.callback.select_purchase_quantity.message",
                1,
                Math.min(maxAmount, entry.getDealsMap().size()),
                1,
                selected -> MarketManagerClient.instance()
                        .purchase(entry.getDealsMap().keySet().stream().limit(selected).collect(Collectors.toSet())));
        Section.tryOpenCallback(callback);
    }

    private long getPlayerBalance() {
        return OxygenClient.getWatcherValue(OxygenMain.CURRENCY_COINS, 0L);
    }
}
