package austeretony.oxygen_market.client.command;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.common.command.CommandArgument;
import austeretony.oxygen_market.common.main.MarketMain;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class MarketArgumentClient implements CommandArgument {

    @Override
    public String getName() {
        return "market";
    }

    @Override
    public void process(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 1) {
            OxygenClient.openScreenWithDelay(MarketMain.SCREEN_ID_MARKET);
        }
    }
}
