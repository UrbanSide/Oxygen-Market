package austeretony.oxygen_trade.client.gui.trade;

import java.util.Collections;
import java.util.List;

import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIButtonPanel;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIContextMenu;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIText;
import austeretony.oxygen_core.client.gui.elements.SectionsGUIDDList;
import austeretony.oxygen_core.client.gui.settings.GUISettings;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_trade.client.PlayerOfferClient;
import austeretony.oxygen_trade.client.TradeManagerClient;
import austeretony.oxygen_trade.client.gui.trade.offers.OffersSectionGUIFiller;
import austeretony.oxygen_trade.client.gui.trade.offers.PlayerOfferGUIButton;
import austeretony.oxygen_trade.client.gui.trade.offers.TotalPriceGUIElement;
import austeretony.oxygen_trade.client.gui.trade.offers.context.CancelOfferContextAction;
import austeretony.oxygen_trade.client.gui.trade.selling.OffersAmountGUIElement;

public class OffersGUISection extends AbstractGUISection {

    private final TradeMenuGUIScreen screen;

    private OxygenGUIText offersEmptyLabel;

    private OxygenGUIButtonPanel playerOffersPanel;

    private OffersAmountGUIElement offersAmountElement;

    private TotalPriceGUIElement totalPriceElement;

    public OffersGUISection(TradeMenuGUIScreen screen) {
        super(screen);
        this.screen = screen;
    }

    @Override
    public void init() {
        this.addElement(new OffersSectionGUIFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new OxygenGUIText(4, 5, ClientReference.localize("oxygen_trade.gui.trade.title"), GUISettings.get().getTitleScale(), GUISettings.get().getEnabledTextColor()));

        this.addElement(this.offersAmountElement = new OffersAmountGUIElement(4, this.getHeight() - 10));

        //player offers panel
        this.addElement(this.playerOffersPanel = new OxygenGUIButtonPanel(this.screen, 6, 16, this.getWidth() - 15, 16, 1, 9, 9, GUISettings.get().getSubTextScale(), true));

        this.playerOffersPanel.initContextMenu(new OxygenGUIContextMenu(GUISettings.get().getContextMenuWidth(), 9, 
                new CancelOfferContextAction()));

        //sections switcher
        this.addElement(new SectionsGUIDDList(this.getWidth() - 4, 5, this, this.screen.getBuySection(), this.screen.getSellingSection(), this.screen.getSalesHistorySection()));

        this.addElement(this.totalPriceElement = new TotalPriceGUIElement(this.getWidth() - 19, this.getHeight() - 10));   
        this.totalPriceElement.setValue(this.screen.getBuySection().getBalanceElement().getValue());

        String offersEmpty = ClientReference.localize("oxygen_trade.gui.trade.noOffersFound");
        this.addElement(this.offersEmptyLabel = new OxygenGUIText(((this.getWidth() - this.textWidth(offersEmpty, GUISettings.get().getSubTextScale() - 0.05F)) / 2), 
                20, offersEmpty, GUISettings.get().getSubTextScale() - 0.05F, GUISettings.get().getEnabledTextColorDark()).setVisible(false));
    }

    public void updateOffersAmount() {
        this.offersAmountElement.setOffersAmount(this.screen.getSellingSection().getOffersAmountElement().getOffersAmount());
    }

    public void loadPlayerOffers() {
        List<PlayerOfferClient> offers = TradeManagerClient.instance().getOffersManager().getPlayerOffers();

        Collections.sort(offers, (o1, o2)->((int) ((o1.getId() - o2.getId()) / 1_000L)));

        this.offersEmptyLabel.setVisible(offers.isEmpty());

        this.playerOffersPanel.reset();
        int totalPrice = 0;
        for (PlayerOfferClient offer : offers) {
            totalPrice += offer.getPrice();
            this.playerOffersPanel.addButton(new PlayerOfferGUIButton(offer));
        }
        this.totalPriceElement.setValue(totalPrice);

        this.playerOffersPanel.getScroller().resetPosition();
        this.playerOffersPanel.getScroller().getSlider().reset();

        this.playerOffersPanel.getScroller().updateRowsAmount(MathUtils.clamp(offers.size(), 9, this.offersAmountElement.getMaxAmount()));
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {}

    public void itemPurchased(PlayerOfferClient offer, long balance) {
        if (offer.isOwner(OxygenHelperClient.getPlayerUsername())) {
            this.offersAmountElement.decrementOffersAmount(1);
            this.loadPlayerOffers();
        }
    }

    public void offerCreated(PlayerOfferClient offer, long balance) {
        this.loadPlayerOffers();
        this.offersAmountElement.incrementOffersAmount(1);
    }

    public void offerCanceled(PlayerOfferClient offer, long balance) {
        this.loadPlayerOffers();
        this.offersAmountElement.decrementOffersAmount(1);
    }

    public OffersAmountGUIElement getOffersAmountElement() {
        return this.offersAmountElement;
    }
}
