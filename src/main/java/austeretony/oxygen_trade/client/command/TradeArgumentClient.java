package austeretony.oxygen_trade.client.command;

import java.util.concurrent.TimeUnit;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.api.PrivilegesProviderClient;
import austeretony.oxygen_core.common.command.ArgumentExecutor;
import austeretony.oxygen_trade.client.TradeManagerClient;
import austeretony.oxygen_trade.client.gui.history.SalesHistoryScreen;
import austeretony.oxygen_trade.client.gui.trade.TradeMenuScreen;
import austeretony.oxygen_trade.common.config.TradeConfig;
import austeretony.oxygen_trade.common.main.EnumTradePrivilege;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class TradeArgumentClient implements ArgumentExecutor {

    @Override
    public String getName() {
        return "trade";
    }

    @Override
    public void process(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 1) {
            if (TradeConfig.ENABLE_TRADE_MENU_ACCESS_CLIENTSIDE.asBoolean())
                OxygenHelperClient.scheduleTask(()->this.openTradeMenu(), 100L, TimeUnit.MILLISECONDS);
        } else if (args.length == 2) {
            if (args[1].equals("-reset-data")) {
                TradeManagerClient.instance().getOffersContainer().reset();
                ClientReference.showChatMessage("oxygen_trade.command.dataReset");
            } else if (args[1].equals("-sales-history"))
                if (PrivilegesProviderClient.getAsBoolean(EnumTradePrivilege.SALES_HISTORY_ACCESS.id(), TradeConfig.ENABLE_SALES_HISTORY_SYNC.asBoolean()))
                    OxygenHelperClient.scheduleTask(()->this.openSalesHistoryMenu(), 100L, TimeUnit.MILLISECONDS);
        }
    }

    private void openTradeMenu() {
        ClientReference.delegateToClientThread(()->ClientReference.displayGuiScreen(new TradeMenuScreen()));
    }

    private void openSalesHistoryMenu() {
        ClientReference.delegateToClientThread(()->ClientReference.displayGuiScreen(new SalesHistoryScreen()));
    }
}
