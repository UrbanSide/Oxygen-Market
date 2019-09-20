package austeretony.oxygen_trade.client.gui.trade.buy;

import austeretony.oxygen_core.client.gui.elements.BackgroundGUIFiller;
import austeretony.oxygen_core.client.gui.elements.CustomRectUtils;

public class BuySectionGUIFiller extends BackgroundGUIFiller {

    public BuySectionGUIFiller(int xPosition, int yPosition, int width, int height) {             
        super(xPosition, yPosition, width, height);
    }

    @Override
    public void drawBackground() {
        //main background  
        drawRect(0, 0, this.getWidth(), this.getHeight(), this.getEnabledBackgroundColor());      

        //title underline
        CustomRectUtils.drawRect(4.0D, 14.0D, this.getWidth() - 4.0D, 14.5D, this.getDisabledBackgroundColor());

        //panel underline
        CustomRectUtils.drawRect(78.0D, this.getHeight() - 13.5D, this.getWidth() - 4.0D, this.getHeight() - 14.0D, this.getDisabledBackgroundColor());
    }
}
