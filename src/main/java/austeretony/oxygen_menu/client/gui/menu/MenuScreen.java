package austeretony.oxygen_menu.client.gui.menu;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.gui.base.Alignment;
import austeretony.oxygen_core.client.gui.base.core.OxygenScreen;
import austeretony.oxygen_core.client.gui.base.core.Section;
import austeretony.oxygen_core.client.gui.base.core.Workspace;
import austeretony.oxygen_core.client.gui.menu.OxygenMenuEntry;
import austeretony.oxygen_core.client.gui.menu.OxygenMenuHelper;
import austeretony.oxygen_core.client.util.MinecraftClient;
import austeretony.oxygen_menu.client.settings.MenuSettings;
import austeretony.oxygen_menu.common.main.MenuMain;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MenuScreen extends OxygenScreen {

    private Section section;

    public static final int MENU_ENTRY_HEIGHT = 18;

    protected final Alignment screenAlignment;
    protected final List<OxygenMenuEntry> entriesList;

    public MenuScreen() {
        screenAlignment = Alignment.valueOf(MenuSettings.OXYGEN_MENU_SCREEN_ALIGNMENT.asString());
        entriesList = getMenuEntries();
    }

    @Override
    public int getScreenId() {
        return MenuMain.SCREEN_ID_MENU;
    }

    @Override
    public Workspace createWorkspace() {
        int height = MENU_ENTRY_HEIGHT * entriesList.size() + (entriesList.size() - 1);
        Workspace workspace = new Workspace(this, 120, height);
        workspace.setAlignment(screenAlignment, 0, 0);
        return workspace;
    }

    @Override
    public void addSections() {
        getWorkspace().addSection(section = new MenuSection(this));
    }

    @Override
    public Section getDefaultSection() {
        return section;
    }

    public static List<OxygenMenuEntry> getMenuEntries() {
        return OxygenMenuHelper.getMenuEntriesMap().values()
                .stream()
                .filter(OxygenMenuEntry::isValid)
                .sorted(Comparator.comparing(OxygenMenuEntry::getPriority))
                .collect(Collectors.toList());
    }

    public static void open() {
        MinecraftClient.displayGuiScreen(new MenuScreen());
    }

    public static void openScreen(OxygenMenuEntry entry) {
        OxygenClient.openScreen(entry.getScreenId());
    }
}
