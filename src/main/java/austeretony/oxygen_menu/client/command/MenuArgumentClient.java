package austeretony.oxygen_menu.client.command;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.common.command.CommandArgument;
import austeretony.oxygen_menu.common.main.MenuMain;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class MenuArgumentClient implements CommandArgument {

    @Override
    public String getName() {
        return "menu";
    }

    @Override
    public void process(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 1) {
            OxygenClient.openScreenWithDelay(MenuMain.SCREEN_ID_MENU);
        }
    }
}
