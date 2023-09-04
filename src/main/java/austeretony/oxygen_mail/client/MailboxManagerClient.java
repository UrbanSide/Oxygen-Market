package austeretony.oxygen_mail.client;

import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_mail.common.EnumMessageOperation;
import austeretony.oxygen_mail.common.mail.Attachment;
import austeretony.oxygen_mail.common.mail.EnumMail;
import austeretony.oxygen_mail.common.mail.Mail;
import austeretony.oxygen_mail.common.network.server.SPMessageOperation;
import austeretony.oxygen_mail.common.network.server.SPSendMessage;

public class MailboxManagerClient {

    private final MailManagerClient manager;

    public MailboxManagerClient(MailManagerClient manager) {
        this.manager = manager;
    }

    public void sendMessageSynced(String addresseeUsername, EnumMail type, String subject, String message, Attachment attachment) {
        OxygenMain.network().sendToServer(new SPSendMessage(addresseeUsername, type, subject, message, attachment));
    }

    public void mailSent(EnumMail type, Attachment attachment, long balance) {
        this.manager.getMenuManager().mailSent(type, attachment, balance); 
    }

    public void processMessageOperationSynced(long messageId, EnumMessageOperation operation) {
        OxygenMain.network().sendToServer(new SPMessageOperation(messageId, operation));
    }

    public void attachmentReceived(long oldMessageId, Mail mail, long balance) {
        this.manager.getMailboxContainer().removeMessage(oldMessageId);
        this.manager.getMailboxContainer().addMessage(mail);

        this.manager.getMenuManager().attachmentReceived(oldMessageId, mail, balance);
    }

    public void messageRemoved(long messageId) {
        this.manager.getMailboxContainer().removeMessage(messageId);

        this.manager.getMenuManager().messageRemoved(messageId);
    }
}
