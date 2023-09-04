package austeretony.oxygen_permissions.server.event;

import austeretony.oxygen_core.common.util.MinecraftCommon;
import austeretony.oxygen_core.server.event.OxygenPlayerEvent;
import austeretony.oxygen_core.server.event.OxygenServerEvent;
import austeretony.oxygen_permissions.common.config.PermissionsConfig;
import austeretony.oxygen_permissions.common.main.PermissionsMain;
import austeretony.oxygen_permissions.common.permissions.PlayerRoles;
import austeretony.oxygen_permissions.common.permissions.Role;
import austeretony.oxygen_permissions.server.PermissionsManagerServer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PermissionsEventsServer {

    @SubscribeEvent
    public void onServerStarting(OxygenServerEvent.Starting event) {
        PermissionsManagerServer.instance().serverStarting();
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(OxygenPlayerEvent.LoggedIn event) {
        PermissionsManagerServer.instance().playerLoggedIn(event.getPlayer());
    }

    @SubscribeEvent
    public void onChatMessage(ServerChatEvent event) {
        if (!PermissionsConfig.ENABLE_FORMATTED_CHAT.asBoolean()) return;
        PermissionsManagerServer manager = PermissionsManagerServer.instance();
        PlayerRoles roles = manager.getPlayerRoles(MinecraftCommon.getEntityUUID(event.getPlayer()));
        if (roles == null || roles.getChatFormattingRole() == PermissionsMain.DEFAULT_ROLE_ID) return;
        Role role = manager.getRole(roles.getChatFormattingRole());
        if (role == null) return;

        event.setComponent(PermissionsMain.getFormattedChatMessage(MinecraftCommon.getEntityName(event.getPlayer()),
                event.getMessage(), role));
    }
}
