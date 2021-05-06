package austeretony.oxygen_market.client.gui.market;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.gui.base.GUIUtils;
import austeretony.oxygen_core.client.gui.base.core.Widget;
import austeretony.oxygen_core.client.gui.util.OxygenGUIUtils;
import austeretony.oxygen_core.client.preset.CurrencyProperties;
import austeretony.oxygen_core.client.settings.CoreSettings;
import austeretony.oxygen_core.client.util.MinecraftClient;
import austeretony.oxygen_core.common.util.objects.Point2F;
import austeretony.oxygen_market.client.market.ItemMarketData;
import austeretony.oxygen_market.common.config.MarketConfig;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ItemProfitabilityTooltip {

    private static final int
            TOOLTIP_WIDTH = 140,
            CHART_WIDTH = 100,
            CHART_HEIGHT = 50,
            CHART_X = 35,
            CHART_Y = 5;

    private final ItemMarketData itemMarketData;
    private final Map<Long, Point2F> chartPointsMap = new HashMap<>();

    private ItemProfitabilityTooltip(ItemMarketData itemMarketData) {
        this.itemMarketData = itemMarketData;
    }

    @Nonnull
    public static ItemProfitabilityTooltip create(ItemMarketData itemMarketData) {
        ItemProfitabilityTooltip tooltipData = new ItemProfitabilityTooltip(itemMarketData);
        tooltipData.init();
        return tooltipData;
    }

    private void init() {
        calculateTransactionsChartCoordinates();
    }

    private void calculateTransactionsChartCoordinates() {
        double minPrice = itemMarketData.getMinPrice();
        double maxPrice = itemMarketData.getMaxPrice();

        long trackingStartMillis = OxygenClient.getInstant()
                .minusMillis(TimeUnit.HOURS.toMillis(MarketConfig.SALES_HISTORY_ENTRY_EXPIRE_TIME_HOURS.asInt())).toEpochMilli();
        long trackingEndMillis = OxygenClient.getCurrentTimeMillis();

        Map<Long, Double> dataMap = itemMarketData.getTransactionIdsToUnitPriceMap();
        for (Map.Entry<Long, Double> entry : dataMap.entrySet()) {
            long id = entry.getKey();
            double unitPrice = entry.getValue();

            float xNormalized = (Math.max(id - trackingStartMillis, 0L)) / (float) (trackingEndMillis - trackingStartMillis);
            float yNormalized = (float) ((Math.max(unitPrice - minPrice, 0.0)) / (maxPrice - minPrice));

            chartPointsMap.put(id, Point2F.of(xNormalized * CHART_WIDTH, yNormalized * CHART_HEIGHT));
        }
    }

    public void draw(Widget widget, CurrencyProperties properties) {
        if (itemMarketData.getTransactionsAmount() < 3) return;

        int x = widget.getX() + widget.getWidth() - 80 - TOOLTIP_WIDTH;
        int y = widget.getY() + widget.getHeight();

        int height = Widget.TOOLTIP_HEIGHT * 3 + CHART_HEIGHT + 8;

        GUIUtils.pushMatrix();
        GUIUtils.translate(x, y);

        GUIUtils.drawRect(0, 0, TOOLTIP_WIDTH, height,
                GUIUtils.scaleAlpha(CoreSettings.COLOR_BACKGROUND_BASE.asInt(), 1F));
        GUIUtils.drawFrame(0, 0, TOOLTIP_WIDTH, height);

        String minPriceStr = MarketScreen.DECIMAL_FORMAT.format(itemMarketData.getMinPrice());
        String maxPriceStr = MarketScreen.DECIMAL_FORMAT.format(itemMarketData.getMaxPrice());
        String avgPriceStr = MarketScreen.DECIMAL_FORMAT.format(itemMarketData.getAveragePrice());

        float textScale = CoreSettings.SCALE_TEXT_ADDITIONAL.asFloat() - .05F;
        float textHeight = GUIUtils.getTextHeight(textScale);
        int textColor = CoreSettings.COLOR_TEXT_ADDITIONAL_ENABLED.asInt();

        float strWidth = GUIUtils.getTextWidth(maxPriceStr, textScale);
        GUIUtils.drawString(maxPriceStr, CHART_X - 11 - strWidth, CHART_Y, textScale, textColor, false);

        GUIUtils.colorDef();
        GUIUtils.drawTexturedRect(CHART_X - 10 + properties.getIconXOffset(),
                CHART_Y - (properties.getIconHeight() - textHeight) / 2F + properties.getIconYOffset(),
                properties.getIconWidth(), properties.getIconHeight(), properties.getIconTexture(), 0, 0,
                properties.getIconWidth(), properties.getIconHeight());

        float avgPriceY = CHART_Y + (float) ((itemMarketData.getAveragePrice() - itemMarketData.getMinPrice())
                / (itemMarketData.getMaxPrice() - itemMarketData.getMinPrice())) * (CHART_HEIGHT + 1);
        strWidth = GUIUtils.getTextWidth(avgPriceStr, textScale);
        GUIUtils.drawString(avgPriceStr, CHART_X - 11 - strWidth, avgPriceY - textHeight / 2F, textScale, textColor, false);

        strWidth = GUIUtils.getTextWidth(minPriceStr, textScale);
        GUIUtils.drawString(minPriceStr, CHART_X - 11 - strWidth, CHART_Y + CHART_HEIGHT + 1 - textHeight,
                textScale, textColor, false);

        GUIUtils.colorDef();
        GUIUtils.drawTexturedRect(CHART_X - 10 + properties.getIconXOffset(),
                CHART_Y + CHART_HEIGHT - textHeight - (properties.getIconHeight() - textHeight) / 2F + properties.getIconYOffset(),
                properties.getIconWidth(), properties.getIconHeight(), properties.getIconTexture(), 0, 0,
                properties.getIconWidth(), properties.getIconHeight());

        GUIUtils.drawFrame(CHART_X, CHART_Y, CHART_WIDTH, CHART_HEIGHT + 1);
        for (Point2F position : chartPointsMap.values()) {
            float pointX = CHART_X + position.getX();
            float pointY = CHART_Y + position.getY();
            GUIUtils.drawRect(pointX, pointY, pointX + .6F, pointY + .6F, CoreSettings.COLOR_TEXT_SPECIAL.asInt());
        }

        GUIUtils.drawRect(CHART_X, avgPriceY, CHART_X + CHART_WIDTH, avgPriceY + Widget.FRAME_WIDTH,
                CoreSettings.COLOR_SLIDER_ENABLED.asInt());

        Instant trackingStartTime = OxygenClient.getInstant()
                .minusMillis(TimeUnit.HOURS.toMillis(MarketConfig.SALES_HISTORY_ENTRY_EXPIRE_TIME_HOURS.asInt()));
        String trackingStartStr = OxygenGUIUtils.geTimePassedLocalizedString(trackingStartTime.toEpochMilli());
        GUIUtils.drawString(trackingStartStr, CHART_X, CHART_Y + CHART_HEIGHT + 4, textScale, textColor, false);

        String nowStr = MinecraftClient.localize("oxygen_market.gui.market.tooltip.now");
        GUIUtils.drawString(nowStr, CHART_X + CHART_WIDTH - GUIUtils.getTextWidth(nowStr, textScale),
                CHART_Y + CHART_HEIGHT + 4, textScale, textColor, false);

        GUIUtils.drawString(MinecraftClient.localize("oxygen_market.gui.market.tooltip.transactions_amount", itemMarketData.getTransactionsAmount()),
                6, CHART_Y + CHART_HEIGHT + 14, textScale, textColor, false);
        GUIUtils.drawString(MinecraftClient.localize("oxygen_market.gui.market.tooltip.sold_amount", itemMarketData.getTotalSoldAmount()),
                6, CHART_Y + CHART_HEIGHT + 22, textScale, textColor, false);

        GUIUtils.popMatrix();
    }
}
