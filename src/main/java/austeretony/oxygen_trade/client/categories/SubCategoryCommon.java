package austeretony.oxygen_trade.client.categories;

import net.minecraft.util.ResourceLocation;

public class SubCategoryCommon extends OfferSubCategoryClient {

    public SubCategoryCommon(String name) {
        super(name);
    }

    public boolean isValid(ResourceLocation registryName) {
        return true;
    }
}