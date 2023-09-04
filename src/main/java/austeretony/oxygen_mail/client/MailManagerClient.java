package austeretony.oxygen_mail.client;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_mail.client.input.MailKeyHandler;

public final class MailManagerClient {

    private static MailManagerClient instance;

    private final MailboxContainerClient mailboxContainer = new MailboxContainerClient();

    private final MailboxManagerClient mailboxManager;

    private final MailMenuManager mailMenuManager = new MailMenuManager();

    private final MailKeyHandler keyHandler = new MailKeyHandler();

    private MailManagerClient() {
        this.mailboxManager = new MailboxManagerClient(this);
        OxygenHelperClient.registerPersistentData(this.mailboxContainer);
        CommonReference.registerEvent(this.keyHandler);
    }

    public static void create() {
        if (instance == null)
            instance = new MailManagerClient();
    }

    public static MailManagerClient instance() {
        return instance;
    }

    public MailboxContainerClient getMailboxContainer() {
        return this.mailboxContainer;
    }

    public MailboxManagerClient getMailboxManager() {
        return this.mailboxManager;
    }

    public MailMenuManager getMenuManager() {
        return this.mailMenuManager;
    }

    public MailKeyHandler getKeyHandler() {
        return this.keyHandler;
    }

    public void worldLoaded() {
        OxygenHelperClient.loadPersistentDataAsync(this.mailboxContainer);
    }
}