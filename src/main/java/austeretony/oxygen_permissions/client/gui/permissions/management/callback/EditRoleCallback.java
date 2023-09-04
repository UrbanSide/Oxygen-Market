package austeretony.oxygen_permissions.client.gui.permissions.management.callback;

import austeretony.oxygen_core.client.gui.base.Keys;
import austeretony.oxygen_core.client.gui.base.Texts;
import austeretony.oxygen_core.client.gui.base.background.Background;
import austeretony.oxygen_core.client.gui.base.core.Callback;
import austeretony.oxygen_core.client.gui.base.special.KeyButton;
import austeretony.oxygen_core.client.gui.base.special.TextFormattingColorPicker;
import austeretony.oxygen_core.client.gui.base.text.TextField;
import austeretony.oxygen_core.client.gui.base.text.TextLabel;
import austeretony.oxygen_core.client.util.MinecraftClient;
import austeretony.oxygen_permissions.client.PermissionsManagerClient;
import austeretony.oxygen_permissions.common.main.PermissionsMain;
import austeretony.oxygen_permissions.common.permissions.Role;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;

public class EditRoleCallback extends Callback {

    private final Role origin;

    private TextField roleNameField, prefixField;
    private TextFormattingColorPicker roleNameColorPicker, prefixColorPicker, usernameColorPicker, chatColorPicker;
    private TextLabel chatFormattingLabel;
    private KeyButton confirmButton;

    @Nullable
    private Role role;

    public EditRoleCallback(Role role) {
        super(132, 148);
        this.origin = role;
    }

    @Override
    public void init() {
        addWidget(new Background.UnderlinedTitleBottom(this));
        addWidget(new TextLabel(4, 12, Texts.title("oxygen_permissions.gui.permissions_management.callback.role_editing")));

        addWidget(new TextLabel(6, 24, Texts.common("oxygen_permissions.gui.permissions_management.label.role_name")));
        addWidget(roleNameField = new TextField(6, 25, 80, Role.ROLE_NAME_MAX_LENGTH)
                .setKeyPressListener((keyCode, keyChar) -> onInput()));
        roleNameField.setText(origin.getName());
        addWidget(roleNameColorPicker = new TextFormattingColorPicker(6, 36, TextFormatting.WHITE)
                .setColorPickListener((from, to) -> onInput()));
        roleNameColorPicker.setFormatting(origin.getNameColor());

        addWidget(new TextLabel(6, 54, Texts.common("oxygen_permissions.gui.permissions_management.label.role_prefix")));
        addWidget(prefixField = new TextField(6, 55, 80, Role.PREFIX_MAX_LENGTH)
                .setKeyPressListener((keyCode, keyChar) -> onInput()));
        prefixField.setText(origin.getPrefix());
        addWidget(prefixColorPicker = new TextFormattingColorPicker(6, 66, TextFormatting.WHITE)
                .setColorPickListener((from, to) -> onInput()));
        prefixColorPicker.setFormatting(origin.getPrefixColor());

        addWidget(new TextLabel(6, 84, Texts.common("oxygen_permissions.gui.permissions_management.label.role_username_color")));
        addWidget(usernameColorPicker = new TextFormattingColorPicker(6, 85, TextFormatting.WHITE)
                .setColorPickListener((from, to) -> onInput()));
        usernameColorPicker.setFormatting(origin.getUsernameColor());

        addWidget(new TextLabel(6, 103, Texts.common("oxygen_permissions.gui.permissions_management.label.role_chat_color")));
        addWidget(chatColorPicker = new TextFormattingColorPicker(6, 104, TextFormatting.WHITE)
                .setColorPickListener((from, to) -> onInput()));
        chatColorPicker.setFormatting(origin.getChatColor());

        addWidget(new TextLabel(6, 124, Texts.common("oxygen_permissions.gui.permissions_info.chat_formatting")));
        addWidget(chatFormattingLabel = new TextLabel(6, 133, Texts.common("")));

        int buttonPosSegment = (int) (getWidth() / 2F);
        addWidget(confirmButton = new KeyButton(0, getHeight() - 10, Keys.CONFIRM_KEY, "oxygen_core.gui.button.confirm")
                .setPressListener(this::confirm).setEnabled(false));
        confirmButton.setX(getX() + (int) ((buttonPosSegment - confirmButton.getText().getWidth()) / 2F));
        KeyButton cancelButton;
        addWidget(cancelButton = new KeyButton(0, getHeight() - 10, Keys.CANCEL_KEY, "oxygen_core.gui.button.close")
                .setPressListener(this::close));
        cancelButton.setX(getX() + buttonPosSegment + (int) ((buttonPosSegment - cancelButton.getText().getWidth()) / 2F));

        updateChatFormattingExample();
    }

    private void onInput() {
        role = null;
        boolean validInput = true;
        if (roleNameField.getTypedText().isEmpty()) {
            validInput = false;
        }

        if (validInput) {
            role = new Role(origin.getId(), roleNameField.getTypedText(), roleNameColorPicker.getFormatting());

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
            PermissionsManagerClient.instance().editRoleRequest(role);
            close();
        }
    }
}
