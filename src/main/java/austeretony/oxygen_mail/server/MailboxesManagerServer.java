package austeretony.oxygen_mail.server;

import java.util.Iterator;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import austeretony.oxygen_core.common.PlayerSharedData;
import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.api.notification.SimpleNotification;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.server.api.CurrencyHelperServer;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_core.server.api.PrivilegesProviderServer;
import austeretony.oxygen_core.server.api.TimeHelperServer;
import austeretony.oxygen_mail.common.EnumMessageOperation;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.mail.Attachment;
import austeretony.oxygen_mail.common.mail.EnumMail;
import austeretony.oxygen_mail.common.mail.Mail;
import austeretony.oxygen_mail.common.main.EnumMailPrivilege;
import austeretony.oxygen_mail.common.main.EnumMailStatusMessage;
import austeretony.oxygen_mail.common.main.MailMain;
import austeretony.oxygen_mail.common.network.client.CPAttachmentReceived;
import austeretony.oxygen_mail.common.network.client.CPMailSent;
import austeretony.oxygen_mail.common.network.client.CPMessageRemoved;
import net.minecraft.entity.player.EntityPlayerMP;

public class MailboxesManagerServer {

    private final MailManagerServer manager;

    private final Queue<QueuedMailSending> mailSendingQueue = new ConcurrentLinkedQueue<>();

    private final Queue<QueuedMailOperation> mailOperationsQueue = new ConcurrentLinkedQueue<>();

    public MailboxesManagerServer(MailManagerServer manager) {
        this.manager = manager;
    }

    public void processExpiredMail() {
        final Runnable task = ()->{
            int removed = 0;
            Iterator<Mail> iterator;
            Mail mail;
            for (Mailbox mailbox : this.manager.getMailboxesContainer().getMailboxes()) {
                iterator = mailbox.getMessages().iterator();
                while (iterator.hasNext()) {
                    mail = iterator.next();
                    if (mail != null) {
                        if (mail.isExpired()) {
                            if (mail.isPending())
                                this.processExpiredMessage(mail);
                            iterator.remove();
                            removed++;
                        }
                    }
                }
            }
            if (removed > 0)
                this.manager.getMailboxesContainer().setChanged(true);
            OxygenMain.LOGGER.info("[Mail] Expired mail processed. Removed {} messages in total.", removed);
        };
        OxygenHelperServer.addRoutineTask(task);
    }

    private void processExpiredMessage(Mail message) {
        if (message.getType() == EnumMail.PARCEL || message.getType() == EnumMail.COD)
            this.sendSystemMail(
                    message.getSenderUUID(), 
                    OxygenMain.SYSTEM_SENDER, 
                    EnumMail.PARCEL, 
                    "mail.subject.returnExp", 
                    message.getAttachment().toParcel(), 
                    true, 
                    "mail.message.returnExp");
        else if (message.getType() == EnumMail.REMITTANCE)
            this.sendSystemMail(
                    message.getSenderUUID(), 
                    OxygenMain.SYSTEM_SENDER, 
                    EnumMail.REMITTANCE, 
                    "mail.subject.returnExp", 
                    message.getAttachment(), 
                    true, 
                    "mail.message.returnExp");
    }

    private void sendNewMessageNotification(UUID playerUUID) {
        if (OxygenHelperServer.isPlayerOnline(playerUUID))
            OxygenHelperServer.addNotification(CommonReference.playerByUUID(playerUUID), new SimpleNotification(MailMain.INCOMING_MESSAGE_NOTIFICATION_ID, "oxygen_mail.incoming"));
    }

    public void sendMail(EntityPlayerMP senderMP, String addresseeUsername, EnumMail type, String subject, String message, Attachment attachment) {
        if (PrivilegesProviderServer.getAsBoolean(CommonReference.getPersistentUUID(senderMP), EnumMailPrivilege.ALLOW_MAIL_SENDING.id(), MailConfig.ALLOW_MAIL_SENDING.asBoolean()))
            this.mailSendingQueue.offer(new QueuedMailSending(senderMP, addresseeUsername, type, subject, message, attachment));
    }

    void process() {
        this.processMailSendingQueue();
        this.processMailOperationsQueue();
    }

    private void processMailSendingQueue() {
        final Runnable task = ()->{
            while (!this.mailSendingQueue.isEmpty()) {
                final QueuedMailSending queued = this.mailSendingQueue.poll();
                if (queued != null)
                    this.sendMailQueue(queued);
            }
        };
        OxygenHelperServer.addRoutineTask(task);
    }

