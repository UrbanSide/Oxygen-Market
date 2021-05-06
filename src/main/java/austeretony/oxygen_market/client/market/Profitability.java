package austeretony.oxygen_market.client.market;

import austeretony.oxygen_core.client.util.MinecraftClient;

public enum Profitability {

    NO_DATA("no_data", 0xffc0c0c0),
    OVERPRICE("overprice", 0xffec0000),
    NORMAL("normal", 0xffffffff),
    GOOD("good", 0xff2dc50e),
    VERY_GOOD("very_good", 0xff3990fc);

    private final String name;
    private final int colorHex;

    Profitability(String name, int colorHex) {
        this.name = name;
        this.colorHex = colorHex;
    }

    public String getDisplayName() {
        return MinecraftClient.localize("oxygen_market.profitability." + name);
    }

    public int getColorHex() {
        return colorHex;
    }
}
