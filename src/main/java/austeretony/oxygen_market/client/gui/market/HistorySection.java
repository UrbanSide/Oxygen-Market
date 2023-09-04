package austeretony.oxygen_market.client.gui.market;

import java.util.List;

import org.lwjgl.input.Keyboard;

import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.api.OxygenGUIHelper;
import austeretony.oxygen_core.client.gui.elements.OxygenDefaultBackgroundWithButtonsUnderlinedFiller;
import austeretony.oxygen_core.client.gui.elements.OxygenDropDownList;
import austeretony.oxygen_core.client.gui.elements.OxygenDropDownList.OxygenDropDownListWrapperEntry;
import austeretony.oxygen_core.client.gui.elements.OxygenKeyButton;
import austeretony.oxygen_core.client.gui.elements.OxygenScrollablePanel;
import austeretony.oxygen_core.client.gui.elements.OxygenSectionSwitcher;
import austeretony.oxygen_core.client.gui.elements.OxygenTextLabel;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_market.client.MarketManagerClient;
import austeretony.oxygen_market.client.gui.market.history.EnumSalesHistoryFilter;
import austeretony.oxygen_market.client.gui.market.history.HistoryPanelEntry;
import austeretony.oxygen_market.client.market.SalesHistoryEntryClient;
import austeretony.oxygen_market.common.config.MarketConfig;
import net.minecraft.client.gui.ScaledResolution;

public class HistorySection extends AbstractGUISection {

    private final MarketMenuScreen screen;

    private OxygenKeyButton filterHistoryButton;

    private OxygenTextLabel entriesAmountLabel, offersEmptyLabel;

    private OxygenScrollablePanel historyEntriesPanel;

    private OxygenDropDownList filtersList;

    //cache

    private EnumSalesHistoryFilter currentFilter = EnumSalesHistoryFilter.PURCHASES;

    private int entriesAmount;

    public HistorySection(MarketMenuScreen screen) {
        super(screen);
        this.screen = screen;
        this.setDisplayText(ClientReference.localize("oxygen_market.gui.market.history"));
    }

    @Override
    public void init() {
        this.addElement(new OxygenDefaultBackgroundWithButtonsUnderlinedFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new OxygenTextLabel(4, 12, ClientReference.localize("oxygen_market.gui.market.title"), EnumBaseGUISetting.TEXT_TITLE_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        //panel
        this.addElement(this.historyEntriesPanel = new OxygenScrollablePanel(this.screen, 76, 16, this.getWidth() - 85, 16, 1, 100, 9, EnumBaseGUISetting.TEXT_PANEL_SCALE.get().asFloat(), true));

        //sections switcher
        this.addElement(new OxygenSectionSwitcher(this.getWidth() - 4, 5, this, this.screen.getBuySection(), this.screen.getSellingSection(), this.screen.getOffersSection()));

        //filter
        this.addElement(new OxygenTextLabel(6, 23, ClientReference.localize("oxygen_market.gui.market.filter"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
        this.filtersList = new OxygenDropDownList(6, 25, 67, this.currentFilter.localizedName());
        for (EnumSalesHistoryFilter filter : EnumSalesHistoryFilter.values())
            this.filtersList.addElement(new OxygenDropDownListWrapperEntry<EnumSalesHistoryFilter>(filter, filter.localizedName()));
        this.addElement(this.filtersList);

        //filter listener
        this.filtersList.<OxygenDropDownListWrapperEntry<EnumSalesHistoryFilter>>setElementClickListener((element)->{
            this.currentFilter = element.getWrapped();
        });

        this.addElement(this.filterHistoryButton = new OxygenKeyButton(0, this.getY() + this.getHeight() + this.screen.guiTop - 8, ClientReference.localize("oxygen_market.gui.market.button.filterHistory"), Keyboard.KEY_E, this::filterHistory).disable());

        this.addElement(this.entriesAmountLabel = new OxygenTextLabel(6, this.getHeight() - 15, "", EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.05F, EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()).disableFull());

        String offersEmpty = ClientReference.localize("oxygen_market.gui.market.noDataFound");
        this.addElement(this.offersEmptyLabel = new OxygenTextLabel(76 + ((this.historyEntriesPanel.getButtonWidth() - this.textWidth(offersEmpty, EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.05F)) / 2), 
                23, offersEmpty, EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.05F, EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()).setVisible(false));

        this.setFilterButtonState(true);
    }

    private void calculateButtonsHorizontalPosition() {
        ScaledResolution sr = new ScaledResolution(this.mc);
        this.filterHistoryButton.setX((sr.getScaledWidth() - (12 + this.textWidth(this.filterHistoryButton.getDisplayText(), this.filterHistoryButton.getTextScale()))) / 2 - this.screen.guiLeft);
    }

    public void filterHistory() {
        List<SalesHistoryEntryClient> entries = null;

        switch (this.currentFilter) {
        case PURCHASES:
            entries = MarketManagerClient.instance().getSalesHistoryManager().getPlayerPurchaseEntries();
            break;
        case SALES:
            entries = MarketManagerClient.instance().getSalesHistoryManager().getPlayerSaleEntries();
            break;
        }

        this.offersEmptyLabel.setVisible(entries.isEmpty());

        this.historyEntriesPanel.reset();
        String noteStr = null;
        for (SalesHistoryEntryClient entry : entries) {
            switch (this.currentFilter) {
            case PURCHASES:
                noteStr = ClientReference.localize("oxygen_market.gui.market.seller", entry.getSellerUsername());
                break;
            case SALES:
                noteStr = ClientReference.localize("oxygen_market.gui.market.buyer", entry.getBuyerUsername());
                break;
            }
            this.historyEntriesPanel.addEntry(new HistoryPanelEntry(entry, noteStr, this.screen.getCurrencyProperties()));
        }

        this.entriesAmountLabel.setDisplayText(String.format("%d/%d", entries.size(), this.entriesAmount));

        this.historyEntriesPanel.getScroller().reset();
        this.historyEntriesPanel.getScroller().updateRowsAmount(MathUtils.clamp(entries.size(), 9, 900));
    }

    public void setFilterButtonState(boolean flag) {
        this.filterHistoryButton.setEnabled(flag);
        if (flag) {
            this.entriesAmount = MarketManagerClient.instance().getSalesHistoryManager().getHistoryEntriesAmountForPlayer();
            this.entriesAmountLabel.enableFull();
            this.entriesAmountLabel.setDisplayText(String.format("0/%d", this.entriesAmount));
        }
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {   
        if (!this.hasCurrentCallback())
            if (OxygenGUIHelper.isOxygenMenuEnabled()) {
                if (keyCode == MarketMenuScreen.MARKET_MENU_ENTRY.getKeyCode())
                    this.screen.close();
            } else if (MarketConfig.ENABLE_MARKET_MENU_KEY.asBoolean() 
                    && keyCode == MarketManagerClient.instance().getKeyHandler().getTradeMenuKeybinding().getKeyCode())
                this.screen.close();
        return super.keyTyped(typedChar, keyCode); 
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (mouseButton == 0) {
            if (element == this.filterHistoryButton)
                this.filterHistory();
        }
    }

    public void salesHistorySynchronized() {
        this.setFilterButtonState(true);

        this.calculateButtonsHorizontalPosition();
    }
}
