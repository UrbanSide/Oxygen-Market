package austeretony.oxygen_mail.client.gui.mail.incoming.context;

import austeretony.oxygen_core.client.gui.base.context.ContextAction;
import austeretony.oxygen_core.client.gui.base.core.Callback;
import austeretony.oxygen_core.client.gui.base.core.Section;
import austeretony.oxygen_core.client.gui.base.special.callback.YesNoCallback;
import austeretony.oxygen_mail.client.MailManagerClient;
import austeretony.oxygen_mail.common.mail.MailEntry;

import javax.annotation.Nonnull;

public class RemoveMailContextAction implements ContextAction<MailEntry> {

    @Nonnull
    @Override
    public String getName(MailEntry entry) {
        return "oxygen_mail.gui.mail.incoming.context.remove_mail";
    }

    @Override
    public boolean isValid(MailEntry entry) {
        return !entry.isPending();
    }

    @Override
    public void execute(MailEntry entry) {
        Callback callback = new YesNoCallback(
                "oxygen_mail.gui.mail.incoming.callback.remove_mail",
                "oxygen_mail.gui.mail.incoming.callback.remove_mail.message",
                () -> MailManagerClient.instance().removeMail(entry.getId()));
        Section.tryOpenCallback(callback);
    }
}
