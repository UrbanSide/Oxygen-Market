package austeretony.oxygen_trade.client.gui.trade;

import java.util.List;

import org.lwjgl.input.Keyboard;

import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.api.OxygenGUIHelper;
import austeretony.oxygen_core.client.gui.elements.OxygenButton;
import austeretony.oxygen_core.client.gui.elements.OxygenDropDownList;
import austeretony.oxygen_core.client.gui.elements.OxygenDropDownList.OxygenDropDownListEntry;
import austeretony.oxygen_core.client.gui.elements.OxygenScrollablePanel;
import austeretony.oxygen_core.client.gui.elements.OxygenSectionSwitcher;
import austeretony.oxygen_core.client.gui.elements.OxygenTextLabel;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_trade.client.SalesHistoryEntryClient;
import austeretony.oxygen_trade.client.TradeManagerClient;
import austeretony.oxygen_trade.client.gui.trade.buy.BuySectionBackgroundFiller;
import austeretony.oxygen_trade.client.gui.trade.history.EnumSalesHistoryFilter;
import austeretony.oxygen_trade.client.gui.trade.history.HistoryPanelEntry;
import austeretony.oxygen_trade.common.config.TradeConfig;

public class HistorySection extends AbstractGUISection {

    private final TradeMenuScreen screen;

    private OxygenButton filterButton;

    private OxygenTextLabel entriesAmountLabel, offersEmptyLabel;

    private OxygenScrollablePanel offersPanel;

    private OxygenDropDownList filtersList;

    //cache

    private EnumSalesHistoryFilter currentFilter = EnumSalesHistoryFilter.PURCHASES;

    private int entriesAmount;

    public HistorySection(TradeMenuScreen screen) {
        super(screen);
        this.screen = screen;
        this.setDisplayText(ClientReference.localize("oxygen_trade.gui.trade.history"));
    }

    @Override
    public void init() {
        this.addElement(new BuySectionBackgroundFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new OxygenTextLabel(4, 12, ClientReference.localize("oxygen_trade.gui.trade.title"), EnumBaseGUISetting.TEXT_TITLE_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        //panel
        this.addElement(this.offersPanel = new OxygenScrollablePanel(this.screen, 76, 16, this.getWidth() - 85, 16, 1, 100, 9, EnumBaseGUISetting.TEXT_PANEL_SCALE.get().asFloat(), true));

        //sections switcher
        this.addElement(new OxygenSectionSwitcher(this.getWidth() - 4, 5, this, this.screen.getBuySection(), this.screen.getSellingSection(), this.screen.getOffersSection()));

        //filter
        this.addElement(new OxygenTextLabel(6, 23, ClientReference.localize("oxygen_trade.gui.trade.filter"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
        this.filtersList = new OxygenDropDownList(6, 25, 67, this.currentFilter.localizedName());
        for (EnumSalesHistoryFilter filter : EnumSalesHistoryFilter.values())
            this.filtersList.addElement(new OxygenDropDownListEntry<EnumSalesHistoryFilter>(filter, filter.localizedName()));
        this.addElement(this.filtersList);

        //filter listener
        this.filtersList.<OxygenDropDownListEntry<EnumSalesHistoryFilter>>setClickListener((element)->{
            this.currentFilter = element.index;
        });

        this.addElement(this.filterButton = new OxygenButton(6, this.getHeight() - 12, 40, 10, ClientReference.localize("oxygen_trade.gui.trade.filterButton")).disable());
        this.filterButton.setKeyPressListener(Keyboard.KEY_E, ()->this.filterHistory());

        this.addElement(this.entriesAmountLabel = new OxygenTextLabel(6, this.getHeight() - 14, "", EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.05F, EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()).disableFull());

        String offersEmpty = ClientReference.localize("oxygen_trade.gui.trade.noDataFound");
        this.addElement(this.offersEmptyLabel = new OxygenTextLabel(76 + ((this.offersPanel.getButtonWidth() - this.textWidth(offersEmpty, EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.05F)) / 2), 
                23, offersEmpty, EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.05F, EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()).setVisible(false));

        this.setFilterButtonState(true);
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
        String noteStr = null;
        for (SalesHistoryEntryClient entry : entries) {
            switch (this.currentFilter) {
            case PURCHASES:
                noteStr = ClientReference.localize("oxygen_trade.gui.trade.seller", entry.getSellerUsername());
                break;
            case SALES:
                noteStr = ClientReference.localize("oxygen_trade.gui.trade.buyer", entry.getBuyerUsername());
                break;
            }
            this.offersPanel.addEntry(new HistoryPanelEntry(entry, noteStr, this.screen.getCurrencyProperties()));
        }

        this.entriesAmountLabel.setDisplayText(String.valueOf(entries.size()) + "/" + String.valueOf(this.entriesAmount));

        this.offersPanel.getScroller().reset();
        this.offersPanel.getScroller().updateRowsAmount(MathUtils.clamp(entries.size(), 9, 900));
    }

    public void setFilterButtonState(boolean flag) {
        this.filterButton.setEnabled(flag);
        if (flag) {
            this.entriesAmount = TradeManagerClient.instance().getSalesHistoryManager().getHistoryEntriesAmountForPlayer();
            this.entriesAmountLabel.enableFull();
            this.entriesAmountLabel.setDisplayText("0/" + String.valueOf(this.entriesAmount));
        }
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {   
        if (!this.hasCurrentCallback())
            if (OxygenGUIHelper.isOxygenMenuEnabled()) {
                if (keyCode == TradeMenuScreen.TRADE_MENU_ENTRY.getKeyCode())
                    this.screen.close();
            } else if (TradeConfig.ENABLE_TRADE_MENU_KEY.asBoolean() 
                    && keyCode == TradeManagerClient.instance().getKeyHandler().getTradeMenuKeybinding().getKeyCode())
                this.screen.close();
        return super.keyTyped(typedChar, keyCode); 
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (mouseButton == 0) {
            if (element == this.filterButton)
                this.filterHistory();
        }
    }

    public void salesHistorySynchronized() {
        this.setFilterButtonState(true);
    }
}
