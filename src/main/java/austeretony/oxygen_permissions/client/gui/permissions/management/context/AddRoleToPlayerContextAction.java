package austeretony.oxygen_permissions.client.gui.permissions.management.context;

import austeretony.oxygen_core.client.gui.base.context.ContextAction;
import austeretony.oxygen_core.client.gui.base.core.Section;
import austeretony.oxygen_permissions.client.gui.permissions.management.callback.AddRoleToPlayerCallback;

import javax.annotation.Nonnull;
import java.util.UUID;

public class AddRoleToPlayerContextAction implements ContextAction<UUID> {

    @Nonnull
    @Override
    public String getName(UUID entry) {
        return "oxygen_permissions.gui.permissions_management.context.add_role_to_player";
    }

    @Override
    public boolean isValid(UUID entry) {
        return true;
    }

    @Override
    public void execute(UUID entry) {
        Section.tryOpenCallback(new AddRoleToPlayerCallback(entry));
    }
}
