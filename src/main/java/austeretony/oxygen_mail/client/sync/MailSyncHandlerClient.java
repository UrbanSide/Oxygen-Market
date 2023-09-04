package austeretony.oxygen_mail.client.sync;

import austeretony.oxygen_core.client.sync.DataSyncHandlerClient;
import austeretony.oxygen_core.client.sync.DataSyncListener;
import austeretony.oxygen_mail.client.MailManagerClient;
import austeretony.oxygen_mail.client.gui.mail.MailScreen;
import austeretony.oxygen_mail.common.mail.MailEntry;
import austeretony.oxygen_mail.common.main.MailMain;

import javax.annotation.Nullable;
import java.util.Map;

public class MailSyncHandlerClient implements DataSyncHandlerClient<MailEntry> {

    @Override
    public int getDataId() {
        return MailMain.DATA_ID_MAIL;
    }

    @Override
    public Class<MailEntry> getSynchronousEntryClass() {
        return MailEntry.class;
    }

    @Nullable
    @Override
    public Map<Long, MailEntry> getDataMap() {
        return MailManagerClient.instance().getMailMap();
    }

    @Override
    public void clear() {
        MailManagerClient.instance().reset();
    }

    @Override
    public void save() {
        MailManagerClient.instance().markChanged();
    }

    @Nullable
    @Override
    public DataSyncListener getSyncListener() {
        return updated -> MailScreen.dataSynchronized();
    }
}
