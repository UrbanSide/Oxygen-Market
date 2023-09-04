package austeretony.oxygen_permissions.client.gui.permissions.management.context;

import austeretony.oxygen_core.client.gui.base.context.ContextAction;
import austeretony.oxygen_core.client.gui.base.core.Section;
import austeretony.oxygen_permissions.client.gui.permissions.management.callback.RemovePermissionsCallback;
import austeretony.oxygen_permissions.common.permissions.Role;

import javax.annotation.Nonnull;

public class RemovePermissionsContextAction implements ContextAction<Role> {

    @Nonnull
    @Override
    public String getName(Role entry) {
        return "oxygen_permissions.gui.permissions_management.context.remove_role_permissions";
    }

    @Override
    public boolean isValid(Role entry) {
        return !entry.getPermissionsMap().isEmpty();
    }

    @Override
    public void execute(Role entry) {
        Section.tryOpenCallback(new RemovePermissionsCallback(entry));
    }
}
