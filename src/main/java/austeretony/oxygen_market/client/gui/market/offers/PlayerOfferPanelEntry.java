package austeretony.oxygen_market.client.gui.market.offers;

import java.util.concurrent.TimeUnit;

import austeretony.alternateui.screen.core.GUIAdvancedElement;
import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.alternateui.util.UIUtils;
import austeretony.oxygen_core.client.api.EnumBaseClientSetting;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.currency.CurrencyProperties;
import austeretony.oxygen_core.client.gui.OxygenGUITextures;
import austeretony.oxygen_core.client.gui.OxygenGUIUtils;
import austeretony.oxygen_core.client.gui.elements.OxygenTexturedButton;
import austeretony.oxygen_core.client.gui.elements.OxygenWrapperPanelEntry;
import austeretony.oxygen_core.common.util.OxygenUtils;
import austeretony.oxygen_market.client.MarketManagerClient;
import austeretony.oxygen_market.client.gui.market.MarketMenuScreen;
import austeretony.oxygen_market.client.gui.market.OfferProfitability;
import austeretony.oxygen_market.client.market.OfferClient;
import austeretony.oxygen_market.common.config.MarketConfig;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;

public class PlayerOfferPanelEntry extends OxygenWrapperPanelEntry<OfferClient> {

    //properties
    private final String amountStr, expireTimeStr, priceStr, unitPriceStr;

    private final boolean singleItem, enableDurabilityBar;

    //currency
    private CurrencyProperties currencyProperties;

    //offer profitability
    private String profitabilityPercentStr, profitabilityTooltipStr;

    private int profitabilityColorHex;

    //widget
    private final int tooltipBackgroundColor, tooltipFrameColor;

    private OxygenTexturedButton cancelOfferButton;

