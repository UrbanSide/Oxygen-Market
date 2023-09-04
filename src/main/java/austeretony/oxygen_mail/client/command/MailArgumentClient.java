package austeretony.oxygen_mail.client.command;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.common.command.CommandArgument;
import austeretony.oxygen_mail.common.main.MailMain;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class MailArgumentClient implements CommandArgument {

    @Override
    public String getName() {
        return "mail";
    }

    @Override
    public void process(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 1) {
            OxygenClient.openScreenWithDelay(MailMain.SCREEN_ID_MAIL_SCREEN);
        }
    }
}
