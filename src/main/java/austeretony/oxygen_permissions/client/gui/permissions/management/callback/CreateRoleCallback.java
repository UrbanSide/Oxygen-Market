package austeretony.oxygen_permissions.client.gui.permissions.management.callback;

import austeretony.oxygen_core.client.gui.base.Keys;
import austeretony.oxygen_core.client.gui.base.Texts;
import austeretony.oxygen_core.client.gui.base.background.Background;
import austeretony.oxygen_core.client.gui.base.core.Callback;
import austeretony.oxygen_core.client.gui.base.special.KeyButton;
import austeretony.oxygen_core.client.gui.base.special.TextFormattingColorPicker;
import austeretony.oxygen_core.client.gui.base.text.NumberField;
import austeretony.oxygen_core.client.gui.base.text.TextField;
import austeretony.oxygen_core.client.gui.base.text.TextLabel;
import austeretony.oxygen_core.client.util.MinecraftClient;
import austeretony.oxygen_permissions.client.PermissionsManagerClient;
import austeretony.oxygen_permissions.common.main.PermissionsMain;
import austeretony.oxygen_permissions.common.permissions.Role;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;

public class CreateRoleCallback extends Callback {

    private NumberField roleIdField;
    private TextField roleNameField, prefixField;
    private TextFormattingColorPicker roleNameColorPicker, prefixColorPicker, usernameColorPicker, chatColorPicker;
    private TextLabel chatFormattingLabel;
    private KeyButton confirmButton;

    @Nullable
    private Role role;

    public CreateRoleCallback() {
        super(132, 168);
    }

    @Override
    public void init() {
        addWidget(new Background.UnderlinedTitleBottom(this));
        addWidget(new TextLabel(4, 12, Texts.title("oxygen_permissions.gui.permissions_management.callback.role_creation")));

        addWidget(new TextLabel(6, 24, Texts.common("oxygen_permissions.gui.permissions_management.label.role_id")));
        addWidget(roleIdField = new NumberField(6, 25, 24, 0, Short.MAX_VALUE)
                .setKeyPressListener((keyCode, keyChar) -> onInput()));

        addWidget(new TextLabel(6, 43, Texts.common("oxygen_permissions.gui.permissions_management.label.role_name")));
        addWidget(roleNameField = new TextField(6, 44, 80, Role.ROLE_NAME_MAX_LENGTH)
                .setKeyPressListener((keyCode, keyChar) -> onInput()));
        addWidget(roleNameColorPicker = new TextFormattingColorPicker(6, 56, TextFormatting.WHITE)
                .setColorPickListener((from, to) -> onInput()));

        addWidget(new TextLabel(6, 74, Texts.common("oxygen_permissions.gui.permissions_management.label.role_prefix")));
        addWidget(prefixField = new TextField(6, 75, 80, Role.PREFIX_MAX_LENGTH)
                .setKeyPressListener((keyCode, keyChar) -> onInput()));
        addWidget(prefixColorPicker = new TextFormattingColorPicker(6, 86, TextFormatting.WHITE)
                .setColorPickListener((from, to) -> onInput()));

        addWidget(new TextLabel(6, 104, Texts.common("oxygen_permissions.gui.permissions_management.label.role_username_color")));
        addWidget(usernameColorPicker = new TextFormattingColorPicker(6, 105, TextFormatting.WHITE)
                .setColorPickListener((from, to) -> onInput()));

        addWidget(new TextLabel(6, 123, Texts.common("oxygen_permissions.gui.permissions_management.label.role_chat_color")));
        addWidget(chatColorPicker = new TextFormattingColorPicker(6, 124, TextFormatting.WHITE)
                .setColorPickListener((from, to) -> onInput()));

        addWidget(new TextLabel(6, 144, Texts.common("oxygen_permissions.gui.permissions_info.chat_formatting")));
        addWidget(chatFormattingLabel = new TextLabel(6, 153, Texts.common("")));

        int buttonPosSegment = (int) (getWidth() / 2F);
        addWidget(confirmButton = new KeyButton(0, getHeight() - 10, Keys.CONFIRM_KEY, "oxygen_core.gui.button.confirm")
                .setPressListener(this::confirm).setEnabled(false));
        confirmButton.setX(getX() + (int) ((buttonPosSegment - confirmButton.getText().getWidth()) / 2F));
        KeyButton cancelButton;
        addWidget(cancelButton = new KeyButton(0, getHeight() - 10, Keys.CANCEL_KEY, "oxygen_core.gui.button.cancel")
                .setPressListener(this::close));
        cancelButton.setX(getX() + buttonPosSegment + (int) ((buttonPosSegment - cancelButton.getText().getWidth()) / 2F));

        updateChatFormattingExample();
    }

    private void onInput() {
        role = null;

        boolean validInput = true;
        int roleId = (int) roleIdField.getTypedNumberAsLong();
        if (PermissionsManagerClient.instance().getRole(roleId) != null) {
            validInput = false;
        }
        if (roleNameField.getTypedText().isEmpty()) {
            validInput = false;
        }

        if (validInput) {
            role = new Role(roleId, roleNameField.getTypedText(), roleNameColorPicker.getFormatting());

            role.setPrefix(prefixField.getTypedText());
            role.setPrefixColor(prefixColorPicker.getFormatting());

            role.setUsernameColor(usernameColorPicker.getFormatting());
            role.setChatColor(chatColorPicker.getFormatting());
        }

        updateChatFormattingExample();
        confirmButton.setEnabled(validInput);
    }

    private void updateChatFormattingExample() {
        String username = MinecraftClient.getEntityName(MinecraftClient.getClientPlayer());
        String sampleMsg = localize("oxygen_permissions.gui.permissions_info.chat_message");
        chatFormattingLabel.getText().setText(PermissionsMain.getFormattedChatMessage(username, sampleMsg,
                prefixField.getTypedText(), prefixColorPicker.getFormatting(), usernameColorPicker.getFormatting(),
                chatColorPicker.getFormatting()).getFormattedText());
    }

    @Override
    public boolean close() {
        if (!roleNameField.isFocused() && !prefixField.isFocused()) {
            super.close();
            return true;
        }
        return false;
    }

    private void confirm() {
        if (!roleNameField.isFocused() && !prefixField.isFocused() && role != null) {
            PermissionsManagerClient.instance().createRoleRequest(role);
            close();
        }
    }
}
