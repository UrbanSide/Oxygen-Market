package austeretony.oxygen_trade.client.gui.trade.selling;

import austeretony.alternateui.screen.core.GUISimpleElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.PrivilegeProviderClient;
import austeretony.oxygen_core.client.gui.settings.GUISettings;
import austeretony.oxygen_trade.client.TradeManagerClient;
import austeretony.oxygen_trade.common.config.TradeConfig;
import austeretony.oxygen_trade.common.main.EnumTradePrivilege;
import net.minecraft.client.renderer.GlStateManager;

public class OffersAmountGUIElement extends GUISimpleElement<OffersAmountGUIElement> {

    private final String label;

    private int offersAmount, maxAmount;

    private String offersAmountStr;

    public OffersAmountGUIElement(int x, int y) {
        this.setPosition(x, y);
        this.label = ClientReference.localize("oxygen_trade.gui.trade.offersAmount");
        this.setTextScale(GUISettings.get().getSubTextScale() - 0.05F);
        this.enableFull();
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        if (this.isVisible()) {        
            GlStateManager.pushMatrix();           
            GlStateManager.translate(this.getX(), this.getY(), 0.0F);            
            GlStateManager.scale(this.getScale(), this.getScale(), 0.0F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);  

            GlStateManager.pushMatrix();           
            GlStateManager.translate(0.0F, 0.0F, 0.0F);            
            GlStateManager.scale(this.getTextScale(), this.getTextScale(), 0.0F);   

            this.mc.fontRenderer.drawString(this.label, 0, 0, GUISettings.get().getEnabledTextColorDark(), false);
            this.mc.fontRenderer.drawString(this.offersAmountStr, this.textWidth(this.label, this.getScale()) + 6, 0, this.getEnabledTextColor(), false);

            GlStateManager.popMatrix();   

            GlStateManager.popMatrix();                
        }
    }

    public int getOffersAmount() {
        return this.offersAmount;
    }

    public int getMaxAmount() {
        return this.maxAmount;
    }

    public boolean reachedMaxAmount() {
        return this.offersAmount == this.maxAmount;
    }

    public void updateOffersAmount() {
        this.setOffersAmount(TradeManagerClient.instance().getOffersManager().getPlayerOffersAmount());
    }

    public void decrementOffersAmount(int value) {
        this.setOffersAmount(this.offersAmount - value);
    }

    public void incrementOffersAmount(int value) {
        this.setOffersAmount(this.offersAmount + value);
    }

    public void setOffersAmount(int value) {
        this.offersAmount = value;
        this.maxAmount = PrivilegeProviderClient.getValue(EnumTradePrivilege.MAX_OFFERS_PER_PLAYER.toString(), TradeConfig.MAX_OFFERS_PER_PLAYER.getIntValue());
        this.offersAmountStr = String.valueOf(value) + "/" + String.valueOf(this.maxAmount);
        this.setEnabledTextColor(value == this.maxAmount ? GUISettings.get().getInactiveElementColor() : GUISettings.get().getEnabledTextColor());
    }
}
