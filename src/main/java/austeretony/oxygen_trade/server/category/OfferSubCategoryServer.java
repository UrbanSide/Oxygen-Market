package austeretony.oxygen_trade.server.category;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import austeretony.oxygen_core.common.util.ByteBufUtils;
import austeretony.oxygen_trade.common.categories.EnumCategoriesFileKey;
import io.netty.buffer.ByteBuf;

public class OfferSubCategoryServer {

    public final String name;

    private final Set<String> registryNames = new HashSet<>();

    public OfferSubCategoryServer(String name) {
        this.name = name;
    }

    protected static OfferSubCategoryServer deserialize(JsonObject jsonObject) {
        OfferSubCategoryServer subCategory = new OfferSubCategoryServer(jsonObject.get(EnumCategoriesFileKey.NAME.get()).getAsString());
        for (JsonElement itemEntry : jsonObject.get(EnumCategoriesFileKey.ITEMS.get()).getAsJsonArray())
            subCategory.registryNames.add(itemEntry.getAsString());
        return subCategory;
    }

    protected void write(ByteBuf buffer) {
        ByteBufUtils.writeString(this.name, buffer);
        buffer.writeShort(this.registryNames.size());
        for (String registryName : this.registryNames)
            ByteBufUtils.writeString(registryName, buffer);
    }
}
