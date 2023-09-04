package austeretony.oxygen_permissions.client.gui.permissions.management;

import austeretony.oxygen_core.client.gui.base.Keys;
import austeretony.oxygen_core.client.gui.base.Layer;
import austeretony.oxygen_core.client.gui.base.Texts;
import austeretony.oxygen_core.client.gui.base.background.Background;
import austeretony.oxygen_core.client.gui.base.button.VerticalSlider;
import austeretony.oxygen_core.client.gui.base.common.ListEntry;
import austeretony.oxygen_core.client.gui.base.core.Section;
import austeretony.oxygen_core.client.gui.base.core.Widget;
import austeretony.oxygen_core.client.gui.base.list.ScrollableList;
import austeretony.oxygen_core.client.gui.base.special.KeyButton;
import austeretony.oxygen_core.client.gui.base.special.SectionSwitcher;
import austeretony.oxygen_core.client.gui.base.text.TextLabel;
import austeretony.oxygen_core.client.gui.util.OxygenGUIUtils;
import austeretony.oxygen_core.client.util.MinecraftClient;
import austeretony.oxygen_core.common.sync.SyncMeta;
import austeretony.oxygen_permissions.client.PermissionsManagerClient;
import austeretony.oxygen_permissions.client.gui.permissions.PermissionListEntry;
import austeretony.oxygen_permissions.client.gui.permissions.management.callback.CreateRoleCallback;
import austeretony.oxygen_permissions.client.gui.permissions.management.context.AddPermissionContextAction;
import austeretony.oxygen_permissions.client.gui.permissions.management.context.EditRoleContextAction;
import austeretony.oxygen_permissions.client.gui.permissions.management.context.RemovePermissionsContextAction;
import austeretony.oxygen_permissions.client.gui.permissions.management.context.RemoveRoleContextAction;
import austeretony.oxygen_permissions.common.main.PermissionsMain;
import austeretony.oxygen_permissions.common.permissions.Permission;
import austeretony.oxygen_permissions.common.permissions.Role;
import austeretony.oxygen_permissions.common.permissions.sync.SyncReason;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class RolesSection extends Section {

    private ScrollableList<Role> rolesList;
    private ScrollableList<Permission> rolePermissionsList;
    private VerticalSlider rolePermissionsListSlider;
    private TextLabel roleNameLabel, chatFormattingTitleLabel, chatFormattingLabel, permissionsTitleLabel, rolesAmountLabel;

    public RolesSection(PermissionsManagementScreen screen) {
        super(screen, localize("oxygen_permissions.gui.permissions_management.roles"), true);
    }

    @Override
    public void init() {
        addWidget(new Background.UnderlinedTitleButtons(this));
        addWidget(new TextLabel(4, 12, Texts.title("oxygen_permissions.gui.permissions_management.title")));
        addWidget(new SectionSwitcher(this));

        int entryWidth = 70;
        addWidget(new TextLabel(6, 24, Texts.common("oxygen_permissions.gui.permissions_management.label.roles")));
        addWidget(rolesAmountLabel = new TextLabel(0, 24, Texts.additionalDark("")));
        addWidget(rolesList = new ScrollableList<>(6, 26, 13, entryWidth, 10)
                .<Role>setEntryMouseClickListener((previous, current, x, y, button) -> {
                    if (current == previous) return;
                    if (previous != null) {
                        previous.setSelected(false);
                    }
                    current.setSelected(true);
                    displayRoleData(current.getEntry());
                }));

        rolesList.createContextMenu(Arrays.asList(
                new RemoveRoleContextAction(),
                new EditRoleContextAction(),
                new AddPermissionContextAction(),
                new RemovePermissionsContextAction()));

        int roleDataX = 6 + entryWidth + 6;
        addWidget(roleNameLabel = new TextLabel(roleDataX, 24, Texts.common("").incrementScale(.05F)).setVisible(false));
        addWidget(chatFormattingTitleLabel = new TextLabel(roleDataX, 36,
                Texts.common("oxygen_permissions.gui.permissions_info.chat_formatting")).setVisible(false));
        addWidget(chatFormattingLabel = new TextLabel(roleDataX, 45, Texts.common("")).setVisible(false));
        addWidget(permissionsTitleLabel = new TextLabel(roleDataX, 58,
                Texts.common("oxygen_permissions.gui.permissions_info.permissions")).setVisible(false));
        addWidget(rolePermissionsList = new ScrollableList<>(roleDataX, 59, 10, getWidth() - roleDataX - 6, 10)
                .setVisible(false));
        addWidget(rolePermissionsListSlider = new VerticalSlider(roleDataX + rolePermissionsList.getWidth() + 1, 59, 2, 109)
                .setVisible(false));
        rolePermissionsList.setSlider(rolePermissionsListSlider);

        String keyButtonText = localize("oxygen_permissions.gui.permissions_management.label.create_role");
        KeyButton keyButton;
        addWidget(keyButton = new KeyButton(0, 0, Keys.ACTION_KEY, keyButtonText)
                .setLayer(Layer.FRONT)
                .setPressListener(() -> openCallback(new CreateRoleCallback())));
        OxygenGUIUtils.calculateBottomCenteredOffscreenButtonPosition(keyButton, 1, 1);
    }

    protected void dataSynchronized(SyncReason reason, @Nullable SyncMeta meta) {
        switch (reason) {
            case MENU_OPEN:
            case ROLE_CREATED:
                initRolesList();
            case ROLE_REMOVED:
                int position = rolesList.getScrollPosition();
                initRolesList();
                rolesList.setScrollPosition(position);
                break;
            case ROLE_EDITED:
            case ROLE_PERMISSION_ADDED:
            case ROLE_PERMISSIONS_REMOVED:
                int pos = rolesList.getScrollPosition();
                initRolesList();
                rolesList.setScrollPosition(pos);

                if (meta == null) return;
                int defaultRole = ((ListEntry<Role>) rolesList.getWidgets().get(0)).getEntry().getId();
                int roleId = meta.getValue("role_id", defaultRole);

                for (Widget widget : rolesList.getWidgets()) {
                    ListEntry<Role> entry = (ListEntry<Role>) widget;
                    entry.setSelected(false);
                    if (entry.getEntry().getId() == roleId) {
                        entry.setSelected(true);
                        displayRoleData(entry.getEntry());
                        break;
                    }
                }
                break;
        }
    }

    private void displayRoleData(@Nonnull Role role) {
        roleNameLabel.setVisible(true);
        chatFormattingTitleLabel.setVisible(true);
        chatFormattingLabel.setVisible(true);
        permissionsTitleLabel.setVisible(true);

        roleNameLabel.getText().setText(role.getNameColor() + role.getName());

        String username = MinecraftClient.getEntityName(MinecraftClient.getClientPlayer());
        String sampleMsg = localize("oxygen_permissions.gui.permissions_info.chat_message");
        chatFormattingLabel.getText().setText(PermissionsMain.getFormattedChatMessage(username, sampleMsg, role)
                .getFormattedText());

        rolePermissionsList.clear();
        if (role.getPermissionsMap().isEmpty()) return;
        rolePermissionsList.setVisible(true);
        rolePermissionsListSlider.setVisible(true);

        for (Permission permission : role.getPermissionsMap().values()) {
            rolePermissionsList.addElement(new PermissionListEntry(permission));
        }
    }

    private void initRolesList() {
        rolesList.clear();
        Set<Role> roles = new TreeSet<>(Comparator.comparing(Role::getId).reversed());
        roles.addAll(PermissionsManagerClient.instance().getRolesMap().values());

        int index = 0;
        for (Role role : roles) {
            rolesList.addElement(ListEntry.of(role.getNameColor() + role.getName() + " [" + role.getId() + "]", role));
            if (index == 0) {
                displayRoleData(role);
            }
            index++;
        }
        rolesList.setFirstElementSelected();

        String amountStr = roles.size() + "/-";
        rolesAmountLabel.getText().setText(amountStr);
        rolesAmountLabel.setX(6 + 70 - (int) rolesAmountLabel.getText().getWidth());
    }
}
