package austeretony.oxygen_market.client.command;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.api.PrivilegesProviderClient;
import austeretony.oxygen_core.common.command.ArgumentExecutor;
import austeretony.oxygen_market.client.MarketManagerClient;
import austeretony.oxygen_market.client.MenuManagerClient;
import austeretony.oxygen_market.common.config.MarketConfig;
import austeretony.oxygen_market.common.main.EnumMarketPrivilege;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class MarketArgumentClient implements ArgumentExecutor {

    @Override
    public String getName() {
        return "market";
    }

    @Override
    public void process(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 1) {
            if (MarketConfig.ENABLE_MARKET_MENU_ACCESS_CLIENTSIDE.asBoolean())
                OxygenHelperClient.scheduleTask(MenuManagerClient::openMarketMenuDelegated, 100L, TimeUnit.MILLISECONDS);
        } else if (args.length == 2) {
            if (args[1].equals("-reset-data")) {
                MarketManagerClient.instance().getOffersContainer().reset();
                ClientReference.showChatMessage("oxygen_market.command.dataReset");
            } else if (args[1].equals("-sales-history"))
                if (PrivilegesProviderClient.getAsBoolean(EnumMarketPrivilege.SALES_HISTORY_ACCESS.id(), MarketConfig.ENABLE_SALES_HISTORY_SYNC.asBoolean()))
                    OxygenHelperClient.scheduleTask(MenuManagerClient::openSalesHistoryMenuDelegated, 100L, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 2)
            return CommandBase.getListOfStringsMatchingLastWord(args, "-reset-data", "-sales-history");
        return Collections.<String>emptyList();
    }
}
