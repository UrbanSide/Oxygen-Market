package austeretony.oxygen_mail.client.gui.mail.incoming.context;

import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.gui.elements.OxygenContextMenu.OxygenContextMenuAction;
import austeretony.oxygen_mail.client.gui.mail.IncomingMailSection;
import austeretony.oxygen_mail.common.mail.EnumMail;

public class TakeAttachmentContextAction implements OxygenContextMenuAction {

    private final IncomingMailSection section;

    public TakeAttachmentContextAction(IncomingMailSection section) {
        this.section = section;
    }

    @Override
    public String getLocalizedName(GUIBaseElement currElement) {
        return this.section.getCurrentMessageEntry().getWrapped().getType() == EnumMail.COD ? 
                ClientReference.localize("oxygen_mail.gui.context.pay") : ClientReference.localize("oxygen_mail.gui.context.take");
    }

    @Override
    public boolean isValid(GUIBaseElement currElement) {
        return this.section.getTakeAttachmentButton().isEnabled();
    }

    @Override
    public void execute(GUIBaseElement currElement) {
        this.section.openTakeAttachmentCallback();
    }
}
