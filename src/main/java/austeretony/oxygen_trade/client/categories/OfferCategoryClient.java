package austeretony.oxygen_trade.client.categories;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.common.util.ByteBufUtils;
import austeretony.oxygen_trade.common.categories.EnumCategoriesFileKey;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.ResourceLocation;

public class OfferCategoryClient {

    public final String name;

    protected final List<OfferSubCategoryClient> subCategories = new ArrayList<>(3);

    public OfferCategoryClient(String name) {
        this.name = name;
    }

    public List<OfferSubCategoryClient> getSubCategories() {
        return this.subCategories;
    }

    public boolean isValid(OfferSubCategoryClient subCategory, ResourceLocation registryName) {
        if (!this.subCategories.contains(subCategory))
            return false;
        return subCategory.isValid(registryName);
    }

    public String localizedName() {
        return ClientReference.localize(this.name);
    }

    private void sortSubCategories() {
        Collections.sort(this.subCategories, (c1, c2)->(c1.localizedName().compareTo(c2.localizedName())));
    }

    protected static OfferCategoryClient deserialize(JsonObject jsonObject) {
        OfferCategoryClient category = new OfferCategoryClient(jsonObject.get(EnumCategoriesFileKey.NAME.get()).getAsString());
        OfferSubCategoryClient commonSubCategory = new OfferSubCategoryClient("oxygen_trade.category.common");
        category.subCategories.add(commonSubCategory);
        OfferSubCategoryClient subCategory;
        for (JsonElement subCategoryEntry : jsonObject.get(EnumCategoriesFileKey.SUB_CATEGORIES.get()).getAsJsonArray()) {
            category.subCategories.add(subCategory = OfferSubCategoryClient.deserialize(subCategoryEntry.getAsJsonObject()));
            commonSubCategory.registryNames.addAll(subCategory.registryNames);
        }
        category.sortSubCategories();
        return category;
    }

    protected JsonObject serialize() {
        JsonObject categoryEntry = new JsonObject();
        categoryEntry.add(EnumCategoriesFileKey.NAME.get(), new JsonPrimitive(this.name));
        JsonArray subCategoryEntries = new JsonArray();
        for (OfferSubCategoryClient subCategory : this.subCategories)
            subCategoryEntries.add(subCategory.serialize());
        categoryEntry.add(EnumCategoriesFileKey.SUB_CATEGORIES.get(), subCategoryEntries);
        return categoryEntry;
    }

    protected static OfferCategoryClient read(ByteBuf buffer) {
        OfferCategoryClient category = new OfferCategoryClient(ByteBufUtils.readString(buffer));
        int amount = buffer.readByte();
        for (int i = 0; i < amount; i++)
            category.subCategories.add(OfferSubCategoryClient.read(buffer));
        return category;
    }
}
