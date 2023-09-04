package austeretony.oxygen_permissions.client;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.util.MinecraftClient;
import austeretony.oxygen_core.common.player.shared.PlayerSharedData;
import austeretony.oxygen_core.common.privileges.PrivilegeProvider;
import austeretony.oxygen_core.common.sync.SyncMeta;
import austeretony.oxygen_core.common.util.ByteBufUtils;
import austeretony.oxygen_core.common.util.value.*;
import austeretony.oxygen_permissions.client.gui.permissions.info.PermissionsInfoScreen;
import austeretony.oxygen_permissions.client.gui.permissions.management.PermissionsManagementScreen;
import austeretony.oxygen_permissions.common.main.PermissionsMain;
import austeretony.oxygen_permissions.common.network.operation.PermissionsOperation;
import austeretony.oxygen_permissions.common.permissions.PlayerRoles;
import austeretony.oxygen_permissions.common.permissions.Permission;
import austeretony.oxygen_permissions.common.permissions.Role;
import austeretony.oxygen_permissions.common.permissions.sync.SyncReason;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.gui.GuiScreen;

import javax.annotation.Nullable;
import java.util.*;

public final class PermissionsManagerClient implements PrivilegeProvider {

    private static PermissionsManagerClient instance;

    private final Map<Integer, Role> rolesMap = new HashMap<>();
    private final Map<UUID, PlayerRoles> playerRolesMap = new HashMap<>();
    private final Map<UUID, PlayerSharedData> sharedDataMap = new HashMap<>();

    private PermissionsManagerClient() {}

    public static PermissionsManagerClient instance() {
        if (instance == null)
            instance = new PermissionsManagerClient();
        return instance;
    }

    public void requestManagementDataSync() {
        OxygenClient.sendToServer(
                PermissionsMain.PERMISSIONS_OPERATIONS_HANDLER_ID,
                PermissionsOperation.REQUEST_MANAGEMENT_DATA.ordinal());
    }

    public void rolesDataSynchronized(byte[] rolesRaw) {
        rolesMap.clear();
        ByteBuf buffer = null;
        try {
            buffer = Unpooled.wrappedBuffer(rolesRaw);
            if (buffer.readableBytes() != 0) {
                int amount = buffer.readShort();
                for (int i = 0; i < amount; i++) {
                    Role role = Role.read(buffer);
                    rolesMap.put(role.getId(), role);
                }
            }
        } finally {
            if (buffer != null) {
                buffer.release();
            }
        }
    }

    public void playerRolesSynced(PlayerRoles roles) {
        playerRolesMap.put(roles.getPlayerUUID(), roles);
    }

    public void managementDataSynced(int reason, @Nullable SyncMeta meta, byte[] rolesRaw, Map<UUID, PlayerRoles> rolesMap,
                                     Map<UUID, PlayerSharedData> sharedMap) {
        rolesDataSynchronized(rolesRaw);
        playerRolesMap.clear();
        playerRolesMap.putAll(rolesMap);
        sharedDataMap.clear();
        sharedDataMap.putAll(sharedMap);

        PermissionsManagementScreen.dataSynchronized(SyncReason.values()[reason], meta);
    }

    public Map<Integer, Role> getRolesMap() {
        return rolesMap;
    }

    @Nullable
    public Role getRole(int roleId) {
        return rolesMap.get(roleId);
    }

    @Nullable
    public PlayerRoles getPlayerRoles(UUID playerUUID) {
        return playerRolesMap.get(playerUUID);
    }

    public Collection<PlayerSharedData> getPlayersSharedData() {
        return sharedDataMap.values();
    }

    @Nullable
    public PlayerSharedData getPlayerSharedData(UUID playerUUID) {
        return sharedDataMap.get(playerUUID);
    }

    public void changeFormattingRoleRequest(int roleId) {
        OxygenClient.sendToServer(
                PermissionsMain.PERMISSIONS_OPERATIONS_HANDLER_ID,
                PermissionsOperation.CHANGE_FORMATTING_ROLE.ordinal(),
                buffer -> buffer.writeShort(roleId));
    }

    public void formattingRoleChanged(int roleId) {
        PlayerRoles roles = playerRolesMap.get(OxygenClient.getClientPlayerUUID());
        if (roles != null) {
            roles.setChatFormattingRole(roleId);
        }

        GuiScreen screen = MinecraftClient.getCurrentScreen();
        if (screen instanceof PermissionsInfoScreen) {
            ((PermissionsInfoScreen) screen).formattingRoleChanged(roleId);
        }
    }

    public void createRoleRequest(Role role) {
        OxygenClient.sendToServer(
                PermissionsMain.PERMISSIONS_OPERATIONS_HANDLER_ID,
                PermissionsOperation.CREATE_ROLE.ordinal(),
                role::write);
    }

