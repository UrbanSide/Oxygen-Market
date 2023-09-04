package austeretony.oxygen_permissions.common.permissions;

import austeretony.oxygen_core.common.util.value.*;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;

import javax.annotation.Nullable;

public class Permission<T extends TypedValue> {

    protected final T value;

    protected final int id;

    public Permission(T value, int id) {
        this.value = value;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public T get() {
        return value;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", id);
        jsonObject.add("value", ValueType.toJson(value));
        return jsonObject;
    }

    @Nullable
    public static Permission fromJson(JsonObject jsonObject) {
        TypedValue value = ValueType.fromJson(jsonObject.getAsJsonObject("value"));
        if (value == null) return null;
        return new Permission(value, jsonObject.get("id").getAsInt());
    }

    public void write(ByteBuf buffer) {
        buffer.writeShort(id);
        ValueType.write(value, buffer);
    }

    @Nullable
    public static Permission read(ByteBuf buffer) {
        int id = buffer.readShort();
        TypedValue value = ValueType.read(buffer);
        if (value == null) return null;
        return new Permission(value, id);
    }
}
