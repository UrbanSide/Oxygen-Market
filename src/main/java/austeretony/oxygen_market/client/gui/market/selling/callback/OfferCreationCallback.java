package austeretony.oxygen_market.client.gui.market.selling.callback;

import org.lwjgl.input.Keyboard;

import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.gui.elements.OxygenCallbackBackgroundFiller;
import austeretony.oxygen_core.client.gui.elements.OxygenCurrencyValue;
import austeretony.oxygen_core.client.gui.elements.OxygenKeyButton;
import austeretony.oxygen_core.client.gui.elements.OxygenTextLabel;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_market.client.MarketManagerClient;
import austeretony.oxygen_market.client.gui.market.MarketMenuScreen;
import austeretony.oxygen_market.client.gui.market.SellingSection;
import austeretony.oxygen_market.client.gui.market.selling.SelectedItem;

public class OfferCreationCallback extends AbstractGUICallback {

    private final MarketMenuScreen screen;

    private final SellingSection section;

    private OxygenKeyButton confirmButton, cancelButton;

    private SelectedItem selectedItem;

    private OxygenCurrencyValue priceValue;

    private int postage;

    public OfferCreationCallback(MarketMenuScreen screen, SellingSection section, int width, int height) {
        super(screen, section, width, height);
        this.screen = screen;
        this.section = section;
    }

    @Override
    public void init() {
        this.enableDefaultBackground(EnumBaseGUISetting.FILL_CALLBACK_COLOR.get().asInt());
        this.addElement(new OxygenCallbackBackgroundFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new OxygenTextLabel(4, 12, ClientReference.localize("oxygen_market.gui.market.callback.offerCreation.title"), EnumBaseGUISetting.TEXT_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        this.addElement(this.selectedItem = new SelectedItem(6, 18));
        this.addElement(this.priceValue = new OxygenCurrencyValue(6, 36));
        this.priceValue.setValue(OxygenMain.COMMON_CURRENCY_INDEX, 0L);

        this.addElement(this.confirmButton = new OxygenKeyButton(15, this.getHeight() - 10, ClientReference.localize("oxygen_core.gui.confirm"), Keyboard.KEY_R, this::confirm));
        this.addElement(this.cancelButton = new OxygenKeyButton(this.getWidth() - 55, this.getHeight() - 10, ClientReference.localize("oxygen_core.gui.cancel"), Keyboard.KEY_X, this::close));
    }

    @Override
    public void onOpen() {
        if (this.section.getCurrentItemButton() != null) {
            this.selectedItem.setItemStack(this.section.getCurrentItemButton().getWrapped().getCachedItemStack(), (int) this.section.getCurrentAmount());
            this.priceValue.updateValue(this.section.getCurrentTotalPrice());
            this.priceValue.setX(this.getX() + 8 + this.textWidth(this.priceValue.getDisplayText(), this.priceValue.getTextScale()));
        }
    }

    private void confirm() {
        MarketManagerClient.instance().getOffersManager().createOfferSynced(
                this.section.getCurrentItemButton().getWrapped(), 
                (int) this.section.getCurrentAmount(), 
                this.section.getCurrentTotalPrice());
        this.close();
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (mouseButton == 0) { 
            if (element == this.cancelButton)
                this.close();
            else if (element == this.confirmButton)
                this.confirm();
        }
    }
}
