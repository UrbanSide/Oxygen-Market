package austeretony.oxygen_trade.client.gui.trade.buy;

import austeretony.oxygen_core.client.gui.OxygenGUIUtils;
import austeretony.oxygen_core.client.gui.elements.OxygenBackgroundFiller;

public class BuySectionBackgroundFiller extends OxygenBackgroundFiller {

    public BuySectionBackgroundFiller(int xPosition, int yPosition, int width, int height) {             
        super(xPosition, yPosition, width, height);
    }

    @Override
    public void drawBackground() {
        //main background  
        drawRect(0, 0, this.getWidth(), this.getHeight(), this.getEnabledBackgroundColor());      

        //title underline
        OxygenGUIUtils.drawRect(4.0D, 14.0D, this.getWidth() - 4.0D, 14.4D, this.getDisabledBackgroundColor());

        //panel underline
        OxygenGUIUtils.drawRect(78.0D, this.getHeight() - 12.6D, this.getWidth() - 4.0D, this.getHeight() - 13.0D, this.getDisabledBackgroundColor());
    }
}
