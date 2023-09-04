package austeretony.oxygen_mail.common.main;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.command.CommandOxygenClient;
import austeretony.oxygen_core.client.gui.menu.OxygenMenuHelper;
import austeretony.oxygen_core.common.api.OxygenCommon;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.util.MinecraftCommon;
import austeretony.oxygen_core.server.api.OxygenServer;
import austeretony.oxygen_core.server.command.CommandOxygenOperator;
import austeretony.oxygen_mail.client.MailManagerClient;
import austeretony.oxygen_mail.client.command.MailArgumentClient;
import austeretony.oxygen_mail.client.event.MailEventsClient;
import austeretony.oxygen_mail.client.gui.mail.MailScreen;
import austeretony.oxygen_mail.client.network.operation.MailNetworkOperationsHandlerClient;
import austeretony.oxygen_mail.client.settings.MailSettings;
import austeretony.oxygen_mail.client.sync.MailSyncHandlerClient;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.server.MailManagerServer;
import austeretony.oxygen_mail.server.command.MailArgumentOperator;
import austeretony.oxygen_mail.server.event.MailEventsServer;
import austeretony.oxygen_mail.server.network.operation.MailNetworkOperationsHandlerServer;
import austeretony.oxygen_mail.server.sync.MailSyncHandlerServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(
        modid = MailMain.MOD_ID,
        name = MailMain.NAME,
        version = MailMain.VERSION,
        dependencies = "required-after:oxygen_core@[0.12.0,);",
        certificateFingerprint = "@FINGERPRINT@",
        updateJSON = MailMain.VERSIONS_FORGE_URL)
public class MailMain {

    public static final String
            MOD_ID = "oxygen_mail",
            NAME = "Oxygen: Mail",
            VERSION = "0.12.0",
            VERSION_CUSTOM = VERSION + ":beta:0",
            VERSIONS_FORGE_URL = "https://raw.githubusercontent.com/AustereTony-MCMods/Oxygen-Mail/info/versions.json";

    //oxygen module index
    public static final int MODULE_INDEX = 8;

    //screen id
    public static final int SCREEN_ID_MAIL_SCREEN = 80;

    //timeout ids
    public static final int TIMEOUT_MAIL_OPERATIONS = 80;

    //data id
    public static final int DATA_ID_MAIL = 80;

    //key binding id
    public static final int KEYBINDING_ID_OPEN_MAIL_SCREEN = 80;

    //items blacklist name
    public static final String ITEMS_BLACKLIST_MAIL = "mail";

    //operations handler id
    public static final int MAIL_OPERATIONS_HANDLER_ID = 80;

    //operations
    public static String
            OPERATION_MAIL_SEND = "mail:mail_send",
            OPERATION_ATTACHMENT_RECEIVE = "mail:attachment_receive";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        OxygenCommon.registerConfig(new MailConfig());
        if (event.getSide() == Side.CLIENT) {
            CommandOxygenClient.registerArgument(new MailArgumentClient());
            OxygenClient.registerKeyBind(
                    KEYBINDING_ID_OPEN_MAIL_SCREEN,
                    "key.oxygen_mail.open_mail_screen",
                    OxygenMain.KEY_BINDINGS_CATEGORY,
                    MailConfig.MAIL_SCREEN_KEY::asInt,
                    MailConfig.ENABLE_MAIL_SCREEN_KEY::asBoolean,
                    true,
                    () -> OxygenClient.openScreen(SCREEN_ID_MAIL_SCREEN));
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MailManagerServer.instance();
        OxygenServer.registerItemsBlacklist(ITEMS_BLACKLIST_MAIL);
        MinecraftCommon.registerEventHandler(new MailEventsServer());
        OxygenServer.registerTimeout(TIMEOUT_MAIL_OPERATIONS, MailConfig.MAIL_SCREEN_OPERATIONS_TIMEOUT_MILLIS.asInt());
        OxygenServer.registerDataSyncHandler(new MailSyncHandlerServer());
        OxygenServer.registerOperationsHandler(new MailNetworkOperationsHandlerServer());
        CommandOxygenOperator.registerArgument(new MailArgumentOperator());
        MailPrivileges.register();
        if (event.getSide() == Side.CLIENT) {
            MailManagerClient.instance();
            MinecraftCommon.registerEventHandler(new MailEventsClient());
            OxygenClient.registerDataSyncHandler(new MailSyncHandlerClient());
            OxygenClient.registerOperationsHandler(new MailNetworkOperationsHandlerClient());
            OxygenClient.registerSharedDataSyncListener(SCREEN_ID_MAIL_SCREEN, MailScreen::sharedDataSynchronized);
            MailSettings.register();
            OxygenMenuHelper.addMenuEntry(MailScreen.MAIL_SCREEN_MENU_ENTRY);
            OxygenClient.registerScreen(SCREEN_ID_MAIL_SCREEN, MailScreen::open,
                    MailConfig.ENABLE_MAIL_ACCESS_CLIENT_SIDE::asBoolean);
        }
    }
}
