package austeretony.oxygen_market.client.gui.market.buy;

import java.util.concurrent.TimeUnit;

import austeretony.alternateui.screen.core.GUIAdvancedElement;
import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.alternateui.util.UIUtils;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.EnumBaseClientSetting;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.currency.CurrencyProperties;
import austeretony.oxygen_core.client.gui.OxygenGUIUtils;
import austeretony.oxygen_core.client.gui.elements.OxygenWrapperPanelEntry;
import austeretony.oxygen_core.common.util.OxygenUtils;
import austeretony.oxygen_market.client.gui.market.MarketMenuScreen;
import austeretony.oxygen_market.client.gui.market.OfferProfitability;
import austeretony.oxygen_market.client.market.OfferClient;
import austeretony.oxygen_market.common.config.MarketConfig;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;

public class OfferPanelEntry extends OxygenWrapperPanelEntry<OfferClient> {    

    //properties
    private final String playerStockStr, amountStr, sellerStr, expireTimeStr, priceStr, unitPriceStr, offerIdStr;

    private final boolean singleItem, enableDurabilityBar;

    private CurrencyProperties currencyProperties;

    //offer profitability
    private String profitabilityPercentStr, profitabilityTooltipStr;

    //currency
    private int profitabilityColorHex;

    //widget
    private final int tooltipBackgroundColor, tooltipFrameColor;

    //cache
    private boolean overpriced, purchased;

    public OfferPanelEntry(OfferClient offer, CurrencyProperties properties, int playerStock, boolean overpriced) {
        super(offer);
        this.playerStockStr = String.valueOf(playerStock);
        this.amountStr = String.valueOf(offer.getAmount());
        this.sellerStr = ClientReference.localize("oxygen_market.gui.market.seller", offer.getUsername());
        this.expireTimeStr = OxygenUtils.getExpirationTimeLocalizedString(TimeUnit.HOURS.toMillis(MarketConfig.OFFER_EXPIRE_TIME_HOURS.asInt()), offer.getId());
        this.priceStr = OxygenUtils.formatCurrencyValue(String.valueOf(offer.getPrice()));
        this.unitPriceStr = OxygenUtils.formatDecimalCurrencyValue(MarketMenuScreen.DECIMAL_FORMAT.format((float) offer.getPrice() / (float) offer.getAmount()));
        this.offerIdStr = String.valueOf(offer.getId());
        this.singleItem = offer.getAmount() == 1;
        this.overpriced = overpriced;

        this.currencyProperties = properties;

        if (!overpriced)
            this.setDisplayText(EnumBaseClientSetting.ENABLE_RARITY_COLORS.get().asBoolean() ? this.wrapped.getStackWrapper().getCachedItemStack().getRarity().rarityColor + this.wrapped.getStackWrapper().getCachedItemStack().getDisplayName() : this.wrapped.getStackWrapper().getCachedItemStack().getDisplayName());
        else
            this.setDisplayText(this.wrapped.getStackWrapper().getCachedItemStack().getDisplayName());
        this.enableDurabilityBar = EnumBaseClientSetting.ENABLE_ITEMS_DURABILITY_BAR.get().asBoolean();
        this.setStaticBackgroundColor(EnumBaseGUISetting.ACTIVE_ELEMENT_COLOR.get().asInt());
        this.setDynamicBackgroundColor(EnumBaseGUISetting.ELEMENT_ENABLED_COLOR.get().asInt(), EnumBaseGUISetting.ELEMENT_DISABLED_COLOR.get().asInt(), EnumBaseGUISetting.ELEMENT_HOVERED_COLOR.get().asInt());
        this.setTextDynamicColor(EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt(), EnumBaseGUISetting.TEXT_DISABLED_COLOR.get().asInt(), EnumBaseGUISetting.TEXT_HOVERED_COLOR.get().asInt());
        this.setDebugColor(EnumBaseGUISetting.INACTIVE_ELEMENT_COLOR.get().asInt());
        this.setEnabledColor(EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt());
        this.setTooltipScaleFactor(EnumBaseGUISetting.TEXT_TOOLTIP_SCALE.get().asFloat());
        this.requireDoubleClick();

        this.tooltipBackgroundColor = EnumBaseGUISetting.BACKGROUND_BASE_COLOR.get().asInt();
        this.tooltipFrameColor = EnumBaseGUISetting.BACKGROUND_ADDITIONAL_COLOR.get().asInt();
    }

