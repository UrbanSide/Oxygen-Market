package austeretony.oxygen_mail.client.gui.mail.incoming.context;

import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.gui.elements.OxygenContextMenu.OxygenContextMenuAction;
import austeretony.oxygen_mail.client.gui.mail.IncomingMailSection;

public class RemoveMessageContextAction implements OxygenContextMenuAction {

    private final IncomingMailSection section;

    public RemoveMessageContextAction(IncomingMailSection section) {
        this.section = section;
    }

    @Override
    public String getLocalizedName(GUIBaseElement currElement) {
        return ClientReference.localize("oxygen_mail.gui.context.remove");
    }

    @Override
    public boolean isValid(GUIBaseElement currElement) {
        return this.section.getRemoveMessageButton().isEnabled();
    }

    @Override
    public void execute(GUIBaseElement currElement) {
        this.section.openRemoveMessageCallback();
    }
}
