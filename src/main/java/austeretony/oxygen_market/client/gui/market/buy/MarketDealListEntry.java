package austeretony.oxygen_market.client.gui.market.buy;

import austeretony.oxygen_core.client.gui.base.GUIUtils;
import austeretony.oxygen_core.client.gui.base.common.ListEntry;
import austeretony.oxygen_core.client.gui.util.OxygenGUIUtils;
import austeretony.oxygen_core.client.preset.CurrencyProperties;
import austeretony.oxygen_core.client.settings.CoreSettings;
import austeretony.oxygen_core.common.util.CommonUtils;
import austeretony.oxygen_market.client.MarketManagerClient;
import austeretony.oxygen_market.client.gui.market.MarketScreen;
import austeretony.oxygen_market.client.market.ItemMarketData;
import austeretony.oxygen_market.client.market.Profitability;
import austeretony.oxygen_market.common.config.MarketConfig;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class MarketDealListEntry extends ListEntry<CombinedDealsEntry> {

    private final int playerStock;
    private final DealProfitability profitability;
    private final CurrencyProperties properties;
    private final String expireTimeStr, priceStr, unitPriceStr;

    private State state = State.NORMAL;

    public MarketDealListEntry(@Nonnull CombinedDealsEntry entry, int playerStock, boolean canAfford,
                               DealProfitability dealProfitability, CurrencyProperties currencyProperties) {
        super("", entry);
        if (!canAfford) {
            state = State.OVERPRICED;
        }
        profitability = dealProfitability;
        this.playerStock = playerStock;

        expireTimeStr = OxygenGUIUtils.getExpirationTimeLocalizedString(entry.getDeal().getId(),
                TimeUnit.HOURS.toMillis(MarketConfig.DEAL_EXPIRE_TIME_HOURS.asInt()));
        priceStr = CommonUtils.formatCurrencyValue(entry.getDeal().getPrice());
        unitPriceStr = CommonUtils.formatDecimalCurrencyValue(MarketScreen.DECIMAL_FORMAT
                .format(entry.getDeal().getUnitPrice()));

        properties = currencyProperties;
    }

    public boolean isAvailable() {
        return state == State.NORMAL;
    }

    public boolean isDealExist(long dealId) {
        return entry.getDealsMap().containsKey(dealId);
    }

    public void purchase() {
        if (!entry.getDealsMap().isEmpty()) {
            MarketManagerClient.instance()
                    .purchase(Collections.singleton(entry.getDealsMap().keySet().iterator().next()));
        }
    }

    public void dealProcessed(long dealId, State newState) {
        entry.getDealsMap().remove(dealId);
        if (entry.getDealsMap().isEmpty()) {
            state = newState;
        }
    }

    public void checkIsOverpriced(long balance) {
        if (entry.getDeal().getPrice() > balance) {
            state = State.OVERPRICED;
        }
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        if (!isVisible()) return;

        int backgroundColor = getColorFromState(fill);
        if (state == State.PURCHASED) {
            backgroundColor = CoreSettings.COLOR_ELEMENT_ACTIVE.asInt();
        } else if (state == State.FAILED) {
            backgroundColor = CoreSettings.COLOR_ELEMENT_INACTIVE.asInt();
        }
        GUIUtils.drawRect(getX(), getY(), getX() + getWidth(), getY() + getHeight(), backgroundColor);

        ItemStack itemStack = entry.getDeal().getStackWrapper().getItemStackCached();
        GUIUtils.renderItemStack(itemStack, getX() + 2, getY(), CoreSettings.ENABLE_DURABILITY_BARS_GUI_DISPLAY.asBoolean());

        GUIUtils.pushMatrix();
        GUIUtils.translate(getX(), getY());

        float textScale = text.getScale() - .08F;
        if (entry.getDealsMap().size() > 1) {
            GUIUtils.drawString(String.valueOf(entry.getDealsMap().size()), 1, 1, textScale + .04F, getColorFromState(text), true);
        }

        if (isMouseOver()) {
            GUIUtils.drawString(String.valueOf(playerStock), 16, 1, textScale, getColorFromState(text), true);
        }
        if (entry.getDeal().getQuantity() > 1) {
            GUIUtils.drawString(String.valueOf(entry.getDeal().getQuantity()), 16, 10, textScale, getColorFromState(text), true);
        }

        int colorHex = getColorFromState(text);
        if (state == State.OVERPRICED) {
            colorHex = CoreSettings.COLOR_TEXT_INACTIVE.asInt();
        }
        String itemDisplayName = itemStack.getDisplayName();
        if (CoreSettings.ENABLE_RARITY_COLORS_GUI_DISPLAY.asBoolean()) {
            itemDisplayName = GUIUtils.getItemStackRarityColor(itemStack) + itemDisplayName;
        }
        GUIUtils.drawString(itemDisplayName, 30, 2, text.getScale() - .05F, colorHex, false);

        GUIUtils.drawString(String.valueOf(entry.getDeal().getSellerUsername()), 35, 10, textScale,
                CoreSettings.COLOR_TEXT_ADDITIONAL_ENABLED.asInt(), true);

        GUIUtils.drawString(expireTimeStr, getWidth() - 80, 2, text.getScale() - .05F,
                CoreSettings.COLOR_TEXT_ADDITIONAL_ENABLED.asInt(), false);
        if (profitability.getProfitability() != Profitability.NO_DATA) {
            GUIUtils.drawString(profitability.getDisplayProfitabilityPercent(), getWidth() - 80, 10, textScale,
                    profitability.getProfitability().getColorHex(), false);
        }

        float priceValueStrWidth = GUIUtils.getTextWidth(priceStr, textScale);
        GUIUtils.drawString(priceStr, getWidth() - 12 - priceValueStrWidth, 2, textScale,
                colorHex, false);

        if (entry.getDeal().getQuantity() > 1) {
            float unitPriceValueStrWidth = GUIUtils.getTextWidth(unitPriceStr, textScale - .05F);
            GUIUtils.drawString(unitPriceStr, getWidth() - 12 - unitPriceValueStrWidth, 10, textScale,
                    colorHex, false);
        }

        GUIUtils.colorDef();
        GUIUtils.drawTexturedRect(getWidth() - 10 + properties.getIconXOffset(),
                properties.getIconYOffset(), properties.getIconWidth(), properties.getIconHeight(),
                properties.getIconTexture(), 0, 0, properties.getIconWidth(), properties.getIconHeight());

        GUIUtils.popMatrix();
    }

    @Override
    public void drawForeground(int mouseX, int mouseY, float partialTicks) {
        if (!isVisible()) return;
        if (mouseX >= getX() + 2 && mouseY >= getY() && mouseX < getX() + 18 && mouseY < getY() + getHeight()) {
            drawToolTip(getX() + 30 - 3, getY() + 1, entry.getDeal().getStackWrapper().getItemStackCached());
        } else if (mouseX >= getX() + getWidth() - 80 && mouseY >= getY() + 10 && mouseX < getX() + getWidth() - 60
                && mouseY < getY() + getHeight()) {
            ItemMarketData itemMarketData = profitability.getItemMarketData();
            if (profitability.getProfitability() != Profitability.NO_DATA && itemMarketData != null) {
                itemMarketData.getTooltip().draw(this, properties);
            }
        }
    }

    public enum State {

        NORMAL,
        OVERPRICED,
        FAILED,
        PURCHASED
    }
}
