package austeretony.oxygen_mail.client.gui.mail.incoming.context;

import austeretony.oxygen_core.client.gui.base.context.ContextAction;
import austeretony.oxygen_core.client.gui.base.core.Callback;
import austeretony.oxygen_core.client.gui.base.core.Section;
import austeretony.oxygen_core.client.gui.base.special.callback.YesNoCallback;
import austeretony.oxygen_mail.client.MailManagerClient;
import austeretony.oxygen_mail.common.mail.MailEntry;

import javax.annotation.Nonnull;

public class ReceiveAttachmentContextAction implements ContextAction<MailEntry> {

    @Nonnull
    @Override
    public String getName(MailEntry entry) {
        return "oxygen_mail.gui.mail.incoming.context.receive_attachment";
    }

    @Override
    public boolean isValid(MailEntry entry) {
        return entry.isPending();
    }

    @Override
    public void execute(MailEntry entry) {
        Callback callback = new YesNoCallback(
                "oxygen_mail.gui.mail.incoming.callback.receive_attachment",
                "oxygen_mail.gui.mail.incoming.callback.receive_attachment.message",
                () -> MailManagerClient.instance().receiveAttachment(entry.getId()));
        Section.tryOpenCallback(callback);
    }
}
