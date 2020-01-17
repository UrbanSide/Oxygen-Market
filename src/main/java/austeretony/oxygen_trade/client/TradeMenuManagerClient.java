package austeretony.oxygen_trade.client;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_trade.client.gui.history.SalesHistoryScreen;
import austeretony.oxygen_trade.client.gui.trade.TradeMenuScreen;
import austeretony.oxygen_trade.common.main.EnumOfferAction;

public final class TradeMenuManagerClient {

    private final TradeManagerClient manager;

    protected TradeMenuManagerClient(TradeManagerClient manager) {
        this.manager = manager;
    }

    public void openTradeMenu() {
        ClientReference.displayGuiScreen(new TradeMenuScreen());
    }

    public void offersSynchronized() {
        ClientReference.delegateToClientThread(()->{
            if (isTradeMenuOpened())
                ((TradeMenuScreen) ClientReference.getCurrentScreen()).offersSynchronized();
        });
    }

    public void salesHistoryDataSynchronized() {
        this.manager.getMarketDataManager().updateMarketData();

        ClientReference.delegateToClientThread(()->{
            if (isTradeMenuOpened())
                ((TradeMenuScreen) ClientReference.getCurrentScreen()).salesHistorySynchronized();
            if (isSalesHistoryScreenOpened())
                ((SalesHistoryScreen) ClientReference.getCurrentScreen()).salesHistorySynchronized();
        });
    }

    public void performedOfferAction(EnumOfferAction action, OfferClient offer, long balance) {
        ClientReference.delegateToClientThread(()->{
            if (isTradeMenuOpened())
                ((TradeMenuScreen) ClientReference.getCurrentScreen()).performedOfferAction(action, offer, balance);
        });
    }

    public static boolean isTradeMenuOpened() {
        return ClientReference.hasActiveGUI() && ClientReference.getCurrentScreen() instanceof TradeMenuScreen;
    }

    public static boolean isSalesHistoryScreenOpened() {
        return ClientReference.hasActiveGUI() && ClientReference.getCurrentScreen() instanceof SalesHistoryScreen;
    }
}
