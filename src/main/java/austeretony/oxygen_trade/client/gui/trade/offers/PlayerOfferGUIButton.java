package austeretony.oxygen_trade.client.gui.trade.offers;

import austeretony.alternateui.screen.core.GUIAdvancedElement;
import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.oxygen_core.client.gui.IndexedGUIButton;
import austeretony.oxygen_core.client.gui.OxygenGUITextures;
import austeretony.oxygen_core.client.gui.elements.CustomRectUtils;
import austeretony.oxygen_core.client.gui.settings.GUISettings;
import austeretony.oxygen_core.common.util.OxygenUtils;
import austeretony.oxygen_trade.client.PlayerOfferClient;
import austeretony.oxygen_trade.common.config.TradeConfig;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

public class PlayerOfferGUIButton extends IndexedGUIButton<Long> {

    private final ItemStack offeredStack;

    private final String amountStr, expireTimeStr, priceStr, unitPriceStr;

    private final boolean singleItem;

    public PlayerOfferGUIButton(PlayerOfferClient offer) {
        super(offer.getId());
        this.offeredStack = offer.getOfferedStack().getItemStack();
        this.amountStr = String.valueOf(offer.getAmount());
        this.expireTimeStr = OxygenUtils.getExpirationTimeLocalizedString(TradeConfig.OFFER_EXPIRE_TIME_HOURS.getIntValue() * 3_600_000L, offer.getId());
        this.priceStr = formatCurrencyValue(String.valueOf(offer.getPrice()));
        this.unitPriceStr = formatCurrencyValue(String.valueOf(offer.getPrice() / offer.getAmount()));
        this.singleItem = offer.getAmount() == 1;
        this.setDisplayText(this.offeredStack.getDisplayName());
        this.setDynamicBackgroundColor(GUISettings.get().getEnabledElementColor(), GUISettings.get().getDisabledElementColor(), GUISettings.get().getHoveredElementColor());
        this.setTextDynamicColor(GUISettings.get().getEnabledTextColor(), GUISettings.get().getDisabledTextColor(), GUISettings.get().getHoveredTextColor());
    }

    //TODO Move to Util (Core 0.9.0b)
    public static String formatCurrencyValue(String input) {
        int index = 1;
        StringBuilder builder = new StringBuilder();
        for (char c : new StringBuilder(input).reverse().toString().toCharArray()) {
            builder.append(c);
            if (index % 3 == 0 && index != input.length())
                builder.append(",");
            index++;
        }
        return builder.reverse().toString();
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        if (this.isVisible()) {      
            RenderHelper.enableGUIStandardItemLighting();            
            GlStateManager.enableDepth();
            this.itemRender.renderItemAndEffectIntoGUI(this.offeredStack, this.getX() + 2, this.getY());                              
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

            CustomRectUtils.drawGradientRect(0.0D, 0.0D, third, this.getHeight(), 0x00000000, color, EnumGUIAlignment.RIGHT);
            drawRect(third, 0, this.getWidth() - third, this.getHeight(), color);
            CustomRectUtils.drawGradientRect(this.getWidth() - third, 0.0D, this.getWidth(), this.getHeight(), 0x00000000, color, EnumGUIAlignment.LEFT);

            color = this.getEnabledTextColor();
            if (!this.isEnabled())                  
                color = this.getDisabledTextColor();           
            else if (this.isHovered())                                          
                color = this.getHoveredTextColor();

            float textScale = GUISettings.get().getSubTextScale() - 0.05F;

            if (!this.singleItem) {
                GlStateManager.pushMatrix();           
                GlStateManager.translate(16.0F, 10.0F, 0.0F);            
                GlStateManager.scale(textScale, textScale, 0.0F);   
                this.mc.fontRenderer.drawString(this.amountStr, 0, 0, color, true);           
                GlStateManager.popMatrix();      
            }

            GlStateManager.pushMatrix();           
            GlStateManager.translate(this.getWidth() - 12.0F - this.textWidth(this.priceStr, textScale), 2.0F, 0.0F);            
            GlStateManager.scale(textScale, textScale, 0.0F); 
            this.mc.fontRenderer.drawString(this.priceStr, 0, 0, color, false);
            GlStateManager.popMatrix();      

            if (!this.singleItem) {
                GlStateManager.pushMatrix();           
                GlStateManager.translate(this.getWidth() - 12.0F - this.textWidth(this.unitPriceStr, textScale - 0.05F), 10.0F, 0.0F);            
                GlStateManager.scale(textScale - 0.05F, textScale - 0.05F, 0.0F); 
                this.mc.fontRenderer.drawString(this.unitPriceStr, 0, 0, color, false);
                GlStateManager.popMatrix();    
            }

            GlStateManager.pushMatrix();           
            GlStateManager.translate(this.getWidth() - 110.0F, (this.getHeight() - this.textHeight(textScale)) / 2.0F, 0.0F);            
            GlStateManager.scale(textScale, textScale, 0.0F); 
            this.mc.fontRenderer.drawString(this.expireTimeStr, 0, 0, color, false);
            GlStateManager.popMatrix();      

            GlStateManager.pushMatrix();           
            GlStateManager.translate(28.0F, (this.getHeight() - this.textHeight(textScale)) / 2.0F, 0.0F);            
            GlStateManager.scale(textScale + 0.1F, textScale + 0.1F, 0.0F);           
            this.mc.fontRenderer.drawString(this.getDisplayText(), 0, 0, color, false);
            GlStateManager.popMatrix();             

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);  

            GlStateManager.enableBlend(); 
            this.mc.getTextureManager().bindTexture(OxygenGUITextures.COIN_ICON);
            GUIAdvancedElement.drawCustomSizedTexturedRect(this.getWidth() - 10, 1, 0, 0, 6, 6, 6, 6);          
            GlStateManager.disableBlend(); 

            GlStateManager.popMatrix();
        }     
    }

    @Override
    public void drawTooltip(int mouseX, int mouseY) {
        if (mouseX >= this.getX() + 2 && mouseY >= this.getY() && mouseX < this.getX() + 18 && mouseY < this.getY() + this.getHeight())
            this.screen.drawToolTip(this.offeredStack, mouseX + 6, mouseY);
    }
}