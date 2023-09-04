package austeretony.oxygen_permissions.client.gui.permissions.management.callback;

import austeretony.oxygen_core.client.gui.base.Keys;
import austeretony.oxygen_core.client.gui.base.Texts;
import austeretony.oxygen_core.client.gui.base.background.Background;
import austeretony.oxygen_core.client.gui.base.button.VerticalSlider;
import austeretony.oxygen_core.client.gui.base.common.ListEntry;
import austeretony.oxygen_core.client.gui.base.core.Callback;
import austeretony.oxygen_core.client.gui.base.list.ScrollableList;
import austeretony.oxygen_core.client.gui.base.special.KeyButton;
import austeretony.oxygen_core.client.gui.base.text.NumberField;
import austeretony.oxygen_core.client.gui.base.text.TextField;
import austeretony.oxygen_core.client.gui.base.text.TextLabel;
import austeretony.oxygen_core.common.privileges.PrivilegeRegistry;
import austeretony.oxygen_core.common.util.value.TypedValue;
import austeretony.oxygen_core.common.util.value.ValueType;
import austeretony.oxygen_permissions.client.PermissionsManagerClient;
import austeretony.oxygen_permissions.client.gui.permissions.management.PermissionRegistryListEntry;
import austeretony.oxygen_permissions.common.permissions.Permission;
import austeretony.oxygen_permissions.common.permissions.Role;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class AddPermissionCallback extends Callback {

    private final Role role;

    private ScrollableList<PrivilegeRegistry.Entry> permissionsList;
    private NumberField permissionIdField;
    private TextField valueField;
    private KeyButton confirmButton;

    @Nullable
    private ListEntry clickedEntry;

    @Nullable
    private Permission permission;

    public AddPermissionCallback(Role role) {
        super(180, 112);
        this.role = role;
    }

    @Override
    public void init() {
        addWidget(new Background.UnderlinedTitleBottom(this));
        addWidget(new TextLabel(4, 12, Texts.title("oxygen_permissions.gui.permissions_management.callback.add_role_permission")));

        addWidget(new TextLabel(6, 24, Texts.common("oxygen_permissions.gui.permissions_info.permissions")));
        addWidget(permissionsList = new ScrollableList<>(6, 25, 5, getWidth() - 2 * 6 - 3, 10)
                .<PrivilegeRegistry.Entry>setEntryMouseClickListener((previous, current, x, y, button) -> {
                    if (current == previous) return;
                    if (previous != null) {
                        previous.setSelected(false);
                    }
                    current.setSelected(true);

                    clickedEntry = current;
                    permissionIdField.setText(String.valueOf(current.getEntry().getId()));
                    onInput();
                }));
        VerticalSlider slider = new VerticalSlider(6 + permissionsList.getWidth() + 1, 25, 2, 54);
        addWidget(slider);
        permissionsList.setSlider(slider);

        Set<PrivilegeRegistry.Entry> permissions = new TreeSet(Comparator.comparing(PrivilegeRegistry.Entry::getId));
        permissions.addAll(PrivilegeRegistry.getRegistryMap().values());
        for (PrivilegeRegistry.Entry entry : permissions) {
            permissionsList.addElement(new PermissionRegistryListEntry(entry));
        }

        addWidget(new TextLabel(6, 89, Texts.common("oxygen_permissions.gui.permissions_management.label.permission_id")));
        addWidget(permissionIdField = new NumberField(6, 90, 24, 0, Short.MAX_VALUE)
                .setKeyPressListener((keyCode, keyChar) -> {
                    if (clickedEntry != null) {
                        clickedEntry.setSelected(false);
                    }
                    onInput();
                }));

        addWidget(new TextLabel(6 + 24 + 6, 89, Texts.common("oxygen_permissions.gui.permissions_management.label.permission_value")));
        addWidget(valueField = new TextField(6 + 24 + 6, 90, 80, 16)
                .setKeyPressListener((keyCode, keyChar) -> onInput()));

        int buttonPosSegment = (int) (getWidth() / 2F);
        addWidget(confirmButton = new KeyButton(0, getHeight() - 10, Keys.CONFIRM_KEY, "oxygen_core.gui.button.confirm")
                .setPressListener(this::confirm).setEnabled(false));
        confirmButton.setX(getX() + (int) ((buttonPosSegment - confirmButton.getText().getWidth()) / 2F));
        KeyButton cancelButton;
        addWidget(cancelButton = new KeyButton(0, getHeight() - 10, Keys.CANCEL_KEY, "oxygen_core.gui.button.cancel")
                .setPressListener(this::close));
        cancelButton.setX(getX() + buttonPosSegment + (int) ((buttonPosSegment - cancelButton.getText().getWidth()) / 2F));
    }

    private void onInput() {
        permission = null;

        boolean validInput;
        int privilegeId = (int) permissionIdField.getTypedNumberAsLong();
        PrivilegeRegistry.Entry entry = PrivilegeRegistry.getEntry(privilegeId);
        if (entry == null) {
            validInput = false;
        } else {
            String valueStr = valueField.getTypedText();
            validInput = !valueStr.isEmpty()
                    && ValueType.fromString(entry.getValueType(), valueStr) != null;
        }

        if (validInput) {
            TypedValue value = ValueType.fromString(entry.getValueType(), valueField.getTypedText());
            if (value == null) {
                validInput = false;
            } else {
                permission = new Permission(value, privilegeId);
            }
        }

        confirmButton.setEnabled(validInput);
    }

    @Override
    public boolean close() {
        if (!permissionIdField.isFocused() && !valueField.isFocused()) {
            super.close();
            return true;
        }
        return false;
    }

    private void confirm() {
        if (!permissionIdField.isFocused() && !valueField.isFocused() && permission != null) {
            PermissionsManagerClient.instance().addPermissionRequest(role.getId(), permission);
            close();
        }
    }
}
