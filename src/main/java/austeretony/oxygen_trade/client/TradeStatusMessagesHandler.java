package austeretony.oxygen_trade.client;

import austeretony.oxygen_core.common.chat.ChatMessagesHandler;
import austeretony.oxygen_trade.common.main.EnumTradeStatusMessage;
import austeretony.oxygen_trade.common.main.TradeMain;

public class TradeStatusMessagesHandler implements ChatMessagesHandler {

    @Override
    public int getModIndex() {
        return TradeMain.TRADE_MOD_INDEX;
    }

    @Override
    public String getMessage(int messageIndex) {
        return EnumTradeStatusMessage.values()[messageIndex].localizedName();
    }
}
