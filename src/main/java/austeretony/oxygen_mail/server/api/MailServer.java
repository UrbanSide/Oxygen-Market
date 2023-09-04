package austeretony.oxygen_mail.server.api;

import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_mail.common.mail.Attachment;
import austeretony.oxygen_mail.common.mail.Attachments;
import austeretony.oxygen_mail.server.MailManagerServer;

import java.util.UUID;

public final class MailServer {

    private MailServer() {}

    public static boolean canReceiveMail(UUID addresseeUUID) {
        return MailManagerServer.instance().getOrCreateMailbox(addresseeUUID).canReceiveMail();
    }

    public static MailBuilder systemMail(UUID addresseeUUID, String subject) {
        return new MailBuilder(addresseeUUID, subject);
    }

    public static class MailBuilder {

        final UUID addresseeUUID;
        final String subject;

        String senderName = OxygenMain.SYSTEM_SENDER, message = "";
        String[] messageArgs = new String[0];
        Attachment attachment = Attachments.none();
        boolean ignoreMailboxCapacity;

        MailBuilder(UUID addresseeUUID, String subject) {
            this.addresseeUUID = addresseeUUID;
            this.subject = subject;
        }

        public MailBuilder withSenderName(String senderName) {
            this.senderName = senderName;
            return this;
        }

        public MailBuilder withMessage(String message, String... messageArgs) {
            this.message = message;
            this.messageArgs = messageArgs;
            return this;
        }

        public MailBuilder withMessage(String message) {
            this.message = message;
            return this;
        }

        public MailBuilder withAttachment(Attachment attachment) {
            this.attachment = attachment;
            return this;
        }

        public MailBuilder withMailBoxCapacityIgnore() {
            this.ignoreMailboxCapacity = true;
            return this;
        }

        public void send() {
            MailManagerServer.instance().sendSystemMail(addresseeUUID, senderName, subject, message, messageArgs,
                    attachment, ignoreMailboxCapacity);
        }
    }
}
