package austeretony.oxygen_market.client.gui.market.history;

import austeretony.alternateui.screen.button.GUIButton;
import austeretony.alternateui.screen.core.GUIAdvancedElement;
import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.EnumBaseClientSetting;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.currency.CurrencyProperties;
import austeretony.oxygen_core.client.gui.OxygenGUIUtils;
import austeretony.oxygen_core.common.util.OxygenUtils;
import austeretony.oxygen_market.client.market.SalesHistoryEntryClient;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

public class HistoryPanelEntry extends GUIButton {

    private final ItemStack itemStack;

    //properties
    private final String amountStr, noteStr, timeStr, priceStr, unitPriceStr;

    private final boolean singleItem, enableDurabilityBar;

    //currency
    private CurrencyProperties currencyProperties;

    public HistoryPanelEntry(SalesHistoryEntryClient entry, String note, CurrencyProperties properties) {
        this.itemStack = entry.getStackWrapper().getCachedItemStack();
        this.amountStr = String.valueOf(entry.getAmount());
        this.noteStr = note;
        this.timeStr = ClientReference.localize("oxygen_market.gui.market.ago", OxygenUtils.getTimePassedLocalizedString(entry.getId()));
        this.priceStr = OxygenUtils.formatCurrencyValue(String.valueOf(entry.getPrice()));
        this.unitPriceStr = OxygenUtils.formatCurrencyValue(String.valueOf(entry.getPrice() / entry.getAmount()));
        this.singleItem = entry.getAmount() == 1;

        this.currencyProperties = properties; 

        this.enableDurabilityBar = EnumBaseClientSetting.ENABLE_ITEMS_DURABILITY_BAR.get().asBoolean();
        this.setDisplayText(EnumBaseClientSetting.ENABLE_RARITY_COLORS.get().asBoolean() ? this.itemStack.getRarity().rarityColor + this.itemStack.getDisplayName() : this.itemStack.getDisplayName());
        this.setDynamicBackgroundColor(EnumBaseGUISetting.ELEMENT_ENABLED_COLOR.get().asInt(), EnumBaseGUISetting.ELEMENT_DISABLED_COLOR.get().asInt(), EnumBaseGUISetting.ELEMENT_HOVERED_COLOR.get().asInt());
        this.setTextDynamicColor(EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt(), EnumBaseGUISetting.TEXT_DISABLED_COLOR.get().asInt(), EnumBaseGUISetting.TEXT_HOVERED_COLOR.get().asInt());
        this.setEnabledColor(EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt());
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        if (this.isVisible()) {    
            RenderHelper.enableGUIStandardItemLighting();            
            GlStateManager.enableDepth();
            this.itemRender.renderItemAndEffectIntoGUI(this.itemStack, this.getX() + 2, this.getY());        

            if (this.enableDurabilityBar) {
                FontRenderer font = this.itemStack.getItem().getFontRenderer(this.itemStack);
                if (font == null) 
                    font = this.mc.fontRenderer;
                this.itemRender.renderItemOverlayIntoGUI(font, this.itemStack, this.getX() + 2, this.getY(), null);
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
            GlStateManager.translate(this.getWidth() - 12.0F - this.textWidth(this.priceStr, this.getTextScale()), 2.0F, 0.0F);            
            GlStateManager.scale(this.getTextScale(), this.getTextScale(), 0.0F); 
            this.mc.fontRenderer.drawString(this.priceStr, 0, 0, color, false);
            GlStateManager.popMatrix();      

            if (!this.singleItem) {
                GlStateManager.pushMatrix();           
                GlStateManager.translate(this.getWidth() - 12.0F - this.textWidth(this.unitPriceStr, this.getTextScale() - 0.05F), 10.0F, 0.0F);            
                GlStateManager.scale(this.getTextScale() - 0.05F, this.getTextScale() - 0.05F, 0.0F); 
                this.mc.fontRenderer.drawString(this.unitPriceStr, 0, 0, color, false);
                GlStateManager.popMatrix();    
            }

            GlStateManager.pushMatrix();           
            GlStateManager.translate(34.0F, 10.0F, 0.0F);            
            GlStateManager.scale(this.getTextScale(), this.getTextScale(), 0.0F);           
            this.mc.fontRenderer.drawString(this.noteStr, 0, 0, this.getEnabledColor(), false);
            GlStateManager.popMatrix();  

            GlStateManager.pushMatrix();           
            GlStateManager.translate(this.getWidth() - 80.0F, (this.getHeight() - this.textHeight(this.getTextScale())) / 2.0F, 0.0F);            
            GlStateManager.scale(this.getTextScale(), this.getTextScale(), 0.0F); 
            this.mc.fontRenderer.drawString(this.timeStr, 0, 0, color, false);
            GlStateManager.popMatrix();      

            GlStateManager.pushMatrix();           
            GlStateManager.translate(31.0F, 2.0F, 0.0F);            
            GlStateManager.scale(this.getTextScale() + 0.05F, this.getTextScale() + 0.05F, 0.0F);           
            this.mc.fontRenderer.drawString(this.getDisplayText(), 0, 0, color, false);
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
            this.screen.drawToolTip(this.itemStack, mouseX + 6, mouseY);
    }
}
