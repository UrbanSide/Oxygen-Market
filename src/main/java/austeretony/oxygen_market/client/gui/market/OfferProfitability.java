package austeretony.oxygen_market.client.gui.market;

public class OfferProfitability {

    public final int profitabilityIndex, colorHex;

    public final String profitabilityPercentStr, profitabilityTooltipStr;

    public OfferProfitability(int profitabilityIndex, int colorHex, String profitabilityPercentStr, String profitabilityTooltipStr) {
        this.profitabilityIndex = profitabilityIndex;
        this.colorHex = colorHex;
        this.profitabilityPercentStr = profitabilityPercentStr;
        this.profitabilityTooltipStr = profitabilityTooltipStr;
    }
}
