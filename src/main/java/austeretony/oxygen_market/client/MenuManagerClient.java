package austeretony.oxygen_market.client;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_market.client.gui.history.SalesHistoryScreen;
import austeretony.oxygen_market.client.gui.market.MarketMenuScreen;
import austeretony.oxygen_market.common.main.EnumOfferAction;

public final class MenuManagerClient {

    private final MarketManagerClient manager;

    protected MenuManagerClient(MarketManagerClient manager) {
        this.manager = manager;
    }

    public static void openMarketMenu() {
        ClientReference.displayGuiScreen(new MarketMenuScreen());
    }

    public static void openMarketMenuDelegated() {
        ClientReference.delegateToClientThread(MenuManagerClient::openMarketMenu);
    }

    public static void openSalesHistoryMenuDelegated() {
        ClientReference.delegateToClientThread(()->ClientReference.displayGuiScreen(new SalesHistoryScreen()));
    }

    public void offersSynchronized() {
        ClientReference.delegateToClientThread(()->{
            if (isTradeMenuOpened())
                ((MarketMenuScreen) ClientReference.getCurrentScreen()).offersSynchronized();
        });
    }

    public void salesHistoryDataSynchronized() {
        this.manager.getMarketDataManager().updateMarketData();

        ClientReference.delegateToClientThread(()->{
            if (isTradeMenuOpened())
                ((MarketMenuScreen) ClientReference.getCurrentScreen()).salesHistorySynchronized();
            if (isSalesHistoryScreenOpened())
                ((SalesHistoryScreen) ClientReference.getCurrentScreen()).salesHistorySynchronized();
        });
    }

    public void performedOfferAction(EnumOfferAction action, OfferClient offer, long balance) {
        ClientReference.delegateToClientThread(()->{
            if (isTradeMenuOpened())
                ((MarketMenuScreen) ClientReference.getCurrentScreen()).performedOfferAction(action, offer, balance);
        });
    }

    public static boolean isTradeMenuOpened() {
        return ClientReference.hasActiveGUI() && ClientReference.getCurrentScreen() instanceof MarketMenuScreen;
    }

    public static boolean isSalesHistoryScreenOpened() {
        return ClientReference.hasActiveGUI() && ClientReference.getCurrentScreen() instanceof SalesHistoryScreen;
    }
}
