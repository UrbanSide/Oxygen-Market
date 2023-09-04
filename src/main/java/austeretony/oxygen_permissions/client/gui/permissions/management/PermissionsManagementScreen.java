package austeretony.oxygen_permissions.client.gui.permissions.management;

import austeretony.oxygen_core.client.gui.base.Alignment;
import austeretony.oxygen_core.client.gui.base.core.OxygenScreen;
import austeretony.oxygen_core.client.gui.base.core.Section;
import austeretony.oxygen_core.client.gui.base.core.Workspace;
import austeretony.oxygen_core.client.util.MinecraftClient;
import austeretony.oxygen_core.common.sync.SyncMeta;
import austeretony.oxygen_permissions.client.PermissionsManagerClient;
import austeretony.oxygen_permissions.client.settings.PermissionsSettings;
import austeretony.oxygen_permissions.common.main.PermissionsMain;
import austeretony.oxygen_permissions.common.permissions.sync.SyncReason;
import net.minecraft.client.gui.GuiScreen;

import javax.annotation.Nullable;

public class PermissionsManagementScreen extends OxygenScreen {

    private RolesSection rolesSection;
    private PlayersSection playersSection;

    @Override
    public void initGui() {
        super.initGui();
        PermissionsManagerClient.instance().requestManagementDataSync();
    }

    @Override
    public int getScreenId() {
        return PermissionsMain.SCREEN_ID_PERMISSIONS_MANAGEMENT;
    }

    @Override
    public Workspace createWorkspace() {
        Workspace workspace = new Workspace(this, 300, 170);
        workspace.setAlignment(Alignment.valueOf(PermissionsSettings.PERMISSIONS_SCREEN_ALIGNMENT.asString()), 0, 0);
        return workspace;
    }

    @Override
    public void addSections() {
        getWorkspace().addSection(rolesSection = new RolesSection(this));
        getWorkspace().addSection(playersSection = new PlayersSection(this));
    }

    @Override
    public Section getDefaultSection() {
        return rolesSection;
    }

    public static void open() {
        MinecraftClient.displayGuiScreen(new PermissionsManagementScreen());
    }

    public static void dataSynchronized(SyncReason reason, @Nullable SyncMeta meta) {
        GuiScreen screen = MinecraftClient.getCurrentScreen();
        if (screen instanceof PermissionsManagementScreen) {
            PermissionsManagementScreen managementScreen = (PermissionsManagementScreen) screen;
            managementScreen.rolesSection.dataSynchronized(reason, meta);
            managementScreen.playersSection.dataSynchronized(reason, meta);
        }
    }
}
