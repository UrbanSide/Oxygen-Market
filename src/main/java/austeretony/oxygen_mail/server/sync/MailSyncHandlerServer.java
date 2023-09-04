package austeretony.oxygen_mail.server.sync;

import austeretony.oxygen_core.server.sync.DataSyncHandlerServer;
import austeretony.oxygen_mail.common.mail.MailEntry;
import austeretony.oxygen_mail.common.main.MailMain;
import austeretony.oxygen_mail.server.MailManagerServer;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;

public class MailSyncHandlerServer implements DataSyncHandlerServer<MailEntry> {

    @Override
    public int getDataId() {
        return MailMain.DATA_ID_MAIL;
    }

    @Override
    public boolean allowSync(UUID playerUUID) {
        return true;
    }

    @Nonnull
    @Override
    public Map<Long, MailEntry> getDataMap(UUID playerUUID) {
        return MailManagerServer.instance().getOrCreateMailbox(playerUUID).getMailMap();
    }
}
