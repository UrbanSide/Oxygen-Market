package austeretony.oxygen_permissions.server.network.operation;

import austeretony.oxygen_core.common.network.operation.NetworkOperationsHandler;
import austeretony.oxygen_core.common.util.ByteBufUtils;
import austeretony.oxygen_permissions.common.main.PermissionsMain;
import austeretony.oxygen_permissions.common.network.operation.PermissionsOperation;
import austeretony.oxygen_permissions.common.permissions.Permission;
import austeretony.oxygen_permissions.common.permissions.Role;
import austeretony.oxygen_permissions.server.PermissionsManagerServer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PermissionsNetworkOperationsHandlerServer implements NetworkOperationsHandler {

    @Override
    public int getId() {
        return PermissionsMain.PERMISSIONS_OPERATIONS_HANDLER_ID;
    }

    @Override
    public void process(EntityPlayer player, int operationIndex, ByteBuf buffer) {
        PermissionsOperation operation = getEnum(PermissionsOperation.values(), operationIndex);
        if (operation == null) return;

        if (operation == PermissionsOperation.CHANGE_FORMATTING_ROLE) {
            int roleId = buffer.readShort();

            PermissionsManagerServer.instance().changeFormattingRole((EntityPlayerMP) player, roleId);
        } else if (operation == PermissionsOperation.REQUEST_MANAGEMENT_DATA) {
            PermissionsManagerServer.instance().processManagementDataSyncRequest((EntityPlayerMP) player);
        } else if (operation == PermissionsOperation.CREATE_ROLE) {
            Role role = Role.read(buffer);

            PermissionsManagerServer.instance().createRole((EntityPlayerMP) player, role);
        } else if (operation == PermissionsOperation.EDIT_ROLE) {
            Role edited = Role.read(buffer);

            PermissionsManagerServer.instance().editRole((EntityPlayerMP) player, edited);
        } else if (operation == PermissionsOperation.REMOVE_ROLE) {
            int roleId = buffer.readShort();

            PermissionsManagerServer.instance().removeRole((EntityPlayerMP) player, roleId);
        } else if (operation == PermissionsOperation.ADD_ROLE_PERMISSION) {
            int roleId = buffer.readShort();
            Permission permission = Permission.read(buffer);

            PermissionsManagerServer.instance().addRolePermission((EntityPlayerMP) player, roleId, permission);
        } else if (operation == PermissionsOperation.REMOVE_ROLE_PERMISSIONS) {
            int roleId = buffer.readShort();
            int amount = buffer.readByte();
            List<Integer> permissionsIds = new ArrayList<>(amount);
            for (int i = 0; i < amount; i++) {
                permissionsIds.add((int) buffer.readShort());
            }

            PermissionsManagerServer.instance().removeRolePermissions((EntityPlayerMP) player, roleId, permissionsIds);
        } else if (operation == PermissionsOperation.ADD_ROLES_TO_PLAYER) {
            UUID playerUUID = ByteBufUtils.readUUID(buffer);
            int amount = buffer.readByte();
            List<Integer> rolesIds = new ArrayList<>(amount);
            for (int i = 0; i < amount; i++) {
                rolesIds.add((int) buffer.readShort());
            }

            PermissionsManagerServer.instance().addRolesToPlayer((EntityPlayerMP) player, playerUUID, rolesIds);
        } else if (operation == PermissionsOperation.REMOVE_ROLES_FROM_PLAYER) {
            UUID playerUUID = ByteBufUtils.readUUID(buffer);
            int amount = buffer.readByte();
            List<Integer> rolesIds = new ArrayList<>(amount);
            for (int i = 0; i < amount; i++) {
                rolesIds.add((int) buffer.readShort());
            }

            PermissionsManagerServer.instance().removeRolesFromPlayer((EntityPlayerMP) player, playerUUID, rolesIds);
        }
    }
}
