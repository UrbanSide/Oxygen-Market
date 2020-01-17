package austeretony.oxygen_trade.client.gui.history;

import austeretony.alternateui.screen.core.AbstractGUIScreen;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.core.GUIWorkspace;
import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.oxygen_core.client.OxygenManagerClient;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.currency.CurrencyProperties;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_trade.common.main.TradeMain;

public class SalesHistoryScreen extends AbstractGUIScreen {

    private SalesHistorySection historySection;

    private final CurrencyProperties currencyProperties = OxygenManagerClient.instance().getCurrencyManager().getProperties(OxygenMain.COMMON_CURRENCY_INDEX);

    public SalesHistoryScreen() {
        OxygenHelperClient.syncData(TradeMain.SALES_HISTORY_DATA_ID);
    }

    @Override
    protected GUIWorkspace initWorkspace() {
        return new GUIWorkspace(this, 320, 183).setAlignment(EnumGUIAlignment.CENTER, 0, 0);
    }

    @Override
    protected void initSections() {
        this.getWorkspace().initSection(this.historySection = new SalesHistorySection());
    }

    @Override
    protected AbstractGUISection getDefaultSection() {
        return this.historySection;
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element) {}

    @Override
    protected boolean doesGUIPauseGame() {
        return false;
    } 

    public void salesHistorySynchronized() {
        this.historySection.salesHistorySynchronized();
    }

    public CurrencyProperties getCurrencyProperties() {
        return this.currencyProperties;
    }
}
