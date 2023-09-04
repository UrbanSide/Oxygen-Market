package austeretony.oxygen_permissions.server.command;

import austeretony.oxygen_core.common.command.CommandArgument;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.privileges.PrivilegeRegistry;
import austeretony.oxygen_core.common.util.MinecraftCommon;
import austeretony.oxygen_core.common.util.objects.Pair;
import austeretony.oxygen_core.common.util.value.*;
import austeretony.oxygen_core.server.api.OxygenServer;
import austeretony.oxygen_permissions.common.main.PermissionsMain;
import austeretony.oxygen_permissions.common.permissions.PlayerRoles;
import austeretony.oxygen_permissions.common.permissions.Permission;
import austeretony.oxygen_permissions.common.permissions.Role;
import austeretony.oxygen_permissions.server.PermissionsManagerServer;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class PermissionsArgumentOperator implements CommandArgument {

    @Override
    public String getName() {
        return "permissions";
    }

    @Override
    public void process(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 3) {
            if (args[1].equals("role-info")) {
                checkPermission(sender, PermissionsMain.PERMISSION_COMMAND_OP_ROLE_INFO);

                Role parsedRole = parseRole(args[2]);

                sender.sendMessage(PermissionsMain.getFormattedChatMessage(sender.getName(), "chat message",
                        parsedRole));
            } else if (args[1].equals("player-info")) {
                checkPermission(sender, PermissionsMain.PERMISSION_COMMAND_OP_PLAYER_INFO);

                Pair<UUID, EntityPlayerMP> pair = parsePlayer(server, sender, args[2]);
                UUID playerUUID = pair.getKey();
                @Nullable EntityPlayerMP playerMP = pair.getValue();

                PlayerRoles roles = PermissionsManagerServer.instance().getPlayerRoles(playerUUID);
                if (roles == null || roles.getRolesAmount() == 0) {
                    sender.sendMessage(new TextComponentString(String.format("Player %s/%s have no roles",
                            playerMP != null ? MinecraftCommon.getEntityName(playerMP) : "Offline", playerUUID)));
                    return;
                }

                sender.sendMessage(new TextComponentString(String.format("Player %s/%s roles:",
                        playerMP != null ? MinecraftCommon.getEntityName(playerMP) : "Offline", playerUUID)));
                for (int roleId : roles.getRolesSet()) {
                    Role role = PermissionsManagerServer.instance().getRole(roleId);
                    if (role == null) continue;
                    sender.sendMessage(new TextComponentString(role.getNameColor() + role.getName() + " [" + role.getId() + "]"));
                }
            } else if (args[1].equals("role-remove")) {
                checkPermission(sender, PermissionsMain.PERMISSION_COMMAND_OP_ROLE_MANAGEMENT);

                Role parsedRole = parseRole(args[2]);

                Future<Boolean> future = OxygenServer.addTask(() -> {
                    PermissionsManagerServer manager = PermissionsManagerServer.instance();

                    Role removed = manager.removeRole(parsedRole.getId());
                    if (removed != null) {
                        manager.compressRolesData();

                        Map<UUID, PlayerRoles> map = new HashMap<>(manager.getPlayersRolesMap().size());
                        for (PlayerRoles roles : manager.getPlayersRolesMap().values()) {
                            map.put(roles.getPlayerUUID(), roles.copy());
                        }
                        manager.validatePlayersRoles();
                        PermissionsManagerServer.syncChangesWithPlayersOnline(roles -> {
                            PlayerRoles oldRoles = map.get(roles.getPlayerUUID());
                            return oldRoles != null && oldRoles.getRolesSet().contains(removed.getId());
                        });

                        manager.markChanged();

                        OxygenMain.logInfo(1, "[Permissions] {} removed role <{}/{}>.",
                                sender.getName(), removed.getId(), removed.getName());

                        return true;
                    }
                    return false;
                });

                try {
                    Boolean result = future.get();
                    if (result != null && result) {
                        sender.sendMessage(new TextComponentString(String.format("<%s> removed role: %s/%s",
                                sender.getName(), parsedRole.getId(), parsedRole.getName())));
                    } else {
                        sender.sendMessage(new TextComponentString(String.format("<%s> failed to remove role: %s/%s",
                                sender.getName(), parsedRole.getId(), parsedRole.getName())));
                    }
                } catch (InterruptedException | ExecutionException exception) {
                    exception.printStackTrace();
                }
            }
        } else if (args.length == 4) {
            if (args[1].equals("role-create")) {
                checkPermission(sender, PermissionsMain.PERMISSION_COMMAND_OP_ROLE_MANAGEMENT);

                int roleId = CommandBase.parseInt(args[2], 0, Short.MAX_VALUE);
                if (PermissionsManagerServer.instance().getRole(roleId) != null) {
                    throw new CommandException("Role with id <" + args[3] + "> already exist");
                }

                Role newRole = new Role(roleId, args[3], TextFormatting.GRAY);
                Future<Boolean> future = OxygenServer.addTask(() -> {
                    PermissionsManagerServer manager = PermissionsManagerServer.instance();

                    manager.addRole(newRole);
                    manager.compressRolesData();
                    manager.markChanged();

                    OxygenMain.logInfo(1, "[Permissions] {} created role <{}/{}>.",
                            sender.getName(), newRole.getId(), newRole.getName());

                    return true;
                });

                try {
                    Boolean result = future.get();
                    if (result != null && result) {
                        sender.sendMessage(new TextComponentString(String.format("<%s> created role: %s/%s",
                                sender.getName(), newRole.getId(), newRole.getName())));
                    } else {
                        sender.sendMessage(new TextComponentString(String.format("<%s> failed to created role: %s/%s",
                                sender.getName(), newRole.getId(), newRole.getName())));
                    }
                } catch (InterruptedException | ExecutionException exception) {
                    exception.printStackTrace();
                }
            } else if (args[1].equals("role-set-name")) {
                checkPermission(sender, PermissionsMain.PERMISSION_COMMAND_OP_ROLE_MANAGEMENT);

                Role parsedRole = parseRole(args[2]);
                String oldName = parsedRole.getName();

                Future<Boolean> future = OxygenServer.addTask(() -> {
                    PermissionsManagerServer manager = PermissionsManagerServer.instance();

                    Role role = manager.getRole(parsedRole.getId());
                    if (role != null) {
                        boolean editedName = !role.getName().equals(args[3]);
                        role.setName(args[3]);

                        manager.compressRolesData();
                        if (editedName) {
                            manager.updateAffectedPlayersRolesSharedData(role);
                        }
                        PermissionsManagerServer.syncChangesWithPlayersOnline(roles -> roles.getRolesSet().contains(role.getId()));
                        manager.markChanged();

                        OxygenMain.logInfo(1, "[Permissions] {} set role <{}/{}> name to: {}.",
                                sender.getName(), role.getId(), oldName, args[3]);

                        return true;
                    }
                    return false;
                });

                try {
                    Boolean result = future.get();
                    if (result != null && result) {
                        sender.sendMessage(new TextComponentString(String.format("<%s> set role <%s/%s> name to: %s",
                                sender.getName(), parsedRole.getId(), oldName, parsedRole.getName())));
                    } else {
                        sender.sendMessage(new TextComponentString(String.format("<%s> failed to set role <%s/%s> name to: %s",
                                sender.getName(), parsedRole.getId(), oldName, parsedRole.getName())));
                    }
                } catch (InterruptedException | ExecutionException exception) {
                    exception.printStackTrace();
                }
            } else if (args[1].equals("role-set-name-color")) {
                checkPermission(sender, PermissionsMain.PERMISSION_COMMAND_OP_ROLE_MANAGEMENT);

                Role parsedRole = parseRole(args[2]);
                TextFormatting formatting = parseFormatting(args[3]);

                Future<Boolean> future = OxygenServer.addTask(() -> {
                    PermissionsManagerServer manager = PermissionsManagerServer.instance();

                    Role role = manager.getRole(parsedRole.getId());
                    if (role != null) {
                        boolean formattingEdited = role.getNameColor() != formatting;
                        role.setNameColor(formatting);

                        manager.compressRolesData();
                        if (formattingEdited) {
                            manager.updateAffectedPlayersRolesSharedData(role);
                        }
                        PermissionsManagerServer.syncChangesWithPlayersOnline(roles -> roles.getRolesSet().contains(role.getId()));
                        manager.markChanged();

                        OxygenMain.logInfo(1, "[Permissions] {} set role <{}/{}> name color to: {}.",
                                sender.getName(), role.getId(), role.getName(), args[3]);

                        return true;
                    }
                    return false;
                });

                try {
                    Boolean result = future.get();
                    if (result != null && result) {
                        sender.sendMessage(new TextComponentString(String.format("<%s> set role <%s/%s> name color to: %s",
                                sender.getName(), parsedRole.getId(), parsedRole.getName(), args[3])));
                    } else {
                        sender.sendMessage(new TextComponentString(String.format("<%s> failed to set role <%s/%s> name color to: %s",
                                sender.getName(), parsedRole.getId(), parsedRole.getName(), args[3])));
                    }
                } catch (InterruptedException | ExecutionException exception) {
                    exception.printStackTrace();
                }
            } else if (args[1].equals("role-set-prefix")) {
                checkPermission(sender, PermissionsMain.PERMISSION_COMMAND_OP_ROLE_MANAGEMENT);

                Role parsedRole = parseRole(args[2]);

                Future<Boolean> future = OxygenServer.addTask(() -> {
                    PermissionsManagerServer manager = PermissionsManagerServer.instance();

                    Role role = manager.getRole(parsedRole.getId());
                    if (role != null) {
                        role.setPrefix(args[3]);

                        manager.compressRolesData();
                        PermissionsManagerServer.syncChangesWithPlayersOnline(roles -> roles.getRolesSet().contains(role.getId()));
                        manager.markChanged();

                        OxygenMain.logInfo(1, "[Permissions] {} set role <{}/{}> prefix to: {}.",
                                sender.getName(), role.getId(), role.getName(), args[3]);

                        return true;
                    }
                    return false;
                });

                try {
                    Boolean result = future.get();
                    if (result != null && result) {
                        sender.sendMessage(new TextComponentString(String.format("<%s> set role <%s/%s> prefix to: %s",
                                sender.getName(), parsedRole.getId(), parsedRole.getName(), args[3])));
                    } else {
                        sender.sendMessage(new TextComponentString(String.format("<%s> failed to set role <%s/%s> prefix to: %s",
                                sender.getName(), parsedRole.getId(), parsedRole.getName(), args[3])));
                    }
                } catch (InterruptedException | ExecutionException exception) {
                    exception.printStackTrace();
                }
            } else if (args[1].equals("role-set-prefix-color")) {
                checkPermission(sender, PermissionsMain.PERMISSION_COMMAND_OP_ROLE_MANAGEMENT);

                Role parsedRole = parseRole(args[2]);
                TextFormatting formatting = parseFormatting(args[3]);

                Future<Boolean> future = OxygenServer.addTask(() -> {
                    PermissionsManagerServer manager = PermissionsManagerServer.instance();

                    Role role = manager.getRole(parsedRole.getId());
                    if (role != null) {
                        role.setPrefixColor(formatting);

                        manager.compressRolesData();
                        PermissionsManagerServer.syncChangesWithPlayersOnline(roles -> roles.getRolesSet().contains(role.getId()));
                        manager.markChanged();

                        OxygenMain.logInfo(1, "[Permissions] {} set role <{}/{}> prefix color to: {}.",
                                sender.getName(), role.getId(), role.getName(), args[3]);

                        return true;
                    }
                    return false;
                });

                try {
                    Boolean result = future.get();
                    if (result != null && result) {
                        sender.sendMessage(new TextComponentString(String.format("<%s> set role <%s/%s> prefix color to: %s",
                                sender.getName(), parsedRole.getId(), parsedRole.getName(), args[3])));
                    } else {
                        sender.sendMessage(new TextComponentString(String.format("<%s> failed to set role <%s/%s> prefix color to: %s",
                                sender.getName(), parsedRole.getId(), parsedRole.getName(), args[3])));
                    }
                } catch (InterruptedException | ExecutionException exception) {
                    exception.printStackTrace();
                }
            } else if (args[1].equals("role-set-username-color")) {
                checkPermission(sender, PermissionsMain.PERMISSION_COMMAND_OP_ROLE_MANAGEMENT);

                Role parsedRole = parseRole(args[2]);
                TextFormatting formatting = parseFormatting(args[3]);

                Future<Boolean> future = OxygenServer.addTask(() -> {
                    PermissionsManagerServer manager = PermissionsManagerServer.instance();

                    Role role = manager.getRole(parsedRole.getId());
                    if (role != null) {
                        role.setUsernameColor(formatting);

                        manager.compressRolesData();
                        PermissionsManagerServer.syncChangesWithPlayersOnline(roles -> roles.getRolesSet().contains(role.getId()));
                        manager.markChanged();

                        OxygenMain.logInfo(1, "[Permissions] {} set role <{}/{}> username color to: {}.",
                                sender.getName(), role.getId(), role.getName(), args[3]);

                        return true;
                    }
                    return false;
                });

                try {
                    Boolean result = future.get();
                    if (result != null && result) {
                        sender.sendMessage(new TextComponentString(String.format("<%s> set role <%s/%s> username color to: %s",
                                sender.getName(), parsedRole.getId(), parsedRole.getName(), args[3])));
                    } else {
                        sender.sendMessage(new TextComponentString(String.format("<%s> failed to set role <%s/%s> username color to: %s",
                                sender.getName(), parsedRole.getId(), parsedRole.getName(), args[3])));
                    }
                } catch (InterruptedException | ExecutionException exception) {
                    exception.printStackTrace();
                }
            } else if (args[1].equals("role-set-chat-color")) {
                checkPermission(sender, PermissionsMain.PERMISSION_COMMAND_OP_ROLE_MANAGEMENT);

                Role parsedRole = parseRole(args[2]);
                TextFormatting formatting = parseFormatting(args[3]);

                Future<Boolean> future = OxygenServer.addTask(() -> {
                    PermissionsManagerServer manager = PermissionsManagerServer.instance();

                    Role role = manager.getRole(parsedRole.getId());
                    if (role != null) {
                        role.setChatColor(formatting);

                        manager.compressRolesData();
                        PermissionsManagerServer.syncChangesWithPlayersOnline(roles -> roles.getRolesSet().contains(role.getId()));
                        manager.markChanged();

                        OxygenMain.logInfo(1, "[Permissions] {} set role <{}/{}> chat color to: {}.",
                                sender.getName(), role.getId(), role.getName(), args[3]);

                        return true;
                    }
                    return false;
                });

                try {
                    Boolean result = future.get();
                    if (result != null && result) {
                        sender.sendMessage(new TextComponentString(String.format("<%s> set role <%s/%s> chat color to: %s",
                                sender.getName(), parsedRole.getId(), parsedRole.getName(), args[3])));
                    } else {
                        sender.sendMessage(new TextComponentString(String.format("<%s> failed to set role <%s/%s> chat color to: %s",
                                sender.getName(), parsedRole.getId(), parsedRole.getName(), args[3])));
                    }
                } catch (InterruptedException | ExecutionException exception) {
                    exception.printStackTrace();
                }
            } else if (args[1].equals("player-add-role")) {
                checkPermission(sender, PermissionsMain.PERMISSION_COMMAND_OP_PLAYER_MANAGEMENT);

                Pair<UUID, EntityPlayerMP> pair = parsePlayer(server, sender, args[2]);
                UUID playerUUID = pair.getKey();
                @Nullable EntityPlayerMP playerMP = pair.getValue();
                Role parsedRole = parseRole(args[3]);

                Future<Boolean> future = OxygenServer.addTask(() -> {
                    PermissionsManagerServer manager = PermissionsManagerServer.instance();

                    Role role = manager.getRole(parsedRole.getId());
                    if (role == null) return false;

                    if (manager.addRoleToPlayer(playerUUID, role.getId())) {
                        manager.updatePlayerRolesSharedData(playerUUID);
                        if (playerMP != null) {
                            manager.syncPlayerRoles(playerMP);
                        }
                        manager.markChanged();

                        OxygenMain.logInfo(1, "[Permissions] {} added role <{}/{}> to player {}/{}.",
                                sender.getName(), parsedRole.getId(), parsedRole.getName(),
                                playerMP != null ? MinecraftCommon.getEntityName(playerMP) : "Offline", playerUUID);

                        return true;
                    }
                    return false;
                });

                try {
                    Boolean result = future.get();
                    if (result != null && result) {
                        sender.sendMessage(new TextComponentString(String.format("<%s> added role <%s/%s> to: %s",
                                sender.getName(), parsedRole.getId(), parsedRole.getName(),
                                playerMP != null ? MinecraftCommon.getEntityName(playerMP) : args[2])));
                    } else {
                        sender.sendMessage(new TextComponentString(String.format("<%s> failed to add role <%s/%s> to: %s",
                                sender.getName(), parsedRole.getId(), parsedRole.getName(),
                                playerMP != null ? MinecraftCommon.getEntityName(playerMP) : args[2])));
                    }
                } catch (InterruptedException | ExecutionException exception) {
                    exception.printStackTrace();
                }
            } else if (args[1].equals("player-remove-role")) {
                checkPermission(sender, PermissionsMain.PERMISSION_COMMAND_OP_PLAYER_MANAGEMENT);

                Pair<UUID, EntityPlayerMP> pair = parsePlayer(server, sender, args[2]);
                UUID playerUUID = pair.getKey();
                @Nullable EntityPlayerMP playerMP = pair.getValue();
                Role parsedRole = parseRole(args[3]);

                Future<Boolean> future = OxygenServer.addTask(() -> {
                    PermissionsManagerServer manager = PermissionsManagerServer.instance();

                    Role role = manager.getRole(parsedRole.getId());
                    if (role == null) return false;

                    if (manager.removeRoleFromPlayer(playerUUID, role.getId())) {
                        manager.updatePlayerRolesSharedData(playerUUID);
                        if (playerMP != null) {
                            manager.syncPlayerRoles(playerMP);
                        }
                        manager.markChanged();

                        OxygenMain.logInfo(1, "[Permissions] {} removed role <{}/{}> from player {}/{}.",
                                sender.getName(), parsedRole.getId(), parsedRole.getName(),
                                playerMP != null ? MinecraftCommon.getEntityName(playerMP) : "Offline", playerUUID);

                        return true;
                    }
                    return false;
                });

                try {
                    Boolean result = future.get();
                    if (result != null && result) {
                        sender.sendMessage(new TextComponentString(String.format("<%s> removed role <%s/%s> from: %s",
                                sender.getName(), parsedRole.getId(), parsedRole.getName(),
                                playerMP != null ? MinecraftCommon.getEntityName(playerMP) : args[2])));
                    } else {
                        sender.sendMessage(new TextComponentString(String.format("<%s> failed to remove role <%s/%s> from: %s",
                                parsedRole.getId(), parsedRole.getName(),
                                playerMP != null ? MinecraftCommon.getEntityName(playerMP) : args[2])));
                    }
                } catch (InterruptedException | ExecutionException exception) {
                    exception.printStackTrace();
                }
            } else if (args[1].equals("role-remove-permission")) {
                checkPermission(sender, PermissionsMain.PERMISSION_COMMAND_OP_ROLE_MANAGEMENT);

                Role parsedRole = parseRole(args[2]);
                PrivilegeRegistry.Entry entry = parsePermission(args[3]);

                Future<Boolean> future = OxygenServer.addTask(() -> {
                    PermissionsManagerServer manager = PermissionsManagerServer.instance();

                    Role role = manager.getRole(parsedRole.getId());
                    if (role == null) return false;

                    if (role.removePrivilege(entry.getId()) != null) {
                        manager.compressRolesData();
                        PermissionsManagerServer.syncChangesWithPlayersOnline(roles -> roles.getRolesSet().contains(role.getId()));
                        manager.markChanged();

                        OxygenMain.logInfo(1, "[Permissions] {} removed permission <{}/{}> from role {}/{}.",
                                sender.getName(), entry.getId(), entry.getDisplayName(), parsedRole.getId(), parsedRole.getName());

                        return true;
                    }
                    return false;
                });

                try {
                    Boolean result = future.get();
                    if (result != null && result) {
                        sender.sendMessage(new TextComponentString(String.format("<%s> removed permission <id/name: %s/%s> from role <%s/%s>",
                                sender.getName(), entry.getId(), entry.getDisplayName(), parsedRole.getId(), parsedRole.getName())));
                    } else {
                        sender.sendMessage(new TextComponentString(String.format("<%s> failed to remove permission <id/name: %s/%s> from role <%s/%s>",
                                sender.getName(), entry.getId(), entry.getDisplayName(), parsedRole.getId(), parsedRole.getName())));
                    }
                } catch (InterruptedException | ExecutionException exception) {
                    exception.printStackTrace();
                }
            }
        } else if (args.length == 5) {
            if (args[1].equals("role-add-permission")) {
                checkPermission(sender, PermissionsMain.PERMISSION_COMMAND_OP_ROLE_MANAGEMENT);

                Role parsedRole = parseRole(args[2]);
                PrivilegeRegistry.Entry entry = parsePermission(args[3]);

                TypedValue value = ValueType.fromString(entry.getValueType(), args[4]);
                if (value == null) {
                    throw new CommandException("Failed to add permission <" + args[3] + "> with value: " + args[4]);
                }
                Permission permission = new Permission<>(value, entry.getId());

                Future<Boolean> future = OxygenServer.addTask(() -> {
                    PermissionsManagerServer manager = PermissionsManagerServer.instance();

                    Role role = manager.getRole(parsedRole.getId());
                    if (role == null) return false;

                    if (role.addPermission(permission) == null) {
                        manager.compressRolesData();
                        PermissionsManagerServer.syncChangesWithPlayersOnline(roles -> roles.getRolesSet().contains(role.getId()));
                        manager.markChanged();

                        OxygenMain.logInfo(1, "[Permissions] {} added permission <{}/{} - {}> to role {}/{}.",
                                sender.getName(), entry.getId(), entry.getDisplayName(), permission.get().toString(),
                                parsedRole.getId(), parsedRole.getName());

                        return true;
                    }
                    return false;
                });

                try {
                    Boolean result = future.get();
                    if (result != null && result) {
                        sender.sendMessage(new TextComponentString(String.format("<%s> added permission <id/name: %s/%s, value: %s> to role <%s/%s>",
                                sender.getName(), permission.getId(), entry.getDisplayName(), permission.get().toString(),
                                parsedRole.getId(), parsedRole.getName())));
                    } else {
                        sender.sendMessage(new TextComponentString(String.format("<%s> failed to add permission <id/name: %s/%s, value: %s> to role <%s/%s>",
                                sender.getName(), permission.getId(), entry.getDisplayName(), permission.get().toString(),
                                parsedRole.getId(), parsedRole.getName())));
                    }
                } catch (InterruptedException | ExecutionException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

    private Role parseRole(String idStr) throws CommandException {
        int roleId = CommandBase.parseInt(idStr, 0, Short.MAX_VALUE);
        Role role = PermissionsManagerServer.instance().getRole(roleId);
        if (role == null) {
            throw new CommandException("Role with id <" + idStr + "> not found");
        }
        return role;
    }

    private TextFormatting parseFormatting(String formattingStr) throws CommandException {
        TextFormatting formatting;
        try {
            formatting = TextFormatting.valueOf(formattingStr.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new CommandException("Unknown formatting: " + formattingStr);
        }
        return formatting;
    }

    private PrivilegeRegistry.Entry parsePermission(String privilegeIdStr) throws CommandException {
        int privilegeId = CommandBase.parseInt(privilegeIdStr, 0, Short.MAX_VALUE);
        PrivilegeRegistry.Entry entry = PrivilegeRegistry.getEntry(privilegeId);
        if (entry == null) {
            throw new CommandException("Permission with id <" + privilegeIdStr + "> not found");
        }
        return entry;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
                                          @Nullable BlockPos targetPos) {
        if (args.length == 2) {
            return CommandBase.getListOfStringsMatchingLastWord(args, "role-info", "player-info",
                    "role-remove", "role-create", "role-set-name", "role-set-name-color", "role-set-prefix",
                    "role-set-prefix-color", "role-set-username-color", "role-set-chat-color", "player-add-role",
                    "player-remove-role", "role-add-permission", "role-remove-permission");
        }
        return Collections.emptyList();
    }
}
