package austeretony.oxygen_trade.common.categories;

public enum EnumCategoriesFileKey {

    NAME,
    SUB_CATEGORIES,
    ITEMS;

    public String get() {
        return this.toString().toLowerCase();
    }
}