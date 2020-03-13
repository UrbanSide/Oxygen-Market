package austeretony.oxygen_market.client.gui.market;

import java.util.Collections;
import java.util.List;

import austeretony.alternateui.screen.button.GUIButton;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.gui.elements.OxygenCurrencyValue;
import austeretony.oxygen_core.client.gui.elements.OxygenDefaultBackgroundWithButtonsUnderlinedFiller;
import austeretony.oxygen_core.client.gui.elements.OxygenScrollablePanel;
import austeretony.oxygen_core.client.gui.elements.OxygenSectionSwitcher;
import austeretony.oxygen_core.client.gui.elements.OxygenTextLabel;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_market.client.MarketManagerClient;
import austeretony.oxygen_market.client.OfferClient;
import austeretony.oxygen_market.client.gui.market.offers.PlayerOfferPanelEntry;
import austeretony.oxygen_market.client.gui.market.selling.OffersAmount;

public class OffersSection extends AbstractGUISection {

    private final MarketMenuScreen screen;

    private OxygenTextLabel offersEmptyLabel;

    private OxygenScrollablePanel offersPanel;

    private OffersAmount offersAmount;

    private OxygenCurrencyValue totalPriceValue;

    public OffersSection(MarketMenuScreen screen) {
        super(screen);
        this.screen = screen;
        this.setDisplayText(ClientReference.localize("oxygen_market.gui.market.offers"));
    }

    @Override
    public void init() {
        this.addElement(new OxygenDefaultBackgroundWithButtonsUnderlinedFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new OxygenTextLabel(4, 12, ClientReference.localize("oxygen_market.gui.market.title"), EnumBaseGUISetting.TEXT_TITLE_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        this.addElement(this.offersAmount = new OffersAmount(6, this.getHeight() - 8));

        //player offers panel
        this.addElement(this.offersPanel = new OxygenScrollablePanel(this.screen, 6, 16, this.getWidth() - 15, 16, 1, 9, 9, EnumBaseGUISetting.TEXT_PANEL_SCALE.get().asFloat(), true));

        //sections switcher
        this.addElement(new OxygenSectionSwitcher(this.getWidth() - 4, 5, this, this.screen.getBuySection(), this.screen.getSellingSection(), this.screen.getSalesHistorySection()));

        this.addElement(this.totalPriceValue = new OxygenCurrencyValue(this.getWidth() - 29, this.getHeight() - 10));   
        this.totalPriceValue.setValue(OxygenMain.COMMON_CURRENCY_INDEX, this.screen.getBuySection().getBalanceValue().getValue());

        String offersEmpty = ClientReference.localize("oxygen_market.gui.market.noOffersFound");
        this.addElement(this.offersEmptyLabel = new OxygenTextLabel(((this.getWidth() - this.textWidth(offersEmpty, EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.05F)) / 2), 
                23, offersEmpty, EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.05F, EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()).setVisible(false));
    }

    public void updateOffersAmount() {
        this.offersAmount.setOffersAmount(this.screen.getSellingSection().getOffersAmountElement().getOffersAmount());
    }

    public void loadPlayerOffers() {
        List<OfferClient> offers = MarketManagerClient.instance().getOffersManager().getPlayerOffers();

        Collections.sort(offers, (o1, o2)->o2.getId() < o1.getId() ? - 1 : o2.getId() > o1.getId() ? 1 : 0);

        this.offersEmptyLabel.setVisible(offers.isEmpty());

        this.offersPanel.reset();
        long totalPrice = 0L;
        PlayerOfferPanelEntry entry;
        for (OfferClient offer : offers) {
            totalPrice += offer.getPrice();
            this.screen.calculateOfferProfitability(offer);
            this.offersPanel.addEntry(entry = new PlayerOfferPanelEntry(offer, this.screen.getCurrencyProperties()));
            if (this.screen.historySynchronized)
                entry.initProfitability(this.screen.getOfferProfitability(offer));
        }
        this.totalPriceValue.updateValue(totalPrice);

        this.offersPanel.getScroller().reset();
        this.offersPanel.getScroller().updateRowsAmount(MathUtils.clamp(offers.size(), 9, this.offersAmount.getMaxAmount()));
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {}

    public void offersSynchronized() {
        this.updateOffersAmount();
        this.loadPlayerOffers();
    }

    public void salesHistorySynchronized() {
        PlayerOfferPanelEntry entry;
        for (GUIButton button : this.offersPanel.buttonsBuffer) {
            entry = (PlayerOfferPanelEntry) button;
            entry.initProfitability(this.screen.getOfferProfitability(entry.getWrapped()));
        }
    }

    public void itemPurchased(OfferClient offer, long balance) {
        if (offer.isOwner(OxygenHelperClient.getPlayerUsername())) {
            this.offersAmount.decrementOffersAmount(1);
            this.loadPlayerOffers();
        }
    }

    public void offerCreated(OfferClient offer, long balance) {
        this.loadPlayerOffers();
        this.offersAmount.incrementOffersAmount(1);
    }

    public void offerCanceled(OfferClient offer, long balance) {
        this.loadPlayerOffers();
        this.offersAmount.decrementOffersAmount(1);
    }

    public OffersAmount getOffersAmountElement() {
        return this.offersAmount;
    }
}