    private void sendMailQueue(QueuedMailSending queued) {
        PlayerSharedData sharedData = OxygenHelperServer.getPlayerSharedData(queued.addresseeUsername);
        if (sharedData == null) {
            this.manager.sendStatusMessages(queued.senderMP, EnumMailStatusMessage.PLAYER_NOT_FOUND);
            return;
        }
        if (this.processPlayerMailSending(queued.senderMP, sharedData.getPlayerUUID(), queued.type, queued.subject, queued.message, queued.attachment, false))
            this.manager.sendStatusMessages(queued.senderMP, EnumMailStatusMessage.MESSAGE_SENT);
        else
            this.manager.sendStatusMessages(queued.senderMP, EnumMailStatusMessage.MESSAGE_SENDING_FAILED);
    }

    private boolean processPlayerMailSending(EntityPlayerMP senderMP, UUID addresseeUUID, EnumMail type, String subject, String message, Attachment attachment, boolean isReturn) {
        if (!isReturn) {
            subject = subject.trim();
            if (subject.isEmpty()) return false;
            if (subject.length() > Mail.MESSAGE_SUBJECT_MAX_LENGTH)
                subject = subject.substring(0, Mail.MESSAGE_SUBJECT_MAX_LENGTH);
            message = message.trim();
            if (message.length() > Mail.MESSAGE_MAX_LENGTH)
                message = message.substring(0, Mail.MESSAGE_MAX_LENGTH);
        }

        UUID senderUUID = CommonReference.getPersistentUUID(senderMP);
        if (!addresseeUUID.equals(senderUUID)) {
            Mailbox senderMailbox = this.manager.getMailboxesContainer().getPlayerMailbox(senderUUID);
            if (senderMailbox.canSendMessage()) {
                Mailbox targetMailbox = this.manager.getMailboxesContainer().getPlayerMailbox(addresseeUUID);
                if (targetMailbox.canAcceptMessages()
                        && attachment.send(senderMP, null)) {
                    senderMailbox.applySendingCooldown();
                    this.addMessage(targetMailbox, type, senderUUID, CommonReference.getName(senderMP), subject, attachment, message);
                    OxygenMain.network().sendTo(new CPMailSent(type, attachment, CurrencyHelperServer.getCurrency(senderUUID, OxygenMain.COMMON_CURRENCY_INDEX)), senderMP);

                    return true;
                }
            }
        }
        return false;
    }

    private void addMessage(Mailbox targetMailbox, EnumMail type, UUID senderUUID, String senderName, String subject, Attachment attachment, String message, String... messageArgs) {
        targetMailbox.addMessage(new Mail(
                targetMailbox.createId(TimeHelperServer.getCurrentMillis()), 
                type, 
                senderUUID, 
                senderName, 
                subject, 
                attachment, 
                message, 
                messageArgs));
        this.sendNewMessageNotification(targetMailbox.getPlayerUUID());

        this.manager.getMailboxesContainer().setChanged(true);

        if (MailConfig.ADVANCED_LOGGING.asBoolean())
            OxygenMain.LOGGER.info("[Mail] Sender {}/{} sent mail <subject: {}, type: {}> with attachment {} to player {}.", 
                    senderName,
                    senderUUID.equals(OxygenMain.SYSTEM_UUID) ? "SYSTEM" : senderUUID,
                            subject,
                            type,
                            attachment,
                            targetMailbox.getPlayerUUID());
    }

    public void processMessageOperation(EntityPlayerMP playerMP, long messageId, EnumMessageOperation operation) {
        this.mailOperationsQueue.offer(new QueuedMailOperation(CommonReference.getPersistentUUID(playerMP), messageId, operation));
    }

    private void processMailOperationsQueue() {
        final Runnable task = ()->{
            while (!this.mailOperationsQueue.isEmpty()) {
                final QueuedMailOperation queued = this.mailOperationsQueue.poll();
                if (queued != null)
                    this.processMessageOperationQueue(queued);
            }
        };
        OxygenHelperServer.addRoutineTask(task);
    }

