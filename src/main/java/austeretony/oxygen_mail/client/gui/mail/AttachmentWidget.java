package austeretony.oxygen_mail.client.gui.mail;

import austeretony.oxygen_core.client.gui.base.GUIUtils;
import austeretony.oxygen_core.client.gui.base.core.Widget;
import austeretony.oxygen_mail.common.mail.Attachment;

public class AttachmentWidget extends Widget<AttachmentWidget> {

    private Attachment attachment;

    public AttachmentWidget(int x, int y) {
        setPosition(x, y);
        setSize(124, 16);

        setEnabled(true);
        setVisible(true);
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        if (!isVisible() || attachment == null) return;
        GUIUtils.pushMatrix();
        GUIUtils.translate(getX(), getY());

        attachment.draw(this, mouseX, mouseY);

        GUIUtils.popMatrix();
    }

    @Override
    public void drawForeground(int mouseX, int mouseY, float partialTicks) {
        if (!isVisible() || attachment == null) return;
        attachment.drawForeground(this, mouseX, mouseY);
    }
}
