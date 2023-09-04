package austeretony.oxygen_market.client.gui.market;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.api.PrivilegesClient;
import austeretony.oxygen_core.client.gui.base.Keys;
import austeretony.oxygen_core.client.gui.base.Layer;
import austeretony.oxygen_core.client.gui.base.Texts;
import austeretony.oxygen_core.client.gui.base.background.Background;
import austeretony.oxygen_core.client.gui.base.block.Text;
import austeretony.oxygen_core.client.gui.base.button.VerticalSlider;
import austeretony.oxygen_core.client.gui.base.core.Section;
import austeretony.oxygen_core.client.gui.base.list.ScrollableList;
import austeretony.oxygen_core.client.gui.base.special.CurrencyValue;
import austeretony.oxygen_core.client.gui.base.special.KeyButton;
import austeretony.oxygen_core.client.gui.base.special.SectionSwitcher;
import austeretony.oxygen_core.client.gui.base.text.TextLabel;
import austeretony.oxygen_core.client.gui.util.OxygenGUIUtils;
import austeretony.oxygen_core.client.settings.CoreSettings;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_market.client.MarketManagerClient;
import austeretony.oxygen_market.client.gui.market.buy.CombinedDealsEntry;
import austeretony.oxygen_market.client.gui.market.my_deals.MyDealListEntry;
import austeretony.oxygen_market.client.gui.market.my_deals.callback.CreateDealCallback;
import austeretony.oxygen_market.common.config.MarketConfig;
import austeretony.oxygen_market.common.main.MarketPrivileges;
import austeretony.oxygen_market.common.market.Deal;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MyDealsSection extends Section {

    private final MarketScreen screen;

    private ScrollableList<CombinedDealsEntry> dealsList;
    private TextLabel dealsAmountLabel, noDealsLabel;
    private CurrencyValue totalPriceValue;
    private KeyButton createDealButton;

    public MyDealsSection(@Nonnull MarketScreen screen) {
        super(screen, localize("oxygen_market.gui.market.section.my_deals"), true);
        this.screen = screen;
    }

    @Override
    public void init() {
        addWidget(new Background.UnderlinedTitleBottomButtons(this));
        addWidget(new TextLabel(4, 12, Texts.title("oxygen_market.gui.market.title")));
        addWidget(new SectionSwitcher(this));

        addWidget(dealsList = new ScrollableList<>(6, 16, 9, getWidth() - 6 * 2 - 3, 16));
        VerticalSlider slider = new VerticalSlider(6 + getWidth() - 6 * 2 - 3 + 1, 16, 2, 16 * 9 + 8);
        addWidget(slider);
        dealsList.setSlider(slider);

        Text text =  Texts.additionalDark("oxygen_market.gui.market.my_deals.label.deals_amount");
        addWidget(new TextLabel(6, getHeight() - 5, text));
        addWidget(dealsAmountLabel = new TextLabel(6 + (int) text.getWidth() + 4, getHeight() - 5, Texts.additional("")));
        addWidget(noDealsLabel = new TextLabel(6, 23, Texts.additionalDark("oxygen_market.gui.market.label.my_deals.no_deals"))
                .setVisible(false));

        addWidget(totalPriceValue = new CurrencyValue(getWidth() - 27, getHeight() - 10)
                .setCurrency(OxygenMain.CURRENCY_COINS, 0L));

        String keyButtonText = localize("oxygen_market.gui.market.my_deals.button.create_deal");
        addWidget(createDealButton = new KeyButton(0, 0, Keys.ACTION_KEY, keyButtonText)
                .setLayer(Layer.FRONT)
                .setPressListener(() -> openCallback(new CreateDealCallback()))
                .setEnabled(false));
        OxygenGUIUtils.calculateBottomCenteredOffscreenButtonPosition(createDealButton, 1, 1);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        OxygenGUIUtils.closeScreenOnKeyPress(getScreen(), keyCode);
        super.keyTyped(typedChar, keyCode);
    }

    protected void dataSynchronized() {
        updateDeals();
    }

    private void updateDeals() {
        List<CombinedDealsEntry> combinedDeals = screen.getCombinedDealsList()
                .stream()
                .filter(e -> e.getDeal().getSellerUUID().equals(OxygenClient.getClientPlayerUUID()))
                .sorted(Comparator.comparingLong(CombinedDealsEntry::getId).reversed())
                .collect(Collectors.toList());
        List<Deal> myDeals = MarketManagerClient.instance().getClientPlayerDeals();

        int maxDeals = PrivilegesClient.getInt(MarketPrivileges.MAX_DEALS_PER_PLAYER.getId(), MarketConfig.MAX_DEALS_PER_PLAYER.asInt());

        dealsAmountLabel.getText().setText(myDeals.size() + "/" + maxDeals);
        if (myDeals.size() < maxDeals) {
            dealsAmountLabel.getText().setColorEnabled(CoreSettings.COLOR_TEXT_ADDITIONAL_ENABLED.asInt());
        } else {
            dealsAmountLabel.getText().setColorEnabled(CoreSettings.COLOR_TEXT_INACTIVE.asInt());
        }

        noDealsLabel.setEnabled(myDeals.isEmpty());
        totalPriceValue.setValue(getTotalCost(myDeals));
        createDealButton.setEnabled(screen.hasAccessToMarket() && myDeals.size() < maxDeals);

        dealsList.clear();
        for (CombinedDealsEntry combinedDeal : combinedDeals) {
            dealsList.addElement(new MyDealListEntry(combinedDeal, screen.getDealProfitability(combinedDeal.getDeal()), screen.getCurrencyProperties()));
        }
    }

    private static long getTotalCost(List<Deal> deals) {
        long totalPrice = 0L;
        for (Deal deal : deals) {
            totalPrice += deal.getPrice();
        }
        return totalPrice;
    }

    protected void dealsCreated(int dealsQuantity, Deal deal, long balance) {
        updateDeals();
        if (getCurrentCallback() instanceof CreateDealCallback) {
            if (!createDealButton.isEnabled()) {
                closeCallback();
            }
        }
    }

    protected void dealCanceled(Set<Long> dealsIds) {
        int position = dealsList.getScrollPosition();
        updateDeals();
        dealsList.setScrollPosition(position);
    }

    protected void purchased(Set<Long> dealsIds, long balance) {
        updateDeals();
    }

    protected void purchaseFailed(Set<Long> dealsIds) {}
}
