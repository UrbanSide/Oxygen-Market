package austeretony.oxygen_permissions.client.command;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.common.command.CommandArgument;
import austeretony.oxygen_permissions.common.main.PermissionsMain;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class PermissionsArgumentClient implements CommandArgument {

    @Override
    public String getName() {
        return "permissions";
    }

    @Override
    public void process(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 1) {
            OxygenClient.openScreenWithDelay(PermissionsMain.SCREEN_ID_PERMISSIONS_INFO);
        } else if (args.length == 2) {
            if (args[1].equals("management")) {
                OxygenClient.openScreenWithDelay(PermissionsMain.SCREEN_ID_PERMISSIONS_MANAGEMENT);
            }
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
                                          @Nullable BlockPos targetPos) {
        if (args.length == 2)
            return CommandBase.getListOfStringsMatchingLastWord(args, "management");
        return Collections.emptyList();
    }
}
