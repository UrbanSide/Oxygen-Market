package austeretony.oxygen_trade.server.category;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import austeretony.oxygen_core.common.util.ByteBufUtils;
import austeretony.oxygen_trade.common.categories.EnumCategoriesFileKey;
import io.netty.buffer.ByteBuf;

public class OfferCategoryServer {

    public final String name;

    private final List<OfferSubCategoryServer> subCategories = new ArrayList<>();

    public OfferCategoryServer(String name) {
        this.name = name;
    }

    public List<OfferSubCategoryServer> getSubCategories() {
        return this.subCategories;
    }

    protected static OfferCategoryServer deserialize(JsonObject jsonObject) {
        OfferCategoryServer category = new OfferCategoryServer(jsonObject.get(EnumCategoriesFileKey.NAME.get()).getAsString());
        for (JsonElement subCategoryEntry : jsonObject.get(EnumCategoriesFileKey.SUB_CATEGORIES.get()).getAsJsonArray())
            category.subCategories.add(OfferSubCategoryServer.deserialize(subCategoryEntry.getAsJsonObject()));
        return category;
    }

    protected void write(ByteBuf buffer) {
        ByteBufUtils.writeString(this.name, buffer);
        buffer.writeByte(this.subCategories.size());
        for (OfferSubCategoryServer subCategory : this.subCategories)
            subCategory.write(buffer);
    }
}
