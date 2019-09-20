package austeretony.oxygen_trade.client.gui.trade;

import java.util.List;

import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIButton;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIButtonPanel;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIDDList;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIDDListElement;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIText;
import austeretony.oxygen_core.client.gui.elements.SectionsGUIDDList;
import austeretony.oxygen_core.client.gui.settings.GUISettings;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_trade.client.SalesHistoryEntryClient;
import austeretony.oxygen_trade.client.TradeManagerClient;
import austeretony.oxygen_trade.client.gui.trade.buy.BuySectionGUIFiller;
import austeretony.oxygen_trade.client.gui.trade.history.EnumSalesHistoryFilter;
import austeretony.oxygen_trade.client.gui.trade.history.SalesHistoryEntryGUIButton;

public class SalesHistoryGUISection extends AbstractGUISection {

    private final TradeMenuGUIScreen screen;

    private OxygenGUIButton filterButton;

    private OxygenGUIText entriesAmountLabel, offersEmptyLabel;

    private OxygenGUIButtonPanel offersPanel;

    private OxygenGUIDDList filterDDList;

    //cache

    private EnumSalesHistoryFilter currentFilter = EnumSalesHistoryFilter.PURCHASES;

    private int playerEntriesAmount;

    public SalesHistoryGUISection(TradeMenuGUIScreen screen) {
        super(screen);
        this.screen = screen;
    }

    @Override
    public void init() {
        this.addElement(new BuySectionGUIFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new OxygenGUIText(4, 5, ClientReference.localize("oxygen_trade.gui.trade.title"), GUISettings.get().getTitleScale(), GUISettings.get().getEnabledTextColor()));

        //panel
        this.addElement(this.offersPanel = new OxygenGUIButtonPanel(this.screen, 76, 16, this.getWidth() - 85, 16, 1, 9, 9, GUISettings.get().getSubTextScale(), true));

        //sections switcher
        this.addElement(new SectionsGUIDDList(this.getWidth() - 4, 5, this, this.screen.getBuySection(), this.screen.getSellingSection(), this.screen.getOffersSection()));

        //filter
        this.addElement(new OxygenGUIText(6, 18, ClientReference.localize("oxygen_trade.gui.trade.filter"), GUISettings.get().getSubTextScale(), GUISettings.get().getEnabledTextColorDark()));
        this.filterDDList = new OxygenGUIDDList(6, 25, 75, 9, EnumSalesHistoryFilter.PURCHASES.localizedName());
        for (EnumSalesHistoryFilter filter : EnumSalesHistoryFilter.values())
            this.filterDDList.addElement(new OxygenGUIDDListElement<EnumSalesHistoryFilter>(filter, filter.localizedName()));
        this.addElement(this.filterDDList);

        //filter listener
        this.filterDDList.<OxygenGUIDDListElement<EnumSalesHistoryFilter>>setClickListener((element)->{
            this.currentFilter = element.index;
        });

        this.addElement(this.filterButton = new OxygenGUIButton(20, this.getHeight() - 14, 40, 10, ClientReference.localize("oxygen_trade.gui.trade.filterButton")).disable());
        this.addElement(this.entriesAmountLabel = new OxygenGUIText(0, this.getHeight() - 22, "", GUISettings.get().getSubTextScale() - 0.05F, GUISettings.get().getEnabledTextColor()).disableFull());

        String offersEmpty = ClientReference.localize("oxygen_trade.gui.trade.noDataFound");
        this.addElement(this.offersEmptyLabel = new OxygenGUIText(76 + ((this.offersPanel.getButtonWidth() - this.textWidth(offersEmpty, GUISettings.get().getSubTextScale() - 0.05F)) / 2), 
                20, offersEmpty, GUISettings.get().getSubTextScale() - 0.05F, GUISettings.get().getEnabledTextColorDark()).setVisible(false));
    }

    public void filterHistory() {
        List<SalesHistoryEntryClient> entries = null;

        switch (this.currentFilter) {
        case PURCHASES:
            entries = TradeManagerClient.instance().getSalesHistoryManager().getPlayerPurchaseEntries();
            break;
        case SALES:
            entries = TradeManagerClient.instance().getSalesHistoryManager().getPlayerSaleEntries();
            break;
        }

        this.offersEmptyLabel.setVisible(entries.isEmpty());

        this.offersPanel.reset();
        for (SalesHistoryEntryClient entry : entries)
            this.offersPanel.addButton(new SalesHistoryEntryGUIButton(entry, this.currentFilter));

        this.offersPanel.getScroller().resetPosition();
        this.offersPanel.getScroller().getSlider().reset();

        this.offersPanel.getScroller().updateRowsAmount(MathUtils.clamp(entries.size(), 9, 3000));

        this.entriesAmountLabel.setDisplayText(String.valueOf(entries.size()) + "/" + String.valueOf(this.playerEntriesAmount));
        this.entriesAmountLabel.setX((80 - this.textWidth(this.entriesAmountLabel.getDisplayText(), this.entriesAmountLabel.getTextScale())) / 2);
    }

    public void enableFilterButton() {
        this.filterButton.enable();
        this.playerEntriesAmount = TradeManagerClient.instance().getSalesHistoryManager().getHistoryEntriesAmountForPlayer();
        this.entriesAmountLabel.enableFull();
        this.entriesAmountLabel.setDisplayText("0/" + String.valueOf(this.playerEntriesAmount));
        this.entriesAmountLabel.setX((80 - this.textWidth(this.entriesAmountLabel.getDisplayText(), this.entriesAmountLabel.getTextScale())) / 2);
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (mouseButton == 0) {
            if (element == this.filterButton)
                this.filterHistory();
        }
    }
}
