package austeretony.oxygen_mail.common.mail;

import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.sync.SynchronousEntry;
import austeretony.oxygen_core.common.util.ByteBufUtils;
import austeretony.oxygen_core.common.util.nbt.NBTUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import java.util.UUID;

public class MailEntry implements SynchronousEntry {

    public static final int
            MESSAGE_SUBJECT_MAX_LENGTH = 24,
            MESSAGE_MAX_LENGTH = 800;

    private long id;
    private UUID senderUUID;
    private String senderName, subject, message;
    private String[] messageArgs;
    private AttachmentType attachmentType;
    private Attachment attachment;

    private boolean pending;

    public MailEntry() {}

    public MailEntry(long id, UUID senderUUID, String senderName, String subject, String message, String[] messageArgs,
                     Attachment attachment) {
        this.id = id;
        this.senderUUID = senderUUID;
        this.senderName = senderName;
        this.subject = subject;
        this.message = message;
        this.messageArgs = messageArgs;
        this.attachmentType = attachment.getType();
        this.attachment = attachment;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public UUID getSenderUUID() {
        return senderUUID;
    }

    public boolean isSentByPlayer() {
        return !senderUUID.equals(OxygenMain.SYSTEM_UUID);
    }

    public String getSenderName() {
        return senderName;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }

    public String[] getMessageArguments() {
        return messageArgs;
    }

    public AttachmentType getAttachmentType() {
        return attachmentType;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public boolean isPending() {
        return pending;
    }

    public void setPending(boolean flag) {
        pending = flag;
    }

    public boolean isExpired(long currentTimeMillis) {
        long expirationTime = attachment.getExpireTimeMillis(this);
        if (expirationTime < 0L) {
            return false;
        }
        return currentTimeMillis - id > expirationTime;
    }

    @Override
    public void write(ByteBuf buffer) {
        buffer.writeLong(id);
        buffer.writeByte(attachmentType.ordinal());
        ByteBufUtils.writeUUID(senderUUID, buffer);
        ByteBufUtils.writeString(senderName, buffer);
        ByteBufUtils.writeString(subject, buffer);

        ByteBufUtils.writeString(message, buffer);
        buffer.writeByte(messageArgs.length);
        for (String arg : messageArgs) {
            ByteBufUtils.writeString(arg, buffer);
        }

        attachment.write(buffer);
        buffer.writeBoolean(pending);
    }

    @Override
    public void read(ByteBuf buffer) {
        id = buffer.readLong();
        attachmentType = AttachmentType.values()[buffer.readByte()];
        senderUUID = ByteBufUtils.readUUID(buffer);
        senderName = ByteBufUtils.readString(buffer);
        subject = ByteBufUtils.readString(buffer);

        message = ByteBufUtils.readString(buffer);
        messageArgs = new String[buffer.readByte()];
        for (int i = 0; i < messageArgs.length; i++) {
            messageArgs[i] = ByteBufUtils.readString(buffer);
        }

        attachment = attachmentType.read(buffer);
        pending = buffer.readBoolean();
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tagCompound = new NBTTagCompound();

        tagCompound.setLong("id", id);
        tagCompound.setByte("attachment_type_ordinal", (byte) attachmentType.ordinal());
        tagCompound.setTag("sender_uuid", NBTUtils.toNBTUUID(senderUUID));
        tagCompound.setString("sender_name", senderName);
        tagCompound.setString("subject", subject);

        tagCompound.setString("message", message);
        NBTTagList argsList = new NBTTagList();
        for (String arg : messageArgs) {
            argsList.appendTag(new NBTTagString(arg));
        }
        tagCompound.setTag("message_args", argsList);

        tagCompound.setTag("attachment", attachment.writeToNBT());
        tagCompound.setBoolean("pending", pending);

        return tagCompound;
    }

    public static MailEntry readFromNBT(NBTTagCompound tagCompound) {
        MailEntry mailEntry = new MailEntry();

        mailEntry.id = tagCompound.getLong("id");
        mailEntry.attachmentType = AttachmentType.values()[tagCompound.getByte("attachment_type_ordinal")];
        mailEntry.senderUUID = NBTUtils.fromNBTUUID(tagCompound.getTag("sender_uuid"));
        mailEntry.senderName = tagCompound.getString("sender_name");
        mailEntry.subject = tagCompound.getString("subject");

        mailEntry.message = tagCompound.getString("message");

        NBTTagList argsList = tagCompound.getTagList("message_args", 8);
        mailEntry.messageArgs = new String[argsList.tagCount()];
        for (int i = 0; i < argsList.tagCount(); i++) {
            mailEntry.messageArgs[i] = argsList.getStringTagAt(i);
        }

        mailEntry.attachment =  mailEntry.attachmentType.readFromNBT(tagCompound.getCompoundTag("attachment"));
        mailEntry.pending = tagCompound.getBoolean("pending");

        return mailEntry;
    }

    @Override
    public String toString() {
        return "MailEntry[" +
                "id= " + id + "," +
                "senderUUID= " + senderUUID + "," +
                "senderName= " + senderName + "," +
                "subject= " + subject + "," +
                "message= " + message + "," +
                "messageArgs= " + "[" + String.join(", ", messageArgs) + "]," +
                "attachmentType= " + attachmentType.toString() + "," +
                "attachment= " + attachment +
                "]";
    }
}
