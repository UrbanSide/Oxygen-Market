package austeretony.oxygen_menu.common.main;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.command.CommandOxygenClient;
import austeretony.oxygen_core.client.gui.menu.OxygenMenuHelper;
import austeretony.oxygen_core.common.api.OxygenCommon;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.util.MinecraftCommon;
import austeretony.oxygen_menu.client.command.MenuArgumentClient;
import austeretony.oxygen_menu.client.event.OxygenMenuOverlay;
import austeretony.oxygen_menu.client.gui.menu.MenuScreen;
import austeretony.oxygen_menu.client.settings.MenuSettings;
import austeretony.oxygen_menu.common.config.MenuConfig;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(
        modid = MenuMain.MOD_ID,
        name = MenuMain.NAME,
        version = MenuMain.VERSION,
        dependencies = "required-after:oxygen_core@[0.12.0,);",
        clientSideOnly = true,
        certificateFingerprint = "@FINGERPRINT@",
        updateJSON = MenuMain.VERSIONS_FORGE_URL)
public class MenuMain {

    public static final String
            MOD_ID = "oxygen_menu",
            NAME = "Oxygen: Menu",
            VERSION = "0.12.0",
            VERSION_CUSTOM = VERSION + ":beta:0",
            VERSIONS_FORGE_URL = "https://raw.githubusercontent.com/AustereTony-MCMods/Oxygen-Menu/info/versions.json";

    //oxygen module index
    public static final int MODULE_INDEX = 4;

    //screen id
    public static final int SCREEN_ID_MENU = 40;

    //key binding id
    public static final int KEYBINDING_ID_OPEN_OXYGEN_MENU = 40;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        OxygenCommon.registerConfig(new MenuConfig());
        if (event.getSide() == Side.CLIENT) {
            OxygenMenuHelper.setMenuEnabled(true);
            CommandOxygenClient.registerArgument(new MenuArgumentClient());
            OxygenClient.registerKeyBind(
                    KEYBINDING_ID_OPEN_OXYGEN_MENU,
                    "key.oxygen_menu.open_oxygen_menu",
                    OxygenMain.KEY_BINDINGS_CATEGORY,
                    MenuConfig.OXYGEN_MENU_SCREEN_KEY_ID::asInt,
                    MenuConfig.ENABLE_OXYGEN_MENU_SCREEN_KEY::asBoolean,
                    false,
                    () -> OxygenClient.openScreen(SCREEN_ID_MENU));
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        if (event.getSide() == Side.CLIENT) {
            MinecraftCommon.registerEventHandler(new OxygenMenuOverlay());
            MenuSettings.register();
            OxygenClient.registerScreen(SCREEN_ID_MENU, MenuScreen::open);
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        if (event.getSide() == Side.CLIENT) {
            KeyBinding keyBinding = OxygenClient.getKeyBinding(KEYBINDING_ID_OPEN_OXYGEN_MENU);
            if (keyBinding != null) {
                OxygenMenuHelper.setMenuKeyBinding(keyBinding);
            }
        }
    }
}