    private void processMessageOperationQueue(QueuedMailOperation queued) {
        EntityPlayerMP playerMP = CommonReference.playerByUUID(queued.playerUUID);
        Mailbox mailbox = this.manager.getMailboxesContainer().getPlayerMailbox(queued.playerUUID);
        Mail mail = mailbox.getMessage(queued.messageId);
        if (mail != null) {
            switch (queued.operation) {
            case TAKE_ATTACHMENT:
                if (mail.isPending() 
                        && mail.getAttachment().receive(playerMP, mail)) {
                    mailbox.removeMessage(queued.messageId);

                    mail.setId(mailbox.createId(queued.messageId));
                    mail.attachmentReceived();  
                    mailbox.addMessage(mail);

                    this.manager.getMailboxesContainer().setChanged(true);

                    OxygenMain.network().sendTo(new CPAttachmentReceived(queued.messageId, mail, CurrencyHelperServer.getCurrency(queued.playerUUID, OxygenMain.COMMON_CURRENCY_INDEX)), playerMP);
                    this.manager.sendStatusMessages(playerMP, EnumMailStatusMessage.ATTACHMENT_RECEIVED);

                    if (MailConfig.ADVANCED_LOGGING.asBoolean())
                        OxygenMain.LOGGER.info("[Mail] Player {}/{} took attachment {} from sender: {}/{}.", 
                                CommonReference.getName(playerMP),
                                queued.playerUUID,
                                mail.getAttachment(),
                                mail.getSenderName(),
                                mail.isSystemMessage() ? "SYSTEM" : mail.getSenderUUID());
                }  
                break;
            case RETURN:
                if (!mail.isSystemMessage() 
                        && mail.isPending()
                        && this.returnAttachmentToSender(playerMP, mail)) {
                    mailbox.removeMessage(queued.messageId);

                    this.manager.getMailboxesContainer().setChanged(true);

                    OxygenMain.network().sendTo(new CPMessageRemoved(queued.messageId), playerMP);
                    this.manager.sendStatusMessages(playerMP, EnumMailStatusMessage.MESSAGE_RETURNED);

                    if (MailConfig.ADVANCED_LOGGING.asBoolean())
                        OxygenMain.LOGGER.info("[Mail] Player {}/{} returned attachment {} to sender: {}/{}.", 
                                CommonReference.getName(playerMP),
                                queued.playerUUID,
                                mail.getAttachment(),
                                mail.getSenderName(),
                                mail.getSenderUUID());
                }
                break;
            case REMOVE_MESSAGE:
                if (!mail.isPending()) {
                    mailbox.removeMessage(queued.messageId);

                    this.manager.getMailboxesContainer().setChanged(true);

                    OxygenMain.network().sendTo(new CPMessageRemoved(queued.messageId), playerMP);
                    this.manager.sendStatusMessages(playerMP, EnumMailStatusMessage.MESSAGE_REMOVED);

                    if (MailConfig.ADVANCED_LOGGING.asBoolean())
                        OxygenMain.LOGGER.info("[Mail] Player {}/{} removed message from sender: {}/{}.", 
                                CommonReference.getName(playerMP),
                                queued.playerUUID,
                                mail.getSenderName(),
                                mail.getSenderUUID());
                }
                break;
            }
        }
    }

    private boolean returnAttachmentToSender(EntityPlayerMP playerMP, Mail message) {
        if (message.getType() == EnumMail.PARCEL || message.getType() == EnumMail.COD) {
            if (this.sendSystemMail(
                    message.getSenderUUID(), 
                    OxygenMain.SYSTEM_SENDER,
                    EnumMail.PARCEL,
                    message.getSubject(), 
                    message.getAttachment().toParcel(), 
                    true,
                    "mail.message.attachmentReturn",
                    CommonReference.getName(playerMP)))
                return true;
        } else if (message.getType() == EnumMail.REMITTANCE) {
            if (this.sendSystemMail(
                    message.getSenderUUID(), 
                    OxygenMain.SYSTEM_SENDER,
                    EnumMail.REMITTANCE,
                    message.getSubject(), 
                    message.getAttachment(), 
                    true,
                    "mail.message.attachmentReturn",
                    CommonReference.getName(playerMP)))
                return true;
        }
        return false;
    }

    public boolean sendSystemMail(UUID addresseeUUID, String senderName, EnumMail type, String subject, Attachment attachment, boolean ignoreMailBoxCapacity, String message, String... messageArgs) {
        Mailbox mailbox = this.manager.getMailboxesContainer().getPlayerMailboxSafe(addresseeUUID);
        if (mailbox.canAcceptMessages() || ignoreMailBoxCapacity) {
            this.addMessage(mailbox, type, OxygenMain.SYSTEM_UUID, senderName, subject, attachment, message, messageArgs);
            return true;
        }
        return false;
    }
}
