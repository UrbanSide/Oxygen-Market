package austeretony.oxygen_menu.client.gui.menu;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.gui.base.core.Section;
import austeretony.oxygen_core.client.gui.base.list.ScrollableList;
import austeretony.oxygen_core.client.gui.menu.OxygenMenuEntry;
import austeretony.oxygen_menu.common.config.MenuConfig;
import austeretony.oxygen_menu.common.main.MenuMain;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Mouse;

public class MenuSection extends Section {

    private final MenuScreen screen;

    public MenuSection(MenuScreen screen) {
        super(screen, "", true);
        this.screen = screen;
    }

    @Override
    public void init() {
        addWidget(new MenuBackground(this, screen.screenAlignment));

        ScrollableList<OxygenMenuEntry> scrollableList;
        addWidget(scrollableList = new ScrollableList<>(0, 0, screen.entriesList.size(),
                screen.getWorkspace().getWidth(), MenuScreen.MENU_ENTRY_HEIGHT)
                .<OxygenMenuEntry>setEntryMouseClickListener((previous, current, x, y, button) -> {
                    screen.close();

                    int cursorX = Mouse.getX();
                    int cursorY = Mouse.getY();
                    MenuScreen.openScreen(current.getEntry());
                    Mouse.setCursorPosition(cursorX, cursorY);
                }));

        for (OxygenMenuEntry entry : screen.entriesList) {
            scrollableList.addElement(new MenuListEntry(entry, screen.screenAlignment));
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        KeyBinding keyBinding = OxygenClient.getKeyBinding(MenuMain.KEYBINDING_ID_OPEN_OXYGEN_MENU);
        if (MenuConfig.ENABLE_OXYGEN_MENU_SCREEN_KEY.asBoolean()
                && keyBinding != null && keyCode == keyBinding.getKeyCode()) {
            screen.close();
        }
        for (OxygenMenuEntry entry : screen.entriesList) {
            if (keyCode == entry.getKeyCode()) {
                screen.close();

                int cursorX = Mouse.getX();
                int cursorY = Mouse.getY();
                MenuScreen.openScreen(entry);
                Mouse.setCursorPosition(cursorX, cursorY);
                break;
            }
        }
        super.keyTyped(typedChar, keyCode);
    }
}
