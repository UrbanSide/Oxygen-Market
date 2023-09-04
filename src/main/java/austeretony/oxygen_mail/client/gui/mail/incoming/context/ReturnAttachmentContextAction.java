package austeretony.oxygen_mail.client.gui.mail.incoming.context;

import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.gui.elements.OxygenContextMenu.OxygenContextMenuAction;
import austeretony.oxygen_mail.client.gui.mail.IncomingMailSection;
import austeretony.oxygen_mail.common.mail.EnumMail;

public class ReturnAttachmentContextAction implements OxygenContextMenuAction {

    private final IncomingMailSection section;

    public ReturnAttachmentContextAction(IncomingMailSection section) {
        this.section = section;
    }

    @Override
    public String getLocalizedName(GUIBaseElement currElement) {
        return ClientReference.localize("oxygen_mail.gui.context.return");
    }

    @Override
    public boolean isValid(GUIBaseElement currElement) {
        if (!this.section.getCurrentMessageEntry().getWrapped().isSystemMessage()
                && this.section.getCurrentMessageEntry().getWrapped().isPending()) {
            EnumMail type = this.section.getCurrentMessageEntry().getWrapped().getType();
            if (type == EnumMail.REMITTANCE 
                    || type == EnumMail.PARCEL 
                    || type == EnumMail.COD) {
                return true;
            }         
        }
        return false;
    }

    @Override
    public void execute(GUIBaseElement currElement) {
        this.section.openReturnAttachmentCallback();
    }
}
