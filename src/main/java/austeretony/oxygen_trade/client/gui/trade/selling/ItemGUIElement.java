package austeretony.oxygen_trade.client.gui.trade.selling;

import austeretony.alternateui.screen.core.GUISimpleElement;
import austeretony.oxygen_core.client.gui.settings.GUISettings;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

public class ItemGUIElement extends GUISimpleElement<ItemGUIElement> {

    private ItemStack itemStack;

    private String playerStockStr;

    public ItemGUIElement(int x, int y) {
        this.setPosition(x, y);
        this.setSize(16, 16);
        this.setTextScale(GUISettings.get().getSubTextScale() - 0.05F);
        this.setEnabledTextColor(GUISettings.get().getEnabledTextColor());
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        if (this.isVisible()) {         
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

    @Override
    public void drawTooltip(int mouseX, int mouseY) {
        if (this.itemStack != null && this.isVisible() && mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + 16 && mouseY < this.getY() + this.getHeight())
            this.screen.drawToolTip(this.itemStack, mouseX + 6, mouseY);
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
