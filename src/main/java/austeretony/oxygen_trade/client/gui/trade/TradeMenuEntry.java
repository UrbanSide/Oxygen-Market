package austeretony.oxygen_trade.client.gui.trade;

import austeretony.oxygen_core.client.gui.menu.AbstractMenuEntry;
import austeretony.oxygen_trade.client.TradeManagerClient;

public class TradeMenuEntry extends AbstractMenuEntry {

    @Override
    public String getName() {
        return "oxygen_trade.gui.trade.title";
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void open() {
        TradeManagerClient.instance().getTradeMenuManager().openTradeMenu();
    }
}
