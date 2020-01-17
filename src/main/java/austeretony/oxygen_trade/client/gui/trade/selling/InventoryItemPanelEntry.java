package austeretony.oxygen_trade.client.gui.trade.selling;

import austeretony.alternateui.screen.core.GUIAdvancedElement;
import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.oxygen_core.client.api.EnumBaseClientSetting;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.currency.CurrencyProperties;
import austeretony.oxygen_core.client.gui.OxygenGUIUtils;
import austeretony.oxygen_core.client.gui.elements.OxygenIndexedPanelEntry;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.util.OxygenUtils;
import austeretony.oxygen_trade.client.gui.trade.TradeMenuScreen;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;

public class InventoryItemPanelEntry extends OxygenIndexedPanelEntry<ItemStackWrapper> {

    //properties
    private String playerStockStr, averageMarketPriceStr;

    private final boolean singleItem, enableDurabilityBar;

    //currency
    private CurrencyProperties currencyProperties;

    //cache 
    private int playerStock;

    private float averageMarketPrice;

    public InventoryItemPanelEntry(ItemStackWrapper stackWrapper, int playerStock, CurrencyProperties properties, float averageMarketPrice) {
        super(stackWrapper);
        this.playerStock = playerStock;
        this.playerStockStr = String.valueOf(playerStock);
        if (averageMarketPrice > 0.0F)
            this.averageMarketPriceStr = OxygenUtils.formatDecimalCurrencyValue(TradeMenuScreen.DECIMAL_FORMAT.format(averageMarketPrice));
        this.averageMarketPrice = averageMarketPrice;   

        this.singleItem = playerStock == 1;

        this.currencyProperties = properties;   

        this.enableDurabilityBar = EnumBaseClientSetting.ENABLE_ITEMS_DURABILITY_BAR.get().asBoolean();
        this.setDisplayText(EnumBaseClientSetting.ENABLE_RARITY_COLORS.get().asBoolean() ? stackWrapper.getCachedItemStack().getRarity().rarityColor + stackWrapper.getCachedItemStack().getDisplayName() : stackWrapper.getCachedItemStack().getDisplayName());
        this.setDynamicBackgroundColor(EnumBaseGUISetting.ELEMENT_ENABLED_COLOR.get().asInt(), EnumBaseGUISetting.ELEMENT_DISABLED_COLOR.get().asInt(), EnumBaseGUISetting.ELEMENT_HOVERED_COLOR.get().asInt());
        this.setTextDynamicColor(EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt(), EnumBaseGUISetting.TEXT_DISABLED_COLOR.get().asInt(), EnumBaseGUISetting.TEXT_HOVERED_COLOR.get().asInt());
        this.requireDoubleClick();
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        if (this.isVisible()) {         
            RenderHelper.enableGUIStandardItemLighting();            
            GlStateManager.enableDepth();
            this.itemRender.renderItemAndEffectIntoGUI(this.index.getCachedItemStack(), this.getX() + 2, this.getY());    

            if (this.enableDurabilityBar) {
                FontRenderer font = this.index.getCachedItemStack().getItem().getFontRenderer(this.index.getCachedItemStack());
                if (font == null) 
                    font = this.mc.fontRenderer;
                this.itemRender.renderItemOverlayIntoGUI(font, this.index.getCachedItemStack(), this.getX() + 2, this.getY(), null);
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
            GlStateManager.translate(30.0F, (this.getHeight() - this.textHeight(this.getTextScale())) / 2.0F, 0.0F);            
            GlStateManager.scale(this.getTextScale() + 0.05F, this.getTextScale() + 0.05F, 0.0F);           

            this.mc.fontRenderer.drawString(this.getDisplayText(), 0, 0, color, false);

            GlStateManager.popMatrix();             

            GlStateManager.popMatrix();
        }     
    }

    @Override
    public void drawTooltip(int mouseX, int mouseY) {
        if (mouseX >= this.getX() + 2 && mouseY >= this.getY() && mouseX < this.getX() + 18 && mouseY < this.getY() + this.getHeight())
            this.screen.drawToolTip(this.index.getCachedItemStack(), mouseX + 6, mouseY);
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
