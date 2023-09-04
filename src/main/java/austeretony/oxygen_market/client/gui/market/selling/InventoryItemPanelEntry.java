package austeretony.oxygen_market.client.gui.market.selling;

import austeretony.alternateui.screen.core.GUIAdvancedElement;
import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.alternateui.util.UIUtils;
import austeretony.oxygen_core.client.api.EnumBaseClientSetting;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.currency.CurrencyProperties;
import austeretony.oxygen_core.client.gui.OxygenGUIUtils;
import austeretony.oxygen_core.client.gui.elements.OxygenWrapperPanelEntry;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.util.OxygenUtils;
import austeretony.oxygen_market.client.MarketDataManagerClient.ItemStackMarketData;
import austeretony.oxygen_market.client.gui.market.MarketMenuScreen;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;

public class InventoryItemPanelEntry extends OxygenWrapperPanelEntry<ItemStackWrapper> {

    //properties
    private String playerStockStr, averageMarketPriceStr;

    private final boolean singleItem, enableDurabilityBar;

    //currency
    private CurrencyProperties currencyProperties;

    //cache
    private int playerStock;

    //widget
    private final int tooltipBackgroundColor, tooltipFrameColor;

    //market data
    private String marketDataTooltipStr;

    private float averageMarketPrice;

    public InventoryItemPanelEntry(ItemStackWrapper stackWrapper, int playerStock, CurrencyProperties properties) {
        super(stackWrapper);
        this.playerStock = playerStock;
        this.playerStockStr = String.valueOf(playerStock);

        this.singleItem = playerStock == 1;

        this.currencyProperties = properties;

        this.enableDurabilityBar = EnumBaseClientSetting.ENABLE_ITEMS_DURABILITY_BAR.get().asBoolean();
        this.setDisplayText(EnumBaseClientSetting.ENABLE_RARITY_COLORS.get().asBoolean() ? stackWrapper.getCachedItemStack().getRarity().color + stackWrapper.getCachedItemStack().getDisplayName() : stackWrapper.getCachedItemStack().getDisplayName());
        this.setDynamicBackgroundColor(EnumBaseGUISetting.ELEMENT_ENABLED_COLOR.get().asInt(), EnumBaseGUISetting.ELEMENT_DISABLED_COLOR.get().asInt(), EnumBaseGUISetting.ELEMENT_HOVERED_COLOR.get().asInt());
        this.setTextDynamicColor(EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt(), EnumBaseGUISetting.TEXT_DISABLED_COLOR.get().asInt(), EnumBaseGUISetting.TEXT_HOVERED_COLOR.get().asInt());
        this.setTooltipScaleFactor(EnumBaseGUISetting.TEXT_TOOLTIP_SCALE.get().asFloat());
        this.tooltipBackgroundColor = EnumBaseGUISetting.BACKGROUND_BASE_COLOR.get().asInt();
        this.tooltipFrameColor = EnumBaseGUISetting.BACKGROUND_ADDITIONAL_COLOR.get().asInt();
        this.requireDoubleClick();
    }

