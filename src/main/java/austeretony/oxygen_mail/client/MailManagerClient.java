package austeretony.oxygen_mail.client;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.common.persistent.AbstractPersistentData;
import austeretony.oxygen_core.common.util.ByteBufUtils;
import austeretony.oxygen_mail.client.gui.mail.MailScreen;
import austeretony.oxygen_mail.common.mail.Attachment;
import austeretony.oxygen_mail.common.mail.AttachmentType;
import austeretony.oxygen_mail.common.mail.MailEntry;
import austeretony.oxygen_mail.common.main.MailMain;
import austeretony.oxygen_mail.common.network.operation.MailOperation;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;

import java.util.*;

public final class MailManagerClient extends AbstractPersistentData {

    private static MailManagerClient instance;

    private final Map<Long, MailEntry> mailMap = new HashMap<>();
    private final Set<Long> readMailSet = new HashSet<>();

    private MailManagerClient() {
        OxygenClient.registerPersistentData(this);
    }

    public static MailManagerClient instance() {
        if (instance == null)
            instance = new MailManagerClient();
        return instance;
    }

    public void clientInitialized() {
        readMailSet.clear();
        OxygenClient.loadPersistentDataAsync(this);
    }

    public Map<Long, MailEntry> getMailMap() {
        return mailMap;
    }

    public MailEntry getMailEntry(long entryId) {
        return mailMap.get(entryId);
    }

    public void sendMail(UUID addresseeUUID, String subject, String message,
                         AttachmentType attachmentType, Attachment attachment) {
        OxygenClient.sendToServer(
                MailMain.MAIL_OPERATIONS_HANDLER_ID,
                MailOperation.SEND_MAIL.ordinal(),
                buffer -> {
                    ByteBufUtils.writeUUID(addresseeUUID, buffer);
                    ByteBufUtils.writeString(subject, buffer);
                    ByteBufUtils.writeString(message, buffer);
                    buffer.writeByte(attachmentType.ordinal());
                    attachment.write(buffer);
                });
    }

    public void removeMail(long entryId) {
        OxygenClient.sendToServer(
                MailMain.MAIL_OPERATIONS_HANDLER_ID,
                MailOperation.REMOVE_MAIL.ordinal(),
                buffer -> buffer.writeLong(entryId));
    }

    public void mailRemoved(long entryId) {
        mailMap.remove(entryId);
        readMailSet.remove(entryId);
        markChanged();

        MailScreen.mailRemoved(entryId);
    }

    public void returnMail(long entryId) {
        OxygenClient.sendToServer(
                MailMain.MAIL_OPERATIONS_HANDLER_ID,
                MailOperation.RETURN_MAIL.ordinal(),
                buffer -> buffer.writeLong(entryId));
    }

    public void receiveAttachment(long entryId) {
        OxygenClient.sendToServer(
                MailMain.MAIL_OPERATIONS_HANDLER_ID,
                MailOperation.RECEIVE_ATTACHMENT.ordinal(),
                buffer -> buffer.writeLong(entryId));
    }

    public void attachmentReceived(long oldEntryId, long newEntryId, int currencyIndex, long balance) {
        MailEntry mailEntry = mailMap.get(oldEntryId);
        if (mailEntry != null) {
            mailMap.remove(oldEntryId);
            mailEntry.setId(newEntryId);
            mailEntry.setPending(false);
            mailMap.put(newEntryId, mailEntry);
            markChanged();
        }

        MailScreen.attachmentReceived(oldEntryId, newEntryId, currencyIndex, balance);
    }

    public Set<Long> getReadMailSet() {
        return readMailSet;
    }

    public void markMailEntryAsRead(long entryId) {
        readMailSet.add(entryId);
    }

    @Override
    public String getName() {
        return "mail:player_mailbox";
    }

    @Override
    public String getPath() {
        return OxygenClient.getDataFolder() + "/players/" + OxygenClient.getClientPlayerUUID()
                + "/mail/player_mailbox.dat";
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        NBTTagList mailList = new NBTTagList();
        for (MailEntry mailEntry : mailMap.values()) {
            mailList.appendTag(mailEntry.writeToNBT());
        }
        tagCompound.setTag("mail_list", mailList);

        NBTTagList readMailIdsList = new NBTTagList();
        for (long entryId : readMailSet) {
            readMailIdsList.appendTag(new NBTTagLong(entryId));
        }
        tagCompound.setTag("read_mail_ids_list", readMailIdsList);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        NBTTagList mailList = tagCompound.getTagList("mail_list", 10);
        for (int i = 0; i < mailList.tagCount(); i++) {
            MailEntry mailEntry = MailEntry.readFromNBT(mailList.getCompoundTagAt(i));
            mailMap.put(mailEntry.getId(), mailEntry);
        }

        NBTTagList readMailIdsList = tagCompound.getTagList("read_mail_ids_list", 4);
        for (int i = 0; i < readMailIdsList.tagCount(); i++) {
            readMailSet.add(((NBTTagLong) readMailIdsList.get(i)).getLong());
        }
    }

    @Override
    public void reset() {
        mailMap.clear();
    }
}
