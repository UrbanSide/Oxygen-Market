package austeretony.oxygen_trade.client.categories;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.common.util.ByteBufUtils;
import austeretony.oxygen_trade.common.categories.EnumCategoriesFileKey;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.ResourceLocation;

public class OfferSubCategoryClient {

    public final String name;

    protected final Set<ResourceLocation> registryNames = new HashSet<>();

    public OfferSubCategoryClient(String name) {
        this.name = name;
    }

    public boolean isValid(ResourceLocation registryName) {
        return this.registryNames.contains(registryName);
    }

    public String localizedName() {
        return ClientReference.localize(this.name);
    }

    protected static OfferSubCategoryClient deserialize(JsonObject jsonObject) {
        OfferSubCategoryClient subCategory = new OfferSubCategoryClient(jsonObject.get(EnumCategoriesFileKey.NAME.get()).getAsString());
        for (JsonElement itemEntry : jsonObject.get(EnumCategoriesFileKey.ITEMS.get()).getAsJsonArray())
            subCategory.registryNames.add(new ResourceLocation(itemEntry.getAsString()));
        return subCategory;
    }

    protected JsonObject serialize() {
        JsonObject subCategoryEntry = new JsonObject();
        subCategoryEntry.add(EnumCategoriesFileKey.NAME.get(), new JsonPrimitive(this.name));
        JsonArray itemEntries = new JsonArray();
        for (ResourceLocation registryName : this.registryNames)
            itemEntries.add(new JsonPrimitive(registryName.toString()));
        subCategoryEntry.add(EnumCategoriesFileKey.ITEMS.get(), itemEntries);
        return subCategoryEntry;
    }

    protected static OfferSubCategoryClient read(ByteBuf buffer) {
        OfferSubCategoryClient subCategory = new OfferSubCategoryClient(ByteBufUtils.readString(buffer));
        int amount = buffer.readShort();
        for (int i = 0; i < amount; i++)
            subCategory.registryNames.add(new ResourceLocation(ByteBufUtils.readString(buffer)));
        return subCategory;
    }
}
