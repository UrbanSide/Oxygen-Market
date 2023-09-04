package austeretony.oxygen_mail.common.network.client;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_mail.client.MailManagerClient;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;

public class CPMessageRemoved extends Packet {

    private long messageId;

    public CPMessageRemoved() {}

    public CPMessageRemoved(long messageId) {
        this.messageId = messageId;
    }

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {
        buffer.writeLong(this.messageId);
    }

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        final long messageId = buffer.readLong();
        OxygenHelperClient.addRoutineTask(()->MailManagerClient.instance().getMailboxManager().messageRemoved(messageId));
    }
}
