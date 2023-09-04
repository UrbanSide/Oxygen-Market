package austeretony.oxygen_permissions.client.gui.permissions.management;

import austeretony.oxygen_core.client.gui.base.GUIUtils;
import austeretony.oxygen_core.client.gui.base.common.ListEntry;
import austeretony.oxygen_core.common.privileges.PrivilegeRegistry;

import javax.annotation.Nonnull;

public class PermissionRegistryListEntry extends ListEntry<PrivilegeRegistry.Entry> {

    public PermissionRegistryListEntry(@Nonnull PrivilegeRegistry.Entry entry) {
        super(entry.getDisplayName(), entry);
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

        color = text.getColorEnabled();
        if (!isEnabled())
            color = text.getColorDisabled();
        else if (isMouseOver() || selected)
            color = text.getColorMouseOver();

        float textY = (getHeight() - GUIUtils.getTextHeight(text.getScale())) / 2F + .5F;
        GUIUtils.drawString(String.valueOf(entry.getId()), 2F, textY, text.getScale(), color, false);
        GUIUtils.drawString(text.getText(), 24F, textY, text.getScale(), color, false);
        GUIUtils.drawString(entry.getValueType().toString(), getWidth() - 32F, textY, text.getScale(), color, false);

        GUIUtils.popMatrix();
    }
}