    public void editRoleRequest(Role edited) {
        OxygenClient.sendToServer(
                PermissionsMain.PERMISSIONS_OPERATIONS_HANDLER_ID,
                PermissionsOperation.EDIT_ROLE.ordinal(),
                edited::write);
    }

    public void removeRoleRequest(int roleId) {
        OxygenClient.sendToServer(
                PermissionsMain.PERMISSIONS_OPERATIONS_HANDLER_ID,
                PermissionsOperation.REMOVE_ROLE.ordinal(),
                buffer -> buffer.writeShort(roleId));
    }

    public void addPermissionRequest(int roleId, Permission permission) {
        OxygenClient.sendToServer(
                PermissionsMain.PERMISSIONS_OPERATIONS_HANDLER_ID,
                PermissionsOperation.ADD_ROLE_PERMISSION.ordinal(),
                buffer -> {
                    buffer.writeShort(roleId);
                    permission.write(buffer);
                });
    }

    public void removePermissionsRequest(int roleId, List<Integer> permissionsIds) {
        OxygenClient.sendToServer(
                PermissionsMain.PERMISSIONS_OPERATIONS_HANDLER_ID,
                PermissionsOperation.REMOVE_ROLE_PERMISSIONS.ordinal(),
                buffer -> {
                    buffer.writeShort(roleId);
                    buffer.writeByte(permissionsIds.size());
                    for (int id : permissionsIds) {
                        buffer.writeShort(id);
                    }
                });
    }

    public void addRolesToPlayerRequest(UUID playerUUID, List<Integer> rolesIds) {
        OxygenClient.sendToServer(
                PermissionsMain.PERMISSIONS_OPERATIONS_HANDLER_ID,
                PermissionsOperation.ADD_ROLES_TO_PLAYER.ordinal(),
                buffer -> {
                    ByteBufUtils.writeUUID(playerUUID, buffer);
                    buffer.writeByte(rolesIds.size());
                    for (int id : rolesIds) {
                        buffer.writeShort(id);
                    }
                });
    }

    public void removeRolesFromPlayer(UUID playerUUID, List<Integer> rolesIds) {
        OxygenClient.sendToServer(
                PermissionsMain.PERMISSIONS_OPERATIONS_HANDLER_ID,
                PermissionsOperation.REMOVE_ROLES_FROM_PLAYER.ordinal(),
                buffer -> {
                    ByteBufUtils.writeUUID(playerUUID, buffer);
                    buffer.writeByte(rolesIds.size());
                    for (int id : rolesIds) {
                        buffer.writeShort(id);
                    }
                });
    }

    @Nullable
    public Permission getHighestPriorityPermission(UUID playerUUID, int privilegeId) {
        PlayerRoles roles = playerRolesMap.get(playerUUID);
        if (roles == null) return null;
        for (int roleId : roles.getRolesSet()) {
            Role role = getRole(roleId);
            if (role == null) continue;
            Permission permission = role.getPermission(privilegeId);
            if (permission != null) return permission;
        }
        return null;
    }

    @Override
    public boolean getBoolean(UUID playerUUID, int privilegeId, boolean defaultValue) {
        Permission<BooleanValue> permission = getHighestPriorityPermission(playerUUID, privilegeId);
        if (permission != null) {
            return permission.get().getValue();
        }
        return defaultValue;
    }

    @Override
    public int getInt(UUID playerUUID, int privilegeId, int defaultValue) {
        Permission<IntegerValue> permission = getHighestPriorityPermission(playerUUID, privilegeId);
        if (permission != null) {
            return permission.get().getValue();
        }
        return defaultValue;
    }

    @Override
    public long getLong(UUID playerUUID, int privilegeId, long defaultValue) {
        Permission<LongValue> permission = getHighestPriorityPermission(playerUUID, privilegeId);
        if (permission != null) {
            return permission.get().getValue();
        }
        return defaultValue;
    }

    @Override
    public float getFloat(UUID playerUUID, int privilegeId, float defaultValue) {
        Permission<FloatValue> permission = getHighestPriorityPermission(playerUUID, privilegeId);
        if (permission != null) {
            return permission.get().getValue();
        }
        return defaultValue;
    }

    @Override
    public double getDouble(UUID playerUUID, int privilegeId, double defaultValue) {
        Permission<DoubleValue> permission = getHighestPriorityPermission(playerUUID, privilegeId);
        if (permission != null) {
            return permission.get().getValue();
        }
        return defaultValue;
    }

    @Override
    public String getString(UUID playerUUID, int privilegeId, String defaultValue) {
        Permission<StringValue> permission = getHighestPriorityPermission(playerUUID, privilegeId);
        if (permission != null) {
            return permission.get().getValue();
        }
        return defaultValue;
    }
}
