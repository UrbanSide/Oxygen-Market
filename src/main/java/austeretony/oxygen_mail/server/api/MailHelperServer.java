package austeretony.oxygen_mail.server.api;

import java.util.UUID;

import javax.annotation.Nonnull;

import austeretony.oxygen_mail.common.mail.Attachment;
import austeretony.oxygen_mail.common.mail.EnumMail;
import austeretony.oxygen_mail.server.MailManagerServer;
import austeretony.oxygen_mail.server.Mailbox;

public class MailHelperServer {

    @Nonnull
    public static Mailbox getPlayerMailbox(UUID playerUUID) {
        return MailManagerServer.instance().getMailboxesContainer().getPlayerMailboxSafe(playerUUID);
    }

    public static boolean canPlayerAcceptMessages(UUID playerUUID) {
        return getPlayerMailbox(playerUUID).canAcceptMessages();
    }

    public static int getPlayerMailboxCapacity(UUID playerUUID) {
        return getPlayerMailbox(playerUUID).getMaxCapacity();
    }

    public static int getPlayerMessagesAmount(UUID playerUUID) {
        return getPlayerMailbox(playerUUID).getMessagesAmount();
    }

    public static int getPlayerMailboxFreeSpace(UUID playerUUID) {
        Mailbox mailbox = getPlayerMailbox(playerUUID);
        return mailbox.getMaxCapacity() - mailbox.getMessagesAmount();
    }

    public static boolean sendSystemMail(UUID addresseeUUID, String senderName, EnumMail type, String subject, Attachment attachment, boolean ignoreMailBoxCapacity, String message, String... messageArgs) {
        return MailManagerServer.instance().getMailboxesManager().sendSystemMail(addresseeUUID, senderName, type, subject, attachment, ignoreMailBoxCapacity, message, messageArgs);
    }
}
