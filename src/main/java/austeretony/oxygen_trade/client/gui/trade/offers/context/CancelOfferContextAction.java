package austeretony.oxygen_trade.client.gui.trade.offers.context;

import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIContextMenuElement.ContextMenuAction;
import austeretony.oxygen_trade.client.TradeManagerClient;
import austeretony.oxygen_trade.client.gui.trade.offers.PlayerOfferGUIButton;

public class CancelOfferContextAction implements ContextMenuAction {

    @Override
    public String getName(GUIBaseElement currElement) {
        return ClientReference.localize("oxygen_trade.gui.trade.context.cancel");
    }

    @Override
    public boolean isValid(GUIBaseElement currElement) {
        return true;
    }

    @Override
    public void execute(GUIBaseElement currElement) {
        TradeManagerClient.instance().getOffersManager().cancelOfferSynced(((PlayerOfferGUIButton) currElement).index);
    }
}
