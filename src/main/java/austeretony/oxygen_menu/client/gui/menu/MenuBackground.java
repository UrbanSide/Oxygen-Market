package austeretony.oxygen_menu.client.gui.menu;

import austeretony.oxygen_core.client.gui.base.Alignment;
import austeretony.oxygen_core.client.gui.base.GUIUtils;
import austeretony.oxygen_core.client.gui.base.background.Background;
import austeretony.oxygen_core.client.gui.base.core.Section;
import austeretony.oxygen_core.client.settings.CoreSettings;

import javax.annotation.Nonnull;

public class MenuBackground extends Background {

    private final Alignment alignment;

    public MenuBackground(@Nonnull Section section, Alignment screenAlignment) {
        super(section);
        alignment = screenAlignment;
    }

    @Override
    public void drawBackgroundAdditions(int mouseX, int mouseY, float partialTicks) {}

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        GUIUtils.pushMatrix();
        GUIUtils.translate(getX(), getY());

        int color = CoreSettings.COLOR_BACKGROUND_BASE.asInt();
        if (alignment == Alignment.RIGHT || alignment == Alignment.LEFT) {
            if (CoreSettings.ENABLE_VERTICAL_GRADIENT.asBoolean()) {
                GUIUtils.drawGradientRect(0D, -getScreen().getWorkspace().getY(), getWidth(), 0D, 0x00000000,
                        color, alignment);
                GUIUtils.drawGradientRect(0D, getHeight(), getWidth(), getHeight() + getScreen().getWorkspace().getY(),
                        0x00000000, color, alignment);
            }
            GUIUtils.drawGradientRect(0D, 0D, getWidth(), getHeight(), 0x00000000, color, alignment);
        } else {
            if (CoreSettings.ENABLE_VERTICAL_GRADIENT.asBoolean()) {
                GUIUtils.drawGradientRect(0D, -getScreen().getWorkspace().getY(), getWidth(), 0D, 0x00000000,
                        color, Alignment.BOTTOM);
                GUIUtils.drawGradientRect(0D, getHeight(), getWidth(), getHeight() + getScreen().getWorkspace().getY(),
                        0x00000000, color, Alignment.TOP);
            }
            GUIUtils.drawRect(0D, 0D, getWidth(), getHeight(), color);
        }

        GUIUtils.popMatrix();
    }

    @Override
    public void drawAdditions(int mouseX, int mouseY, float partialTicks) {}
}
