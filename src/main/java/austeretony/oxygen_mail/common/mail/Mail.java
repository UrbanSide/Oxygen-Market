package austeretony.oxygen_mail.common.mail;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.UUID;

import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.persistent.PersistentEntry;
import austeretony.oxygen_core.common.sync.SynchronousEntry;
import austeretony.oxygen_core.common.util.ByteBufUtils;
import austeretony.oxygen_core.common.util.StreamUtils;
import austeretony.oxygen_mail.common.config.MailConfig;
import io.netty.buffer.ByteBuf;

public class Mail implements PersistentEntry, SynchronousEntry {

    public static final int 
    MESSAGE_SUBJECT_MAX_LENGTH = 24,
    MESSAGE_MAX_LENGTH = 800;

    private long messageId;

    private EnumMail type;

    private UUID senderUUID;

    private String senderName, subject, message;

    private String[] messageArgs;

    private Attachment attachment;

    private boolean pending;

    public Mail() {}

    public Mail(long messageId, EnumMail type, UUID senderUUID, String senderName, String subject, Attachment attachment, String message, String... messageArguments) {
        this.messageId = messageId;
        this.type = type;
        this.senderUUID = senderUUID;
        this.senderName = senderName;
        this.subject = subject;
        this.attachment = attachment;
        this.message = message;
        this.messageArgs = messageArguments;

        if (type != EnumMail.LETTER)
            this.pending = true;
    }

    @Override
    public long getId() {              
        return this.messageId;
    }

    public void setId(long messageId) {
        this.messageId = messageId;
    }

    public EnumMail getType() {
        return this.type;
    }

    public UUID getSenderUUID() {
        return this.senderUUID;
    }

    public boolean isSystemMessage() {
        return this.senderUUID.equals(OxygenMain.SYSTEM_UUID);
    }

    public String getSenderName() {
        return this.senderName;
    }

    public String getSubject() {
        return this.subject;
    }

    public String getMessage() {
        return this.message;
    }

    public String[] getMessageArguments() {
        return this.messageArgs;
    }

    public Attachment getAttachment() {
        return this.attachment;
    }

    public boolean isPending() {
        return this.pending;
    }  

    public void attachmentReceived() {
        this.pending = false;
    }

    public boolean isExpired() {
        int expiresInHours = - 1;
        switch (this.type) {
        case LETTER:
            expiresInHours = this.isSystemMessage() ? MailConfig.SYSTEM_LETTER_EXPIRE_TIME_HOURS.asInt() : MailConfig.LETTER_EXPIRE_TIME_HOURS.asInt();
            break;
        case REMITTANCE:
            expiresInHours = this.isSystemMessage()? MailConfig.SYSTEM_REMITTANCE_EXPIRE_TIME_HOURS.asInt() : MailConfig.REMITTANCE_EXPIRE_TIME_HOURS.asInt();
            break;
        case PARCEL:
            expiresInHours = this.isSystemMessage() ? MailConfig.SYSTEM_PARCEL_EXPIRE_TIME_HOURS.asInt() : MailConfig.PARCEL_EXPIRE_TIME_HOURS.asInt();
            break;
        case COD:
            expiresInHours = MailConfig.COD_EXPIRE_TIME_HOURS.asInt();
            break;  
        }
        if (expiresInHours < 0)
            return false;
        return System.currentTimeMillis() - this.messageId > expiresInHours * 3_600_000L;
    }

    @Override
    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write(this.messageId, bos);
        StreamUtils.write((byte) this.type.ordinal(), bos);
        StreamUtils.write(this.senderUUID, bos);
        StreamUtils.write(this.senderName, bos);
        StreamUtils.write(this.subject, bos);

        StreamUtils.write(this.message, bos);
        StreamUtils.write((byte) this.messageArgs.length, bos);
        for (String arg : this.messageArgs)
            StreamUtils.write(arg, bos);

        this.attachment.write(bos);
        StreamUtils.write(this.pending, bos);
    }

    @Override
    public void read(BufferedInputStream bis) throws IOException {        
        this.messageId = StreamUtils.readLong(bis);
        this.type = EnumMail.values()[StreamUtils.readByte(bis)]; 
        this.senderUUID = StreamUtils.readUUID(bis);
        this.senderName = StreamUtils.readString(bis);
        this.subject = StreamUtils.readString(bis);

        this.message = StreamUtils.readString(bis);
        this.messageArgs = new String[StreamUtils.readByte(bis)];
        for (int i = 0; i < this.messageArgs.length; i++)
            this.messageArgs[i] = StreamUtils.readString(bis);

        this.attachment = this.type.readAttachment(bis);
        this.pending = StreamUtils.readBoolean(bis);
    }

    @Override
    public void write(ByteBuf buffer) {
        buffer.writeLong(this.messageId);
        buffer.writeByte(this.type.ordinal());
        ByteBufUtils.writeUUID(this.senderUUID, buffer);
        ByteBufUtils.writeString(this.senderName, buffer);
        ByteBufUtils.writeString(this.subject, buffer);

        ByteBufUtils.writeString(this.message, buffer);
        buffer.writeByte(this.messageArgs.length);
        for (String arg : this.messageArgs)
            ByteBufUtils.writeString(arg, buffer);

        this.attachment.write(buffer);
        buffer.writeBoolean(this.pending);
    }

    @Override
    public void read(ByteBuf buffer) {
        this.messageId = buffer.readLong();
        this.type = EnumMail.values()[buffer.readByte()]; 
        this.senderUUID = ByteBufUtils.readUUID(buffer);
        this.senderName = ByteBufUtils.readString(buffer);
        this.subject = ByteBufUtils.readString(buffer);

        this.message = ByteBufUtils.readString(buffer);
        this.messageArgs = new String[buffer.readByte()];
        for (int i = 0; i < this.messageArgs.length; i++)
            this.messageArgs[i] = ByteBufUtils.readString(buffer);

        this.attachment = this.type.readAttachment(buffer);
        this.pending = buffer.readBoolean();
    }
}
