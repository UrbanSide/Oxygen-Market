package austeretony.oxygen_mail.client.network.operation;

import austeretony.oxygen_core.common.network.operation.NetworkOperationsHandler;
import austeretony.oxygen_mail.client.MailManagerClient;
import austeretony.oxygen_mail.client.gui.mail.MailScreen;
import austeretony.oxygen_mail.common.main.MailMain;
import austeretony.oxygen_mail.common.network.operation.MailOperation;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

public class MailNetworkOperationsHandlerClient implements NetworkOperationsHandler {

    @Override
    public int getId() {
        return MailMain.MAIL_OPERATIONS_HANDLER_ID;
    }

    @Override
    public void process(EntityPlayer player, int operationIndex, ByteBuf buffer) {
        MailOperation operation = getEnum(MailOperation.values(), operationIndex);
        if (operation == null) return;

        if (operation == MailOperation.MAIL_SENT) {
            int currencyIndex = buffer.readByte();
            long balance = buffer.readLong();

            MailScreen.mailSent(currencyIndex, balance);
        } else if (operation == MailOperation.MAIL_REMOVED) {
            long entryId = buffer.readLong();

            MailManagerClient.instance().mailRemoved(entryId);
        } else if (operation == MailOperation.ATTACHMENT_RECEIVED) {
            long oldEntryId = buffer.readLong();
            long newEntryId = buffer.readLong();
            int currencyIndex = buffer.readInt();
            long balance = buffer.readLong();

            MailManagerClient.instance().attachmentReceived(oldEntryId, newEntryId, currencyIndex, balance);
        }
    }
}
