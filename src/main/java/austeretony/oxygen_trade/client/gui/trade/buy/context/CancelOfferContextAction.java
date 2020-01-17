package austeretony.oxygen_trade.client.gui.trade.buy.context;

import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.gui.elements.OxygenContextMenu.OxygenContextMenuAction;
import austeretony.oxygen_core.client.gui.elements.OxygenIndexedPanelEntry;
import austeretony.oxygen_trade.client.OfferClient;
import net.minecraft.client.entity.EntityPlayerSP;

public class CancelOfferContextAction implements OxygenContextMenuAction {

    @Override
    public String getLocalizedName(GUIBaseElement currElement) {
        return ClientReference.localize("oxygen_core.gui.cancel");
    }

    @Override
    public boolean isValid(GUIBaseElement currElement) {
        return true;
    }

    @Override
    public void execute(GUIBaseElement currElement) {
        ((EntityPlayerSP) ClientReference.getClientPlayer()).sendChatMessage(
                String.format("/oxygens trade -cancel-offer %s true", ((OxygenIndexedPanelEntry<OfferClient>) currElement).index.getId()));
    }
}
