package austeretony.oxygen_mail.server;

import austeretony.oxygen_core.common.chat.StatusMessageType;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.persistent.AbstractPersistentData;
import austeretony.oxygen_core.common.util.CommonUtils;
import austeretony.oxygen_core.common.util.MinecraftCommon;
import austeretony.oxygen_core.common.util.objects.Pair;
import austeretony.oxygen_core.server.api.OxygenServer;
import austeretony.oxygen_core.server.api.PrivilegesServer;
import austeretony.oxygen_core.server.operation.Operation;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.mail.Attachment;
import austeretony.oxygen_mail.common.mail.MailEntry;
import austeretony.oxygen_mail.common.main.MailMain;
import austeretony.oxygen_mail.common.main.MailPrivileges;
import austeretony.oxygen_mail.common.network.operation.MailOperation;
import austeretony.oxygen_mail.server.mail.PlayerMailbox;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class MailManagerServer extends AbstractPersistentData {

    private static MailManagerServer instance;

    private final Map<UUID, PlayerMailbox> mailboxesMap = new HashMap<>();

    private MailManagerServer() {
        OxygenServer.registerPersistentData(this);
        OxygenServer.scheduleTask(this::processExpiredMail, 1L, TimeUnit.HOURS);
    }

    private void processExpiredMail() {
        final Runnable task = () -> {
            int processed = 0;

            long currentTimeMillis = OxygenServer.getCurrentTimeMillis();
            for (PlayerMailbox mailbox : mailboxesMap.values()) {
                Iterator<MailEntry> iterator = mailbox.getMailMap().values().iterator();
                while (iterator.hasNext()) {
                    MailEntry mailEntry = iterator.next();
                    if (mailEntry == null) continue;

                    if (mailEntry.isExpired(currentTimeMillis)) {
                        Attachment attachment = mailEntry.getAttachment();
                        if (mailEntry.isPending() && mailEntry.isSentByPlayer()) {
                            attachment.returnToSender(OxygenMain.SYSTEM_UUID, mailEntry);
                        }
                        iterator.remove();
                        processed++;
                    }
                }
            }
            if (processed > 0) {
                markChanged();
            }
            OxygenMain.logInfo(1, "[Mail] Expired mail processed: {} messages in total.", processed);
        };
        OxygenServer.addTask(task);
    }

    public static MailManagerServer instance() {
        if (instance == null)
            instance = new MailManagerServer();
        return instance;
    }

    public void serverStarting() {
        final Runnable task = () -> {
            OxygenServer.loadPersistentData(this);
            processExpiredMail();
        };
        OxygenServer.addTask(task);
    }

    @Nonnull
    public PlayerMailbox getOrCreateMailbox(UUID playerUUID) {
        PlayerMailbox mailbox = mailboxesMap.get(playerUUID);
        if (mailbox == null) {
            mailbox = new PlayerMailbox(playerUUID);
            mailboxesMap.put(playerUUID, mailbox);
            markChanged();
        }
        return mailbox;
    }

    public void sendSystemMail(UUID addresseeUUID, String senderName, String subject, String message, String[] messageArgs,
                               Attachment attachment, boolean ignoreMailboxCapacity) {
        PlayerMailbox mailbox = getOrCreateMailbox(addresseeUUID);
        if (mailbox.canReceiveMail() || ignoreMailboxCapacity) {
            long entryId = CommonUtils.createId(OxygenServer.getCurrentTimeMillis(), mailbox.getMailMap().keySet());
            MailEntry mailEntry = new MailEntry(entryId, OxygenMain.SYSTEM_UUID, senderName, subject, message, messageArgs,
                    attachment);
            mailEntry.setPending(true);

            mailbox.addMailEntry(mailEntry);
            markChanged();

            OxygenMain.logInfo(2, "[Mail] {} received system mail: {}", addresseeUUID, mailEntry);
        }
    }

    public void sendMail(EntityPlayerMP senderMP, UUID addresseeUUID, String subject, String message, Attachment attachment) {
        UUID senderUUID = MinecraftCommon.getEntityUUID(senderMP);
        if (!MailConfig.ENABLE_MAIL_ACCESS_CLIENT_SIDE.asBoolean()
                && OxygenServer.isTimeout(MailMain.TIMEOUT_MAIL_OPERATIONS, senderUUID)) {
            OxygenServer.sendStatusMessage(senderMP, MailMain.MODULE_INDEX, StatusMessageType.ERROR,
                    "oxygen.status_message.operation_timeout");
            return;
        }
        if (senderUUID.equals(addresseeUUID)) return;

        if (!PrivilegesServer.getBoolean(senderUUID, MailPrivileges.ALLOW_MAIL_SENDING.getId(),
                MailConfig.ENABLE_MAIL_SENDING.asBoolean())) {
            OxygenServer.sendStatusMessage(senderMP, MailMain.MODULE_INDEX, StatusMessageType.ERROR,
                    "oxygen_mail.status_message.mail_sending_unavailable");
            return;
        }

        subject = subject.trim();
        message = message.trim();
        if (subject.isEmpty() || subject.length() > MailEntry.MESSAGE_SUBJECT_MAX_LENGTH
                || message.length() > MailEntry.MESSAGE_MAX_LENGTH) {
            OxygenServer.sendStatusMessage(senderMP, MailMain.MODULE_INDEX, StatusMessageType.ERROR,
                    "oxygen_mail.status_message.invalid_subject_or_message");
            return;
        }

        PlayerMailbox senderMailbox = getOrCreateMailbox(senderUUID);
        if (!senderMailbox.canSendMail()) {
            OxygenServer.sendStatusMessage(senderMP, MailMain.MODULE_INDEX, StatusMessageType.ERROR,
                    "oxygen_mail.status_message.mail_sending_cool_down");
            return;
        }

        if (!attachment.isValid(senderMP)) {
            OxygenServer.sendStatusMessage(senderMP, MailMain.MODULE_INDEX, StatusMessageType.ERROR,
                    "oxygen_mail.status_message.invalid_attachment");
            return;
        }

        if (OxygenServer.getPlayerSharedData(addresseeUUID) == null) {
            OxygenServer.sendStatusMessage(senderMP, MailMain.MODULE_INDEX, StatusMessageType.ERROR,
                    "oxygen_mail.status_message.player_not_found");
            return;
        }
        PlayerMailbox addresseeMailbox = getOrCreateMailbox(addresseeUUID);
        if (!addresseeMailbox.canReceiveMail()) {
            OxygenServer.sendStatusMessage(senderMP, MailMain.MODULE_INDEX, StatusMessageType.ERROR,
                    "oxygen_mail.status_message.addressee_mailbox_full");
            return;
        }

        Pair<Integer, Long> postage = attachment.getPostage(senderMP);
        String finalMessage = message;
        String finalSubject = subject;
        final Runnable successTask = () -> {
            long currentTime = OxygenServer.getCurrentTimeMillis();
            long entryId = CommonUtils.createId(currentTime, addresseeMailbox.getMailMap().keySet());
            MailEntry mailEntry = new MailEntry(entryId, senderUUID, MinecraftCommon.getEntityName(senderMP), finalSubject,
                    finalMessage, new String[0], attachment);
            mailEntry.setPending(true);

            addresseeMailbox.addMailEntry(mailEntry);
            senderMailbox.setLastSendTimeMillis(currentTime);
            markChanged();

            attachment.playSendSound(senderMP);

            OxygenMain.logInfo(2, "[Mail] {} sent mail to {}: {}", senderUUID, addresseeUUID, mailEntry);
            OxygenServer.sendStatusMessage(senderMP, MailMain.MODULE_INDEX, StatusMessageType.COMMON,
                    "oxygen_mail.status_message.mail_sent");

            Pair<Integer, Long> pair = attachment.getBalance(senderMP);
            OxygenServer.sendToClient(
                    senderMP,
                    MailMain.MAIL_OPERATIONS_HANDLER_ID,
                    MailOperation.MAIL_SENT.ordinal(),
                    buffer -> {
                        buffer.writeByte(pair.getKey());
                        buffer.writeLong(pair.getValue());
                    });
        };

        Operation operation = Operation.of(MailMain.OPERATION_MAIL_SEND, senderMP)
                .withSuccessTask(successTask)
                .withFailTask(reason -> OxygenServer.sendMessageOnOperationFail(senderMP, reason, MailMain.MODULE_INDEX))
                .withCurrencyWithdraw(postage.getKey(), postage.getValue());
        attachment.send(senderMP, operation);
        operation
                .process();
    }

    public void removeMail(EntityPlayerMP playerMP, long entryId) {
        UUID playerUUID = MinecraftCommon.getEntityUUID(playerMP);
        if (!MailConfig.ENABLE_MAIL_ACCESS_CLIENT_SIDE.asBoolean()
                && OxygenServer.isTimeout(MailMain.TIMEOUT_MAIL_OPERATIONS, playerUUID)) {
            OxygenServer.sendStatusMessage(playerMP, MailMain.MODULE_INDEX, StatusMessageType.ERROR,
                    "oxygen.status_message.operation_timeout");
            return;
        }

        PlayerMailbox playerMailbox = getOrCreateMailbox(playerUUID);
        MailEntry mailEntry = playerMailbox.getMailEntry(entryId);
        if (mailEntry == null || mailEntry.isPending()) return;

        playerMailbox.removeMailEntry(entryId);
        markChanged();

        OxygenServer.sendStatusMessage(playerMP, MailMain.MODULE_INDEX, StatusMessageType.COMMON,
                "oxygen_mail.status_message.mail_removed");

        OxygenServer.sendToClient(
                playerMP,
                MailMain.MAIL_OPERATIONS_HANDLER_ID,
                MailOperation.MAIL_REMOVED.ordinal(),
                buffer -> buffer.writeLong(mailEntry.getId()));
    }

    public void returnMail(EntityPlayerMP playerMP, long entryId) {
        UUID playerUUID = MinecraftCommon.getEntityUUID(playerMP);
        if (!MailConfig.ENABLE_MAIL_ACCESS_CLIENT_SIDE.asBoolean()
                && OxygenServer.isTimeout(MailMain.TIMEOUT_MAIL_OPERATIONS, playerUUID)) {
            OxygenServer.sendStatusMessage(playerMP, MailMain.MODULE_INDEX, StatusMessageType.ERROR,
                    "oxygen.status_message.operation_timeout");
            return;
        }

        PlayerMailbox playerMailbox = getOrCreateMailbox(playerUUID);
        MailEntry mailEntry = playerMailbox.getMailEntry(entryId);
        if (mailEntry == null || !mailEntry.isPending()) return;

        mailEntry.getAttachment().returnToSender(playerUUID, mailEntry);
        playerMailbox.removeMailEntry(entryId);
        markChanged();

        OxygenServer.sendStatusMessage(playerMP, MailMain.MODULE_INDEX, StatusMessageType.COMMON,
                "oxygen_mail.status_message.mail_returned");

        OxygenServer.sendToClient(
                playerMP,
                MailMain.MAIL_OPERATIONS_HANDLER_ID,
                MailOperation.MAIL_REMOVED.ordinal(),
                buffer -> buffer.writeLong(mailEntry.getId()));
    }

    public void receiveAttachment(EntityPlayerMP playerMP, long entryId) {
        UUID playerUUID = MinecraftCommon.getEntityUUID(playerMP);
        if (!MailConfig.ENABLE_MAIL_ACCESS_CLIENT_SIDE.asBoolean()
                && OxygenServer.isTimeout(MailMain.TIMEOUT_MAIL_OPERATIONS, playerUUID)) {
            OxygenServer.sendStatusMessage(playerMP, MailMain.MODULE_INDEX, StatusMessageType.ERROR,
                    "oxygen.status_message.operation_timeout");
            return;
        }

        PlayerMailbox playerMailbox = getOrCreateMailbox(playerUUID);
        MailEntry mailEntry = playerMailbox.getMailEntry(entryId);
        if (mailEntry == null || !mailEntry.isPending()) return;

        final Runnable successTask = () -> {
            long newEntryId = CommonUtils.createId(mailEntry.getId(), playerMailbox.getMailMap().keySet());
            playerMailbox.removeMailEntry(entryId);
            mailEntry.setId(newEntryId);
            mailEntry.setPending(false);
            playerMailbox.addMailEntry(mailEntry);
            markChanged();

            mailEntry.getAttachment().playReceiveSound(playerMP);

            OxygenMain.logInfo(2, "[Mail] {} received attachment: {}", playerUUID, mailEntry);
            OxygenServer.sendStatusMessage(playerMP, MailMain.MODULE_INDEX, StatusMessageType.COMMON,
                    "oxygen_mail.status_message.attachment_received");

            Pair<Integer, Long> pair = mailEntry.getAttachment().getBalance(playerMP);
            OxygenServer.sendToClient(
                    playerMP,
                    MailMain.MAIL_OPERATIONS_HANDLER_ID,
                    MailOperation.ATTACHMENT_RECEIVED.ordinal(),
                    buffer -> {
                        buffer.writeLong(entryId);
                        buffer.writeLong(newEntryId);
                        buffer.writeInt(pair.getKey());
                        buffer.writeLong(pair.getValue());
                    });
        };

        Operation operation = Operation.of(MailMain.OPERATION_ATTACHMENT_RECEIVE, playerMP)
                .withSuccessTask(successTask)
                .withFailTask(reason -> OxygenServer.sendMessageOnOperationFail(playerMP, reason, MailMain.MODULE_INDEX));
        mailEntry.getAttachment().receive(operation, mailEntry);
        operation
                .process();
    }

    @Override
    public String getName() {
        return "mail:players_mailboxes";
    }

    @Override
    public String getPath() {
        return OxygenServer.getDataFolder() + "/world/mail/players_mailboxes.dat";
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        NBTTagList tagList = new NBTTagList();
        for (PlayerMailbox mailbox : mailboxesMap.values()) {
            tagList.appendTag(mailbox.writeToNBT());
        }
        tagCompound.setTag("mailboxes_list", tagList);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        NBTTagList tagList = tagCompound.getTagList("mailboxes_list", 10);
        for (int i = 0; i < tagList.tagCount(); i++) {
            PlayerMailbox mailbox = PlayerMailbox.readFromNBT(tagList.getCompoundTagAt(i));
            mailboxesMap.put(mailbox.getPlayerUUID(), mailbox);
        }
    }

    @Override
    public void reset() {
        mailboxesMap.clear();
    }
}
