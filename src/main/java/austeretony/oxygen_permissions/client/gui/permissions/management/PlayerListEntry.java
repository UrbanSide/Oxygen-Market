package austeretony.oxygen_permissions.client.gui.permissions.management;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.gui.base.GUIUtils;
import austeretony.oxygen_core.client.gui.base.Textures;
import austeretony.oxygen_core.client.gui.base.block.Texture;
import austeretony.oxygen_core.client.gui.base.common.ListEntry;
import austeretony.oxygen_core.client.gui.util.OxygenGUIUtils;
import austeretony.oxygen_core.common.player.ActivityStatus;
import austeretony.oxygen_core.common.player.shared.PlayerSharedData;
import austeretony.oxygen_permissions.client.PermissionsManagerClient;
import austeretony.oxygen_permissions.common.permissions.PlayerRoles;
import austeretony.oxygen_permissions.common.permissions.Role;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import java.util.UUID;

public class PlayerListEntry extends ListEntry<UUID> {

    private static final Texture STATUS_ICONS_TEXTURE = Texture.builder()
            .texture(Textures.STATUS_ICONS)
            .size(3, 3)
            .imageSize(12, 3)
            .build();

    private final String lastActivityStr, usernameStr, rolesStr;
    private final ActivityStatus activityStatus;

    public PlayerListEntry(@Nonnull PlayerSharedData sharedData) {
        super(sharedData.getUsername(), sharedData.getPlayerUUID());
        activityStatus = OxygenClient.getPlayerActivityStatus(sharedData);
        usernameStr = sharedData.getUsername();
        lastActivityStr = OxygenGUIUtils.getLastActivityFormattedString(sharedData);
        rolesStr = getRolesStr();
    }

    private String getRolesStr() {
        PlayerRoles roles = PermissionsManagerClient.instance().getPlayerRoles(entry);
        if (roles == null) return "";

        StringBuilder rolesBuilder = new StringBuilder();
        for (int roleId : roles.getRolesSet()) {
            Role role = PermissionsManagerClient.instance().getRole(roleId);
            if (role == null) continue;

            if (rolesBuilder.length() != 0) {
                rolesBuilder.append(TextFormatting.RESET);
                rolesBuilder.append(", ");
            }
            rolesBuilder.append(role.getNameColor());
            rolesBuilder.append(role.getName());
            rolesBuilder.append(" [");
            rolesBuilder.append(role.getId());
            rolesBuilder.append("]");
        }
        return rolesBuilder.toString();
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        if (!isVisible()) return;
        GUIUtils.pushMatrix();
        GUIUtils.translate(getX(), getY());

        GUIUtils.drawRect(0, 0, getWidth(), getHeight(), getColorFromState(fill));

        GUIUtils.colorDef();
        GUIUtils.drawTexturedRect(7, 4, activityStatus.ordinal() * STATUS_ICONS_TEXTURE.getWidth(),
                STATUS_ICONS_TEXTURE.getV(), STATUS_ICONS_TEXTURE);

        float textY = (getHeight() - GUIUtils.getTextHeight(text.getScale())) / 2F + .5F;
        GUIUtils.drawString(usernameStr, 18F, textY, text.getScale(), getColorFromState(text), false);
        GUIUtils.drawString(rolesStr, 85F, textY, text.getScale(), getColorFromState(text), false);

        GUIUtils.popMatrix();
    }

    @Override
    public void drawForeground(int mouseX, int mouseY, float partialTicks) {
        if (!isVisible() || activityStatus != ActivityStatus.OFFLINE) return;
        if (mouseX >= getX() + 6 && mouseY >= getY() + 3 && mouseX < getX() + 11 && mouseY < getY() + 8) {
            drawToolTip(getX() + 10, getY() + 4 - TOOLTIP_HEIGHT, lastActivityStr);
        }
    }
}
