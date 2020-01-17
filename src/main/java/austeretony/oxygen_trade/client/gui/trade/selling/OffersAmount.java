package austeretony.oxygen_trade.client.gui.trade.selling;

import austeretony.alternateui.screen.core.GUISimpleElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.api.PrivilegesProviderClient;
import austeretony.oxygen_trade.client.TradeManagerClient;
import austeretony.oxygen_trade.common.config.TradeConfig;
import austeretony.oxygen_trade.common.main.EnumTradePrivilege;
import net.minecraft.client.renderer.GlStateManager;

public class OffersAmount extends GUISimpleElement<OffersAmount> {

    private final String label;

    private int offersAmount, maxAmount;

    private String offersAmountStr;

    public OffersAmount(int x, int y) {
        this.setPosition(x, y);
        this.label = ClientReference.localize("oxygen_trade.gui.trade.offersAmount");
        this.setTextScale(EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.05F);
        this.setTextDynamicColor(EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt(), EnumBaseGUISetting.TEXT_DISABLED_COLOR.get().asInt(), EnumBaseGUISetting.TEXT_HOVERED_COLOR.get().asInt());
        this.setEnabledColor(EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt());
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

            this.mc.fontRenderer.drawString(this.label, 0, 0, this.getEnabledColor(), false);
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
        this.maxAmount = PrivilegesProviderClient.getAsInt(EnumTradePrivilege.MAX_OFFERS_PER_PLAYER.id(), TradeConfig.MAX_OFFERS_PER_PLAYER.asInt());
        this.offersAmountStr = String.valueOf(value) + "/" + String.valueOf(this.maxAmount);
        this.setEnabledTextColor(value == this.maxAmount ? EnumBaseGUISetting.INACTIVE_TEXT_COLOR.get().asInt() : EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt());
    }
}
