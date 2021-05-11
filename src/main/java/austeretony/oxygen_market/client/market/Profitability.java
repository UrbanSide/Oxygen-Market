package austeretony.oxygen_market.client.market;

import austeretony.oxygen_core.client.util.MinecraftClient;

public enum Profitability {

    NO_DATA("no_data", 0xffc0c0c0, -1F),
    OVERPRICE("overprice", 0xffec0000, -1F),
    NORMAL("normal", 0xffffffff, 0F),
    GOOD("good", 0xff2dc50e, 0.2F),
    VERY_GOOD("very_good", 0xff3990fc, 0.4F);

    private final String name;
    private final int colorHex;
    private final float deltaFactor;

    Profitability(String name, int colorHex, float deltaFactor) {
        this.name = name;
        this.colorHex = colorHex;
        this.deltaFactor = deltaFactor;
    }

    public String getDisplayName() {
        String name = MinecraftClient.localize("oxygen_market.profitability." + this.name);
        if (this != NO_DATA && this != OVERPRICE) {
            name += " (" + (int) (deltaFactor * 100) + "%+)";
        }
        return name;
    }

    public int getColorHex() {
        return colorHex;
    }

    public float getDeltaFactor() {
        return deltaFactor;
    }
}
