package austeretony.oxygen_menu.client.gui.menu;

import austeretony.oxygen_core.client.gui.base.Alignment;
import austeretony.oxygen_core.client.gui.base.GUIUtils;
import austeretony.oxygen_core.client.gui.base.common.ListEntry;
import austeretony.oxygen_core.client.gui.menu.OxygenMenuEntry;

import javax.annotation.Nonnull;

public class MenuListEntry extends ListEntry<OxygenMenuEntry> {

    private final Alignment alignment;
    private final String keyStr;

    public MenuListEntry(@Nonnull OxygenMenuEntry entry, Alignment screenAlignment) {
        super(entry.getDisplayName(), entry);
        alignment = screenAlignment;
        keyStr = "[" + GUIUtils.getKeyDisplayString(entry.getKeyCode()) + "]";
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        if (!isVisible()) return;
        GUIUtils.pushMatrix();
        GUIUtils.translate(getX(), getY());

        if (alignment == Alignment.RIGHT || alignment == Alignment.LEFT) {
            GUIUtils.drawGradientRect(0D, 0D, getWidth(), getHeight(), 0x00000000, getColorFromState(fill), alignment);
        } else {
            GUIUtils.drawRect(0, 0, getWidth(), getHeight(), getColorFromState(fill));
        }

        float textY = (getHeight() - GUIUtils.getTextHeight(text.getScale())) / 2F + .5F;
        GUIUtils.drawString(text.getText(), alignment == Alignment.RIGHT ? getWidth() - text.getWidth() - 6.0F : 6.0F, textY,
                text.getScale(), getColorFromState(text), false);
        if (isMouseOver()) {
            GUIUtils.drawString(keyStr, alignment == Alignment.RIGHT ? getWidth() - text.getWidth() - 6.0F - GUIUtils.getTextWidth(keyStr, text.getScale()) - 4F : text.getWidth() + 6F + 4F,
                    textY, text.getScale(), text.getColorDisabled(), false);
        }

        GUIUtils.popMatrix();
    }
}
