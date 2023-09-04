package austeretony.oxygen_mail.client.mail;

import austeretony.oxygen_core.client.gui.base.GUIUtils;
import austeretony.oxygen_core.client.gui.base.Textures;
import austeretony.oxygen_core.client.gui.base.block.Texture;
import austeretony.oxygen_core.client.gui.base.button.ImageButton;
import austeretony.oxygen_core.client.gui.base.core.Widget;
import austeretony.oxygen_core.client.settings.CoreSettings;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

public class SelectedItemStackWidget extends Widget<SelectedItemStackWidget> {

    public static final int BTN_SIZE = 5;
    public static final Texture CROSS_ICONS_TEXTURE = Texture.builder()
            .texture(Textures.CROSS_ICONS)
            .size(BTN_SIZE, BTN_SIZE)
            .imageSize(BTN_SIZE * 3, BTN_SIZE)
            .build();

    protected final @Nonnull Runnable callback;
    protected final ItemStackWrapper stackWrapper;
    protected final int amount;

    private boolean removed;

    public SelectedItemStackWidget(@Nonnull Runnable callback, int x, int y, ItemStackWrapper stackWrapper, int amount) {
        setPosition(x, y);
        setSize(16, 16);
        this.callback = callback;
        this.stackWrapper = stackWrapper;
        this.amount = amount;

        setEnabled(true);
        setVisible(true);
    }

    public ItemStackWrapper getStackWrapper() {
        return stackWrapper;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isRemoved() {
        return removed;
    }

    @Override
    public void init() {
        addWidget(new ImageButton(14, -3, BTN_SIZE, BTN_SIZE, CROSS_ICONS_TEXTURE, "")
                .setMouseClickListener((mouseX, mouseY, button) -> {
                    removed = true;
                    callback.run();
                }));
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        if (!isVisible()) return;

        ItemStack itemStack = stackWrapper.getItemStackCached();
        GUIUtils.renderItemStack(itemStack, getX(), getY(),
                CoreSettings.ENABLE_DURABILITY_BARS_GUI_DISPLAY.asBoolean());

        GUIUtils.pushMatrix();
        GUIUtils.translate(getX(), getY());

        if (amount > 1) {
            GUIUtils.drawString(String.valueOf(amount), 14, 12, CoreSettings.SCALE_TEXT_PANEL.asFloat() - .04F,
                    CoreSettings.COLOR_TEXT_BASE_ENABLED.asInt(), true);
        }

        mouseX -= getX();
        mouseY -= getY();

        for (Widget widget : getWidgets()) {
            widget.draw(mouseX, mouseY, partialTicks);
        }

        GUIUtils.popMatrix();
    }

    @Override
    public void drawForeground(int mouseX, int mouseY, float partialTicks) {
        if (!isVisible() || !isMouseOver()) return;
        int offset = getWidth() + 8;
        int x = getX() + offset;
        int y = getY() + 2;

        ItemStack itemStack = stackWrapper.getItemStackCached();
        List<String> tooltipLines = GUIUtils.getItemStackToolTip(itemStack);
        float width = 0;
        for (String line : tooltipLines) {
            float lineWidth = GUIUtils.getTextWidth(line, CoreSettings.SCALE_TEXT_TOOLTIP.asFloat()) + 6F;
            if (lineWidth > width) {
                width = lineWidth;
            }
        }
        int startX = getScreenX() + width + offset > getScreen().width ? (int) (x - width - offset) : x;

        drawToolTip(startX, y, tooltipLines);
    }
}
