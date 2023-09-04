package austeretony.oxygen_mail.client.command;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.command.ArgumentExecutor;
import austeretony.oxygen_mail.client.MailManagerClient;
import austeretony.oxygen_mail.client.MailMenuManager;
import austeretony.oxygen_mail.common.config.MailConfig;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class MailArgumentClient implements ArgumentExecutor {

    @Override
    public String getName() {
        return "mail";
    }

    @Override
    public void process(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 1) {
            if (MailConfig.ENABLE_MAIL_ACCESS_CLIENTSIDE.asBoolean())
                OxygenHelperClient.scheduleTask(MailMenuManager::openMailMenuDelegated, 100L, TimeUnit.MILLISECONDS);
        } else if (args.length == 2) {
            if (args[1].equals("-reset-data")) {
                MailManagerClient.instance().getMailboxContainer().reset();
                ClientReference.showChatMessage("oxygen_mail.command.dataReset");
            }
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 2)
            return CommandBase.getListOfStringsMatchingLastWord(args, "-reset-data");
        return Collections.<String>emptyList();
    }
}
