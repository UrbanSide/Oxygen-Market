package austeretony.oxygen_mail.server;

import java.util.Set;
import java.util.UUID;

import austeretony.oxygen_core.server.sync.DataSyncHandlerServer;
import austeretony.oxygen_mail.common.mail.Mail;
import austeretony.oxygen_mail.common.main.MailMain;

public class MailDataSyncHandlerServer implements DataSyncHandlerServer<Mail> {

    @Override
    public int getDataId() {
        return MailMain.MAIL_DATA_ID;
    }

    @Override
    public boolean allowSync(UUID playerUUID) {
        return true;
    }

    @Override
    public Set<Long> getIds(UUID playerUUID) {
        return MailManagerServer.instance().getMailboxesContainer().getPlayerMailboxSafe(playerUUID).getMessagesIds();
    }

    @Override
    public Mail getEntry(UUID playerUUID, long entryId) {
        return MailManagerServer.instance().getMailboxesContainer().getPlayerMailboxSafe(playerUUID).getMessage(entryId);
    }
}
