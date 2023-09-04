package austeretony.oxygen_permissions.client.network.operation;

import austeretony.oxygen_core.common.network.operation.NetworkOperationsHandler;
import austeretony.oxygen_core.common.player.shared.PlayerSharedData;
import austeretony.oxygen_core.common.sync.SyncMeta;
import austeretony.oxygen_permissions.client.PermissionsManagerClient;
import austeretony.oxygen_permissions.common.main.PermissionsMain;
import austeretony.oxygen_permissions.common.network.operation.PermissionsOperation;
import austeretony.oxygen_permissions.common.permissions.PlayerRoles;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PermissionsNetworkOperationsHandlerClient implements NetworkOperationsHandler {

    @Override
    public int getId() {
        return PermissionsMain.PERMISSIONS_OPERATIONS_HANDLER_ID;
    }

    @Override
    public void process(EntityPlayer player, int operationIndex, ByteBuf buffer) {
        PermissionsOperation operation = getEnum(PermissionsOperation.values(), operationIndex);
        if (operation == null) return;

        if (operation == PermissionsOperation.SYNC_ROLES_DATA) {
            byte[] bytes = new byte[buffer.readInt()];
            buffer.readBytes(bytes);

            PermissionsManagerClient.instance().rolesDataSynchronized(bytes);
        } else if (operation == PermissionsOperation.SYNC_PLAYER_ROLES) {
            PlayerRoles roles = PlayerRoles.read(buffer);

            PermissionsManagerClient.instance().playerRolesSynced(roles);
        } else if (operation == PermissionsOperation.FORMATTING_ROLE_CHANGED) {
            int roleId = buffer.readShort();

            PermissionsManagerClient.instance().formattingRoleChanged(roleId);
        } else if (operation == PermissionsOperation.SYNC_MANAGEMENT_DATA) {
            int reason = buffer.readByte();
            SyncMeta meta = buffer.readBoolean() ? SyncMeta.read(buffer) : null;

            byte[] rolesRaw = new byte[buffer.readInt()];
            buffer.readBytes(rolesRaw);

            int rolesEntriesAmount = buffer.readInt();
            Map<UUID, PlayerRoles> rolesMap = new HashMap<>(rolesEntriesAmount);
            for (int i = 0; i < rolesEntriesAmount; i++) {
                PlayerRoles roles = PlayerRoles.read(buffer);
                rolesMap.put(roles.getPlayerUUID(), roles);
            }

            int sharedDataEntriesAmount = buffer.readInt();
            Map<UUID, PlayerSharedData> sharedDataMap = new HashMap<>(sharedDataEntriesAmount);
            for (int i = 0; i < sharedDataEntriesAmount; i++) {
                PlayerSharedData data = new PlayerSharedData();
                data.read(buffer);
                sharedDataMap.put(data.getPlayerUUID(), data);
            }

            PermissionsManagerClient.instance().managementDataSynced(reason, meta, rolesRaw, rolesMap, sharedDataMap);
        }
    }
}
