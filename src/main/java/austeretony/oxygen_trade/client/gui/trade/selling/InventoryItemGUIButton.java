package austeretony.oxygen_trade.client.gui.trade.selling;

import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.oxygen_core.client.gui.IndexedGUIButton;
import austeretony.oxygen_core.client.gui.elements.CustomRectUtils;
import austeretony.oxygen_core.client.gui.settings.GUISettings;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

public class InventoryItemGUIButton extends IndexedGUIButton<ItemStack> {

    public final ItemStackWrapper stackWrapper;

    private String playerStockStr;

    private int playerStock;

    private boolean singleItem;

    public InventoryItemGUIButton(ItemStackWrapper stackWrapper, int playerStock) {
        super(stackWrapper.getCachedItemStack());
        this.stackWrapper = stackWrapper;
        this.playerStock = playerStock;
        this.playerStockStr = String.valueOf(playerStock);
        this.singleItem = playerStock == 1;
        this.requireDoubleClick();
        this.setDisplayText(this.index.getDisplayName());
        this.setDynamicBackgroundColor(GUISettings.get().getEnabledElementColor(), GUISettings.get().getDisabledElementColor(), GUISettings.get().getHoveredElementColor());
        this.setTextDynamicColor(GUISettings.get().getEnabledTextColor(), GUISettings.get().getDisabledTextColor(), GUISettings.get().getHoveredTextColor());
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        if (this.isVisible()) {         
            RenderHelper.enableGUIStandardItemLighting();            
            GlStateManager.enableDepth();
            this.itemRender.renderItemAndEffectIntoGUI(this.index, this.getX() + 2, this.getY());                              
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

            CustomRectUtils.drawGradientRect(0.0D, 0.0D, third, this.getHeight(), 0x00000000, color, EnumGUIAlignment.RIGHT);
            drawRect(third, 0, this.getWidth() - third, this.getHeight(), color);
            CustomRectUtils.drawGradientRect(this.getWidth() - third, 0.0D, this.getWidth(), this.getHeight(), 0x00000000, color, EnumGUIAlignment.LEFT);

            color = this.getEnabledTextColor();
            if (!this.isEnabled())                  
                color = this.getDisabledTextColor();           
            else if (this.isHovered() || this.isToggled())                                          
                color = this.getHoveredTextColor();

            float textScale = GUISettings.get().getSubTextScale() - 0.05F;

            if (!this.singleItem) {
                GlStateManager.pushMatrix();           
                GlStateManager.translate(16.0F, 10.0F, 0.0F);            
                GlStateManager.scale(textScale, textScale, 0.0F);   

                this.mc.fontRenderer.drawString(this.playerStockStr, 0, 0, color, true);

                GlStateManager.popMatrix();     
            }

            GlStateManager.pushMatrix();           
            GlStateManager.translate(30.0F, (this.getHeight() - this.textHeight(textScale)) / 2.0F, 0.0F);            
            GlStateManager.scale(textScale + 0.1F, textScale + 0.1F, 0.0F);           

            this.mc.fontRenderer.drawString(this.getDisplayText(), 0, 0, color, false);

            GlStateManager.popMatrix();             

            GlStateManager.popMatrix();
        }     
    }

    @Override
    public void drawTooltip(int mouseX, int mouseY) {
        if (mouseX >= this.getX() + 2 && mouseY >= this.getY() && mouseX < this.getX() + 18 && mouseY < this.getY() + this.getHeight())
            this.screen.drawToolTip(this.index, mouseX + 6, mouseY);
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
}