    public void initProfitability(OfferProfitability profitability) {
        if (profitability != null) {
            if (profitability.profitabilityIndex != - 1)
                this.profitabilityPercentStr = profitability.profitabilityPercentStr;
            else
                this.profitabilityPercentStr = null;
            this.profitabilityColorHex = profitability.colorHex;
            this.profitabilityTooltipStr = profitability.profitabilityTooltipStr;
        }
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        if (this.isVisible()) {      
            RenderHelper.enableGUIStandardItemLighting();            
            GlStateManager.enableDepth();
            this.itemRender.renderItemAndEffectIntoGUI(this.wrapped.getStackWrapper().getCachedItemStack(), this.getX() + 2, this.getY());    

            if (this.enableDurabilityBar) {
                FontRenderer font = this.wrapped.getStackWrapper().getCachedItemStack().getItem().getFontRenderer(this.wrapped.getStackWrapper().getCachedItemStack());
                if (font == null) 
                    font = this.mc.fontRenderer;
                this.itemRender.renderItemOverlayIntoGUI(font, this.wrapped.getStackWrapper().getCachedItemStack(), this.getX() + 2, this.getY(), null);
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
            else if (this.isHovered())                  
                color = this.getHoveredBackgroundColor();      

            if (this.purchased)
                color = this.getStaticBackgroundColor();

            int third = this.getWidth() / 3;
            OxygenGUIUtils.drawGradientRect(0.0D, 0.0D, third, this.getHeight(), 0x00000000, color, EnumGUIAlignment.RIGHT);
            drawRect(third, 0, this.getWidth() - third, this.getHeight(), color);
            OxygenGUIUtils.drawGradientRect(this.getWidth() - third, 0.0D, this.getWidth(), this.getHeight(), 0x00000000, color, EnumGUIAlignment.LEFT);

            color = this.getEnabledTextColor();
            if (!this.isEnabled())                  
                color = this.getDisabledTextColor();           
            else if (this.isHovered())                                          
                color = this.getHoveredTextColor();

            if (this.isHovered()) {
                GlStateManager.pushMatrix();           
                GlStateManager.translate(16.0F, 1.0F, 0.0F);            
                GlStateManager.scale(this.getTextScale(), this.getTextScale(), 0.0F);   
                this.mc.fontRenderer.drawString(this.playerStockStr, 0, 0, color, true); 
                GlStateManager.popMatrix();
            }

            if (!this.singleItem) {
                GlStateManager.pushMatrix();           
                GlStateManager.translate(16.0F, 10.0F, 0.0F);            
                GlStateManager.scale(this.getTextScale(), this.getTextScale(), 0.0F);   
                this.mc.fontRenderer.drawString(this.amountStr, 0, 0, color, true);           
                GlStateManager.popMatrix();      
            }

            GlStateManager.pushMatrix();           
            GlStateManager.translate(this.getWidth() - 12.0F - this.textWidth(this.priceStr, this.getTextScale()), 2.0F, 0.0F);            
            GlStateManager.scale(this.getTextScale(), this.getTextScale(), 0.0F); 
            this.mc.fontRenderer.drawString(this.priceStr, 0, 0, !this.overpriced ? color : this.getDebugColor(), false);
            GlStateManager.popMatrix();      

            if (!this.singleItem) {
                GlStateManager.pushMatrix();           
                GlStateManager.translate(this.getWidth() - 12.0F - this.textWidth(this.unitPriceStr, this.getTextScale() - 0.05F), 10.0F, 0.0F);            
                GlStateManager.scale(this.getTextScale() - 0.05F, this.getTextScale() - 0.05F, 0.0F); 
                this.mc.fontRenderer.drawString(this.unitPriceStr, 0, 0, color, false);
                GlStateManager.popMatrix();    
            }

            GlStateManager.pushMatrix();           
            GlStateManager.translate(35.0F, 10.0F, 0.0F);            
            GlStateManager.scale(this.getTextScale() - 0.05F, this.getTextScale() - 0.05F, 0.0F);           
            this.mc.fontRenderer.drawString(this.sellerStr, 0, 0, this.getEnabledColor(), false);
            GlStateManager.popMatrix();  

            GlStateManager.pushMatrix();           
            GlStateManager.translate(this.getWidth() - 80.0F, 2.0F, 0.0F);            
            GlStateManager.scale(this.getTextScale() - 0.05F, this.getTextScale() - 0.05F, 0.0F); 
            this.mc.fontRenderer.drawString(this.expireTimeStr, 0, 0, color, false);
            GlStateManager.popMatrix();      

            if (this.profitabilityPercentStr != null) {
                GlStateManager.pushMatrix();           
                GlStateManager.translate(this.getWidth() - 80.0F, 10.0F, 0.0F);            
                GlStateManager.scale(this.getTextScale() - 0.05F, this.getTextScale() - 0.05F, 0.0F); 
                this.mc.fontRenderer.drawString(this.profitabilityPercentStr, 0, 0, this.profitabilityColorHex, false);
                GlStateManager.popMatrix();      
            }

            GlStateManager.pushMatrix();           
            GlStateManager.translate(31.0F, 2.0F, 0.0F);            
            GlStateManager.scale(this.getTextScale() + 0.05F, this.getTextScale() + 0.05F, 0.0F);           
            this.mc.fontRenderer.drawString(this.getDisplayText(), 0, 0, !this.overpriced ? color : this.getDebugColor(), false);
            GlStateManager.popMatrix();             

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);  

            GlStateManager.enableBlend(); 
            this.mc.getTextureManager().bindTexture(this.currencyProperties.getIcon());
            GUIAdvancedElement.drawCustomSizedTexturedRect(this.getWidth() - 10 + this.currencyProperties.getXOffset(), this.currencyProperties.getYOffset(), 0, 0, this.currencyProperties.getIconWidth(), this.currencyProperties.getIconHeight(), this.currencyProperties.getIconWidth(), this.currencyProperties.getIconHeight());                 
            GlStateManager.disableBlend(); 

            GlStateManager.popMatrix();
        }     
    }

