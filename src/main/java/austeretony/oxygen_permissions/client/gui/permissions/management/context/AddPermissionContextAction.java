package austeretony.oxygen_permissions.client.gui.permissions.management.context;

import austeretony.oxygen_core.client.gui.base.context.ContextAction;
import austeretony.oxygen_core.client.gui.base.core.Section;
import austeretony.oxygen_permissions.client.gui.permissions.management.callback.AddPermissionCallback;
import austeretony.oxygen_permissions.common.permissions.Role;

import javax.annotation.Nonnull;

public class AddPermissionContextAction implements ContextAction<Role> {

    @Nonnull
    @Override
    public String getName(Role entry) {
        return "oxygen_permissions.gui.permissions_management.context.add_role_permission";
    }

    @Override
    public boolean isValid(Role entry) {
        return true;
    }

    @Override
    public void execute(Role entry) {
        Section.tryOpenCallback(new AddPermissionCallback(entry));
    }
}
