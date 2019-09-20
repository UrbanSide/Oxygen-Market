package austeretony.oxygen_trade.client.gui.trade.buy.context;

import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIContextMenuElement.ContextMenuAction;
import austeretony.oxygen_trade.client.TradeManagerClient;
import austeretony.oxygen_trade.client.gui.trade.buy.OfferGUIButton;

public class PurchaseItemContextAction implements ContextMenuAction {

    @Override
    public String getName(GUIBaseElement currElement) {
        return ClientReference.localize("oxygen_trade.gui.trade.context.purchase");
    }

    @Override
    public boolean isValid(GUIBaseElement currElement) {
        OfferGUIButton button = (OfferGUIButton) currElement;
        return !button.isOverpriced() && !button.isPurchased();
    }

    @Override
    public void execute(GUIBaseElement currElement) {
        TradeManagerClient.instance().getOffersManager().purchaseItemSynced(((OfferGUIButton) currElement).index);
    }
}
