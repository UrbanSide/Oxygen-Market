package austeretony.oxygen_mail.common.network.client;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_mail.client.MailManagerClient;
import austeretony.oxygen_mail.common.mail.Attachment;
import austeretony.oxygen_mail.common.mail.EnumMail;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;

public class CPMailSent extends Packet {

    private int ordinal;

    private Attachment attachment;

    private long balance;

    public CPMailSent() {}

    public CPMailSent(EnumMail type, Attachment attachment, long balance) {
        this.ordinal = type.ordinal();
        this.attachment = attachment;
        this.balance = balance;
    }

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {
        buffer.writeByte(this.ordinal);
        this.attachment.write(buffer);
        buffer.writeLong(this.balance);
    }

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        final EnumMail type = EnumMail.values()[buffer.readByte()];
        final Attachment attachment = type.readAttachment(buffer);
        final long balance = buffer.readLong();
        OxygenHelperClient.addRoutineTask(()->MailManagerClient.instance().getMailboxManager().mailSent(type, attachment, balance));
    }
}
