package austeretony.oxygen_mail.client.gui.mail.incoming;

import austeretony.oxygen_core.client.gui.base.GUIUtils;
import austeretony.oxygen_core.client.gui.base.common.ListEntry;
import austeretony.oxygen_core.client.settings.CoreSettings;
import austeretony.oxygen_core.client.util.MinecraftClient;
import austeretony.oxygen_mail.client.MailManagerClient;
import austeretony.oxygen_mail.common.mail.MailEntry;

import javax.annotation.Nonnull;

public class MailListEntry extends ListEntry<MailEntry> {

    private boolean isRead;

    public MailListEntry(@Nonnull MailEntry entry) {
        super(localize(entry.getSubject()), entry);
        checkRead();
    }

    private void checkRead() {
        isRead = MailManagerClient.instance().getReadMailSet().contains(entry.getId());
    }

    @Override
    public void update() {
        super.update();
        if (MinecraftClient.getPlayer().ticksExisted % 10 == 0) {
            checkRead();
        }
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        if (!isVisible()) return;
        GUIUtils.pushMatrix();
        GUIUtils.translate(getX(), getY());

        int color = fill.getColorEnabled();
        if (!isEnabled())
            color = fill.getColorDisabled();
        else if (isMouseOver() || selected)
            color = fill.getColorMouseOver();
        GUIUtils.drawRect(0, 0, getWidth(), getHeight(), color);

        color = isRead ? CoreSettings.COLOR_TEXT_ADDITIONAL_ENABLED.asInt() : text.getColorEnabled();
        if (!isEnabled())
            color = text.getColorDisabled();
        else if (isMouseOver() || selected)
            color = text.getColorMouseOver();
        if (entry.isPending()) {
            GUIUtils.drawString("!", 2F, (getHeight() - GUIUtils.getTextHeight(text.getScale())) / 2F + .5F,
                    text.getScale(), CoreSettings.COLOR_TEXT_SPECIAL.asInt(), false);
        }
        GUIUtils.drawString(text, 6F, (getHeight() - GUIUtils.getTextHeight(text.getScale())) / 2F + .5F, color);

        GUIUtils.popMatrix();
    }
}
