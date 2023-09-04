package austeretony.oxygen_permissions.client.gui.permissions.info;

import austeretony.oxygen_core.client.gui.base.Alignment;
import austeretony.oxygen_core.client.gui.base.core.OxygenScreen;
import austeretony.oxygen_core.client.gui.base.core.Section;
import austeretony.oxygen_core.client.gui.base.core.Workspace;
import austeretony.oxygen_core.client.gui.menu.OxygenMenuEntry;
import austeretony.oxygen_core.client.util.MinecraftClient;
import austeretony.oxygen_permissions.client.gui.menu.PermissionsInfoScreenEntry;
import austeretony.oxygen_permissions.client.settings.PermissionsSettings;
import austeretony.oxygen_permissions.common.main.PermissionsMain;

public class PermissionsInfoScreen extends OxygenScreen {

    public static final OxygenMenuEntry PRIVILEGES_INFO_SCREEN_ENTRY = new PermissionsInfoScreenEntry();

    private PermissionsInfoSection commonSection;

    @Override
    public int getScreenId() {
        return PermissionsMain.SCREEN_ID_PERMISSIONS_INFO;
    }

    @Override
    public Workspace createWorkspace() {
        Workspace workspace = new Workspace(this, 280, 162);
        workspace.setAlignment(Alignment.valueOf(PermissionsSettings.PERMISSIONS_SCREEN_ALIGNMENT.asString()), 0, 0);
        return workspace;
    }

    @Override
    public void addSections() {
        getWorkspace().addSection(commonSection = new PermissionsInfoSection(this));
    }

    @Override
    public Section getDefaultSection() {
        return commonSection;
    }

    public void formattingRoleChanged(int roleId) {
        commonSection.formattingRoleChanged(roleId);
    }

    public static void open() {
        MinecraftClient.displayGuiScreen(new PermissionsInfoScreen());
    }
}
