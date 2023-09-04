package austeretony.oxygen_mail.server.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import austeretony.oxygen_core.common.PlayerSharedData;
import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.command.ArgumentExecutor;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.server.OxygenManagerServer;
import austeretony.oxygen_core.server.api.CurrencyHelperServer;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.mail.Attachments;
import austeretony.oxygen_mail.common.mail.EnumMail;
import austeretony.oxygen_mail.common.mail.Mail;
import austeretony.oxygen_mail.common.main.EnumMailStatusMessage;
import austeretony.oxygen_mail.common.main.MailMain;
import austeretony.oxygen_mail.common.network.client.CPOpenMailMenu;
import austeretony.oxygen_mail.server.MailManagerServer;
import austeretony.oxygen_mail.server.Mailbox;
import austeretony.oxygen_mail.server.api.MailHelperServer;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public class MailArgumentOperator implements ArgumentExecutor {

    @Override
    public String getName() {
        return "mail";
    }

    @Override
    public void process(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

        //Usage: /oxygens mail -send <target> <mail type> <subject> <message> <args...>
        //Text specified in brackets ({}): subject MUST present, message may be empty (use empty brackets - {}).
        //
        //Letter - /oxygens mail -send @a 0 {Letter} {Some dummy message to yourself.}
        //Remittance - /oxygens mail -send AustereTony 1 {Remittance} {1000 coins for you!} 0 10000
        //Package - /oxygens mail -send -all-online 2 {Gift} {Some dummy message for anyone ONLINE.} 10 minecraft:diamond (if last argument is absent, held item will be used) 
        //property '-all' allows to send message to EVERY player ever logged in.

        EntityPlayerMP senderPlayerMP = null, targetPlayerMP = null;
        if (sender instanceof EntityPlayerMP)
            senderPlayerMP = CommandBase.getCommandSenderAsPlayer(sender);

        if (args.length == 3) {
            if (args[1].equals("-open-menu")) {
                targetPlayerMP = CommandBase.getPlayer(server, sender, args[2]);
                OxygenHelperServer.resetTimeOut(CommonReference.getPersistentUUID(targetPlayerMP), MailMain.MAIL_TIMEOUT_ID);
                OxygenMain.network().sendTo(new CPOpenMailMenu(), targetPlayerMP);
            }
        }
        if (args.length >= 3) {
            if (args[1].equals("-clear-mail")) {
                if (args[2].equals("-global")) {
                    MailManagerServer.instance().getMailboxesContainer().reset();
                    MailManagerServer.instance().getMailboxesContainer().setChanged(true);

                    if (senderPlayerMP != null)
                        OxygenHelperServer.sendStatusMessage(senderPlayerMP, MailMain.MAIL_MOD_INDEX, EnumMailStatusMessage.MAILBOXES_CLEARED.ordinal());

                    if (MailConfig.ADVANCED_LOGGING.asBoolean())
                        OxygenMain.LOGGER.info("[Mail] (Operator/Console) {} cleared all mailboxes.",
                                sender.getName());
                } else if (args[2].equals("-player")) {
                    if (args.length == 4) {
                        try {
                            targetPlayerMP = CommandBase.getPlayer(server, sender, args[3]);
                        } catch(PlayerNotFoundException exception) {}

                        UUID playerUUID = null;
                        Mailbox mailbox = null;

                        if (targetPlayerMP == null) {
                            try {
                                playerUUID = UUID.fromString(args[3]);
                            } catch (IllegalArgumentException exception) {
                                exception.printStackTrace();
                                throw new WrongUsageException("Invalid playerUUID: %s", args[3]);
                            }

                            if (playerUUID != null)
                                mailbox = MailManagerServer.instance().getMailboxesContainer().getPlayerMailbox(playerUUID);
                        } else {
                            playerUUID = CommonReference.getPersistentUUID(targetPlayerMP);  
                            mailbox = MailManagerServer.instance().getMailboxesContainer().getPlayerMailbox(playerUUID);
                        }

                        if (mailbox != null) {
                            mailbox.clear();
                            MailManagerServer.instance().getMailboxesContainer().setChanged(true);

                            if (senderPlayerMP != null)
                                OxygenHelperServer.sendStatusMessage(senderPlayerMP, MailMain.MAIL_MOD_INDEX, EnumMailStatusMessage.MAILBOX_CLEARED.ordinal());

                            if (MailConfig.ADVANCED_LOGGING.asBoolean())
                                OxygenMain.LOGGER.info("[Mail] (Operator/Console) {} cleared player {} mailbox.",
                                        sender.getName(),
                                        targetPlayerMP != null ? CommonReference.getName(targetPlayerMP) : playerUUID); 
                        }
                    }
                }
            } else if (args[1].equals("-remove-message")) {
                if (args[2].equals("-subject")) {
                    if (args.length >= 5) { 
                        try {
                            targetPlayerMP = CommandBase.getPlayer(server, sender, args[3]);
                        } catch(PlayerNotFoundException exception) {}

                        UUID playerUUID = null;
                        Mailbox mailbox = null;

                        if (targetPlayerMP == null) {
                            try {
                                playerUUID = UUID.fromString(args[3]);
                            } catch (IllegalArgumentException exception) {
                                exception.printStackTrace();
                                throw new WrongUsageException("Invalid playerUUID: %s", args[3]);
                            }

                            if (playerUUID != null)
                                mailbox = MailManagerServer.instance().getMailboxesContainer().getPlayerMailbox(playerUUID);
                        } else {
                            playerUUID = CommonReference.getPersistentUUID(targetPlayerMP);  
                            mailbox = MailManagerServer.instance().getMailboxesContainer().getPlayerMailbox(playerUUID);
                        }

                        String subject = StringUtils.join(Arrays.copyOfRange(args, 4, args.length), ' ');

                        if (mailbox != null) {                            
                            Iterator<Mail> iterator = mailbox.getMessages().iterator();
                            boolean successful = false;
                            while (iterator.hasNext()) {
                                if (iterator.next().getSubject().equals(subject)) {
                                    iterator.remove();
                                    successful = true;
                                }
                            }

                            if (successful) {
                                MailManagerServer.instance().getMailboxesContainer().setChanged(true);

                                if (senderPlayerMP != null)
                                    OxygenHelperServer.sendStatusMessage(senderPlayerMP, MailMain.MAIL_MOD_INDEX, EnumMailStatusMessage.MESSAGE_REMOVED.ordinal());

                                if (MailConfig.ADVANCED_LOGGING.asBoolean())
                                    OxygenMain.LOGGER.info("[Mail] (Operator/Console) {} removed message <{}> from player {} mailbox.",
                                            sender.getName(),
                                            subject,
                                            targetPlayerMP != null ? CommonReference.getName(targetPlayerMP) : playerUUID); 
                            }
                        }
                    }
                }
            } else if (args[1].equals("-send")) {
                int messageType = CommandBase.parseInt(args[3], 0, 2);

                StringBuilder builder = new StringBuilder();
                String word, subject, message;
                //subject
                int index;
                for (index = 4; index < args.length; index++) {
                    word = args[index];
                    if (index == 4) {
                        if (!word.startsWith("{"))
                            throw new WrongUsageException("Invalid subject!");
                        else
                            word = word.substring(1);
                    }
                    if (word.endsWith("}")) {
                        word = word.substring(0, word.length() - 1);
                        builder.append(word);
                        break;
                    } else
                        builder.append(word).append(' ');
                }
                subject = builder.toString();
                if (subject.isEmpty())
                    throw new WrongUsageException("Empty subject!");

                //message
                builder.delete(0, builder.length());
                boolean first = true;
                index++;
                for (; index < args.length; index++) {
                    word = args[index];
                    if (first) {
                        if (!word.startsWith("{"))
                            throw new WrongUsageException("Invalid message!");
                        else
                            word = word.substring(1);
                        first = false;
                    }
                    if (word.endsWith("}")) {
                        word = word.substring(0, word.length() - 1);
                        builder.append(word);
                        break;
                    } else
                        builder.append(word).append(' ');
                }
                message = builder.toString();

                if (args[2].equals("-all-online")) {  
                    switch (messageType) {
                    case 0:
                        for (UUID playerUUID : OxygenHelperServer.getOnlinePlayersUUIDs())
                            MailHelperServer.sendSystemMail(
                                    playerUUID, 
                                    OxygenMain.SYSTEM_SENDER, 
                                    EnumMail.LETTER,
                                    subject, 
                                    Attachments.dummy(),
                                    true,
                                    message);

                        if (sender instanceof EntityPlayerMP)
                            senderPlayerMP.sendMessage(new TextComponentTranslation("oxygen_mail.message.command.oxygens.mail.letterSent", 
                                    "ALL-ONLINE",
                                    subject,
                                    message)); 
                        else
                            server.sendMessage(new TextComponentString(String.format("Letter sent to <all-online> - subject: %s, message: %s", 
                                    subject,
                                    message)));

                        if (MailConfig.ADVANCED_LOGGING.asBoolean())
                            OxygenMain.LOGGER.info("[Mail] (Operator/Console) {} sent LETTER to all players online: subject - {}, message - {}",
                                    sender.getName(),
                                    subject,
                                    message); 
                        break;
                    case 1:
                        index++;
                        if (index < args.length) {
                            int currencyIndex = CommandBase.parseInt(args[index], 0, 127);
                            if (CurrencyHelperServer.getCurrencyProvider(currencyIndex) == null)
                                throw new WrongUsageException("Invalid currency index: %d", currencyIndex);

                            long value = CommandBase.parseLong(args[++index], 0, Long.MAX_VALUE);      

                            for (UUID playerUUID : OxygenHelperServer.getOnlinePlayersUUIDs())
                                MailHelperServer.sendSystemMail(
                                        playerUUID, 
                                        OxygenMain.SYSTEM_SENDER, 
                                        EnumMail.REMITTANCE,
                                        subject, 
                                        Attachments.remittance(currencyIndex, value),
                                        true,
                                        message);

                            if (sender instanceof EntityPlayerMP)
                                senderPlayerMP.sendMessage(new TextComponentTranslation("oxygen_mail.message.command.oxygens.mail.remittanceSent", 
                                        "ALL-ONLINE",
                                        subject,
                                        message,
                                        currencyIndex,
                                        value)); 
                            else
                                server.sendMessage(new TextComponentString(String.format("Remittance sent to <all-online> - subject: %s, message: %s, index: %d, value: %d", 
                                        subject,
                                        message,
                                        currencyIndex,
                                        value)));

                            if (MailConfig.ADVANCED_LOGGING.asBoolean())
                                OxygenMain.LOGGER.info("[Mail] (Operator/Console) {} sent REMITTANCE to all players online: subject - {}, message - {}, index - {}, value - {}",
                                        sender.getName(),
                                        subject,
                                        message,
                                        currencyIndex,
                                        value); 
                        } else
                            throw new WrongUsageException("Invalid remittance value!");
                        break;
                    case 2:
                        index++;
                        if (index < args.length) {
                            int amount = CommandBase.parseInt(args[index++], 0, 1000);
                            ItemStack itemStack;
                            if (index < args.length)
                                itemStack = new ItemStack(CommandBase.getItemByText(sender, args[index]));
                            else {
                                if (sender instanceof MinecraftServer)
                                    throw new WrongUsageException("Invalid item registry name!");
                                if (senderPlayerMP.getHeldItemMainhand() != ItemStack.EMPTY)
                                    itemStack = senderPlayerMP.getHeldItemMainhand().copy();
                                else
                                    throw new WrongUsageException("Main hand is empty!");
                            }

                            for (UUID playerUUID : OxygenHelperServer.getOnlinePlayersUUIDs())
                                MailHelperServer.sendSystemMail(
                                        playerUUID, 
                                        OxygenMain.SYSTEM_SENDER, 
                                        EnumMail.PARCEL,
                                        subject, 
                                        Attachments.parcel(ItemStackWrapper.of(itemStack), amount),
                                        true,
                                        message);

                            if (sender instanceof EntityPlayerMP)
                                senderPlayerMP.sendMessage(new TextComponentTranslation("oxygen_mail.message.command.oxygens.mail.parcelSent", 
                                        "ALL-ONLINE",
                                        subject,
                                        message,
                                        amount,
                                        itemStack.getDisplayName())); 
                            else
                                server.sendMessage(new TextComponentString(String.format("Parcel sent to <all-online> - subject: %s, message: %s, amount: %d, item: %s", 
                                        subject,
                                        message,
                                        amount,
                                        itemStack.getDisplayName())));

                            if (MailConfig.ADVANCED_LOGGING.asBoolean())
                                OxygenMain.LOGGER.info("[Mail] (Operator/Console) {} sent PARCEL to all players online: subject - {}, message - {}, amount - {}, item - {}",
                                        sender.getName(),
                                        subject,
                                        message,
                                        amount,
                                        itemStack.getDisplayName()); 
                        } else
                            throw new WrongUsageException("Invalid items amount!");
                        break;
                    }
                } else if (args[2].equals("-all")) {
                    switch (messageType) {
                    case 0:            
                        for (PlayerSharedData sharedData : OxygenManagerServer.instance().getSharedDataManager().getPlayersSharedData())
                            MailHelperServer.sendSystemMail(
                                    sharedData.getPlayerUUID(), 
                                    OxygenMain.SYSTEM_SENDER, 
                                    EnumMail.LETTER,
                                    subject, 
                                    Attachments.dummy(),
                                    true,
                                    message);

                        if (sender instanceof EntityPlayerMP)
                            senderPlayerMP.sendMessage(new TextComponentTranslation("oxygen_mail.message.command.oxygens.mail.letterSent", 
                                    "ALL",
                                    subject,
                                    message)); 
                        else
                            server.sendMessage(new TextComponentString(String.format("Letter sent to <all> - subject: %s, message: %s", 
                                    subject,
                                    message)));

                        if (MailConfig.ADVANCED_LOGGING.asBoolean())
                            OxygenMain.LOGGER.info("[Mail] (Operator/Console) {} sent LETTER to ALL players: subject - {}, message - {}",
                                    sender.getName(),
                                    subject,
                                    message); 
                        break;
                    case 1:
                        index++;
                        if (index < args.length) {
                            int currencyIndex = CommandBase.parseInt(args[index], 0, 127);
                            if (CurrencyHelperServer.getCurrencyProvider(currencyIndex) == null)
                                throw new WrongUsageException("Invalid currency index: %d", currencyIndex);

                            long value = CommandBase.parseLong(args[++index], 0, Long.MAX_VALUE);

                            for (PlayerSharedData sharedData : OxygenManagerServer.instance().getSharedDataManager().getPlayersSharedData())
                                MailHelperServer.sendSystemMail(
                                        sharedData.getPlayerUUID(), 
                                        OxygenMain.SYSTEM_SENDER, 
                                        EnumMail.REMITTANCE,
                                        subject, 
                                        Attachments.remittance(currencyIndex, value),
                                        true,
                                        message);

                            if (sender instanceof EntityPlayerMP)
                                senderPlayerMP.sendMessage(new TextComponentTranslation("oxygen_mail.message.command.oxygens.mail.remittanceSent", 
                                        "ALL",
                                        subject,
                                        message,
                                        currencyIndex,
                                        value)); 
                            else
                                server.sendMessage(new TextComponentString(String.format("Remittance sent to <all> - subject: %s, message: %s, index: %d, value: %d", 
                                        subject,
                                        message,
                                        currencyIndex,
                                        value)));

                            if (MailConfig.ADVANCED_LOGGING.asBoolean())
                                OxygenMain.LOGGER.info("[Mail] (Operator/Console) {} sent REMITTANCE to ALL players: subject - {}, message - {}, index - {}, value - {}",
                                        sender.getName(),
                                        subject,
                                        message,
                                        currencyIndex,
                                        value); 
                        } else
                            throw new WrongUsageException("Invalid remittance value!");
                        break;
                    case 2:
                        index++;
                        if (index < args.length) {
                            int amount = CommandBase.parseInt(args[index++], 0, 1000);
                            ItemStack itemStack;
                            if (index < args.length)
                                itemStack = new ItemStack(CommandBase.getItemByText(sender, args[index]));
                            else {
                                if (sender instanceof MinecraftServer)
                                    throw new WrongUsageException("Invalid item registry name!");
                                if (senderPlayerMP.getHeldItemMainhand() != ItemStack.EMPTY)
                                    itemStack = senderPlayerMP.getHeldItemMainhand().copy();
                                else
                                    throw new WrongUsageException("Main hand is empty!");
                            }

                            for (PlayerSharedData sharedData : OxygenManagerServer.instance().getSharedDataManager().getPlayersSharedData())
                                MailHelperServer.sendSystemMail(
                                        sharedData.getPlayerUUID(), 
                                        OxygenMain.SYSTEM_SENDER, 
                                        EnumMail.PARCEL,
                                        subject, 
                                        Attachments.parcel(ItemStackWrapper.of(itemStack), amount),
                                        true,
                                        message);

                            if (sender instanceof EntityPlayerMP)
                                senderPlayerMP.sendMessage(new TextComponentTranslation("oxygen_mail.message.command.oxygens.mail.parcelSent", 
                                        "ALL",
                                        subject,
                                        message,
                                        amount,
                                        itemStack.getDisplayName())); 
                            else
                                server.sendMessage(new TextComponentString(String.format("Parcel sent to <all> - subject: %s, message: %s, amount: %d, item: %s", 
                                        subject,
                                        message,
                                        amount,
                                        itemStack.getDisplayName())));

                            if (MailConfig.ADVANCED_LOGGING.asBoolean())
                                OxygenMain.LOGGER.info("[Mail] (Operator/Console) {} sent PARCEL to ALL players: subject - {}, message - {}, amount - {}, item - {}",
                                        sender.getName(),
                                        subject,
                                        message,
                                        amount,
                                        itemStack.getDisplayName()); 
                        } else
                            throw new WrongUsageException("Invalid items amount!");
                        break;
                    }
                } else {

                    try {
                        targetPlayerMP = CommandBase.getPlayer(server, sender, args[2]);
                    } catch(PlayerNotFoundException exception) {}

                    UUID playerUUID = null;
                    Mailbox mailbox = null;

                    if (targetPlayerMP == null) {
                        try {
                            playerUUID = UUID.fromString(args[2]);
                        } catch (IllegalArgumentException exception) {
                            exception.printStackTrace();
                            throw new WrongUsageException("Invalid playerUUID: %s", args[2]);
                        }

                        if (playerUUID != null)
                            mailbox = MailManagerServer.instance().getMailboxesContainer().getPlayerMailboxSafe(playerUUID);
                    } else {
                        playerUUID = CommonReference.getPersistentUUID(targetPlayerMP);  
                        mailbox = MailManagerServer.instance().getMailboxesContainer().getPlayerMailboxSafe(playerUUID);
                    }

                    if (mailbox != null) {
                        switch (messageType) {
                        case 0:                        
                            MailHelperServer.sendSystemMail(
                                    playerUUID, 
                                    OxygenMain.SYSTEM_SENDER, 
                                    EnumMail.LETTER,
                                    subject, 
                                    Attachments.dummy(),
                                    true,
                                    message);

                            if (sender instanceof EntityPlayerMP)
                                senderPlayerMP.sendMessage(new TextComponentTranslation("oxygen_mail.message.command.oxygens.mail.letterSent", 
                                        targetPlayerMP != null ? CommonReference.getName(targetPlayerMP) : playerUUID,
                                                subject,
                                                message)); 
                            else
                                server.sendMessage(new TextComponentString(String.format("Letter sent to <%s> - subject: %s, message: %s", 
                                        targetPlayerMP != null ? CommonReference.getName(targetPlayerMP) : playerUUID,
                                                subject,
                                                message)));

                            if (MailConfig.ADVANCED_LOGGING.asBoolean())
                                OxygenMain.LOGGER.info("[Mail] (Operator/Console) {} sent LETTER to player {}: subject - {}, message - {}",
                                        sender.getName(),
                                        playerUUID,
                                        subject,
                                        message); 
                            break;
                        case 1:
                            index++;
                            if (index < args.length) {
                                int currencyIndex = CommandBase.parseInt(args[index], 0, 127);
                                if (CurrencyHelperServer.getCurrencyProvider(currencyIndex) == null)
                                    throw new WrongUsageException("Invalid currency index: %s", currencyIndex);

                                long value = CommandBase.parseLong(args[++index], 0, Long.MAX_VALUE);
                                MailHelperServer.sendSystemMail(
                                        playerUUID, 
                                        OxygenMain.SYSTEM_SENDER, 
                                        EnumMail.REMITTANCE,
                                        subject, 
                                        Attachments.remittance(currencyIndex, value),
                                        true,
                                        message);

                                if (sender instanceof EntityPlayerMP)
                                    senderPlayerMP.sendMessage(new TextComponentTranslation("oxygen_mail.message.command.oxygens.mail.remittanceSent", 
                                            targetPlayerMP != null ? CommonReference.getName(targetPlayerMP) : playerUUID,
                                                    subject,
                                                    message,
                                                    currencyIndex,
                                                    value)); 
                                else
                                    server.sendMessage(new TextComponentString(String.format("Remittance sent to <%s> - subject: %s, message: %s, index: %d, value: %d", 
                                            targetPlayerMP != null ? CommonReference.getName(targetPlayerMP) : playerUUID,
                                                    subject,
                                                    message,
                                                    currencyIndex,
                                                    value)));

                                if (MailConfig.ADVANCED_LOGGING.asBoolean())
                                    OxygenMain.LOGGER.info("[Mail] (Operator/Console) {} sent REMITTANCE to player {}: subject - {}, message - {}, index - {}, value - {}",
                                            sender.getName(),
                                            playerUUID,
                                            subject,
                                            message,
                                            currencyIndex,
                                            value); 
                            } else
                                throw new WrongUsageException("Invalid remittance value!");
                            break;
                        case 2:
                            index++;
                            if (index < args.length) {
                                int amount = CommandBase.parseInt(args[index++], 0, 1000);
                                ItemStack itemStack;
                                if (index < args.length)
                                    itemStack = new ItemStack(CommandBase.getItemByText(sender, args[index]));
                                else {
                                    if (sender instanceof MinecraftServer)
                                        throw new WrongUsageException("Invalid item registry name!");
                                    if (senderPlayerMP.getHeldItemMainhand() != ItemStack.EMPTY)
                                        itemStack = senderPlayerMP.getHeldItemMainhand().copy();
                                    else
                                        throw new WrongUsageException("Main hand is empty!");
                                }

                                MailHelperServer.sendSystemMail(
                                        playerUUID, 
                                        OxygenMain.SYSTEM_SENDER, 
                                        EnumMail.PARCEL,
                                        subject, 
                                        Attachments.parcel(ItemStackWrapper.of(itemStack), amount),
                                        true,
                                        message);

                                if (sender instanceof EntityPlayerMP)
                                    senderPlayerMP.sendMessage(new TextComponentTranslation("oxygen_mail.message.command.oxygens.mail.parcelSent", 
                                            targetPlayerMP != null ? CommonReference.getName(targetPlayerMP) : playerUUID,
                                                    subject,
                                                    message,
                                                    amount,
                                                    itemStack.getDisplayName())); 
                                else
                                    server.sendMessage(new TextComponentString(String.format("Parcel sent to <%s> - subject: %s, message: %s, amount: %d, item: %s", 
                                            targetPlayerMP != null ? CommonReference.getName(targetPlayerMP) : playerUUID,
                                                    subject,
                                                    message,
                                                    amount,
                                                    itemStack.getDisplayName())));

                                if (MailConfig.ADVANCED_LOGGING.asBoolean())
                                    OxygenMain.LOGGER.info("[Mail] (Operator/Console) {} sent PARCEL to player {}: subject - {}, message - {}, amount - {}, item - {}",
                                            sender.getName(),
                                            playerUUID,
                                            subject,
                                            message,
                                            amount,
                                            itemStack.getDisplayName()); 
                            } else
                                throw new WrongUsageException("Invalid items amount!");
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 2)
            return CommandBase.getListOfStringsMatchingLastWord(args, "-open-menu", "-clear-mail", "-remove-message", "-send");
        else if (args.length >= 3) {
            if (args[1].equals("-clear-mail"))
                return CommandBase.getListOfStringsMatchingLastWord(args, "-global", "-player");
            else if (args[1].equals("-remove-message"))
                return CommandBase.getListOfStringsMatchingLastWord(args, "-subject");
            else if (args[1].equals("-send"))
                return CommandBase.getListOfStringsMatchingLastWord(args, "-all-online", "-all");
        }
        return Collections.<String>emptyList();
    }
}
