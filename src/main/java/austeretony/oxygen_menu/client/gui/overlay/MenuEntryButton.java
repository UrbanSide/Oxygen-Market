package austeretony.oxygen_menu.client.gui.overlay;

import austeretony.oxygen_core.client.gui.base.GUIUtils;
import austeretony.oxygen_core.client.gui.base.Layer;
import austeretony.oxygen_core.client.gui.base.block.Texture;
import austeretony.oxygen_core.client.gui.base.button.ImageButton;
import austeretony.oxygen_core.client.gui.menu.OxygenMenuEntry;
import austeretony.oxygen_core.client.settings.CoreSettings;
import austeretony.oxygen_menu.client.gui.menu.MenuScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

public class MenuEntryButton extends ImageButton {

    public MenuEntryButton(int x, int y, int size, OxygenMenuEntry entry) {
        super(x, y, size, size,
                Texture.builder()
                        .texture(getTexture(entry))
                        .size(size, size)
                        .imageSize(size * 3, size)
                        .build(),
                entry.getDisplayName());

        setMouseClickListener((mouseX, mouseY, button) -> {
            screen.close();

            int cursorX = Mouse.getX();
            int cursorY = Mouse.getY();
            MenuScreen.openScreen(entry);
            Mouse.setCursorPosition(cursorX, cursorY);
        });
        setLayer(Layer.FRONT);
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        if (!isVisible()) return;
        GUIUtils.pushMatrix();
        GUIUtils.translate(getX(), getY());

        if (!isEnabled() || isMouseOver()) {
            GUIUtils.drawRect(-3, -3, getWidth() + 3, getHeight() + 3,
                    CoreSettings.COLOR_ELEMENT_MOUSE_OVER.asInt());
        }

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        int iconU = texture.getU();
        if (!isEnabled() || isMouseOver())
            iconU += texture.getWidth() * 2;
        GUIUtils.colorDef();
        GUIUtils.drawTexturedRect((getWidth() - texture.getWidth()) / 2.0, (getHeight() - texture.getHeight()) / 2.0,
                iconU, texture.getV(), texture);

        GlStateManager.disableBlend();

        GUIUtils.popMatrix();
    }

    @Override
    public void drawForeground(int mouseX, int mouseY, float partialTicks) {
        if (!isVisible() || !isMouseOver() || tooltip.isEmpty()) return;
        float width = GUIUtils.getTextWidth(tooltip, CoreSettings.SCALE_TEXT_TOOLTIP.asFloat()) + 6F;
        drawToolTip((int) ((getX() + getWidth() / 2F) - (width / 2F)), (int) (getY() + getHeight() + 4F), tooltip);
    }

    private static ResourceLocation getTexture(OxygenMenuEntry entry) {
        return entry.getIconTexture() != null ? entry.getIconTexture() : GUIUtils.getMissingTextureLocation();
    }
}
