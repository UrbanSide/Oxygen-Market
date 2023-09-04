package austeretony.oxygen_permissions.client.gui.permissions.management.context;

import austeretony.oxygen_core.client.gui.base.context.ContextAction;
import austeretony.oxygen_core.client.gui.base.core.Callback;
import austeretony.oxygen_core.client.gui.base.core.Section;
import austeretony.oxygen_core.client.gui.base.special.callback.YesNoCallback;
import austeretony.oxygen_core.client.util.MinecraftClient;
import austeretony.oxygen_permissions.client.PermissionsManagerClient;
import austeretony.oxygen_permissions.common.permissions.Role;

import javax.annotation.Nonnull;

public class RemoveRoleContextAction implements ContextAction<Role> {

    @Nonnull
    @Override
    public String getName(Role entry) {
        return "oxygen_permissions.gui.permissions_management.context.remove_role";
    }

    @Override
    public boolean isValid(Role entry) {
        return true;
    }

    @Override
    public void execute(Role entry) {
        Callback callback = new YesNoCallback(
                "oxygen_permissions.gui.permissions_management.callback.deleting_a_role",
                MinecraftClient.localize("oxygen_permissions.gui.permissions_management.callback.deleting_a_role.message",
                        entry.getName() + " [" + entry.getId() + "]"),
                () -> PermissionsManagerClient.instance().removeRoleRequest(entry.getId()));
        Section.tryOpenCallback(callback);
    }
}
