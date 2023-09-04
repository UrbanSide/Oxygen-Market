package austeretony.oxygen_mail.common.network.server;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_mail.common.EnumMessageOperation;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.main.MailMain;
import austeretony.oxygen_mail.server.MailManagerServer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;

public class SPMessageOperation extends Packet {

    private long messageId;

    private int ordinal;

    public SPMessageOperation() {}

    public SPMessageOperation(long messageId, EnumMessageOperation operation) {
        this.messageId = messageId;
        this.ordinal = operation.ordinal();
    }

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {
        buffer.writeByte(this.ordinal);
        buffer.writeLong(this.messageId);
    }

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        final EntityPlayerMP playerMP = getEntityPlayerMP(netHandler);
        if ((MailConfig.ENABLE_MAIL_ACCESS_CLIENTSIDE.asBoolean() || OxygenHelperServer.checkTimeOut(CommonReference.getPersistentUUID(playerMP), MailMain.MAIL_TIMEOUT_ID))
                && OxygenHelperServer.isNetworkRequestAvailable(CommonReference.getPersistentUUID(playerMP), MailMain.MESSAGE_OPERATION_REQUEST_ID)) {
            final int ordinal = buffer.readByte();
            final long messageId = buffer.readLong();
            if (ordinal >= 0 && ordinal < EnumMessageOperation.values().length)
                OxygenHelperServer.addRoutineTask(()->MailManagerServer.instance().getMailboxesManager()
                        .processMessageOperation(playerMP, messageId, EnumMessageOperation.values()[ordinal]));
        }
    }
}
