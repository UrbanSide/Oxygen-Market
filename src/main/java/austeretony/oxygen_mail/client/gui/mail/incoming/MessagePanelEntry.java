package austeretony.oxygen_mail.client.gui.mail.incoming;

import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.gui.OxygenGUIUtils;
import austeretony.oxygen_core.client.gui.elements.OxygenWrapperPanelEntry;
import austeretony.oxygen_mail.client.MailManagerClient;
import austeretony.oxygen_mail.common.mail.Mail;
import net.minecraft.client.renderer.GlStateManager;

public class MessagePanelEntry extends OxygenWrapperPanelEntry<Mail> {

    private boolean pending;

    public MessagePanelEntry(Mail mail) {
        super(mail);
        this.pending = mail.isPending();
        this.setStaticBackgroundColor(EnumBaseGUISetting.STATUS_TEXT_COLOR.get().asInt());
        this.setDynamicBackgroundColor(EnumBaseGUISetting.ELEMENT_ENABLED_COLOR.get().asInt(), EnumBaseGUISetting.ELEMENT_DISABLED_COLOR.get().asInt(), EnumBaseGUISetting.ELEMENT_HOVERED_COLOR.get().asInt());
        this.setTextDynamicColor(EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt(), EnumBaseGUISetting.TEXT_DISABLED_COLOR.get().asInt(), EnumBaseGUISetting.TEXT_HOVERED_COLOR.get().asInt());
        this.setDisplayText(ClientReference.localize(mail.getSubject()));
        if (MailManagerClient.instance().getMailboxContainer().isMarkedAsRead(mail.getId()))
            this.read();
    }

    public void read() {
        this.setTextDynamicColor(EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt(), EnumBaseGUISetting.TEXT_DARK_DISABLED_COLOR.get().asInt(), EnumBaseGUISetting.TEXT_DARK_HOVERED_COLOR.get().asInt());
    }

    public void setPending(boolean flag) {
        this.pending = flag;
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        GlStateManager.pushMatrix();           
        GlStateManager.translate(this.getX(), this.getY(), 0.0F);    
        GlStateManager.scale(this.getScale(), this.getScale(), 0.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);  

        int 
        color = this.getEnabledBackgroundColor(), 
        textColor = this.getEnabledTextColor();

        if (!this.isEnabled()) {                 
            color = this.getDisabledBackgroundColor();
            textColor = this.getDisabledTextColor();   
        } else if (this.isHovered() || this.isToggled()) {                 
            color = this.getHoveredBackgroundColor();
            textColor = this.getHoveredTextColor();
        }

        int third = this.getWidth() / 3;
        OxygenGUIUtils.drawGradientRect(0.0D, 0.0D, third, this.getHeight(), 0x00000000, color, EnumGUIAlignment.RIGHT);
        drawRect(third, 0, this.getWidth() - third, this.getHeight(), color);
        OxygenGUIUtils.drawGradientRect(this.getWidth() - third, 0.0D, this.getWidth(), this.getHeight(), 0x00000000, color, EnumGUIAlignment.LEFT);

        GlStateManager.pushMatrix();           
        GlStateManager.translate(1.0F, (this.getHeight() - this.textHeight(this.getTextScale())) / 2.0F + 1.0F, 0.0F); 
        GlStateManager.scale(this.getTextScale(), this.getTextScale(), 0.0F); 
        if (this.pending)
            this.mc.fontRenderer.drawString("!", 0, 0, this.getStaticBackgroundColor(), false);
        this.mc.fontRenderer.drawString(this.getDisplayText(), 5, 0, textColor, false);
        GlStateManager.popMatrix();

        GlStateManager.popMatrix();
    }
}
