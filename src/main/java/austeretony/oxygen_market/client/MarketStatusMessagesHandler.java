package austeretony.oxygen_market.client;

import austeretony.oxygen_core.client.chat.MessageFormatter;
import austeretony.oxygen_core.common.chat.ChatMessagesHandler;
import austeretony.oxygen_market.common.main.EnumMarketStatusMessage;
import austeretony.oxygen_market.common.main.MarketMain;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;

public class MarketStatusMessagesHandler implements ChatMessagesHandler {

    private final MessageFormatter formatter = (messageIndex, args)->{
        if (messageIndex == EnumMarketStatusMessage.ITEM_PURCHASED.ordinal()) {
            ITextComponent 
            message = new TextComponentTranslation("oxygen_market.status.message.itemPurchased"),
            command = new TextComponentTranslation("oxygen_market.message.clickHere");

            message.getStyle().setItalic(true);
            message.getStyle().setColor(TextFormatting.AQUA);
            command.getStyle().setItalic(true);
            command.getStyle().setUnderlined(true);
            command.getStyle().setColor(TextFormatting.WHITE);
            command.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/oxygenc mail"));

            return message.appendSibling(command);
        }
        return null;
    };

    @Override
    public int getModIndex() {
        return MarketMain.MARKET_MOD_INDEX;
    }

    @Override
    public String getMessage(int messageIndex) {
        return EnumMarketStatusMessage.values()[messageIndex].localizedName();
    }

    @Override
    public MessageFormatter getMessageFormatter() {
        return this.formatter;
    }
}
