package austeretony.oxygen_permissions.server;

import austeretony.oxygen_core.common.api.OxygenCommon;
import austeretony.oxygen_core.common.chat.StatusMessageType;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.player.shared.PlayerSharedData;
import austeretony.oxygen_core.common.privileges.PrivilegeProvider;
import austeretony.oxygen_core.common.privileges.PrivilegeRegistry;
import austeretony.oxygen_core.common.sync.SyncMeta;
import austeretony.oxygen_core.common.util.JsonUtils;
import austeretony.oxygen_core.common.util.MinecraftCommon;
import austeretony.oxygen_core.common.util.value.*;
import austeretony.oxygen_core.server.OxygenManagerServer;
import austeretony.oxygen_core.server.api.OxygenServer;
import austeretony.oxygen_permissions.common.config.PermissionsConfig;
import austeretony.oxygen_permissions.common.main.PermissionsMain;
import austeretony.oxygen_permissions.common.network.operation.PermissionsOperation;
import austeretony.oxygen_permissions.common.permissions.PlayerRoles;
import austeretony.oxygen_permissions.common.permissions.Permission;
import austeretony.oxygen_permissions.common.permissions.Role;
import austeretony.oxygen_permissions.common.permissions.sync.SyncReason;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class PermissionsManagerServer implements PrivilegeProvider {

    private static PermissionsManagerServer instance;

    private final Map<Integer, Role> rolesMap = new HashMap<>();
    private final Map<UUID, PlayerRoles> playersRolesMap = new HashMap<>();

    private ByteBuf rolesRaw = Unpooled.buffer();
    private volatile boolean changed;

    private PermissionsManagerServer() {
        OxygenServer.registerPersistentData(this::saveData);
    }

    private void saveData() {
        if (changed) {
            changed = false;
            saveRoles();
            savePlayersList();
        }
    }

    public static PermissionsManagerServer instance() {
        if (instance == null)
            instance = new PermissionsManagerServer();
        return instance;
    }

    public void serverStarting() {
        final Runnable task = () -> {
            loadRoles();
            loadPlayersList();
            validatePlayersRoles();
            compressRolesData();
        };
        OxygenServer.addTask(task);
    }

    private void loadRoles() {
        String folder = OxygenCommon.getConfigFolder() + "/data/server/permissions/roles.json";
        Path path = Paths.get(folder);
        if (Files.exists(path)) {
            try {
                JsonArray rolesArray = JsonUtils.getExternalJsonData(folder).getAsJsonArray();
                for (JsonElement roleElement : rolesArray)
                    addRole(Role.fromJson(roleElement.getAsJsonObject()));
                OxygenMain.logInfo(1, "[Permissions] Roles loaded.");
            } catch (IOException exception) {
                OxygenMain.logError(1, "[Permissions] Roles loading failed.");
                exception.printStackTrace();
            }
        }
    }

    private void loadPlayersList() {
        String folder = OxygenCommon.getConfigFolder() + "/data/server/permissions/players.json";
        Path path = Paths.get(folder);
        if (Files.exists(path)) {
            try {
                JsonArray playersArray = JsonUtils.getExternalJsonData(folder).getAsJsonArray();
                for (JsonElement playerElement : playersArray) {
                    PlayerRoles roles = PlayerRoles.fromJson(playerElement.getAsJsonObject());
                    playersRolesMap.put(roles.getPlayerUUID(), roles);
                }
                OxygenMain.logInfo(1, "[Permissions] Loaded players list.");
            } catch (IOException exception) {
                OxygenMain.logError(1, "[Permissions] Players list loading failed.");
                exception.printStackTrace();
            }
        }
    }

    public void validatePlayersRoles() {
        for (PlayerRoles roles : playersRolesMap.values()) {
            roles.getRolesSet().retainAll(getRolesMap().keySet());

            int formattingRoleId = roles.getChatFormattingRole();
            if (!roles.haveRole(formattingRoleId)) {
                roles.setChatFormattingRole(PermissionsMain.DEFAULT_ROLE_ID);
            }
            markChanged();
        }
    }

    public void compressRolesData() {
        rolesRaw.clear();
        rolesRaw.writeShort(rolesMap.size());
        for (Role role : rolesMap.values()) {
            role.write(rolesRaw);
        }
    }

    private void saveRoles() {
        String folder = OxygenCommon.getConfigFolder() + "/data/server/permissions/roles.json";
        Path path = Paths.get(folder);
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
            }

            JsonArray rolesArray = new JsonArray();
            getRolesMap().values()
                    .stream()
                    .sorted(Comparator.comparingInt(Role::getId))
                    .forEach(e -> rolesArray.add(e.toJson()));

            JsonUtils.createExternalJsonFile(folder, rolesArray);
            OxygenMain.logInfo(1, "[Permissions] Roles saved.");
        } catch (IOException exception) {
            OxygenMain.logError(1, "[Permissions] Roles saving failed.");
            exception.printStackTrace();
        }
    }

    private void savePlayersList() {
        String folder = OxygenCommon.getConfigFolder() + "/data/server/permissions/players.json";
        Path path = Paths.get(folder);
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
            }

            JsonArray playersArray = new JsonArray();
            for (PlayerRoles roles : playersRolesMap.values()) {
                playersArray.add(roles.toJson());
            }

            JsonUtils.createExternalJsonFile(folder, playersArray);
            OxygenMain.logInfo(1, "[Permissions] Players list saved.");
        } catch (IOException exception) {
            OxygenMain.logError(1, "[Permissions] Players list saving failed.");
            exception.printStackTrace();
        }
    }

    public Map<Integer, Role> getRolesMap() {
        return rolesMap;
    }

    @Nullable
    public Role getRole(int roleId) {
        return rolesMap.get(roleId);
    }

    public void addRole(Role role) {
        rolesMap.put(role.getId(), role);
    }

    @Nullable
    public Role removeRole(int roleId) {
        return rolesMap.remove(roleId);
    }

    public Map<UUID, PlayerRoles> getPlayersRolesMap() {
        return playersRolesMap;
    }

    @Nullable
    public PlayerRoles getPlayerRoles(UUID playerUUID) {
        return playersRolesMap.get(playerUUID);
    }

    public void playerLoggedIn(EntityPlayerMP playerMP) {
        final Runnable task = () -> {
            syncRolesData(playerMP);
            syncPlayerRoles(playerMP);
        };
        OxygenServer.addTask(task);
    }

    public void syncRolesData(EntityPlayerMP playerMP) {
        byte[] bytes = new byte[rolesRaw.writerIndex()];
        rolesRaw.getBytes(0, bytes);
        OxygenServer.sendToClient(
                playerMP,
                PermissionsMain.PERMISSIONS_OPERATIONS_HANDLER_ID,
                PermissionsOperation.SYNC_ROLES_DATA.ordinal(),
                buffer -> {
                    buffer.writeInt(bytes.length);
                    buffer.writeBytes(bytes);
                });
    }

    public void syncPlayerRoles(EntityPlayerMP playerMP) {
        PlayerRoles roles = playersRolesMap.get(MinecraftCommon.getEntityUUID(playerMP));
        if (roles != null) {
            OxygenServer.sendToClient(
                    playerMP,
                    PermissionsMain.PERMISSIONS_OPERATIONS_HANDLER_ID,
                    PermissionsOperation.SYNC_PLAYER_ROLES.ordinal(),
                    buffer -> roles.copy().write(buffer));
        }
    }

    public void processManagementDataSyncRequest(EntityPlayerMP playerMP) {
        syncManagementData(playerMP, SyncReason.MENU_OPEN);
    }

    public void syncManagementData(EntityPlayerMP playerMP, SyncReason reason) {
        syncManagementData(playerMP, reason, null);
    }

    public void syncManagementData(EntityPlayerMP playerMP, SyncReason reason, @Nullable SyncMeta meta) {
        if (!OxygenServer.isPlayerOperator(playerMP)) return;

        byte[] bytes = new byte[rolesRaw.writerIndex()];
        rolesRaw.getBytes(0, bytes);

        Map<UUID, PlayerRoles> rolesMap = new HashMap<>(playersRolesMap.size());
        Map<UUID, PlayerSharedData> sharedDataMap = new HashMap<>();
        List<PlayerSharedData> onlinePlayersData = OxygenManagerServer.instance().getSharedDataManager()
                .getOnlinePlayersSharedData();
        for (PlayerSharedData sharedData : onlinePlayersData) {
            sharedDataMap.put(sharedData.getPlayerUUID(), sharedData.copy());

            PlayerRoles roles = getPlayerRoles(sharedData.getPlayerUUID());
            if (roles != null) {
                rolesMap.put(sharedData.getPlayerUUID(), roles.copy());
            }
        }

        for (PlayerRoles roles : playersRolesMap.values()) {
            if (rolesMap.containsKey(roles.getPlayerUUID())) continue;
            rolesMap.put(roles.getPlayerUUID(), roles.copy());

            PlayerSharedData sharedData = OxygenServer.getPlayerSharedData(roles.getPlayerUUID());
            if (sharedData != null) {
                sharedDataMap.put(roles.getPlayerUUID(), sharedData.copy());
            }
        }

        OxygenServer.sendToClient(
                playerMP,
                PermissionsMain.PERMISSIONS_OPERATIONS_HANDLER_ID,
                PermissionsOperation.SYNC_MANAGEMENT_DATA.ordinal(),
                buffer -> {
                    buffer.writeByte(reason.ordinal());
                    buffer.writeBoolean(meta != null);
                    if (meta != null) {
                        meta.write(buffer);
                    }

                    buffer.writeInt(bytes.length);
                    buffer.writeBytes(bytes);

                    buffer.writeInt(rolesMap.size());
                    for (PlayerRoles roles : rolesMap.values()) {
                        roles.write(buffer);
                    }

                    buffer.writeInt(sharedDataMap.size());
                    for (PlayerSharedData data : sharedDataMap.values()) {
                        data.write(buffer);
                    }
                });
    }

    public void createRole(EntityPlayerMP playerMP, Role role) {
        if (!OxygenServer.isPlayerOperator(playerMP)) return;

        addRole(role);
        compressRolesData();
        markChanged();

        OxygenMain.logInfo(1, "[Permissions] {}/{} created role <{}/{}>.",
                MinecraftCommon.getEntityName(playerMP), MinecraftCommon.getEntityUUID(playerMP), role.getId(),
                role.getName());

        syncManagementData(playerMP, SyncReason.ROLE_CREATED);
    }

    public void editRole(EntityPlayerMP playerMP, Role editedRole) {
        if (!OxygenServer.isPlayerOperator(playerMP)) return;

        Role role = getRole(editedRole.getId());
        if (role == null) return;

        boolean nameUpdated = !role.getName().equals(editedRole.getName())
                || role.getNameColor() != editedRole.getNameColor();
        role.setName(editedRole.getName());
        role.setNameColor(editedRole.getNameColor());
        role.setPrefix(editedRole.getPrefix());
        role.setPrefixColor(editedRole.getPrefixColor());
        role.setUsernameColor(editedRole.getUsernameColor());
        role.setChatColor(editedRole.getChatColor());

        compressRolesData();
        if (nameUpdated) {
            updateAffectedPlayersRolesSharedData(role);
        }
        syncChangesWithPlayersOnline(roles -> roles.getRolesSet().contains(role.getId()));
        markChanged();

        OxygenMain.logInfo(1, "[Permissions] {}/{} edited role <{}/{}>.",
                MinecraftCommon.getEntityName(playerMP), MinecraftCommon.getEntityUUID(playerMP), editedRole.getId(),
                editedRole.getName());

        syncManagementData(playerMP, SyncReason.ROLE_EDITED,
                SyncMeta.builder()
                        .withShort("role_id", editedRole.getId())
                        .build());
    }

    public void updateAffectedPlayersRolesSharedData(Role role) {
        List<PlayerRoles> affectedPlayers = getPlayersRolesMap().values()
                .stream()
                .filter(e -> e.haveRole(role.getId()))
                .collect(Collectors.toList());
        for (PlayerRoles playerRoles : affectedPlayers) {
            PlayerSharedData sharedData = OxygenServer.getPlayerSharedData(playerRoles.getPlayerUUID());
            if (sharedData == null) continue;
            updatePlayerRolesSharedData(sharedData);
        }
    }

    public void removeRole(EntityPlayerMP playerMP, int roleId) {
        if (!OxygenServer.isPlayerOperator(playerMP)) return;

        Role removed = removeRole(roleId);
        if (removed != null) {
            compressRolesData();

            Map<UUID, PlayerRoles> map = new HashMap<>(getPlayersRolesMap().size());
            for (PlayerRoles roles : getPlayersRolesMap().values()) {
                map.put(roles.getPlayerUUID(), roles.copy());
            }
            validatePlayersRoles();
            syncChangesWithPlayersOnline(roles -> {
                PlayerRoles oldRoles = map.get(roles.getPlayerUUID());
                return oldRoles != null && oldRoles.getRolesSet().contains(removed.getId());
            });

            markChanged();

            OxygenMain.logInfo(1, "[Permissions] {}/{} removed role <{}/{}>.",
                    MinecraftCommon.getEntityName(playerMP), MinecraftCommon.getEntityUUID(playerMP), removed.getId(),
                    removed.getName());

            syncManagementData(playerMP, SyncReason.ROLE_REMOVED);
        }
    }

    public void addRolePermission(EntityPlayerMP playerMP, int roleId, Permission permission) {
        if (!OxygenServer.isPlayerOperator(playerMP)) return;

        Role role = getRole(roleId);
        if (role == null) return;
        PrivilegeRegistry.Entry entry = PrivilegeRegistry.getEntry(permission.getId());
        if (entry == null) return;

        if (role.addPermission(permission) == null) {
            compressRolesData();
            syncChangesWithPlayersOnline(roles -> roles.getRolesSet().contains(role.getId()));
            markChanged();

            OxygenMain.logInfo(1, "[Permissions] {}/{} added permission <{}/{} - {}> to role {}/{}.",
                    MinecraftCommon.getEntityName(playerMP), MinecraftCommon.getEntityUUID(playerMP), permission.getId(),
                    entry.getDisplayName(), permission.get().toString(), role.getId(), role.getName());

            syncManagementData(playerMP, SyncReason.ROLE_PERMISSION_ADDED,
                    SyncMeta.builder()
                            .withShort("role_id", roleId)
                            .build());
        }
    }

    public void removeRolePermissions(EntityPlayerMP playerMP, int roleId, List<Integer> permissionsIds) {
        if (!OxygenServer.isPlayerOperator(playerMP)) return;

        Role role = getRole(roleId);
        if (role == null) return;

        boolean removed = false;
        for (int id : permissionsIds) {
            PrivilegeRegistry.Entry entry = PrivilegeRegistry.getEntry(id);
            if (entry == null) continue;

            if (role.removePrivilege(id) != null) {
                OxygenMain.logInfo(1, "[Permissions] {}/{} removed permission <{}/{}> from role {}/{}.",
                        MinecraftCommon.getEntityName(playerMP), MinecraftCommon.getEntityUUID(playerMP), entry.getId(),
                        entry.getDisplayName(), role.getId(), role.getName());
                removed = true;
            }
        }

        if (removed) {
            compressRolesData();
            syncChangesWithPlayersOnline(roles -> roles.getRolesSet().contains(role.getId()));
            markChanged();

            syncManagementData(playerMP, SyncReason.ROLE_PERMISSIONS_REMOVED,
                    SyncMeta.builder()
                            .withShort("role_id", roleId)
                            .build());
        }
    }

    public static void syncChangesWithPlayersOnline(Predicate<PlayerRoles> condition) {
        PermissionsManagerServer manager = PermissionsManagerServer.instance();
        for (PlayerRoles roles : manager.getPlayersRolesMap().values()) {
            if (condition.test(roles)) {
                EntityPlayerMP playerMP = MinecraftCommon.getPlayerByUUID(roles.getPlayerUUID());
                if (playerMP != null) {
                    manager.syncRolesData(playerMP);
                }
            }
        }
    }

    public void addRolesToPlayer(EntityPlayerMP operatorMP, UUID playerUUID, List<Integer> rolesIds) {
        if (!OxygenServer.isPlayerOperator(operatorMP)) return;

        EntityPlayerMP playerMP = MinecraftCommon.getPlayerByUUID(playerUUID);
        boolean added = false;
        for (int roleId : rolesIds) {
            if (addRoleToPlayer(playerUUID, roleId)) {
                added = true;

                Role role = getRole(roleId);
                OxygenMain.logInfo(1, "[Permissions] {} added role <{}/{}> to player: {}/{}.",
                        MinecraftCommon.getEntityName(operatorMP), role.getId(), role.getName(),
                        playerMP != null ? MinecraftCommon.getEntityName(playerMP) : "Offline", playerUUID);
                OxygenServer.sendStatusMessage(operatorMP, PermissionsMain.MODULE_INDEX, StatusMessageType.SPECIAL,
                        String.format("Added role <%s/%s> to: %s",
                                role.getId(), role.getName(), playerMP != null ? MinecraftCommon.getEntityName(playerMP) : "Offline"));
            } else {
                OxygenServer.sendStatusMessage(operatorMP, PermissionsMain.MODULE_INDEX, StatusMessageType.ERROR,
                        String.format("Failed to add role <%s> to: %s",
                                roleId, playerMP != null ? MinecraftCommon.getEntityName(playerMP) : "Offline"));
            }
        }

        if (added) {
            updatePlayerRolesSharedData(playerUUID);
            if (playerMP != null) {
                syncPlayerRoles(playerMP);
            }
            markChanged();

            syncManagementData(operatorMP, SyncReason.PLAYER_ROLES_ADDED);
        }
    }

    public boolean addRoleToPlayer(UUID playerUUID, int roleId) {
        Role role = getRole(roleId);
        if (role == null) return false;

        PlayerRoles roles = playersRolesMap.get(playerUUID);
        if (roles == null) {
            roles = new PlayerRoles(playerUUID);
            playersRolesMap.put(playerUUID, roles);
        }

        if (roles.getRolesAmount() < PermissionsMain.MAX_ROLES_PER_PLAYER) {
            roles.addRole(roleId);

            if (!PermissionsConfig.ENABLE_CHAT_FORMATTING_ROLE_SELECTION.asBoolean()) {
                roles.setChatFormattingRole(roles.getFirstRole());
            }

            return true;
        }
        return false;
    }

    public void updatePlayerRolesSharedData(UUID playerUUID) {
        PlayerSharedData sharedData = OxygenServer.getPlayerSharedData(playerUUID);
        if (sharedData != null) {
            updatePlayerRolesSharedData(sharedData);
        }
    }

    public void updatePlayerRolesSharedData(PlayerSharedData sharedData) {
        StringBuilder rolesBuilder = new StringBuilder();
        PlayerRoles roles = playersRolesMap.get(sharedData.getPlayerUUID());
        if (roles != null && roles.getRolesAmount() > 0) {
            for (int roleId : roles.getRolesSet()) {
                Role role = getRole(roleId);
                if (role == null) continue;

                if (rolesBuilder.length() != 0) {
                    rolesBuilder.append(',');
                }

                rolesBuilder.append(role.getNameColor());
                rolesBuilder.append(role.getName());
            }
        }
        sharedData.setValue(PermissionsMain.SHARED_ROLES, rolesBuilder.toString());
    }

    public void removeRolesFromPlayer(EntityPlayerMP operatorMP, UUID playerUUID, List<Integer> rolesIds) {
        if (!OxygenServer.isPlayerOperator(operatorMP)) return;

        EntityPlayerMP playerMP = MinecraftCommon.getPlayerByUUID(playerUUID);
        boolean removed = false;
        for (int roleId : rolesIds) {
            if (removeRoleFromPlayer(playerUUID, roleId)) {
                removed = true;

                Role role = getRole(roleId);
                OxygenMain.logInfo(1, "[Permissions] {} removed role <{}/{}> from player: {}/{}.",
                        MinecraftCommon.getEntityName(operatorMP), role.getId(), role.getName(),
                        playerMP != null ? MinecraftCommon.getEntityName(playerMP) : "Offline", playerUUID);
                OxygenServer.sendStatusMessage(operatorMP, PermissionsMain.MODULE_INDEX, StatusMessageType.SPECIAL,
                        String.format("Removed role <%s/%s> from: %s",
                                role.getId(), role.getName(), playerMP != null ? MinecraftCommon.getEntityName(playerMP) : "Offline"));
            } else {
                OxygenServer.sendStatusMessage(operatorMP, PermissionsMain.MODULE_INDEX, StatusMessageType.ERROR,
                        String.format("Failed to remove role <%s> from: %s",
                                roleId, playerMP != null ? MinecraftCommon.getEntityName(playerMP) : "Offline"));
            }
        }

        if (removed) {
            updatePlayerRolesSharedData(playerUUID);
            if (playerMP != null) {
                syncPlayerRoles(playerMP);
            }
            markChanged();

            syncManagementData(operatorMP, SyncReason.PLAYER_ROLES_REMOVED);
        }
    }

    public boolean removeRoleFromPlayer(UUID playerUUID, int roleId) {
        PlayerRoles roles = playersRolesMap.get(playerUUID);
        if (roles == null) return false;

        if (roles.removeRole(roleId)) {
            if (roles.getChatFormattingRole() == roleId) {
                roles.setChatFormattingRole(PermissionsMain.DEFAULT_ROLE_ID);
            }
            return true;
        }
        return false;
    }

    public void changeFormattingRole(EntityPlayerMP playerMP, int roleId) {
        if (!PermissionsConfig.ENABLE_CHAT_FORMATTING_ROLE_SELECTION.asBoolean()) return;
        PlayerRoles roles = getPlayerRoles(MinecraftCommon.getEntityUUID(playerMP));
        if (roles != null && (roles.haveRole(roleId) || roleId == PermissionsMain.DEFAULT_ROLE_ID)
                && roles.getChatFormattingRole() != roleId) {
            roles.setChatFormattingRole(roleId);
            markChanged();

            OxygenServer.sendStatusMessage(playerMP, PermissionsMain.MODULE_INDEX, StatusMessageType.COMMON,
                    "oxygen_permissions.chat.message.formatting_role_changed");

            OxygenServer.sendToClient(
                    playerMP,
                    PermissionsMain.PERMISSIONS_OPERATIONS_HANDLER_ID,
                    PermissionsOperation.FORMATTING_ROLE_CHANGED.ordinal(),
                    buffer -> buffer.writeShort(roleId));
        }
    }

    @Nullable
    public Permission getHighestPriorityPermission(UUID playerUUID, int privilegeId) {
        PlayerRoles roles = playersRolesMap.get(playerUUID);
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

    public void markChanged() {
        changed = true;
    }
}
