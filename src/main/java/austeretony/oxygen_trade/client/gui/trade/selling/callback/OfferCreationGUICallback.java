package austeretony.oxygen_trade.client.gui.trade.selling.callback;

import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.gui.elements.CurrencyValueGUIElement;
import austeretony.oxygen_core.client.gui.elements.OxygenCallbackGUIFiller;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIButton;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIText;
import austeretony.oxygen_core.client.gui.settings.GUISettings;
import austeretony.oxygen_trade.client.TradeManagerClient;
import austeretony.oxygen_trade.client.gui.trade.SellingGUISection;
import austeretony.oxygen_trade.client.gui.trade.TradeMenuGUIScreen;
import austeretony.oxygen_trade.client.gui.trade.selling.ItemGUIElement;

public class OfferCreationGUICallback extends AbstractGUICallback {

    private final TradeMenuGUIScreen screen;

    private final SellingGUISection section;

    private OxygenGUIButton confirmButton, cancelButton;

    private ItemGUIElement itemElement;

    private CurrencyValueGUIElement priceElement;

    private int postage;

    public OfferCreationGUICallback(TradeMenuGUIScreen screen, SellingGUISection section, int width, int height) {
        super(screen, section, width, height);
        this.screen = screen;
        this.section = section;
    }

    @Override
    public void init() {
        this.addElement(new OxygenCallbackGUIFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new OxygenGUIText(4, 5, ClientReference.localize("oxygen_trade.gui.trade.callback.offerCreation.title"), GUISettings.get().getTextScale(), GUISettings.get().getEnabledTextColor()));

        this.addElement(this.itemElement = new ItemGUIElement(6, 18));
        this.addElement(new OxygenGUIText(32, 24, ClientReference.localize("oxygen_trade.gui.trade.callback.offerCreation.for"), GUISettings.get().getSubTextScale(), GUISettings.get().getEnabledTextColorDark()));
        this.addElement(this.priceElement = new CurrencyValueGUIElement(70, 24));   

        this.addElement(this.confirmButton = new OxygenGUIButton(15, this.getHeight() - 12, 40, 10, ClientReference.localize("oxygen.gui.confirmButton"))); 
        this.addElement(this.cancelButton = new OxygenGUIButton(this.getWidth() - 55, this.getHeight() - 12, 40, 10, ClientReference.localize("oxygen.gui.cancelButton"))); 
    }

    @Override
    public void onOpen() {
        if (this.section.getCurrentItemButton() != null) {
            this.itemElement.setItemStack(this.section.getCurrentItemButton().index, (int) this.section.getAmountTextField().getTypedNumber());
            this.priceElement.setValue(this.section.getTotalPriceTextField().getTypedNumber());
            this.priceElement.setX(this.getX() + 50 + this.textWidth(this.priceElement.getDisplayText(), GUISettings.get().getSubTextScale() - 0.05F));
        }
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (mouseButton == 0) { 
            if (element == this.cancelButton)
                this.close();
            else if (element == this.confirmButton) {
                TradeManagerClient.instance().getOffersManager().createOfferSynced(
                        this.section.getCurrentItemButton().stackWrapper, 
                        (int) this.section.getAmountTextField().getTypedNumber(), 
                        this.section.getTotalPriceTextField().getTypedNumber());
                this.close();
            }
        }
    }
}
