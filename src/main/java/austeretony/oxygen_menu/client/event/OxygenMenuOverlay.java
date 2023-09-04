package austeretony.oxygen_menu.client.event;

import austeretony.oxygen_core.client.gui.base.GUIUtils;
import austeretony.oxygen_core.client.gui.base.Layer;
import austeretony.oxygen_core.client.gui.base.block.Fill;
import austeretony.oxygen_core.client.gui.base.common.Rectangle;
import austeretony.oxygen_core.client.gui.base.core.OxygenScreen;
import austeretony.oxygen_core.client.gui.base.core.Section;
import austeretony.oxygen_core.client.gui.base.event.OxygenScreenInitEvent;
import austeretony.oxygen_core.client.gui.menu.OxygenMenuEntry;
import austeretony.oxygen_core.client.gui.menu.OxygenMenuHelper;
import austeretony.oxygen_core.client.settings.CoreSettings;
import austeretony.oxygen_menu.client.gui.menu.MenuScreen;
import austeretony.oxygen_menu.client.gui.overlay.MenuEntryButton;
import austeretony.oxygen_menu.client.settings.MenuSettings;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

public class OxygenMenuOverlay {

    @SubscribeEvent
    public void onScreenInit(OxygenScreenInitEvent.Post event) {
        if (!MenuSettings.ENABLE_OXYGEN_MENU_GUI_OVERLAY.asBoolean()) return;
        int screenId = event.getScreen().getScreenId();
        OxygenMenuEntry menuEntry = OxygenMenuHelper.getMenuEntriesMap().get(screenId);
        if (menuEntry != null && menuEntry.isValid()) {
            addMenuOverlay(event.getScreen());
        }
    }

    private void addMenuOverlay(OxygenScreen screen) {
        List<OxygenMenuEntry> entries = MenuScreen.getMenuEntries();

        int displayWidth = GUIUtils.getScaledDisplayWidth();
        int buttonSize = 14;
        int offset = 4;
        Fill backgroundFill = Fill.builder()
                .colorDefault(CoreSettings.COLOR_BACKGROUND_BASE.asInt())
                .build();

        for (Section section : screen.getWorkspace().getSections()) {
            section.addWidget(new Rectangle(0, 0, displayWidth, 20, backgroundFill).setLayer(Layer.FRONT));

            int i = 0;
            for (OxygenMenuEntry entry : entries) {
                float xPos = displayWidth / 2F - ((buttonSize + offset) * entries.size() / 2F) + i++ * (buttonSize + offset);
                section.addWidget(new MenuEntryButton((int) xPos, 3, buttonSize, entry)
                        .setEnabled(entry.getScreenId() != screen.getScreenId()));
            }
        }
    }
}
