package austeretony.oxygen_market.client.gui.market.my_deals;

import austeretony.oxygen_core.client.gui.base.GUIUtils;
import austeretony.oxygen_core.client.gui.base.Textures;
import austeretony.oxygen_core.client.gui.base.block.Texture;
import austeretony.oxygen_core.client.gui.base.button.ImageButton;
import austeretony.oxygen_core.client.gui.base.common.ListEntry;
import austeretony.oxygen_core.client.gui.base.core.Callback;
import austeretony.oxygen_core.client.gui.base.core.Section;
import austeretony.oxygen_core.client.gui.base.core.Widget;
import austeretony.oxygen_core.client.gui.base.special.callback.SelectQuantityCallback;
import austeretony.oxygen_core.client.gui.util.OxygenGUIUtils;
import austeretony.oxygen_core.client.preset.CurrencyProperties;
import austeretony.oxygen_core.client.settings.CoreSettings;
import austeretony.oxygen_core.common.util.CommonUtils;
import austeretony.oxygen_market.client.MarketManagerClient;
import austeretony.oxygen_market.client.gui.market.MarketScreen;
import austeretony.oxygen_market.client.gui.market.buy.CombinedDealsEntry;
import austeretony.oxygen_market.client.gui.market.buy.DealProfitability;
import austeretony.oxygen_market.client.market.ItemMarketData;
import austeretony.oxygen_market.client.market.Profitability;
import austeretony.oxygen_market.common.config.MarketConfig;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MyDealListEntry extends ListEntry<CombinedDealsEntry> {

    private final DealProfitability profitability;
    private final CurrencyProperties properties;
    private final String expireTimeStr, priceStr, unitPriceStr;

    public MyDealListEntry(@Nonnull CombinedDealsEntry entry, DealProfitability dealProfitability,
                           CurrencyProperties currencyProperties) {
        super("", entry);
        profitability = dealProfitability;

        expireTimeStr = OxygenGUIUtils.getExpirationTimeLocalizedString(entry.getId(),
                TimeUnit.HOURS.toMillis(MarketConfig.DEAL_EXPIRE_TIME_HOURS.asInt()));
        priceStr = CommonUtils.formatCurrencyValue(entry.getDeal().getPrice());
        unitPriceStr = CommonUtils.formatDecimalCurrencyValue(MarketScreen.DECIMAL_FORMAT
                .format(entry.getDeal().getUnitPrice()));

        properties = currencyProperties;
    }

    @Override
    public void init() {
        addWidget(new ImageButton(getWidth() - 11, 6, 6, 6,
                Texture.builder()
                        .texture(Textures.CROSS_ICONS)
                        .imageSize(18, 6)
                        .size(6, 6)
                        .build(),
                localize("oxygen_market.gui.market.my_deals.button.cancel_deal"))
                .setMouseClickListener((mouseX, mouseY, button) -> {
                    if (entry.getDealsMap().size() == 1) {
                        MarketManagerClient.instance()
                                .cancelDeals(Collections.singleton(entry.getDealsMap().keySet().iterator().next()));
                    } else {
                        Callback callback = new SelectQuantityCallback(
                                "oxygen_market.gui.market.my_deals.callback.select_cancel_quantity",
                                "oxygen_market.gui.market.my_deals.callback.select_cancel_quantity.message",
                                1,
                                entry.getDealsMap().size(),
                                1,
                                selected -> MarketManagerClient.instance()
                                        .cancelDeals(entry.getDealsMap().keySet().stream().limit(selected).collect(Collectors.toSet())));
                        Section.tryOpenCallback(callback);
                    }
                }));
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        if (!isVisible()) return;

        GUIUtils.drawRect(getX(), getY(), getX() + getWidth(), getY() + getHeight(), getColorFromState(fill));

        ItemStack itemStack = entry.getDeal().getStackWrapper().getItemStackCached();
        GUIUtils.renderItemStack(itemStack, getX() + 2, getY(), CoreSettings.ENABLE_DURABILITY_BARS_GUI_DISPLAY.asBoolean());

        GUIUtils.pushMatrix();
        GUIUtils.translate(getX(), getY());

        float textScale = text.getScale() - .08F;
        if (entry.getDealsMap().size() > 1) {
            GUIUtils.drawString(String.valueOf(entry.getDealsMap().size()), 1, 1, textScale + .04F, getColorFromState(text), true);
        }

        if (entry.getDeal().getQuantity() > 1) {
            GUIUtils.drawString(String.valueOf(entry.getDeal().getQuantity()), 16, 10, textScale, getColorFromState(text), true);
        }

        int colorHex = getColorFromState(text);
        String itemDisplayName = itemStack.getDisplayName();
        if (CoreSettings.ENABLE_RARITY_COLORS_GUI_DISPLAY.asBoolean()) {
            itemDisplayName = GUIUtils.getItemStackRarityColor(itemStack) + itemDisplayName;
        }
        GUIUtils.drawString(itemDisplayName, 30, 2, text.getScale() - .05F, colorHex, false);

        GUIUtils.drawString(String.valueOf(entry.getDeal().getSellerUsername()), 35, 10, textScale,
                CoreSettings.COLOR_TEXT_ADDITIONAL_ENABLED.asInt(), true);

        GUIUtils.drawString(expireTimeStr, getWidth() - 100 - 11, 2, text.getScale() - .05F,
                CoreSettings.COLOR_TEXT_ADDITIONAL_ENABLED.asInt(), false);
        if (profitability.getProfitability() != Profitability.NO_DATA) {
            GUIUtils.drawString(profitability.getDisplayProfitabilityPercent(), getWidth() - 100 - 11, 10, textScale,
                    profitability.getProfitability().getColorHex(), false);
        }

        float priceValueStrWidth = GUIUtils.getTextWidth(priceStr, textScale);
        GUIUtils.drawString(priceStr, getWidth() - 12 - 11 - priceValueStrWidth, 2, textScale,
                colorHex, false);

        if (entry.getDeal().getQuantity() > 1) {
            float unitPriceValueStrWidth = GUIUtils.getTextWidth(unitPriceStr, textScale - .05F);
            GUIUtils.drawString(unitPriceStr, getWidth() - 12 - 11 - unitPriceValueStrWidth, 10, textScale,
                    colorHex, false);
        }

        GUIUtils.colorDef();
        GUIUtils.drawTexturedRect(getWidth() - 10 - 11 + properties.getIconXOffset(),
                properties.getIconYOffset(), properties.getIconWidth(), properties.getIconHeight(),
                properties.getIconTexture(), 0, 0, properties.getIconWidth(), properties.getIconHeight());

        mouseX -= getX();
        mouseY -= getY();

        for (Widget widget : getWidgets()) {
            widget.draw(mouseX, mouseY, partialTicks);
        }

        GUIUtils.popMatrix();
    }

    @Override
    public void drawForeground(int mouseX, int mouseY, float partialTicks) {
        super.drawForeground(mouseX, mouseY, partialTicks);
        if (!isVisible()) return;
        if (mouseX >= getX() + 2 && mouseY >= getY() && mouseX < getX() + 18 && mouseY < getY() + getHeight()) { ;
            drawToolTip(getX() + 30 - 3, getY() + 1, entry.getDeal().getStackWrapper().getItemStackCached());
        } else if (profitability.getProfitability() != Profitability.NO_DATA
                && mouseX >= getX() + getWidth() - 100 - 11 && mouseY >= getY() + 10 && mouseX < getX() + getWidth() - 80 - 11 && mouseY < getY() + getHeight()) {
            ItemMarketData itemMarketData = profitability.getItemMarketData();
            if (profitability.getProfitability() != Profitability.NO_DATA && itemMarketData != null) {
                itemMarketData.getTooltip().draw(this, properties);
            }
        }
    }
}