    public void initMarketData(ItemStackMarketData marketData) {
        if (marketData != null) {
            if (marketData.getAveragePrice() > 0.0F)
                this.averageMarketPriceStr = OxygenUtils.formatDecimalCurrencyValue(MarketMenuScreen.DECIMAL_FORMAT.format(marketData.getAveragePrice()));
            this.averageMarketPrice = marketData.getAveragePrice();
            this.marketDataTooltipStr = "(" + String.valueOf(marketData.getCompletedTransactionsAmount()) +
                    "/" + String.valueOf(marketData.getTotalItemsSoldAmount()) +
                    "): " + String.valueOf(MarketMenuScreen.DECIMAL_FORMAT.format(marketData.getAveragePrice()));
        }
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        if (this.isVisible()) {
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.enableDepth();
            this.itemRender.renderItemAndEffectIntoGUI(this.wrapped.getCachedItemStack(), this.getX() + 2, this.getY());

            if (this.enableDurabilityBar) {
                FontRenderer font = this.wrapped.getCachedItemStack().getItem().getFontRenderer(this.wrapped.getCachedItemStack());
                if (font == null)
                    font = this.mc.fontRenderer;
                this.itemRender.renderItemOverlayIntoGUI(font, this.wrapped.getCachedItemStack(), this.getX() + 2, this.getY(), null);
            }

            GlStateManager.disableDepth();
            RenderHelper.disableStandardItemLighting();

            GlStateManager.pushMatrix();
            GlStateManager.translate(this.getX(), this.getY(), 0.0F);
            GlStateManager.scale(this.getScale(), this.getScale(), 0.0F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            int color = this.getEnabledBackgroundColor();
            if (!this.isEnabled())
                color = this.getDisabledBackgroundColor();
            else if (this.isHovered() || this.isToggled())
                color = this.getHoveredBackgroundColor();

            int third = this.getWidth() / 3;

            OxygenGUIUtils.drawGradientRect(0.0D, 0.0D, third, this.getHeight(), 0x00000000, color, EnumGUIAlignment.RIGHT);
            drawRect(third, 0, this.getWidth() - third, this.getHeight(), color);
            OxygenGUIUtils.drawGradientRect(this.getWidth() - third, 0.0D, this.getWidth(), this.getHeight(), 0x00000000, color, EnumGUIAlignment.LEFT);

            color = this.getEnabledTextColor();
            if (!this.isEnabled())
                color = this.getDisabledTextColor();
            else if (this.isHovered() || this.isToggled())
                color = this.getHoveredTextColor();

            if (!this.singleItem) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(16.0F, 10.0F, 0.0F);
                GlStateManager.scale(this.getTextScale(), this.getTextScale(), 0.0F);

                this.mc.fontRenderer.drawString(this.playerStockStr, 0, 0, color, true);

                GlStateManager.popMatrix();
            }

            if (this.averageMarketPriceStr != null) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(this.getWidth() - 12.0F - this.textWidth(this.averageMarketPriceStr, this.getTextScale()), (this.getHeight() - this.textHeight(this.getTextScale())) / 2.0F + 0.5F, 0.0F);
                GlStateManager.scale(this.getTextScale(), this.getTextScale(), 0.0F);

                this.mc.fontRenderer.drawString(this.averageMarketPriceStr, 0, 0, color, false);

                GlStateManager.popMatrix();

                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

                GlStateManager.enableBlend();
                this.mc.getTextureManager().bindTexture(this.currencyProperties.getIcon());
                GUIAdvancedElement.drawCustomSizedTexturedRect(this.getWidth() - 10 + this.currencyProperties.getXOffset(), (this.getHeight() - this.currencyProperties.getIconHeight()) / 2 + this.currencyProperties.getYOffset(), 0, 0, this.currencyProperties.getIconWidth(), this.currencyProperties.getIconHeight(), this.currencyProperties.getIconWidth(), this.currencyProperties.getIconHeight());
                GlStateManager.disableBlend();
            }

            GlStateManager.pushMatrix();
            GlStateManager.translate(31.0F, (this.getHeight() - this.textHeight(this.getTextScale())) / 2.0F, 0.0F);
            GlStateManager.scale(this.getTextScale() + 0.05F, this.getTextScale() + 0.05F, 0.0F);

            this.mc.fontRenderer.drawString(this.getDisplayText(), 0, 0, color, false);

            GlStateManager.popMatrix();

            GlStateManager.popMatrix();
        }
    }

    @Override
    public void drawTooltip(int mouseX, int mouseY) {
        if (mouseX >= this.getX() + 2 && mouseY >= this.getY() && mouseX < this.getX() + 18 && mouseY < this.getY() + this.getHeight())
            this.screen.drawToolTip(this.wrapped.getCachedItemStack(), mouseX + 6, mouseY);
        else if (this.marketDataTooltipStr != null && mouseX >= this.getX() + this.getWidth() - 12 - this.textWidth(this.averageMarketPriceStr, this.getTextScale()) && mouseY >= this.getY() + 4 && mouseX < this.getX() + this.getWidth() - 10 && mouseY < this.getY() + 12)
            this.drawMarketDataTooltip(mouseX, mouseY);
    }

    private void drawMarketDataTooltip(int mouseX, int mouseY) {
        int
        width = this.textWidth(this.marketDataTooltipStr, this.getTooltipScaleFactor()) + 14,
        height = 10;
        GlStateManager.pushMatrix();
        GlStateManager.translate(mouseX, mouseY - height - 2, 0.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        //background
        drawRect(0, 0, width, height, this.tooltipBackgroundColor);

        //frame
        OxygenGUIUtils.drawRect(0.0D, 0.0D, 0.4D, (double) height, this.tooltipFrameColor);
        OxygenGUIUtils.drawRect((double) width - 0.4D, 0.0D, (double) width, (double) height, this.tooltipFrameColor);
        OxygenGUIUtils.drawRect(0.0D, 0.0D, (double) width, 0.4D, this.tooltipFrameColor);
        OxygenGUIUtils.drawRect(0.0D, (double) height - 0.4D, (double) width, (double) height, this.tooltipFrameColor);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.pushMatrix();
        GlStateManager.translate(((width - 8) - this.textWidth(this.marketDataTooltipStr, this.getTooltipScaleFactor())) / 2, (height - UIUtils.getTextHeight(this.getTooltipScaleFactor())) / 2.0F + 1.0F, 0.0F);
        GlStateManager.scale(this.getTooltipScaleFactor(), this.getTooltipScaleFactor(), 0.0F);

        this.mc.fontRenderer.drawString(this.marketDataTooltipStr, 0, 0, this.getEnabledTextColor(), false);

        GlStateManager.popMatrix();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.enableBlend();
        this.mc.getTextureManager().bindTexture(this.currencyProperties.getIcon());
        GUIAdvancedElement.drawCustomSizedTexturedRect(width - 10 + this.currencyProperties.getXOffset(), 1 + this.currencyProperties.getYOffset(), 0, 0, this.currencyProperties.getIconWidth(), this.currencyProperties.getIconHeight(), this.currencyProperties.getIconWidth(), this.currencyProperties.getIconHeight());
        GlStateManager.disableBlend();

        GlStateManager.popMatrix();
    }

    public int getPlayerStock() {
        return this.playerStock;
    }

    public void decrementPlayerStock(int value) {
        this.setPlayerStock(this.playerStock - value);
    }

    public void setPlayerStock(int stock) {
        this.playerStock = stock;
        this.playerStockStr = String.valueOf(stock);
    }

    public float getAverageMarketPrice() {
        return this.averageMarketPrice;
    }
}
