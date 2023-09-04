package austeretony.oxygen_mail.server.network.operation;

import austeretony.oxygen_core.common.network.operation.NetworkOperationsHandler;
import austeretony.oxygen_core.common.util.ByteBufUtils;
import austeretony.oxygen_mail.common.mail.Attachment;
import austeretony.oxygen_mail.common.mail.AttachmentType;
import austeretony.oxygen_mail.common.main.MailMain;
import austeretony.oxygen_mail.common.network.operation.MailOperation;
import austeretony.oxygen_mail.server.MailManagerServer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.UUID;

public class MailNetworkOperationsHandlerServer implements NetworkOperationsHandler {

    @Override
    public int getId() {
        return MailMain.MAIL_OPERATIONS_HANDLER_ID;
    }

    @Override
    public void process(EntityPlayer player, int operationIndex, ByteBuf buffer) {
        MailOperation operation = getEnum(MailOperation.values(), operationIndex);
        if (operation == null) return;

        if (operation == MailOperation.SEND_MAIL) {
            UUID addresseeUUID = ByteBufUtils.readUUID(buffer);
            String subject = ByteBufUtils.readString(buffer);
            String message = ByteBufUtils.readString(buffer);
            AttachmentType attachmentType = AttachmentType.values()[buffer.readByte()];
            Attachment attachment = attachmentType.read(buffer);

            MailManagerServer.instance().sendMail((EntityPlayerMP) player, addresseeUUID, subject, message, attachment);
        } else if (operation == MailOperation.RETURN_MAIL) {
            long entryId = buffer.readLong();

            MailManagerServer.instance().returnMail((EntityPlayerMP) player, entryId);
        } else if (operation == MailOperation.REMOVE_MAIL) {
            long entryId = buffer.readLong();

            MailManagerServer.instance().removeMail((EntityPlayerMP) player, entryId);
        } else if (operation == MailOperation.RECEIVE_ATTACHMENT) {
            long entryId = buffer.readLong();

            MailManagerServer.instance().receiveAttachment((EntityPlayerMP) player, entryId);
        }
    }
}
