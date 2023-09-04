package austeretony.oxygen_permissions.client.gui.permissions.management.callback;

import austeretony.oxygen_core.client.gui.base.Keys;
import austeretony.oxygen_core.client.gui.base.Texts;
import austeretony.oxygen_core.client.gui.base.background.Background;
import austeretony.oxygen_core.client.gui.base.button.VerticalSlider;
import austeretony.oxygen_core.client.gui.base.common.ListEntry;
import austeretony.oxygen_core.client.gui.base.core.Callback;
import austeretony.oxygen_core.client.gui.base.list.ScrollableList;
import austeretony.oxygen_core.client.gui.base.special.KeyButton;
import austeretony.oxygen_core.client.gui.base.text.TextLabel;
import austeretony.oxygen_permissions.client.PermissionsManagerClient;
import austeretony.oxygen_permissions.common.permissions.PlayerRoles;
import austeretony.oxygen_permissions.common.permissions.Role;

import java.util.*;
import java.util.stream.Collectors;

public class AddRoleToPlayerCallback extends Callback {

    private final UUID playerUUID;

    private ScrollableList<Integer> rolesList;
    private KeyButton confirmButton;

    private final List<Integer> rolesIds = new ArrayList<>();

    public AddRoleToPlayerCallback(UUID playerUUID) {
        super(160, 116);
        this.playerUUID = playerUUID;
    }

    @Override
    public void init() {
        addWidget(new Background.UnderlinedTitleBottom(this));
        addWidget(new TextLabel(4, 12, Texts.title("oxygen_permissions.gui.permissions_management.callback.add_role_to_player")));

        addWidget(new TextLabel(6, 24, Texts.common("oxygen_permissions.gui.permissions_management.label.roles")));
        addWidget(rolesList = new ScrollableList<>(6, 25, 7, getWidth() - 2 * 6, 10)
                .<Integer>setEntryMouseClickListener((previous, current, x, y, button) -> {
                    boolean selected = current.isSelected();
                    current.setSelected(!selected);

                    int id = current.getEntry();
                    if (current.isSelected()) {
                        if (!rolesIds.contains(id)) {
                            rolesIds.add(id);
                        }
                    } else {
                        rolesIds.remove((Integer) id);
                    }

                    confirmButton.setEnabled(!rolesIds.isEmpty());
                }));
        VerticalSlider slider = new VerticalSlider(6 + rolesList.getWidth() + 1, 25, 2, 76);
        addWidget(slider);
        rolesList.setSlider(slider);

        PlayerRoles playerRoles = PermissionsManagerClient.instance().getPlayerRoles(playerUUID);
        List<Role> roles = PermissionsManagerClient.instance().getRolesMap().values()
                .stream()
                .sorted(Comparator.comparing(Role::getId).reversed())
                .filter(role -> playerRoles == null || !playerRoles.haveRole(role.getId()))
                .collect(Collectors.toList());

        for (Role role : roles) {
            rolesList.addElement(ListEntry.of(role.getNameColor() + role.getName() + " [" + role.getId() + "]", role.getId()));
        }

        int buttonPosSegment = (int) (getWidth() / 2F);
        addWidget(confirmButton = new KeyButton(0, getHeight() - 10, Keys.CONFIRM_KEY, "oxygen_core.gui.button.confirm")
                .setPressListener(this::confirm).setEnabled(false));
        confirmButton.setX(getX() + (int) ((buttonPosSegment - confirmButton.getText().getWidth()) / 2F));
        KeyButton cancelButton;
        addWidget(cancelButton = new KeyButton(0, getHeight() - 10, Keys.CANCEL_KEY, "oxygen_core.gui.button.cancel")
                .setPressListener(this::close));
        cancelButton.setX(getX() + buttonPosSegment + (int) ((buttonPosSegment - cancelButton.getText().getWidth()) / 2F));
    }

    private void confirm() {
        PermissionsManagerClient.instance().addRolesToPlayerRequest(playerUUID, rolesIds);
        close();
    }
}
