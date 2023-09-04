package austeretony.oxygen_permissions.common.main;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.command.CommandOxygenClient;
import austeretony.oxygen_core.client.gui.menu.OxygenMenuHelper;
import austeretony.oxygen_core.common.api.OxygenCommon;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.player.shared.SharedDataRegistry;
import austeretony.oxygen_core.common.util.MinecraftCommon;
import austeretony.oxygen_core.common.util.value.ValueType;
import austeretony.oxygen_core.server.api.OxygenServer;
import austeretony.oxygen_core.server.command.CommandOxygenOperator;
import austeretony.oxygen_permissions.client.PermissionsManagerClient;
import austeretony.oxygen_permissions.client.command.PermissionsArgumentClient;
import austeretony.oxygen_permissions.client.gui.permissions.info.PermissionsInfoScreen;
import austeretony.oxygen_permissions.client.gui.permissions.management.PermissionsManagementScreen;
import austeretony.oxygen_permissions.client.network.operation.PermissionsNetworkOperationsHandlerClient;
import austeretony.oxygen_permissions.client.settings.PermissionsSettings;
import austeretony.oxygen_permissions.common.config.PermissionsConfig;
import austeretony.oxygen_permissions.common.permissions.Role;
import austeretony.oxygen_permissions.server.PermissionsManagerServer;
import austeretony.oxygen_permissions.server.command.PermissionsArgumentOperator;
import austeretony.oxygen_permissions.server.event.PermissionsEventsServer;
import austeretony.oxygen_permissions.server.network.operation.PermissionsNetworkOperationsHandlerServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod(
        modid = PermissionsMain.MOD_ID,
        name = PermissionsMain.NAME,
        version = PermissionsMain.VERSION,
        dependencies = "required-after:oxygen_core@[0.12.0,);",
        certificateFingerprint = "@FINGERPRINT@",
        updateJSON = PermissionsMain.VERSIONS_FORGE_URL)
public class PermissionsMain {

    public static final String
            MOD_ID = "oxygen_permissions",
            NAME = "Oxygen: Permissions",
            VERSION = "0.12.0",
            VERSION_CUSTOM = VERSION + ":beta:0",
            VERSIONS_FORGE_URL = "https://raw.githubusercontent.com/AustereTony-MCMods/Oxygen-Permissions/info/versions.json";

    //oxygen module index
    public static final int MODULE_INDEX = 2;

    //screen id
    public static final int
            SCREEN_ID_PERMISSIONS_INFO = 20,
            SCREEN_ID_PERMISSIONS_MANAGEMENT = 21;

    //shared data ids
    public static final int SHARED_ROLES = 20;

    public static final int
            DEFAULT_ROLE_ID = -1,
            MAX_ROLES_PER_PLAYER = 5;

    public static String
            PERMISSION_COMMAND_OP_ROLE_INFO = "oxygen.permissions.command-op.role.info",
            PERMISSION_COMMAND_OP_ROLE_MANAGEMENT = "oxygen.permissions.command-op.role.management",

            PERMISSION_COMMAND_OP_PLAYER_INFO = "oxygen.permissions.command-op.player.info",
            PERMISSION_COMMAND_OP_PLAYER_MANAGEMENT = "oxygen.permissions.command-op.player.management";

    //operations handler id
    public static final int PERMISSIONS_OPERATIONS_HANDLER_ID = 20;

    //key binding id
    public static final int KEYBINDING_ID_OPEN_PERMISSIONS_INFO_SCREEN = 20;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        OxygenCommon.registerConfig(new PermissionsConfig());
        if (event.getSide() == Side.CLIENT) {
            CommandOxygenClient.registerArgument(new PermissionsArgumentClient());
            OxygenClient.registerKeyBind(
                    KEYBINDING_ID_OPEN_PERMISSIONS_INFO_SCREEN,
                    "key.oxygen_permissions.open_permissions_info",
                    OxygenMain.KEY_BINDINGS_CATEGORY,
                    PermissionsConfig.PERMISSIONS_INFO_SCREEN_KEY_ID::asInt,
                    PermissionsConfig.ENABLE_PERMISSIONS_INFO_SCREEN_KEY::asBoolean,
                    true,
                    () -> OxygenClient.openScreen(SCREEN_ID_PERMISSIONS_INFO));
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        OxygenServer.registerPrivilegesProvider(PermissionsManagerServer.instance());
        MinecraftCommon.registerEventHandler(new PermissionsEventsServer());
        CommandOxygenOperator.registerArgument(new PermissionsArgumentOperator());
        OxygenServer.registerOperationsHandler(new PermissionsNetworkOperationsHandlerServer());
        SharedDataRegistry.register(SHARED_ROLES, ValueType.STRING);
        if (event.getSide() == Side.CLIENT) {
            OxygenClient.registerPrivilegesProvider(PermissionsManagerClient.instance());
            OxygenClient.registerOperationsHandler(new PermissionsNetworkOperationsHandlerClient());
            PermissionsSettings.register();
            OxygenMenuHelper.addMenuEntry(PermissionsInfoScreen.PRIVILEGES_INFO_SCREEN_ENTRY);
            OxygenClient.registerScreen(SCREEN_ID_PERMISSIONS_INFO, PermissionsInfoScreen::open);
            OxygenClient.registerScreen(SCREEN_ID_PERMISSIONS_MANAGEMENT, PermissionsManagementScreen::open);
        }
    }

    public static @Nonnull TextComponentTranslation getFormattedChatMessage(String playerUsername, String chatMessage,
                                                                            @Nullable String prefix,
                                                                            @Nullable TextFormatting prefixColor,
                                                                            @Nullable TextFormatting usernameColor,
                                                                            @Nullable TextFormatting chatColor) {
        if (prefix != null && prefixColor != null && usernameColor != null && chatColor != null) {
            StringBuilder username = new StringBuilder();
            if (!prefix.isEmpty()) {
                username.append("[");
                username.append(prefixColor);
                username.append(prefix);
                username.append(TextFormatting.RESET);
                username.append("] ");
            }
            username.append(usernameColor);
            username.append(playerUsername);
            username.append(TextFormatting.RESET);

            ITextComponent messageComponent = ForgeHooks.newChatWithLinks(chatMessage);
            messageComponent.getStyle().setColor(chatColor);

            return new TextComponentTranslation("chat.type.text", username.toString(), messageComponent);
        }
        return new TextComponentTranslation("chat.type.text", playerUsername,
                ForgeHooks.newChatWithLinks(chatMessage));
    }

    public static @Nonnull TextComponentTranslation getFormattedChatMessage(String playerUsername, String chatMessage,
                                                                            @Nullable Role role) {
        if (role == null) {
            return getFormattedChatMessage(playerUsername, chatMessage, null, null, null, null);
        }
        return getFormattedChatMessage(playerUsername, chatMessage, role.getPrefix(), role.getPrefixColor(),
                role.getUsernameColor(), role.getChatColor());
    }
}