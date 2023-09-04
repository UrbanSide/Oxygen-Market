package austeretony.oxygen_permissions.client.gui.permissions.management.context;

import austeretony.oxygen_core.client.gui.base.context.ContextAction;
import austeretony.oxygen_core.client.gui.base.core.Section;
import austeretony.oxygen_permissions.client.PermissionsManagerClient;
import austeretony.oxygen_permissions.client.gui.permissions.management.callback.RemoveRoleFromPlayerCallback;
import austeretony.oxygen_permissions.common.permissions.PlayerRoles;

import javax.annotation.Nonnull;
import java.util.UUID;

public class RemoveRoleFromPlayerContextAction implements ContextAction<UUID> {

    @Nonnull
    @Override
    public String getName(UUID entry) {
        return "oxygen_permissions.gui.permissions_management.context.remove_role_from_player";
    }

    @Override
    public boolean isValid(UUID entry) {
        PlayerRoles roles = PermissionsManagerClient.instance().getPlayerRoles(entry);
        return roles != null && roles.getRolesAmount() != 0;
    }

    @Override
    public void execute(UUID entry) {
        Section.tryOpenCallback(new RemoveRoleFromPlayerCallback(entry));
    }
}
