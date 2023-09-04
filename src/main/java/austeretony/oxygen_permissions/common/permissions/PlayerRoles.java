package austeretony.oxygen_permissions.common.permissions;

import austeretony.oxygen_core.common.util.ByteBufUtils;
import austeretony.oxygen_permissions.common.main.PermissionsMain;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.netty.buffer.ByteBuf;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public class PlayerRoles {

    private final UUID playerUUID;
    private final Set<Integer> rolesSet = new TreeSet<>(Comparator.reverseOrder());
    private int chatFormattingRole;

    public PlayerRoles(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public Set<Integer> getRolesSet() {
        return rolesSet;
    }

    public boolean haveRole(int roleId) {
        return rolesSet.contains(roleId);
    }

    public int getRolesAmount() {
        return rolesSet.size();
    }

    public boolean addRole(int roleId) {
        return rolesSet.add(roleId);
    }

    public boolean removeRole(int roleId) {
        return rolesSet.remove(roleId);
    }

    public int getFirstRole() {
        if (!rolesSet.isEmpty()) {
            return ((TreeSet<Integer>) rolesSet).first();
        }
        return PermissionsMain.DEFAULT_ROLE_ID;
    }

    public int getChatFormattingRole() {
        return chatFormattingRole;
    }

    public void setChatFormattingRole(int roleId) {
        chatFormattingRole = roleId;
    }

    public JsonObject toJson() {
        JsonObject rolesObject = new JsonObject();

        rolesObject.addProperty("player_uuid", playerUUID.toString());
        rolesObject.addProperty("chat_formatting_role", chatFormattingRole);

        JsonArray rolesArray = new JsonArray();
        for (int roleId : rolesSet) {
            rolesArray.add(new JsonPrimitive(roleId));
        }
        rolesObject.add("roles", rolesArray);

        return rolesObject;
    }

    public static PlayerRoles fromJson(JsonObject jsonObject) {
        PlayerRoles roles = new PlayerRoles(UUID.fromString(jsonObject.get("player_uuid").getAsString()));
        roles.setChatFormattingRole(jsonObject.get("chat_formatting_role").getAsInt());

        JsonArray rolesIdsArray = jsonObject.getAsJsonArray("roles");
        for (JsonElement roleIdElement : rolesIdsArray) {
            roles.addRole(roleIdElement.getAsInt());
        }

        return roles;
    }

    public void write(ByteBuf buffer) {
        ByteBufUtils.writeUUID(playerUUID, buffer);
        buffer.writeShort(chatFormattingRole);

        buffer.writeByte(rolesSet.size());
        for (int roleId : rolesSet) {
            buffer.writeShort(roleId);
        }
    }

    public static PlayerRoles read(ByteBuf buffer) {
        PlayerRoles roles = new PlayerRoles(ByteBufUtils.readUUID(buffer));
        roles.setChatFormattingRole(buffer.readShort());

        int amount = buffer.readByte();
        for (int i = 0; i < amount; i++) {
            roles.addRole(buffer.readShort());
        }

        return roles;
    }

    public PlayerRoles copy() {
        PlayerRoles copy = new PlayerRoles(playerUUID);
        copy.setChatFormattingRole(chatFormattingRole);
        copy.getRolesSet().addAll(rolesSet);
        return copy;
    }
}
