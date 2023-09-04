package austeretony.oxygen_market.client.gui.market.selling;

import austeretony.alternateui.screen.core.GUISimpleElement;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

public class SelectedItem extends GUISimpleElement<SelectedItem> {

    private ItemStack itemStack;

    private String playerStockStr;

    public SelectedItem(int x, int y) {
        this.setPosition(x, y);
        this.setSize(16, 16);
        this.setTextScale(EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.05F);
        this.setTextDynamicColor(EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt(), EnumBaseGUISetting.TEXT_DISABLED_COLOR.get().asInt(), EnumBaseGUISetting.TEXT_HOVERED_COLOR.get().asInt());
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        if (this.isVisible() && this.itemStack != null) {         
            RenderHelper.enableGUIStandardItemLighting();            
            GlStateManager.enableDepth();
            this.itemRender.renderItemAndEffectIntoGUI(this.itemStack, this.getX(), this.getY());                              
            GlStateManager.disableDepth();
            RenderHelper.disableStandardItemLighting();

            GlStateManager.pushMatrix();           
            GlStateManager.translate(this.getX(), this.getY(), 0.0F);            
            GlStateManager.scale(this.getScale(), this.getScale(), 0.0F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);  

            GlStateManager.pushMatrix();           
            GlStateManager.translate(14.0F, 10.0F, 0.0F);            
            GlStateManager.scale(this.getTextScale(), this.getTextScale(), 0.0F);   

            this.mc.fontRenderer.drawString(this.playerStockStr, 0, 0, this.getEnabledTextColor(), true);

            GlStateManager.popMatrix();                

            GlStateManager.popMatrix();
        }     
    }

    public void setItemStack(ItemStack itemStack, int stock) {
        this.itemStack = itemStack;
        this.playerStockStr = String.valueOf(stock);
        this.setDisplayText(itemStack.getDisplayName());
        this.setVisible(itemStack != null);
    }

    public void setPlayerStock(int stock) {
        this.playerStockStr = String.valueOf(stock);
    }
}
