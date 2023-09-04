package austeretony.oxygen_permissions.client.gui.permissions.management.callback;

import austeretony.oxygen_core.client.gui.base.Keys;
import austeretony.oxygen_core.client.gui.base.Texts;
import austeretony.oxygen_core.client.gui.base.background.Background;
import austeretony.oxygen_core.client.gui.base.button.VerticalSlider;
import austeretony.oxygen_core.client.gui.base.core.Callback;
import austeretony.oxygen_core.client.gui.base.list.ScrollableList;
import austeretony.oxygen_core.client.gui.base.special.KeyButton;
import austeretony.oxygen_core.client.gui.base.text.TextLabel;
import austeretony.oxygen_permissions.client.PermissionsManagerClient;
import austeretony.oxygen_permissions.client.gui.permissions.PermissionListEntry;
import austeretony.oxygen_permissions.common.permissions.Permission;
import austeretony.oxygen_permissions.common.permissions.Role;

import java.util.ArrayList;
import java.util.List;

public class RemovePermissionsCallback extends Callback {

    private final Role role;

    private ScrollableList<Permission> permissionsList;
    private KeyButton confirmButton;

    private final List<Integer> permissionsIds = new ArrayList<>();

    public RemovePermissionsCallback(Role role) {
        super(180, 94);
        this.role = role;
    }

    @Override
    public void init() {
        addWidget(new Background.UnderlinedTitleBottom(this));
        addWidget(new TextLabel(4, 12, Texts.title("oxygen_permissions.gui.permissions_management.callback.remove_role_permissions")));

        addWidget(new TextLabel(6, 24, Texts.common("oxygen_permissions.gui.permissions_info.permissions")));
        addWidget(permissionsList = new ScrollableList<>(6, 25, 5, getWidth() - 2 * 6 - 3, 10)
                .<Permission>setEntryMouseClickListener((previous, current, x, y, button) -> {
                    boolean selected = current.isSelected();
                    current.setSelected(!selected);

                    int id = current.getEntry().getId();
                    if (current.isSelected()) {
                        if (!permissionsIds.contains(id)) {
                            permissionsIds.add(id);
                        }
                    } else {
                        permissionsIds.remove(id);
                    }

                    confirmButton.setEnabled(!permissionsIds.isEmpty());
                }));
        VerticalSlider slider = new VerticalSlider(6 + permissionsList.getWidth() + 1, 25, 2, 54);
        addWidget(slider);
        permissionsList.setSlider(slider);

        for (Permission permission : role.getPermissionsMap().values()) {
            permissionsList.addElement(new PermissionListEntry(permission));
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
        PermissionsManagerClient.instance().removePermissionsRequest(role.getId(), permissionsIds);
        close();
    }
}
