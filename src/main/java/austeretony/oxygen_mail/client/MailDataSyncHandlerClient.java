package austeretony.oxygen_mail.client;

import java.util.Set;

import austeretony.oxygen_core.client.sync.DataSyncHandlerClient;
import austeretony.oxygen_core.client.sync.DataSyncListener;
import austeretony.oxygen_mail.common.mail.Mail;
import austeretony.oxygen_mail.common.main.MailMain;

public class MailDataSyncHandlerClient implements DataSyncHandlerClient<Mail> {

    @Override
    public int getDataId() {
        return MailMain.MAIL_DATA_ID;
    }

    @Override
    public Class<Mail> getDataContainerClass() {
        return Mail.class;
    }

    @Override
    public Set<Long> getIds() {
        return MailManagerClient.instance().getMailboxContainer().getMessagesIds();
    }

    @Override
    public void clearData() {
        MailManagerClient.instance().getMailboxContainer().reset();
    }

    @Override
    public Mail getEntry(long entryId) {
        return MailManagerClient.instance().getMailboxContainer().getMessage(entryId);
    }

    @Override
    public void addEntry(Mail entry) {
        MailManagerClient.instance().getMailboxContainer().addMessage(entry);
    }

    @Override
    public void save() {
        MailManagerClient.instance().getMailboxContainer().setChanged(true);
    }

    @Override
    public DataSyncListener getSyncListener() {
        return (updated)->MailManagerClient.instance().getMenuManager().mailSynchronized();
    }
}
