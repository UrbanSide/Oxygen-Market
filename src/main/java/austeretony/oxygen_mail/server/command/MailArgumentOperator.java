package austeretony.oxygen_mail.server.command;

import austeretony.oxygen_core.common.command.CommandArgument;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.util.MinecraftCommon;
import austeretony.oxygen_core.common.util.objects.Pair;
import austeretony.oxygen_core.server.api.OxygenServer;
import austeretony.oxygen_mail.common.mail.Attachments;
import austeretony.oxygen_mail.common.main.MailMain;
import austeretony.oxygen_mail.server.MailManagerServer;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MailArgumentOperator implements CommandArgument {

    @Override
    public String getName() {
        return "mail";
    }

    @Override
    public void process(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length >= 2) {
            if (args[1].equals("open")) {
                if (args.length != 3) return;

                EntityPlayerMP playerMP = CommandBase.getPlayer(server, sender, args[2]);

                OxygenServer.resetTimeout(MailMain.TIMEOUT_MAIL_OPERATIONS, MinecraftCommon.getEntityUUID(playerMP));
                OxygenServer.openScreen(playerMP, MailMain.SCREEN_ID_MAIL_SCREEN);
            }
            if (args[1].equals("mail-send-letter")) {
                args = processDoubleQuotes(args);
                if (args.length != 5) return;

                Pair<UUID, EntityPlayerMP> pair = parsePlayer(server, sender, args[2]);
                UUID playerUUID = pair.getKey();

                String subject = args[3];
                String message = args[4];

                Future<Boolean> future = OxygenServer.addTask(() -> {
                    MailManagerServer.instance().sendSystemMail(playerUUID, OxygenMain.SYSTEM_SENDER, subject,
                            message, new String[0], Attachments.none(), true);
                    return true;
                });

                try {
                    Boolean result = future.get();
                    if (result != null && result) {
                        sender.sendMessage(new TextComponentString("Letter send successfully"));
                    } else {
                        sender.sendMessage(new TextComponentString("Failed to send letter"));
                    }
                } catch (InterruptedException | ExecutionException exception) {
                    exception.printStackTrace();
                }
            } else if (args[1].equals("mail-send-parcel-in-game")) {
                args = processDoubleQuotes(args);
                if (args.length != 6) return;

                if (!(sender instanceof EntityPlayer)) {
                    throw new CommandException("Command available for players only");
                }
                Pair<UUID, EntityPlayerMP> pair = parsePlayer(server, sender, args[2]);
                UUID playerUUID = pair.getKey();

                String subject = args[3];
                String message = args[4];
                ItemStackWrapper stackWrapper = getHeldItemStackWrapper(CommandBase.getCommandSenderAsPlayer(sender));
                int itemsAmount = CommandBase.parseInt(args[5], 1, Short.MAX_VALUE);

                Future<Boolean> future = OxygenServer.addTask(() -> {
                    MailManagerServer.instance().sendSystemMail(playerUUID, OxygenMain.SYSTEM_SENDER, subject,
                            message, new String[0], Attachments.parcel(stackWrapper, itemsAmount),
                            true);
                    return true;
                });

                try {
                    Boolean result = future.get();
                    if (result != null && result) {
                        sender.sendMessage(new TextComponentString("Parcel send successfully"));
                    } else {
                        sender.sendMessage(new TextComponentString("Failed to send parcel"));
                    }
                } catch (InterruptedException | ExecutionException exception) {
                    exception.printStackTrace();
                }
            } else if (args[1].equals("mail-send-remittance")) {
                args = processDoubleQuotes(args);
                if (args.length != 7) return;

                Pair<UUID, EntityPlayerMP> pair = parsePlayer(server, sender, args[2]);
                UUID playerUUID = pair.getKey();

                String subject = args[3];
                String message = args[4];
                int currencyIndex = CommandBase.parseInt(args[5], 0, 100);
                if (OxygenServer.getCurrencyProvider(currencyIndex) == null) {
                    throw new CommandException("Currency with index <" + args[5] + "> not exist");
                }
                long value = CommandBase.parseLong(args[6], 0, Long.MAX_VALUE);

                Future<Boolean> future = OxygenServer.addTask(() -> {
                    MailManagerServer.instance().sendSystemMail(playerUUID, OxygenMain.SYSTEM_SENDER, subject,
                            message, new String[0], Attachments.remittance(currencyIndex, value),
                            true);
                    return true;
                });

                try {
                    Boolean result = future.get();
                    if (result != null && result) {
                        sender.sendMessage(new TextComponentString("Remittance send successfully"));
                    } else {
                        sender.sendMessage(new TextComponentString("Failed to send remittance"));
                    }
                } catch (InterruptedException | ExecutionException exception) {
                    exception.printStackTrace();
                }
            } else if (args[1].equals("mail-send-parcel")) {
                args = processDoubleQuotes(args);
                if (args.length != 7) return;

                Pair<UUID, EntityPlayerMP> pair = parsePlayer(server, sender, args[2]);
                UUID playerUUID = pair.getKey();

                String subject = args[3];
                String message = args[4];
                ItemStackWrapper stackWrapper = parseItemStackWrapperFromJsonString(args[5]);
                int itemsAmount = CommandBase.parseInt(args[6], 1, Short.MAX_VALUE);

                Future<Boolean> future = OxygenServer.addTask(() -> {
                    MailManagerServer.instance().sendSystemMail(playerUUID, OxygenMain.SYSTEM_SENDER, subject,
                            message, new String[0], Attachments.parcel(stackWrapper, itemsAmount),
                            true);
                    return true;
                });

                try {
                    Boolean result = future.get();
                    if (result != null && result) {
                        sender.sendMessage(new TextComponentString("Parcel send successfully"));
                    } else {
                        sender.sendMessage(new TextComponentString("Failed to send parcel"));
                    }
                } catch (InterruptedException | ExecutionException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
                                          @Nullable BlockPos targetPos) {
        if (args.length == 2) {
            return CommandBase.getListOfStringsMatchingLastWord(args, "open", "mail-send");
        }
        return Collections.emptyList();
    }
}
