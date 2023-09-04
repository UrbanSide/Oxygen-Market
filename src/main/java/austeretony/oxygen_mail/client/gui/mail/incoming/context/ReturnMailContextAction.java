package austeretony.oxygen_mail.client.gui.mail.incoming.context;

import austeretony.oxygen_core.client.gui.base.context.ContextAction;
import austeretony.oxygen_core.client.gui.base.core.Callback;
import austeretony.oxygen_core.client.gui.base.core.Section;
import austeretony.oxygen_core.client.gui.base.special.callback.YesNoCallback;
import austeretony.oxygen_mail.client.MailManagerClient;
import austeretony.oxygen_mail.common.mail.MailEntry;

import javax.annotation.Nonnull;

public class ReturnMailContextAction implements ContextAction<MailEntry> {

    @Nonnull
    @Override
    public String getName(MailEntry entry) {
        return "oxygen_mail.gui.mail.incoming.context.return_mail";
    }

    @Override
    public boolean isValid(MailEntry entry) {
        return entry.isPending() && entry.getAttachment().canBeReturned(entry);
    }

    @Override
    public void execute(MailEntry entry) {
        Callback callback = new YesNoCallback(
                "oxygen_mail.gui.mail.incoming.callback.return_mail",
                "oxygen_mail.gui.mail.incoming.callback.return_mail.message",
                () -> MailManagerClient.instance().returnMail(entry.getId()));
        Section.tryOpenCallback(callback);
    }
}
