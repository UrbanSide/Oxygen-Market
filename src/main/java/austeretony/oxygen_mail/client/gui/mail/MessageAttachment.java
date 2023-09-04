package austeretony.oxygen_mail.client.gui.mail;

import austeretony.alternateui.screen.core.GUISimpleElement;
import austeretony.oxygen_core.client.api.EnumBaseClientSetting;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_mail.common.mail.Attachment;
import austeretony.oxygen_mail.common.mail.EnumMail;

public class MessageAttachment extends GUISimpleElement<MessageAttachment> {

    private Attachment attachment;

    public MessageAttachment(int xPosition, int yPosition) {             
        this.setPosition(xPosition, yPosition);   
        this.setSize(16, 16);       

        if (EnumBaseClientSetting.ENABLE_ITEMS_DURABILITY_BAR.get().asBoolean())
            this.debugMode();
        this.setTextScale(EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.05F);
        this.setEnabledTextColor(EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt());
        this.setStaticBackgroundColor(EnumBaseGUISetting.INACTIVE_TEXT_COLOR.get().asInt()); 
        this.enableFull();
    }

    @Override
    public void draw(int mouseX, int mouseY) {          
        if (this.isVisible())
            this.attachment.draw(this, mouseX, mouseY);
    }

    public void load(EnumMail type, Attachment attachment) {
        this.attachment = attachment;
    }

    @Override
    public void drawTooltip(int mouseX, int mouseY) {
        if (this.isEnabled() && this.attachment != null && this.attachment.getItemStack() != null && mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.getWidth() && mouseY < this.getY() + this.getHeight())
            this.screen.drawToolTip(this.attachment.getItemStack(), mouseX, mouseY);
    }
}
