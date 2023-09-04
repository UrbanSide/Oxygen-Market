package austeretony.oxygen_mail.common.network.client;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_mail.client.MailManagerClient;
import austeretony.oxygen_mail.common.mail.Mail;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;

public class CPAttachmentReceived extends Packet {

    private Mail mail;

    private long oldMessageId, balance;

    public CPAttachmentReceived() {}

    public CPAttachmentReceived(long oldMessageId, Mail mail, long balance) {
        this.oldMessageId = oldMessageId;
        this.mail = mail;
        this.balance = balance;
    }

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {
        buffer.writeLong(this.oldMessageId);
        this.mail.write(buffer);
        buffer.writeLong(this.balance);
    }

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        final long oldMessageId = buffer.readLong();
        final Mail mail = new Mail();
        mail.read(buffer);
        final long balance = buffer.readLong();
        OxygenHelperClient.addRoutineTask(()->MailManagerClient.instance().getMailboxManager().attachmentReceived(oldMessageId, mail, balance));
    }
}
