package austeretony.oxygen_permissions.client.gui.permissions;

import austeretony.oxygen_core.client.gui.base.GUIUtils;
import austeretony.oxygen_core.client.gui.base.common.ListEntry;
import austeretony.oxygen_core.common.privileges.PrivilegeRegistry;
import austeretony.oxygen_permissions.common.permissions.Permission;

import javax.annotation.Nonnull;

public class PermissionListEntry extends ListEntry<Permission> {

    private final String name;

    public PermissionListEntry(@Nonnull Permission permission) {
        super("", permission);
        PrivilegeRegistry.Entry entry = PrivilegeRegistry.getEntry(this.entry.getId());
        name = entry != null ? entry.getDisplayName() : "...";
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
        GUIUtils.drawString(name, 24F, textY, text.getScale(), color, false);
        GUIUtils.drawString(entry.get().toString(), getWidth() - 32F, textY, text.getScale(), color, false);

        GUIUtils.popMatrix();
    }
}
