package austeretony.oxygen_market.server.command;

import austeretony.oxygen_core.common.command.CommandArgument;
import austeretony.oxygen_core.common.util.MinecraftCommon;
import austeretony.oxygen_core.server.api.OxygenServer;
import austeretony.oxygen_market.common.main.MarketMain;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class MarketArgumentOperator implements CommandArgument {

    @Override
    public String getName() {
        return "market";
    }

    @Override
    public void process(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 3) {
            if (args[1].equals("open")) {
                EntityPlayerMP playerMP = CommandBase.getPlayer(server, sender, args[2]);

                OxygenServer.resetTimeout(MarketMain.TIMEOUT_MARKET_OPERATIONS, MinecraftCommon.getEntityUUID(playerMP));
                OxygenServer.openScreen(playerMP, MarketMain.SCREEN_ID_MARKET);
            }
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
                                          @Nullable BlockPos targetPos) {
        if (args.length == 2) {
            return CommandBase.getListOfStringsMatchingLastWord(args, "open");
        }
        return Collections.emptyList();
    }
}
