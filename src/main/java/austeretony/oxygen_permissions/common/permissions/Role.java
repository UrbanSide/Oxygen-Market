package austeretony.oxygen_permissions.common.permissions;

import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.util.ByteBufUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.text.TextFormatting;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class Role {

    public static final int
            ROLE_NAME_MAX_LENGTH = 24,
            PREFIX_MAX_LENGTH = 16;

    private final int roleId;
    private String name, prefix;
    private TextFormatting nameColor, usernameColor, prefixColor, chatColor;

    private final Map<Integer, Permission> permissionsMap = new HashMap<>();

    public Role(int roleId, String name, TextFormatting nameColor) {
        this.roleId = roleId;
        this.name = name;
        this.nameColor = nameColor;
        prefix = "";
        usernameColor = prefixColor = chatColor = TextFormatting.GRAY;
    }

    public int getId() {
        return roleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public TextFormatting getNameColor() {
        return nameColor;
    }

    public void setNameColor(TextFormatting color) {
        nameColor = color;
    }

    public TextFormatting getUsernameColor() {
        return usernameColor;
    }

    public void setUsernameColor(TextFormatting color) {
        usernameColor = color;
    }

    public TextFormatting getPrefixColor() {
        return prefixColor;
    }

    public void setPrefixColor(TextFormatting color) {
        prefixColor = color;
    }

    public TextFormatting getChatColor() {
        return chatColor;
    }

    public void setChatColor(TextFormatting color) {
        chatColor = color;
    }

    public Map<Integer, Permission> getPermissionsMap() {
        return permissionsMap;
    }

    public Permission getPermission(int id) {
        return permissionsMap.get(id);
    }

    public Permission addPermission(Permission permission) {
        return permissionsMap.put(permission.getId(), permission);
    }

    public Permission removePrivilege(int id) {
        return permissionsMap.remove(id);
    }

    public JsonObject toJson() {
        JsonObject roleObject = new JsonObject();

        roleObject.addProperty("id", roleId);
        roleObject.addProperty("name", name);
        roleObject.addProperty("prefix", prefix);
        roleObject.addProperty("role_name_color", nameColor.name());
        roleObject.addProperty("player_username_color", usernameColor.name());
        roleObject.addProperty("prefix_color", prefixColor.name());
        roleObject.addProperty("chat_color", chatColor.name());

        JsonArray permissionsArray = new JsonArray();
        getPermissionsMap().values()
                .stream()
                .sorted(Comparator.comparingInt(Permission::getId))
                .forEach(e -> permissionsArray.add(e.toJson()));
        roleObject.add("permissions", permissionsArray);

        return roleObject;
    }

    public static Role fromJson(JsonObject jsonObject) {
        Role role = new Role(jsonObject.get("id").getAsInt(), jsonObject.get("name").getAsString(),
                TextFormatting.valueOf(jsonObject.get("role_name_color").getAsString()));

        role.setPrefix(jsonObject.get("prefix").getAsString());
        role.setUsernameColor(TextFormatting.valueOf(jsonObject.get("player_username_color").getAsString()));
        role.setPrefixColor(TextFormatting.valueOf(jsonObject.get("prefix_color").getAsString()));
        role.setChatColor(TextFormatting.valueOf(jsonObject.get("chat_color").getAsString()));

        JsonArray privilegesArray = jsonObject.get("permissions").getAsJsonArray();
        for (JsonElement privilegeElement : privilegesArray)
            role.addPermission(Permission.fromJson(privilegeElement.getAsJsonObject()));

        OxygenMain.logInfo(1, "[Permissions] Loaded role <{}> ({}).", role.getName(), role.getId());

        return role;
    }

    public void write(ByteBuf buffer) {
        buffer.writeShort(roleId);
        ByteBufUtils.writeString(name, buffer);
        buffer.writeByte(nameColor.ordinal());

        ByteBufUtils.writeString(prefix, buffer);
        buffer.writeByte(usernameColor.ordinal());
        buffer.writeByte(prefixColor.ordinal());
        buffer.writeByte(chatColor.ordinal());

        buffer.writeByte(permissionsMap.size());
        for (Permission permission : permissionsMap.values()) {
            permission.write(buffer);
        }
    }

    public static Role read(ByteBuf buffer) {
        Role role = new Role(buffer.readShort(), ByteBufUtils.readString(buffer), TextFormatting.values()[buffer.readByte()]);
        role.setPrefix(ByteBufUtils.readString(buffer));
        role.setUsernameColor(TextFormatting.values()[buffer.readByte()]);
        role.setPrefixColor(TextFormatting.values()[buffer.readByte()]);
        role.setChatColor(TextFormatting.values()[buffer.readByte()]);

        int amount = buffer.readByte();
        for (int i = 0; i < amount; i++) {
            role.addPermission(Permission.read(buffer));
        }

        return role;
    }
}
