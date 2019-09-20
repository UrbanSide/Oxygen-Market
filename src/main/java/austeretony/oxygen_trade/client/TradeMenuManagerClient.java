package austeretony.oxygen_trade.client;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_trade.client.gui.trade.TradeMenuGUIScreen;
import austeretony.oxygen_trade.common.main.EnumOfferAction;
import austeretony.oxygen_trade.common.main.TradeMain;

public final class TradeMenuManagerClient {

    protected TradeMenuManagerClient() {}

    public void openTradeMenu() {
        if (TradeManagerClient.instance().getItemCategoriesPreset().isVerified())
            ClientReference.displayGuiScreen(new TradeMenuGUIScreen());
    }

    public void offersSynchronized() {
        ClientReference.delegateToClientThread(()->{
            if (isMenuOpened())
                ((TradeMenuGUIScreen) ClientReference.getCurrentScreen()).offersSynchronized();
        });
    }

    public void salesHistoryDataSynchronized() {
        ClientReference.delegateToClientThread(()->{
            if (isMenuOpened())
                ((TradeMenuGUIScreen) ClientReference.getCurrentScreen()).salesHistorySynchronized();
        });
    }
    
    public void performedOfferAction(EnumOfferAction action, PlayerOfferClient offer, long balance) {
        ClientReference.delegateToClientThread(()->{
            if (isMenuOpened())
                ((TradeMenuGUIScreen) ClientReference.getCurrentScreen()).performedOfferAction(action, offer, balance);
        });
    }

    public static boolean isMenuOpened() {
        return ClientReference.hasActiveGUI() && ClientReference.getCurrentScreen() instanceof TradeMenuGUIScreen;
    }
}
