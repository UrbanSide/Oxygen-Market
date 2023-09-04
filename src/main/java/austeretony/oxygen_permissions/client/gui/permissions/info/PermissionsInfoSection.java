package austeretony.oxygen_permissions.client.gui.permissions.info;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.gui.base.Texts;
import austeretony.oxygen_core.client.gui.base.background.Background;
import austeretony.oxygen_core.client.gui.base.button.VerticalSlider;
import austeretony.oxygen_core.client.gui.base.common.ListEntry;
import austeretony.oxygen_core.client.gui.base.core.Section;
import austeretony.oxygen_core.client.gui.base.list.DropDownList;
import austeretony.oxygen_core.client.gui.base.list.ScrollableList;
import austeretony.oxygen_core.client.gui.base.text.TextLabel;
import austeretony.oxygen_core.client.gui.util.OxygenGUIUtils;
import austeretony.oxygen_core.client.util.MinecraftClient;
import austeretony.oxygen_permissions.client.PermissionsManagerClient;
import austeretony.oxygen_permissions.client.gui.permissions.PermissionListEntry;
import austeretony.oxygen_permissions.common.main.PermissionsMain;
import austeretony.oxygen_permissions.common.permissions.PlayerRoles;
import austeretony.oxygen_permissions.common.permissions.Permission;
import austeretony.oxygen_permissions.common.permissions.Role;

import javax.annotation.Nonnull;

public class PermissionsInfoSection extends Section {

    private ScrollableList<Role> rolesList;
    private ScrollableList<Permission> rolePermissionsList;
    private VerticalSlider rolePermissionsListSlider;
    private TextLabel roleNameLabel, chatFormattingTitleLabel, chatFormattingLabel, permissionsTitleLabel,
            currentFormattingLabel;
    private DropDownList<Integer> formattingList;

    public PermissionsInfoSection(PermissionsInfoScreen screen) {
        super(screen, "", true);
    }

    @Override
    public void init() {
        addWidget(new Background.UnderlinedTitle(this));
        addWidget(new TextLabel(4, 12, Texts.title("oxygen_permissions.gui.permissions_info.title")));

        int entryWidth = 70;
        addWidget(new TextLabel(6, 24, Texts.common("oxygen_permissions.gui.permissions_info.your_roles")));
        addWidget(rolesList = new ScrollableList<>(6, 26, 10, entryWidth, 10)
                .<Role>setEntryMouseClickListener((previous, current, x, y, button) -> {
                    if (current == previous) return;
                    if (previous != null) {
                        previous.setSelected(false);
                    }
                    current.setSelected(true);
                    displayRoleData(current.getEntry());
                }));

        int roleDataX = 6 + entryWidth + 6;
        addWidget(roleNameLabel = new TextLabel(roleDataX, 24, Texts.common("").incrementScale(.05F)).setVisible(false));
        addWidget(chatFormattingTitleLabel = new TextLabel(roleDataX, 36,
                Texts.common("oxygen_permissions.gui.permissions_info.chat_formatting")).setVisible(false));
        addWidget(chatFormattingLabel = new TextLabel(roleDataX, 45, Texts.common("")).setVisible(false));
        addWidget(permissionsTitleLabel = new TextLabel(roleDataX, 58,
                Texts.common("oxygen_permissions.gui.permissions_info.permissions")).setVisible(false));
        addWidget(rolePermissionsList = new ScrollableList<>(roleDataX, 59, 7, getWidth() - roleDataX - 6, 10)
                .setVisible(false));
        addWidget(rolePermissionsListSlider = new VerticalSlider(roleDataX + rolePermissionsList.getWidth() + 1, 59, 2, 76)
                .setVisible(false));
        rolePermissionsList.setSlider(rolePermissionsListSlider);

        initRolesList();

        addWidget(new TextLabel(6, 145, Texts.common("oxygen_permissions.gui.permissions_info.chat_formatting")));
        addWidget(formattingList = new DropDownList<>(6, 147, entryWidth, "")
                .<Integer>setEntryMouseClickListener((previous, current, x, y, button) ->
                        PermissionsManagerClient.instance().changeFormattingRoleRequest(current.getEntry())));
        addWidget(currentFormattingLabel = new TextLabel(6 + entryWidth + 6, 155, Texts.common("")));

        initChatFormattingData();
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        OxygenGUIUtils.closeScreenOnKeyPress(getScreen(), keyCode);
        super.keyTyped(typedChar, keyCode);
    }

    private void initRolesList() {
        PlayerRoles roles = PermissionsManagerClient.instance().getPlayerRoles(OxygenClient.getClientPlayerUUID());
        if (roles == null) return;

        for (int roleId : roles.getRolesSet()) {
            Role role = PermissionsManagerClient.instance().getRole(roleId);
            if (role == null) continue;

            rolesList.addElement(ListEntry.of(role.getNameColor() + role.getName(), role));
            if (role.getId() == roles.getFirstRole()) {
                displayRoleData(role);
            }
        }
        rolesList.setFirstElementSelected();
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

    private void initChatFormattingData() {
        PlayerRoles roles = PermissionsManagerClient.instance().getPlayerRoles(OxygenClient.getClientPlayerUUID());

        String noFormattingStr = localize("oxygen_permissions.gui.permissions_info.no_formatting");
        formattingList.getText().setText(noFormattingStr);
        updateCurrentChatFormattingDisplay(PermissionsMain.DEFAULT_ROLE_ID);

        if (roles == null) return;

        formattingList.addElement(ListEntry.of(noFormattingStr, PermissionsMain.DEFAULT_ROLE_ID));
        for (int roleId : roles.getRolesSet()) {
            Role role = PermissionsManagerClient.instance().getRole(roleId);
            if (role == null) continue;
            formattingList.addElement(ListEntry.of(role.getNameColor() + role.getName(), role.getId()));
        }

        int formattingRoleId = roles.getChatFormattingRole();
        Role formattingRole = PermissionsManagerClient.instance().getRole(formattingRoleId);
        if (formattingRole != null) {
            formattingList.getText().setText(formattingRole.getNameColor() + formattingRole.getName());
            updateCurrentChatFormattingDisplay(formattingRole.getId());
        }
    }

    private void updateCurrentChatFormattingDisplay(int roleId) {
        Role role = PermissionsManagerClient.instance().getRole(roleId);
        String username = MinecraftClient.getEntityName(MinecraftClient.getClientPlayer());
        String sampleMsg = localize("oxygen_permissions.gui.permissions_info.chat_message");
        currentFormattingLabel.getText().setText(PermissionsMain.getFormattedChatMessage(username, sampleMsg, role)
                .getFormattedText());
    }

    public void formattingRoleChanged(int roleId) {
        updateCurrentChatFormattingDisplay(roleId);
    }
}