    public PlayerOfferPanelEntry(OfferClient offer, CurrencyProperties properties) {
        super(offer);
        this.amountStr = String.valueOf(offer.getAmount());
        this.expireTimeStr = OxygenUtils.getExpirationTimeLocalizedString(TimeUnit.HOURS.toMillis(MarketConfig.OFFER_EXPIRE_TIME_HOURS.asInt()), offer.getId());
        this.priceStr = OxygenUtils.formatCurrencyValue(String.valueOf(offer.getPrice()));
        this.unitPriceStr = OxygenUtils.formatDecimalCurrencyValue(MarketMenuScreen.DECIMAL_FORMAT.format((float) offer.getPrice() / (float) offer.getAmount()));
        this.singleItem = offer.getAmount() == 1;

        this.currencyProperties = properties;

        this.enableDurabilityBar = EnumBaseClientSetting.ENABLE_ITEMS_DURABILITY_BAR.get().asBoolean();
        this.setDisplayText(EnumBaseClientSetting.ENABLE_RARITY_COLORS.get().asBoolean() ? this.wrapped.getStackWrapper().getCachedItemStack().getRarity().rarityColor + this.wrapped.getStackWrapper().getCachedItemStack().getDisplayName() : this.wrapped.getStackWrapper().getCachedItemStack().getDisplayName());
        this.setDynamicBackgroundColor(EnumBaseGUISetting.ELEMENT_ENABLED_COLOR.get().asInt(), EnumBaseGUISetting.ELEMENT_DISABLED_COLOR.get().asInt(), EnumBaseGUISetting.ELEMENT_HOVERED_COLOR.get().asInt());
        this.setTextDynamicColor(EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt(), EnumBaseGUISetting.TEXT_DISABLED_COLOR.get().asInt(), EnumBaseGUISetting.TEXT_HOVERED_COLOR.get().asInt());
        this.setTooltipScaleFactor(EnumBaseGUISetting.TEXT_TOOLTIP_SCALE.get().asFloat());
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
    public void init() { 
        this.cancelOfferButton = new OxygenTexturedButton(this.getWidth() - 8, 5, 6, 6, OxygenGUITextures.CROSS_ICONS, 6, 6, "").initScreen(this.getScreen());
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

            int third = this.getWidth() / 3;
            OxygenGUIUtils.drawGradientRect(0.0D, 0.0D, third, this.getHeight(), 0x00000000, color, EnumGUIAlignment.RIGHT);
            drawRect(third, 0, this.getWidth() - third, this.getHeight(), color);
            OxygenGUIUtils.drawGradientRect(this.getWidth() - third, 0.0D, this.getWidth(), this.getHeight(), 0x00000000, color, EnumGUIAlignment.LEFT);

            color = this.getEnabledTextColor();
            if (!this.isEnabled())                  
                color = this.getDisabledTextColor();           
            else if (this.isHovered())                                          
                color = this.getHoveredTextColor();

            if (!this.singleItem) {
                GlStateManager.pushMatrix();           
                GlStateManager.translate(16.0F, 10.0F, 0.0F);            
                GlStateManager.scale(this.getTextScale(), this.getTextScale(), 0.0F);   
                this.mc.fontRenderer.drawString(this.amountStr, 0, 0, color, true);           
                GlStateManager.popMatrix();      
            }

            GlStateManager.pushMatrix();           
            GlStateManager.translate(this.getWidth() - 22.0F - this.textWidth(this.priceStr, this.getTextScale()), 2.0F, 0.0F);            
            GlStateManager.scale(this.getTextScale(), this.getTextScale(), 0.0F); 
            this.mc.fontRenderer.drawString(this.priceStr, 0, 0, color, false);
            GlStateManager.popMatrix();      

            if (!this.singleItem) {
                GlStateManager.pushMatrix();           
                GlStateManager.translate(this.getWidth() - 22.0F - this.textWidth(this.unitPriceStr, this.getTextScale() - 0.05F), 10.0F, 0.0F);            
                GlStateManager.scale(this.getTextScale() - 0.05F, this.getTextScale() - 0.05F, 0.0F); 
                this.mc.fontRenderer.drawString(this.unitPriceStr, 0, 0, color, false);
                GlStateManager.popMatrix();    
            }

            GlStateManager.pushMatrix();           
            GlStateManager.translate(this.getWidth() - 100.0F, 2.0F, 0.0F);            
            GlStateManager.scale(this.getTextScale() - 0.05F, this.getTextScale() - 0.05F, 0.0F); 
            this.mc.fontRenderer.drawString(this.expireTimeStr, 0, 0, color, false);
            GlStateManager.popMatrix();      

            if (this.profitabilityPercentStr != null) {
                GlStateManager.pushMatrix();           
                GlStateManager.translate(this.getWidth() - 100.0F, 10.0F, 0.0F);            
                GlStateManager.scale(this.getTextScale() - 0.05F, this.getTextScale() - 0.05F, 0.0F); 
                this.mc.fontRenderer.drawString(this.profitabilityPercentStr, 0, 0, this.profitabilityColorHex, false);
                GlStateManager.popMatrix();      
            }

            GlStateManager.pushMatrix();           
            GlStateManager.translate(31.0F, (this.getHeight() - this.textHeight(this.getTextScale())) / 2.0F, 0.0F);            
            GlStateManager.scale(this.getTextScale() + 0.05F, this.getTextScale() + 0.05F, 0.0F);           
            this.mc.fontRenderer.drawString(this.getDisplayText(), 0, 0, color, false);
            GlStateManager.popMatrix();             

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);  

            GlStateManager.enableBlend(); 
            this.mc.getTextureManager().bindTexture(this.currencyProperties.getIcon());
            GUIAdvancedElement.drawCustomSizedTexturedRect(this.getWidth() - 20 + this.currencyProperties.getXOffset(), this.currencyProperties.getYOffset(), 0, 0, this.currencyProperties.getIconWidth(), this.currencyProperties.getIconHeight(), this.currencyProperties.getIconWidth(), this.currencyProperties.getIconHeight());                         
            GlStateManager.disableBlend(); 

            this.cancelOfferButton.draw(mouseX, mouseY);

            GlStateManager.popMatrix();
        }     
    }

    @Override
    public void drawTooltip(int mouseX, int mouseY) {
        if (mouseX >= this.getX() + 2 && mouseY >= this.getY() && mouseX < this.getX() + 18 && mouseY < this.getY() + this.getHeight())
            this.screen.drawToolTip(this.wrapped.getStackWrapper().getCachedItemStack(), mouseX + 6, mouseY);
        else if (this.profitabilityPercentStr != null && mouseX >= this.getX() + this.getWidth() - 100 && mouseY >= this.getY() + 10 && mouseX < this.getX() + this.getWidth() - 80 && mouseY < this.getY() + this.getHeight())
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

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {       
        if (this.cancelOfferButton.mouseClicked(mouseX, mouseY, mouseButton)) {
            MarketManagerClient.instance().getOffersManager().cancelOfferSynced(this.wrapped.getId());
            return true;
        }
        return false;
    }

    @Override
    public void mouseOver(int mouseX, int mouseY) {
        this.cancelOfferButton.mouseOver(mouseX - this.getX(), mouseY - this.getY());
        this.setHovered(this.isEnabled() && mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.getWidth() && mouseY < this.getY() + this.getHeight());   
    }
}