    @Override
    public void drawTooltip(int mouseX, int mouseY) {
        if (mouseX >= this.getX() + 2 && mouseY >= this.getY() && mouseX < this.getX() + 18 && mouseY < this.getY() + this.getHeight())
            this.screen.drawToolTip(this.wrapped.getStackWrapper().getCachedItemStack(), mouseX + 6, mouseY);
        else if (this.profitabilityPercentStr != null && mouseX >= this.getX() + this.getWidth() - 80 && mouseY >= this.getY() + 10 && mouseX < this.getX() + this.getWidth() - 60 && mouseY < this.getY() + this.getHeight())
            this.drawMarketDataTooltip(mouseX, mouseY);
    }

    private void drawMarketDataTooltip(int mouseX, int mouseY) {
        int 
        width = this.textWidth(this.profitabilityTooltipStr, this.getTooltipScaleFactor()) + 14,
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
        GlStateManager.translate(((width - 8) - this.textWidth(this.profitabilityTooltipStr, this.getTooltipScaleFactor())) / 2, (height - UIUtils.getTextHeight(this.getTooltipScaleFactor())) / 2.0F + 1.0F, 0.0F);            
        GlStateManager.scale(this.getTooltipScaleFactor(), this.getTooltipScaleFactor(), 0.0F);

        this.mc.fontRenderer.drawString(this.profitabilityTooltipStr, 0, 0, this.getEnabledTextColor(), false);

        GlStateManager.popMatrix();     

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);  

        GlStateManager.enableBlend(); 
        this.mc.getTextureManager().bindTexture(this.currencyProperties.getIcon());
        GUIAdvancedElement.drawCustomSizedTexturedRect(width - 10 + this.currencyProperties.getXOffset(), 1 + this.currencyProperties.getYOffset(), 0, 0, this.currencyProperties.getIconWidth(), this.currencyProperties.getIconHeight(), this.currencyProperties.getIconWidth(), this.currencyProperties.getIconHeight());                   
        GlStateManager.disableBlend(); 

        GlStateManager.popMatrix(); 
    }

    public void setOverpriced() {
        this.setDisplayText(this.wrapped.getStackWrapper().getCachedItemStack().getDisplayName());
        this.overpriced = true;
    }

    public boolean isOverpriced() {
        return this.overpriced;
    }

    public void setPurchased() {
        this.purchased = true;
    }

    public boolean isPurchased() {
        return this.purchased;
    }
}
